package com.wjh.mapper;

import com.wjh.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoucherMapper {

    @Select("select * from voucher where shop_id = #{shopId}")
    List<Voucher> getByShopId(Long shopId);
}
