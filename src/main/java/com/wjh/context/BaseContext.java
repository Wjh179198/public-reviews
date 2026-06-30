package com.wjh.context;

import com.wjh.dto.UserDTO;

public class BaseContext {
    private static final ThreadLocal<UserDTO> threadLocal = new ThreadLocal<>();

    public static void setThreadLocal (UserDTO userDTO) {
        threadLocal.set(userDTO);
    }

    public static UserDTO getThreadLocal () {
        return threadLocal.get();
    }

    public static void removeThreadLocal () {
        threadLocal.remove();
    }
}
