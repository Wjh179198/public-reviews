package com.wjh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopRegisterDTO implements Serializable {

    private Long typeId;
    private String name;
    private String address;
    private BigDecimal price;
    private String images;
}
