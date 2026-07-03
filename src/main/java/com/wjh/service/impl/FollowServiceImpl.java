package com.wjh.service.impl;

import com.wjh.constant.FollowConstant;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.constant.UserStatusConstant;
import com.wjh.context.BaseContext;
import com.wjh.entity.Follow;
import com.wjh.entity.User;
import com.wjh.mapper.FollowMapper;
import com.wjh.mapper.UserMapper;
import com.wjh.result.Result;
import com.wjh.service.FollowService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    @Lazy
    private FollowServiceImpl self;

    private static final ExecutorService FOLLOW_EXECUTOR = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void init () {
        try {
            stringRedisTemplate.opsForStream().createGroup("stream.follow", "follow_group");
        } catch (Exception e) {
            log.info("创建消费者组失败，因为消费者组已经存在");
        }
        FOLLOW_EXECUTOR.submit(new FollowTask());
    }

    @PreDestroy
    public void destroy () {
        running = false;
        FOLLOW_EXECUTOR.shutdown();
        try {
            if (!FOLLOW_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                FOLLOW_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            FOLLOW_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private class FollowTask implements Runnable {

        @Override
        public void run() {
            while (running) {
                try {
                    List<MapRecord<String, Object, Object>> msg = stringRedisTemplate.opsForStream().read(
                            Consumer.from("follow_group", "follow_consumer"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                            StreamOffset.create("stream.follow", ReadOffset.lastConsumed())
                    );
                    if(msg == null || msg.isEmpty()) {
                        continue;
                    }
                    self.handleFollow(msg);
                } catch (RedisConnectionFailureException e) {
                    log.warn("Redis 连接失败，稍后重试", e);
                    sleep(5);
                } catch (Exception e) {
                    while (running) {
                        try {
                            List<MapRecord<String, Object, Object>> msg = stringRedisTemplate.opsForStream().read(
                                    Consumer.from("follow_group", "follow_consumer"),
                                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                                    StreamOffset.create("stream.follow", ReadOffset.from("0"))
                            );
                            if(msg == null || msg.isEmpty()) {
                                break;
                            }
                            self.handleFollow(msg);
                        } catch (RedisConnectionFailureException ex) {
                            log.warn("Redis 连接失败，稍后重试", ex);
                            sleep(5);
                            break;
                        } catch (Exception ex) {
                            log.error("处理关注消息失败", ex);
                        }
                    }
                }
            }
        }

        private void sleep(long seconds) {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleFollow(List<MapRecord<String, Object, Object>> msg) {
        MapRecord<String, Object, Object> record = msg.get(0);
        String mesId = record.getId().getValue();
        Map<Object, Object> collect = record.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        String status = collect.get("status").toString();
        Long userId = Long.valueOf(collect.get("userId").toString());
        Long followId = Long.valueOf(collect.get("followId").toString());
        if(status.equals(FollowConstant.IS_FOLLOW)) {
            int deleted = followMapper.deleteByUserAndFollower(userId, followId);
            if (deleted > 0) {
                User user = userMapper.getById(userId);
                user.setFollowerCounts(user.getFollowerCounts() - 1);
                userMapper.update(user);
                User follower = userMapper.getById(followId);
                follower.setFansCounts(follower.getFansCounts() - 1);
                userMapper.update(follower);
            }
            stringRedisTemplate.opsForStream().acknowledge("stream.follow", "follow_consumer", mesId);
        } else {
            Follow follow = new Follow();
            follow.setFollowUserId(followId);
            follow.setUserId(userId);
            follow.setCreateTime(LocalDateTime.now());
            int inserted = followMapper.insert(follow);
            if (inserted > 0) {
                User user = userMapper.getById(userId);
                user.setFollowerCounts(user.getFollowerCounts() + 1);
                userMapper.update(user);
                User follower = userMapper.getById(followId);
                follower.setFansCounts(follower.getFansCounts() + 1);
                userMapper.update(follower);
            }
            stringRedisTemplate.opsForStream().acknowledge("stream.follow", "follow_consumer", mesId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result follow(Long userId) {
        User user = userMapper.getById(userId);
        if(user.getStatus().equals(UserStatusConstant.BAN_USER)) {
            return Result.error(MessageConstant.USER_BAN_ERROR + "无法操作");
        }
        String key = RedisConstant.USER_FAN_KEY + userId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, BaseContext.getThreadLocal().getId().toString());
        if(isMember) {
            stringRedisTemplate.opsForSet().remove(key, BaseContext.getThreadLocal().getId().toString());
            stringRedisTemplate.opsForSet().remove(RedisConstant.USER_FOLLOW_KEY + BaseContext.getThreadLocal().getId().toString(), userId.toString());
            try {
                Map<String, String> message = new HashMap<>();
                message.put("status", FollowConstant.IS_FOLLOW);
                message.put("userId", BaseContext.getThreadLocal().getId().toString());
                message.put("followId", userId.toString());
                stringRedisTemplate.opsForStream().add("stream.follow", message);
            } catch (Exception e) {
                log.info("取消关注失败, 当前用户={} 关注用户={}", BaseContext.getThreadLocal().toString(), userId.toString());
                stringRedisTemplate.opsForSet().add(key, BaseContext.getThreadLocal().getId().toString());
                stringRedisTemplate.opsForSet().add(RedisConstant.USER_FOLLOW_KEY + BaseContext.getThreadLocal().getId().toString(), userId.toString());
                return Result.error("取消关注失败");
            }
            return Result.success("取消关注成功");
        } else {
            stringRedisTemplate.opsForSet().add(key, BaseContext.getThreadLocal().getId().toString());
            stringRedisTemplate.opsForSet().add(RedisConstant.USER_FOLLOW_KEY + BaseContext.getThreadLocal().getId().toString(), userId.toString());
            try {
                Map<String, String> message = new HashMap<>();
                message.put("status", FollowConstant.IS_NOT_FOLLOW);
                message.put("userId", BaseContext.getThreadLocal().getId().toString());
                message.put("followId", userId.toString());
                stringRedisTemplate.opsForStream().add("stream.follow", message);
            } catch (Exception e) {
                log.info("关注失败, 当前用户={} 关注用户={}", BaseContext.getThreadLocal().toString(), userId.toString());
                stringRedisTemplate.opsForSet().remove(key, BaseContext.getThreadLocal().getId().toString());
                stringRedisTemplate.opsForSet().remove(RedisConstant.USER_FOLLOW_KEY + BaseContext.getThreadLocal().getId().toString(), userId.toString());
                return Result.error("关注失败");
            }
            return Result.success("关注成功");
        }
    }
}
