package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.RedisConstant;
import com.wjh.entity.Voucher;
import com.wjh.mapper.VoucherMapper;
import com.wjh.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Voucher> getVoucherList(Long shopId) {
        String key = RedisConstant.SHOP_VOUCHER_KEY + shopId.toString();
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            List<Voucher> voucherList = voucherMapper.getByShopId(shopId);
            try {
                stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(voucherList));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return voucherList;
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Voucher>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
