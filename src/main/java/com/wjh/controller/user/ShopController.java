package com.wjh.controller.user;

import com.wjh.dto.ShopRegisterDTO;
import com.wjh.entity.Shop;
import com.wjh.result.Result;
import com.wjh.service.ShopService;
import com.wjh.vo.ShopTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/types")
    public Result<List<ShopTypeVO>> getShopTypes () {
        List<ShopTypeVO> shopTypes = shopService.getShopTypes();
        return Result.success(shopTypes);
    }

    @PostMapping("/register")
    public Result<Shop> registerShop (@RequestBody ShopRegisterDTO shopRegisterDTO) {
        Shop shop = shopService.registerShop(shopRegisterDTO);
        return Result.success("商户注册成功", shop);
    }

    @GetMapping("/list")
    public Result<List<Shop>> getListShopByPageParam (@RequestParam Long typeId, @RequestParam String scoreRange, @RequestParam Integer page, @RequestParam Integer pageSize) {
        List<Shop> shopList = shopService.getListShopByPageParam(typeId, scoreRange, page, pageSize);
        return Result.success(shopList);
    }
}
