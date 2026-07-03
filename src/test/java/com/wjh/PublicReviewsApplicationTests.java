package com.wjh;

import com.wjh.constant.RedisConstant;
import com.wjh.entity.User;
import com.wjh.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
class PublicReviewsApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void syncUserMoneyToRedis() {
        List<User> users = userMapper.getListByParam(new User());
        int count = 0;
        for (User user : users) {
            BigDecimal money = user.getMoney() != null ? user.getMoney() : BigDecimal.ZERO;
            stringRedisTemplate.opsForHash().put(
                    RedisConstant.USER_MONEY_KEY,
                    user.getId().toString(),
                    money.toPlainString()
            );
            count++;
        }
        System.out.println("同步完成，共 " + count + " 个用户");
    }

    @Test
    void verifyUserMoneyInRedis() {
        List<User> users = userMapper.getListByParam(new User());
        for (User user : users) {
            String redisMoney = (String) stringRedisTemplate.opsForHash()
                    .get(RedisConstant.USER_MONEY_KEY, user.getId().toString());
            System.out.printf("用户 id=%s, name=%s, MySQL余额=%s, Redis余额=%s%n",
                    user.getId(), user.getName(), user.getMoney(), redisMoney);
        }
    }
}
