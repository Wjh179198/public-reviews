package com.wjh.mapper;

import com.wjh.entity.ShopOrder;
import com.wjh.vo.ShopOrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ShopOrderMapper {

    @Insert("insert into shop_order(shop_id, user_id, voucher_id, price, status, order_time) " +
            "values(#{shopId}, #{userId}, #{voucherId}, #{price}, #{status}, #{orderTime})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShopOrder shopOrder);

    List<ShopOrderVO> getByUserId(Long userId, Integer status);

    @Select("select * from shop_order where user_id = #{userId} and id = #{orderId}")
    ShopOrder getOrderByUserIdAndOrderId(Long userId, Long orderId);

    @Update("update shop_order set status = #{status} where id = #{orderId}")
    void updateStatusById(Long orderId, int status);

    @Select("select * from shop_order where shop_id = #{shopId} and status = 1 and order_time >= #{startDateTime} and order_time <= #{endDateTime}")
    List<ShopOrder> getShopRevenue(Long shopId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
