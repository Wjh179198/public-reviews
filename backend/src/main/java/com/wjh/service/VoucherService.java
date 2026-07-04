package com.wjh.service;

import com.wjh.dto.VoucherDTO;
import com.wjh.entity.Voucher;
import com.wjh.result.Result;
import com.wjh.vo.VoucherOrderVO;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.util.List;

public interface VoucherService {

    List<Voucher> getVoucherList(Long shopId);

    Result<Voucher> createVoucher(VoucherDTO voucherDTO);

    List<Voucher> getShopVouchers(Long shopId);

    Result<List<VoucherOrderVO>> getVoucherOrder(Long userId);

    Result<Boolean> getVoucherStock(Long voucherId);

    Result grabVoucher(Long voucherId);

    void handleVoucherOrder (List<MapRecord<String, Object, Object>> msg);
}
