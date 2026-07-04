package com.wjh.service;

import com.wjh.dto.ShopOrderDTO;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.vo.ShopOrderVO;

public interface ShopOrderService {

    Result<ShopOrderVO> createOrder(ShopOrderDTO shopOrderDTO);

    Result<PageResult> getOrderListByUserId(Long userId, Integer status, Integer page, Integer pageSize);
}
