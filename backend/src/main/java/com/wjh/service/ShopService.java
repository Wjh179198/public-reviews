package com.wjh.service;

import com.wjh.dto.ShopRegisterDTO;
import com.wjh.entity.Shop;
import com.wjh.result.PageResult;
import com.wjh.vo.ShopRevenue;
import com.wjh.vo.ShopTypeVO;
import com.wjh.vo.ShopVO;

import java.util.List;

public interface ShopService {

    List<ShopTypeVO> getShopTypes();

    Shop registerShop(ShopRegisterDTO shopRegisterDTO);

    PageResult getListShopByPageParam(Long typeId, String scoreRange, Integer page, Integer pageSize);

    ShopVO getShopById(Long shopId);

    List<ShopVO> searchShops(String keyword);

    List<ShopRevenue> getShopRevenue(Long shopId);
}
