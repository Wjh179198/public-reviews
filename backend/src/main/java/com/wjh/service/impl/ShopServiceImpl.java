package com.wjh.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjh.constant.RedisConstant;
import com.wjh.constant.UserStatusConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.ShopRegisterDTO;
import com.wjh.entity.Shop;
import com.wjh.entity.ShopOrder;
import com.wjh.entity.ShopType;
import com.wjh.entity.User;
import com.wjh.mapper.ShopMapper;
import com.wjh.mapper.ShopOrderMapper;
import com.wjh.mapper.UserMapper;
import com.wjh.result.PageResult;
import com.wjh.service.ShopService;
import com.wjh.vo.ShopOrderVO;
import com.wjh.vo.ShopRevenue;
import com.wjh.vo.ShopTypeVO;
import com.wjh.vo.ShopVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShopOrderMapper shopOrderMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Shop registerShop(ShopRegisterDTO shopRegisterDTO) {
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopRegisterDTO, shop);
        shop.setCreateTime(LocalDateTime.now());
        shop.setUpdateTime(LocalDateTime.now());
        shopMapper.insert(shop);
        User user = new User();
        user.setId(BaseContext.getThreadLocal().getId());
        user.setShopId(shop.getId());
        user.setStatus(UserStatusConstant.SHOP_USER);
        userMapper.update(user);
        return shop;
    }

    @Override
    public PageResult getListShopByPageParam(Long typeId, String scoreRange, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        BigDecimal lowScore = BigDecimal.valueOf(0);
        BigDecimal highScore = BigDecimal.valueOf(5);
        if(scoreRange != null && !scoreRange.isEmpty()) {
            if("below2".equals(scoreRange)) {
                highScore = BigDecimal.valueOf(2);
            } else if("2to4".equals(scoreRange)) {
                lowScore = BigDecimal.valueOf(2);
                highScore = BigDecimal.valueOf(4);
            } else if("4to5".equals(scoreRange)) {
                lowScore = BigDecimal.valueOf(4);
                highScore = BigDecimal.valueOf(5);
            } else {
                List<ShopVO> shopList = shopMapper.selectWithNoComment(typeId);
                Page<ShopVO> shopPage = (Page<ShopVO>) shopList;
                return PageResult.builder().total(shopPage.getTotal()).records(shopPage.getResult()).pages(shopPage.getPages()).pageSize(shopPage.getPageSize()).build();
            }
        }
        List<ShopVO> shopList = shopMapper.getListByPageParam(typeId, lowScore, highScore);
        Page<ShopVO> shopPage = (Page<ShopVO>) shopList;
        return PageResult.builder().total(shopPage.getTotal()).records(shopPage.getResult()).pages(shopPage.getPages()).pageSize(shopPage.getPageSize()).build();
    }

    @Override
    public ShopVO getShopById(Long shopId) {
        return shopMapper.getShopVoById(shopId);
    }

    @Override
    public List<ShopVO> searchShops(String keyword) {
        List<ShopVO> shopVOList = shopMapper.getListByName(keyword);
        return shopVOList;
    }

    @Override
    public List<ShopRevenue> getShopRevenue(Long shopId) {
        LocalDate endDay = LocalDate.now();
        LocalDate startDay = LocalDate.now().plusDays(-6);
        LocalDateTime startDateTime = LocalDateTime.of(startDay, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endDay, LocalTime.MAX);
        List<ShopOrder> shopOrderList = shopOrderMapper.getShopRevenue(shopId, startDateTime, endDateTime);
        List<ShopRevenue> res = new ArrayList<>();
        for(int i = 0; i < 7; i++) {
            res.add(new ShopRevenue(startDay.plusDays(i), BigDecimal.valueOf(0)));
        }
        if(shopOrderList != null && !shopOrderList.isEmpty()) {
            for(ShopOrder shopOrder : shopOrderList) {
                int index = (int) ChronoUnit.DAYS.between(startDay, shopOrder.getOrderTime().toLocalDate());
                res.get(index).setAmount(res.get(index).getAmount().add(shopOrder.getPrice()));
            }
        }
        return res;
    }
}
