package com.wjh.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminVO implements Serializable {

    private Long id;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
