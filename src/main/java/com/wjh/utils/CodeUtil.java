package com.wjh.utils;

import java.util.Random;

public class CodeUtil {
    public static String getCode () {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i = 0; i < 6; i++) {
            int num = r.nextInt(10);
            sb.append(num + '0');
        }
        return sb.toString();
    }
}
