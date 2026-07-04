package com.wjh.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderVO implements Serializable {

    private Long id;
    private Long shopId;
    private String shopName;
    private Long userId;
    private BigDecimal price;
    private Integer status;
    private Long voucherId;
    private BigDecimal voucherValue;
    private LocalDateTime orderTime;

}
