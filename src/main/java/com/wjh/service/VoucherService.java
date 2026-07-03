package com.wjh.service;

import com.wjh.entity.Voucher;

import java.util.List;

public interface VoucherService {

    List<Voucher> getVoucherList(Long shopId);
}
