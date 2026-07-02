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
public class CommentVO implements Serializable {

    private Long id;
    private Long shopId;
    private Long userId;
    private String userName;
    private String userImage;
    private String content;
    private Integer score;
    private Integer likes;
    private String images;
    private LocalDateTime createTime;

}
