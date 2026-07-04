package com.wjh.controller.user;

import com.wjh.dto.ShopOrderDTO;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.ShopOrderService;
import com.wjh.vo.ShopOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class ShopOrderController {

    @Autowired
    private ShopOrderService shopOrderService;

    @PostMapping("/create")
    public Result<ShopOrderVO> createOrder (@RequestBody ShopOrderDTO shopOrderDTO) {
        return shopOrderService.createOrder(shopOrderDTO);
    }

    @GetMapping("/user/{userId}")
    public Result<PageResult> getOrderListByUserId (@PathVariable Long userId, @RequestParam Integer status, @RequestParam Integer page, @RequestParam Integer pageSize) {
        return shopOrderService.getOrderListByUserId(userId, status, page, pageSize);
    }
}
