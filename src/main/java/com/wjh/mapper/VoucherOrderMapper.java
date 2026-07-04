package com.wjh.mapper;

import com.wjh.entity.VoucherOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface VoucherOrderMapper {

    @Select("SELECT * FROM voucher_order WHERE user_id = #{userId} ORDER BY order_time DESC")
    List<VoucherOrder> getByUserId(Long userId);

    int insert(VoucherOrder voucherOrder);

    @Select("SELECT * FROM voucher_order WHERE user_id = #{userId} AND voucher_id = #{id}")
    VoucherOrder getByUserIdAndVoucherId(Long userId, Long id);

    @Update("UPDATE voucher_order SET status = #{status} WHERE voucher_id = #{id}")
    void updateStatus(Long id, Integer status);
}
