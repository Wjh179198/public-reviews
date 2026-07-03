package com.wjh.controller.user;

import com.wjh.entity.Voucher;
import com.wjh.result.Result;
import com.wjh.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/shop/{shopId}")
    public Result<List<Voucher>> getVoucherByShopId (@PathVariable Long shopId) {
        List<Voucher> voucherList = voucherService.getVoucherList(shopId);
        return Result.success(voucherList);
    }
}
