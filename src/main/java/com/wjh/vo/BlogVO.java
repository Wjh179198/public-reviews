package com.wjh.vo;

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
public class BlogVO implements Serializable {

    private Long id;
    private Long userId;
    private String userName;
    private String userImage;
    private Long shopId;
    private String shopName;
    private Integer likes;
    private String title;
    private String content;
    private String images;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
