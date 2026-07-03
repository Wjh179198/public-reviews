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
public class VoucherOrderVO implements Serializable {

    private Long id;
    private Long userId;
    private Long voucherId;
    private BigDecimal voucherValue;
    private BigDecimal voucherPrice;
    private String shopName;
    private Integer status;
    private LocalDateTime orderTime;

}
