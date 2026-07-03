package com.wjh.controller.user;

import com.wjh.dto.VoucherDTO;
import com.wjh.entity.Voucher;
import com.wjh.result.Result;
import com.wjh.service.VoucherService;
import com.wjh.vo.VoucherOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    public Result<Voucher> createVoucher(@RequestBody VoucherDTO voucherDTO) {
        return voucherService.createVoucher(voucherDTO);
    }

    @GetMapping("/manage/{shopId}")
    public Result<List<Voucher>> getShopVouchers (@PathVariable Long shopId) {
        List<Voucher> voucherList = voucherService.getShopVouchers(shopId);
        return Result.success(voucherList);
    }

    @GetMapping("/user/{userId}")
    public Result<VoucherOrderVO> getVoucherOrder(@PathVariable Long userId) {
        return voucherService.getVoucherOrder(userId);
    }
}
