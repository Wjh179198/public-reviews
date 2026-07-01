package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjh.constant.RedisConstant;
import com.wjh.entity.ShopType;
import com.wjh.mapper.ShopMapper;
import com.wjh.service.ShopService;
import com.wjh.vo.ShopTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ShopTypeVO> getShopTypes() {
        String key = RedisConstant.SHOP_TYPE_KEY;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(json == null || json.isEmpty()) {
            List<ShopType> shopTypeList = shopMapper.getType();
            List<ShopTypeVO> shopTypeVOList = shopTypeList.stream().map(shopType -> ShopTypeVO.builder()
                    .id(shopType.getId()).name(shopType.getName()).build()).collect(Collectors.toList());
            try {
                stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(shopTypeVOList));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return shopTypeVOList;
        }
        List<ShopTypeVO> shopTypeVOList = null;
        try {
            shopTypeVOList = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<ShopTypeVO>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return shopTypeVOList;
    }
}
