package com.wjh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long shopId;
    private Long userId;
    private Long voucherId;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime orderTime;
}
