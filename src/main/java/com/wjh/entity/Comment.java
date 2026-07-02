package com.wjh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long shopId;
    private Long userId;
    private String content;
    private Integer score;
    private Integer likes;
    private String images;
    private LocalDateTime createTime;
}
