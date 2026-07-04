package com.wjh.vo;

import com.wjh.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminLoginVO implements Serializable {

    private String token;
    private Admin admin;
}
