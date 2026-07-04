package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.constant.VoucherStatusConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.VoucherDTO;
import com.wjh.dto.VoucherRedis;
import com.wjh.entity.Shop;
import com.wjh.entity.User;
import com.wjh.entity.Voucher;
import com.wjh.entity.VoucherOrder;
import com.wjh.mapper.ShopMapper;
import com.wjh.mapper.UserMapper;
import com.wjh.mapper.VoucherMapper;
import com.wjh.mapper.VoucherOrderMapper;
import com.wjh.result.Result;
import com.wjh.service.VoucherService;
import com.wjh.utils.MoneyUtil;
import com.wjh.vo.VoucherOrderVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VoucherOrderMapper voucherOrderMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    @Lazy
    private VoucherServiceImpl self;

    private static final ExecutorService SECKILL_EXECUTOR = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void init () {
        try {
            stringRedisTemplate.opsForStream().createGroup("stream.voucher", "voucher_group");
        } catch (Exception e) {
            log.info("创建消费者组失败，因为消费者组已经存在");
        }
        SECKILL_EXECUTOR.submit(new VoucherServiceImpl.VoucherTask());
    }

    @PreDestroy
    public void destroy () {
        running = false;
        SECKILL_EXECUTOR.shutdown();
        try {
            if (!SECKILL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                SECKILL_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            SECKILL_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("voucher.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private class VoucherTask implements Runnable {

        @Override
        public void run() {
            while (running) {
                try {
                    List<MapRecord<String, Object, Object>> msg = stringRedisTemplate.opsForStream().read(
                            Consumer.from("voucher_group", "voucher_consumer"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                            StreamOffset.create("stream.voucher", ReadOffset.lastConsumed())
                    );
                    if(msg == null || msg.isEmpty()) {
                        continue;
                    }
                    self.handleVoucherOrder(msg);
                } catch (Exception e) {
                    while (running) {
                        try {
                            List<MapRecord<String, Object, Object>> msg = stringRedisTemplate.opsForStream().read(
                                    Consumer.from("voucher_group", "voucher_consumer"),
                                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                                    StreamOffset.create("stream.voucher", ReadOffset.from("0"))
                            );
                            if(msg == null || msg.isEmpty()) {
                                continue;
                            }
                            self.handleVoucherOrder(msg);
                        } catch (Exception ex) {
                            log.info("处理订单异常：{}", ex.getMessage());
                        }
                    }
                }
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleVoucherOrder(List<MapRecord<String, Object, Object>> msg) {
        MapRecord<String, Object, Object> record = msg.get(0);
        String msgId = record.getId().getValue();
        Map<Object, Object> collect = record.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Long voucherId = Long.valueOf(collect.get("voucherId").toString());
        Long userId = Long.valueOf(collect.get("userId").toString());
        Long shopId = Long.valueOf(collect.get("shopId").toString());
        BigDecimal voucherPrice = new BigDecimal(collect.get("voucherPrice").toString());
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(userId);
        voucherOrder.setShopId(shopId);
        voucherOrder.setStatus(VoucherStatusConstant.AVAILABLE);
        voucherOrder.setOrderTime(LocalDateTime.now());
        int inserted = voucherOrderMapper.insert(voucherOrder);
        if (inserted == 0) {
            stringRedisTemplate.opsForStream().acknowledge("stream.voucher", "voucher_consumer", msgId);
            return;
        }
        User userByShopId = userMapper.getByShopId(shopId);
        BigDecimal shopMoney = MoneyUtil.getMoney(userByShopId.getId(), stringRedisTemplate);
        MoneyUtil.setMoney(userByShopId.getId(), shopMoney.add(voucherPrice), stringRedisTemplate);
        BigDecimal money = MoneyUtil.getMoney(userId, stringRedisTemplate);
        MoneyUtil.setMoney(userId, money.subtract(voucherPrice), stringRedisTemplate);
        try {
            voucherMapper.decrementStock(voucherId);
            User user = userMapper.getById(userId);
            user.setMoney(money.subtract(voucherPrice));
            userMapper.update(user);
            User user1 = userMapper.getByShopId(shopId);
            user1.setMoney(user1.getMoney().add(voucherPrice));
            userMapper.update(user1);
        } catch (Exception e) {
            MoneyUtil.setMoney(userId, money, stringRedisTemplate);
            MoneyUtil.setMoney(userByShopId.getId(), shopMoney.subtract(voucherPrice), stringRedisTemplate);
            throw e;
        }
        stringRedisTemplate.opsForStream().acknowledge("stream.voucher", "voucher_consumer", msgId);
    }

    @Override
    public List<Voucher> getVoucherList(Long shopId) {
        String key = RedisConstant.SHOP_VOUCHER_KEY + shopId.toString();
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            List<Voucher> voucherList = voucherMapper.getByShopId(shopId);
            try {
                stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(voucherList), RedisConstant.SHOP_VOUCHER_EXPIRE, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return voucherList;
        }
        try {
            List<Voucher> voucherList = objectMapper.readValue(json, new TypeReference<List<Voucher>>() {});
            voucherList.forEach(voucher -> voucher.setStock(null));
            return voucherList;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result<Voucher> createVoucher(VoucherDTO voucherDTO) {
        Long userId = BaseContext.getThreadLocal().getId();
        User user = userMapper.getById(userId);
        Long shopId = user.getShopId();
        if(shopId == null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherDTO, voucher);
        voucher.setShopId(shopId);
        voucherMapper.insert(voucher);
        String key = RedisConstant.SHOP_VOUCHER_KEY + shopId;
        stringRedisTemplate.delete(key);
        LocalDateTime endTime = voucher.getEndTime();
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
        stringRedisTemplate.opsForValue().set(RedisConstant.VOUCHER_STOCK_KEY + voucher.getId(), voucher.getStock().toString(), seconds, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(RedisConstant.VOUCHER_BEGIN_KEY + voucher.getId(), voucher.getBeginTime().toString(), seconds, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(RedisConstant.VOUCHER_END_KEY + voucher.getId(), voucher.getEndTime().toString(), seconds, TimeUnit.SECONDS);
        VoucherRedis voucherRedis = new VoucherRedis(voucher.getId(), voucher.getShopId(), voucher.getPrice());
        try {
            stringRedisTemplate.opsForValue().set(RedisConstant.VOUCHER_KEY + voucher.getId(), objectMapper.writeValueAsString(voucherRedis), seconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Result.success("发布成功", voucher);
    }

    @Override
    public List<Voucher> getShopVouchers(Long shopId) {
        List<Voucher> voucherList = voucherMapper.getByShopId(shopId);
        if (voucherList == null || voucherList.isEmpty()) {
            return Collections.emptyList();
        }
        return voucherList;
    }

    @Override
    public Result<List<VoucherOrderVO>> getVoucherOrder(Long userId) {
        List<VoucherOrder> voucherOrderList = voucherOrderMapper.getByUserId(userId);
        if (voucherOrderList == null || voucherOrderList.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<VoucherOrderVO> res = voucherOrderList.stream().map(voucherOrder -> {
            Shop shop = shopMapper.getById(voucherOrder.getShopId());
            Voucher voucher = voucherMapper.getById(voucherOrder.getVoucherId());
            return VoucherOrderVO.builder()
                    .id(voucherOrder.getId())
                    .userId(voucherOrder.getUserId())
                    .voucherId(voucherOrder.getVoucherId())
                    .voucherPrice(voucher.getPrice())
                    .voucherValue(voucher.getValue())
                    .shopName(shop.getName())
                    .status(voucherOrder.getStatus())
                    .orderTime(voucherOrder.getOrderTime())
                    .build();
        }).toList();
        return Result.success(res);
    }

    @Override
    public Result<Boolean> getVoucherStock(Long voucherId) {
        String key = RedisConstant.VOUCHER_STOCK_KEY + voucherId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            int stock = Integer.parseInt(json);
            return Result.success(stock > 0);
        }
        return Result.error(MessageConstant.VOUCHER_NOT_EXISTS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result grabVoucher(Long voucherId) {
        String key = RedisConstant.VOUCHER_KEY + voucherId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return Result.error(MessageConstant.VOUCHER_NOT_EXISTS);
        }
        VoucherRedis voucherRedis = null;
        try {
            voucherRedis = objectMapper.readValue(json, VoucherRedis.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Long userId = BaseContext.getThreadLocal().getId();
        BigDecimal money = MoneyUtil.getMoney(userId, stringRedisTemplate);
        if (money.compareTo(voucherRedis.getPrice()) < 0) {
            return Result.error(MessageConstant.MONEY_INVALID);
        }
        Long res = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString(), LocalDateTime.now().toString());
        int value = res.intValue();
        if(value == 1) {
            return Result.error(MessageConstant.VOUCHER_NOT_BEGIN);
        }
        if(value == 2) {
            return Result.error(MessageConstant.VOUCHER_ENDED);
        }
        if(value == 3) {
            return Result.error(MessageConstant.VOUCHER_STOCK_END);
        }
        if(value == 4) {
            return Result.error(MessageConstant.VOUCHER_ALREADY_CLAIMED);
        }
        try {
            Map<String, String> map = new HashMap<>();
            map.put("voucherId", voucherId.toString());
            map.put("userId", userId.toString());
            map.put("shopId", voucherRedis.getShopId().toString());
            map.put("voucherPrice", voucherRedis.getPrice().toString());
            stringRedisTemplate.opsForStream().add("stream.voucher", map);
        } catch (Exception e) {
            log.info("购买优惠卷失败,下单用户id={}, 优惠卷id={}", userId, voucherId);
            stringRedisTemplate.opsForValue().increment(RedisConstant.VOUCHER_STOCK_KEY + voucherId);
            stringRedisTemplate.opsForSet().remove(RedisConstant.VOUCHER_ORDER_KEY + voucherId, userId.toString());
            return Result.error(MessageConstant.VOUCHER_CLAIM_FAILED);
        }
        return Result.success("购买成功");
    }
}
