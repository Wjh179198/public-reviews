package com.wjh.mapper;

import com.wjh.entity.Shop;
import com.wjh.entity.ShopType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper {

    @Select("SELECT * FROM shop_type")
    List<ShopType> getType();

    void insert(Shop shop);
}
