package com.wjh.controller.user;

import com.wjh.result.Result;
import com.wjh.service.ShopService;
import com.wjh.vo.ShopTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
