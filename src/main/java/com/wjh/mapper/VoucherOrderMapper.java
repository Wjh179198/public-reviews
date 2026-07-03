package com.wjh.mapper;

import com.wjh.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoucherOrderMapper {

    @Select("SELECT * FROM voucher_order WHERE user_id = #{userId} ORDER BY order_time DESC")
    List<VoucherOrder> getByUserId(Long userId);
}
