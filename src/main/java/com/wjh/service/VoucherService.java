package com.wjh.service;

import com.wjh.dto.VoucherDTO;
import com.wjh.entity.Voucher;
import com.wjh.result.Result;
import com.wjh.vo.VoucherOrderVO;

import java.util.List;

public interface VoucherService {

    List<Voucher> getVoucherList(Long shopId);

    Result<Voucher> createVoucher(VoucherDTO voucherDTO);

    List<Voucher> getShopVouchers(Long shopId);

    Result<List<VoucherOrderVO>> getVoucherOrder(Long userId);
}
