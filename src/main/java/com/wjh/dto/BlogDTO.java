package com.wjh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDTO implements Serializable {

    private Long shopId;
    private String title;
    private String content;
    private String images;
}
