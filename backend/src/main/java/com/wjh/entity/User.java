package com.wjh.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = 1L; // Changed from private to public

    private Long id;
    private String name;
    private String phone;
    private String image;
    private String password;
    private Integer fansCounts;
    private Integer followerCounts;
    private BigDecimal money;
    private String address;
    private Integer status;
    private Long shopId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;


}
