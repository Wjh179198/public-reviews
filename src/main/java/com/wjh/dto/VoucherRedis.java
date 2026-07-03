package com.wjh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VoucherRedis implements Serializable {

    private Long id;
    private Long shopId;
    private BigDecimal price;
}
