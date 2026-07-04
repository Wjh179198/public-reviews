package com.wjh.context;

import com.wjh.dto.AdminDTO;

public class AdminBaseContext {

    private static final ThreadLocal<AdminDTO> threadLocal = new ThreadLocal<>();

    public static void set(AdminDTO adminDTO) {
        threadLocal.set(adminDTO);
    }

    public static AdminDTO get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
