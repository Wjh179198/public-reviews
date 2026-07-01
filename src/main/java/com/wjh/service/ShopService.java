package com.wjh.service;

import com.wjh.dto.ShopRegisterDTO;
import com.wjh.entity.Shop;
import com.wjh.vo.ShopTypeVO;

import java.util.List;

public interface ShopService {

    List<ShopTypeVO> getShopTypes();

    Shop registerShop(ShopRegisterDTO shopRegisterDTO);

    List<Shop> getListShopByPageParam(Long typeId, String scoreRange, Integer page, Integer pageSize);
}
