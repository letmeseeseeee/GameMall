package com.gamemall.security;

import com.gamemall.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityContext {
    private SecurityContext() {
    }

    public static SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser)) {
            throw new BizException(401, "not logged in");
        }
        return (SecurityUser) authentication.getPrincipal();
    }

    public static Long currentUserId() {
        return currentUser().getUserId();
    }
}
