package com.wjh.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO implements Serializable {

    private Long id;
    private String name;
    private String image;
    private String address;
    private Integer fansCounts;
    private Integer followerCounts;
    private Integer status;
}
