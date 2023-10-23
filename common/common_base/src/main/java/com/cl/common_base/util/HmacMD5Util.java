package com.cl.common_base.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacMD5Util {


    //HmacMD5算法
    public static final String KEY_MD5 = "HmacMD5";

    /**
     * MD5加密-》获取签名
     *
     * @param data      要签名的内容
     * @param sigSecret 签名秘钥
     * @return
     * @throws Exception
     */
    public static String creatSign(String data, String sigSecret) {
        try {
            byte[] bytes = data.getBytes();
            SecretKey secretKey = new SecretKeySpec(sigSecret.getBytes(), KEY_MD5);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] b = mac.doFinal(bytes);
            return bytesToHexString(b);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字节转16进制 字母大写
     * @param b MD5加密签名后的byte数组
     * @return
     */
    public static String bytesToHexString(byte[] b){
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        String sig = sb.toString();
        return sig.toUpperCase();
    }



    public static boolean verify(String sign, String verifyValue) {
        if(sign==null || verifyValue==null){
            return false;
        }
        return sign.toUpperCase().equals(verifyValue.toUpperCase());
    }


    public static void main(String[] args) {
        String value = "accessoryId=23&status=1&&timestamp=1697608237&organization=heyabby";
        String str = creatSign(value, "8E)mujI2");
        System.out.println(str);
    }
}
