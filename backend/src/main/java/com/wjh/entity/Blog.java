package com.wjh.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long shopId;
    private Integer likes;
    private String title;
    private String content;
    private String images;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
