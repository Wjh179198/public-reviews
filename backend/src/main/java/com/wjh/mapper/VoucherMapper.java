package com.wjh.mapper;

import com.wjh.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface VoucherMapper {

    @Select("select * from voucher where shop_id = #{shopId}")
    List<Voucher> getByShopId(Long shopId);

    void insert(Voucher voucher);

    @Select("select * from voucher where id = #{voucherId}")
    Voucher getById(Long voucherId);

    @Update("update voucher set stock = stock - 1 where id = #{id} and stock > 0")
    int decrementStock(Long id);
}
