package com.wjh.constant;

public class RedisConstant {
    public static final String SMS_CODE_KEY = "user:code";
    public static final Long SMS_CODE_EXPIRE = 60L;
    public static final String USER_LOGIN_KEY = "user:login";
    public static final Long USER_LOGIN_EXPIRE = 60L * 60L * 2L;
    public static final String USER_FOLLOW_KEY = "user:follow";
    public static final String USER_FAN_KEY = "user:fan";
}
