package com.bms.BMSProject.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private final static String REFRESH_COOKIE_NAME = "refreshToken";

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME,token)
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .maxAge(30*24*60*60)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie",cookie.toString());
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies()==null) {
            return null;
        }
        for(Cookie cookie: request.getCookies()) {
            if(REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME,"")
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie",cookie.toString());
    }

}
