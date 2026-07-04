package com.wjh.controller.user;

import com.wjh.dto.ShopRegisterDTO;
import com.wjh.entity.Shop;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.ShopService;
import com.wjh.vo.ShopRevenue;
import com.wjh.vo.ShopTypeVO;
import com.wjh.vo.ShopVO;
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
    public Result<PageResult> getListShopByPageParam (@RequestParam(required = false) Long typeId, @RequestParam(required = false) String scoreRange, @RequestParam Integer page, @RequestParam Integer pageSize) {
        PageResult pageResult = shopService.getListShopByPageParam(typeId, scoreRange, page, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/{shopId}")
    public Result<ShopVO> getShopById (@PathVariable Long shopId) {
        ShopVO shopVO = shopService.getShopById(shopId);
        return Result.success(shopVO);
    }

    @GetMapping("/search")
    public Result<List<ShopVO>> searchShops(@RequestParam String keyword) {
        List<ShopVO> shopVOList = shopService.searchShops(keyword);
        return Result.success(shopVOList);
    }

    @GetMapping("/{shopId}/revenue")
    public Result<List<ShopRevenue>> getShopRevenue (@PathVariable Long shopId) {
        return Result.success(shopService.getShopRevenue(shopId));
    }
}
