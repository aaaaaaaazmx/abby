package com.cl.common_base.util;

import com.auth0.android.jwt.JWT;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class JwtParser {
    public static String parser(String jwtToken) {
        try {
            JWT jwt = new JWT(jwtToken);
            return jwt.getClaim("aud").asString();
        } catch (Exception e) {
            // JWT验证失败
            return null;
        }
    }
}
