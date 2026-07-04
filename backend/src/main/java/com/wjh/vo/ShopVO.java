package com.wjh.vo;

import com.wjh.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopVO implements Serializable {

    private Long id;
    private Long typeId;
    private String name;
    private String images;
    private String address;
    private BigDecimal price;
    private Integer comments;
    private Integer sold;
    private BigDecimal score;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String typeName;
}
