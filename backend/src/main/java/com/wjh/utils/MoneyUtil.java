package com.wjh.utils;

import com.wjh.constant.RedisConstant;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;

public class MoneyUtil {

    public static BigDecimal getMoney(Long userId, StringRedisTemplate stringRedisTemplate) {
        String moneyStr = (String) stringRedisTemplate.opsForHash().get(RedisConstant.USER_MONEY_KEY, userId.toString());
        if (moneyStr == null || "NaN".equals(moneyStr)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(moneyStr);
    }

    public static void setMoney(Long userId, BigDecimal money, StringRedisTemplate stringRedisTemplate) {
        stringRedisTemplate.opsForHash().put(RedisConstant.USER_MONEY_KEY, userId.toString(), money.toString());
    }
}
