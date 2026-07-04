package com.wjh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopType implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本号

    private Long id;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
