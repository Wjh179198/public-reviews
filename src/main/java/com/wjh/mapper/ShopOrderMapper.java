package com.wjh.mapper;

import com.wjh.entity.ShopOrder;
import com.wjh.vo.ShopOrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShopOrderMapper {

    @Insert("insert into shop_order(shop_id, user_id, voucher_id, price, status, order_time) " +
            "values(#{shopId}, #{userId}, #{voucherId}, #{price}, #{status}, #{orderTime})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShopOrder shopOrder);

    List<ShopOrderVO> getByUserId(Long userId, Integer status);

}
