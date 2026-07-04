package com.wjh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRedis implements Serializable {

    private Long id;
    private Long shopId;
    private BigDecimal price;
}
