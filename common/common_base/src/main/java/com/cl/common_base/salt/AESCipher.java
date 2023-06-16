package com.cl.common_base.salt;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 密码加解密工具类
 *
 * @author 85157
 */
public class AESCipher {

    private static final String IV_STRING = "4Ta7OaH6ZypA7856";
    public final static String KEY = "4Ta7OaH6ZypA7856";
    private static final String charset = "UTF-8";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String aesEncryptString(String content, String key) throws Exception {
        if (TextUtils.isEmpty(key)) {
            key = KEY;
        }
        byte[] contentBytes = content.getBytes(charset);
        byte[] keyBytes = key.getBytes(charset);
        byte[] encryptedBytes = aesEncryptBytes(contentBytes, keyBytes);
        Encoder encoder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encoder = Base64.getEncoder();
        }
        return encoder.encodeToString(encryptedBytes);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String aesDecryptString(String content, String key) throws Exception {
        if (TextUtils.isEmpty(key)) {
            key = KEY;
        }
        Decoder decoder = Base64.getDecoder();
        byte[] encryptedBytes = decoder.decode(content);
        byte[] keyBytes = key.getBytes(charset);
        byte[] decryptedBytes = aesDecryptBytes(encryptedBytes, keyBytes);
        return new String(decryptedBytes, charset);
    }

    public static byte[] aesEncryptBytes(byte[] contentBytes, byte[] keyBytes) throws Exception {
        return cipherOperation(contentBytes, keyBytes, Cipher.ENCRYPT_MODE);
    }

    public static byte[] aesDecryptBytes(byte[] contentBytes, byte[] keyBytes) throws Exception {
        return cipherOperation(contentBytes, keyBytes, Cipher.DECRYPT_MODE);
    }

    private static byte[] cipherOperation(byte[] contentBytes, byte[] keyBytes, int mode) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] initParam = IV_STRING.getBytes(charset);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, secretKey, ivParameterSpec);

        return cipher.doFinal(contentBytes);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void main(String[] args) throws Exception {
//		String string = AESCipher.aesEncryptString("20", "4Ta7OaH6ZypA7816");
//		System.out.println(string);
//		System.out.println(AESCipher.aesDecryptString(string, "4Ta7OaH6ZypA7856"));

        String sb = "https://heyabbytest.s3.us-west-1.amazonaws.com/user/head/cf88b3b6ca3143b48b56ef9cbdac2e8b.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20220802T075736Z&X-Amz-SignedHeaders=host&X-Amz-Expires=518400&X-Amz-Credential=AKIA5UEWHT7XNCDKTYWY%2F20220802%2Fus-west-1%2Fs3%2Faws4_request&X-Amz-Signature=7aa3c71853e617eae1c088ef74a55565a78deb30308b15135d91409637a3fe65";
        String str = sb.substring(3, sb.indexOf("/"));
        System.out.println(str);

    }
}