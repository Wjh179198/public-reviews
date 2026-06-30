package com.wjh.vo;

import com.wjh.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVO implements Serializable{
    private String token;
    private User user;
}
