package com.wjh.mapper;

import com.wjh.entity.Shop;
import com.wjh.entity.ShopType;
import com.wjh.vo.ShopVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ShopMapper {

    @Select("SELECT * FROM shop_type")
    List<ShopType> getType();

    void insert(Shop shop);

    List<ShopVO> selectWithNoComment(Long typeId);

    List<ShopVO> getListByPageParam(Long typeId, BigDecimal lowScore, BigDecimal highScore);

    @Select("select s.*, st.name as type_name from shop s left join shop_type st on s.type_id = st.id where s.id = #{shopId}")
    ShopVO getShopVoById(Long shopId);

    List<ShopVO> getListByName(String keyword);

    @Select("select * from shop where id = #{shopId}")
    Shop getById(Long shopId);

    void update(Shop shop);
}
