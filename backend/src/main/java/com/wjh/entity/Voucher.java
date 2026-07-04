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
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long shopId;
    private BigDecimal price;
    private BigDecimal value;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer stock;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
