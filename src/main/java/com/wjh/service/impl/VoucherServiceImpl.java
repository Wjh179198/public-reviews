package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.RedisConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.VoucherDTO;
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
import com.wjh.vo.VoucherOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
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
        String key = RedisConstant.SHOP_VOUCHER_KEY + shopId.toString();
        stringRedisTemplate.delete(key);
        return Result.success("发布成功", voucher);
    }

    @Override
    public List<Voucher> getShopVouchers(Long shopId) {
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
}
