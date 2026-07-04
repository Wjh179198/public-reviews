package com.wjh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherDTO implements Serializable {

    private BigDecimal price;
    private BigDecimal value;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer stock;
}
