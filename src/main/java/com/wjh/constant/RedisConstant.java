package com.wjh.constant;

public class RedisConstant {
    public static final String SMS_CODE_KEY = "user:code";
    public static final Long SMS_CODE_EXPIRE = 60L;
    public static final String USER_LOGIN_KEY = "user:login";
    public static final Long USER_LOGIN_EXPIRE = 60L * 60L * 2L;
    public static final String USER_FOLLOW_KEY = "user:follow";
    public static final String USER_FAN_KEY = "user:fan";
    public static final String SHOP_TYPE_KEY = "shop:type";
    public static final Long SHOP_TYPE_EXPIRE = 60L * 60L * 24L;
    public static final String SHOP_COMMENT_KEY = "shop:comment";

    public static final String BLOG_KEY = "shop:blog";
    public static final String ADMIN_LOGIN_KEY = "admin:login";
    public static final Long ADMIN_LOGIN_EXPIRE = 60L * 60L * 2L;
    public static final String USER_MONEY_KEY = "user:money";
    public static final String BAN_USER_KEY = "user:ban";
    public static final String SHOP_VOUCHER_KEY = "shop:voucher";
    public static final Long SHOP_VOUCHER_EXPIRE = 60L * 10L;
    public static final String VOUCHER_STOCK_KEY = "voucher:stock";
    public static final String VOUCHER_BEGIN_KEY = "voucher:begin";
    public static final String VOUCHER_END_KEY = "voucher:end";
    public static final String VOUCHER_ORDER_KEY = "voucher:order";
    public static final String VOUCHER_KEY = "voucher";
}
