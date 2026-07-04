package com.wjh.entity;

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
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
