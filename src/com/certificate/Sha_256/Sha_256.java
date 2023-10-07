package com.certificate.Sha_256;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha_256 {

    private static MessageDigest md;

    static{
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw(new RuntimeException(e));
        }
    }

    /**
     * 不确定正确性
     * @return salt
     */
    public static String getSalt(){
        return RandomStringUtils.random(10, new char[]{'0','1','2','3',
                '4','5','6','7','8','9','a','b','c','d','e','f'});
    }

    public static String Bytes2Hex(byte[] ByteValue){
        String HexValue = "";
        for(int i = 0; i < ByteValue.length; i++){
            int val = ((int) ByteValue[i]) & 0xff;
            if(val < 16){
                HexValue += "0";
            }
            HexValue += Integer.toHexString(val);
        }
        return HexValue;
    }

    public static String Sha_256(String password, String salt){
        byte[] bt = new byte[0];
        String str = password + salt;
        try {
            bt = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw(new RuntimeException(e));
        }
        byte[] bt_encoded = md.digest(bt);
        return Bytes2Hex(bt_encoded);
    }

    public static void main(String[] args) {
        System.out.println(Sha_256.Sha_256("helloworld",Sha_256.getSalt()));
        System.out.println(Sha_256.Sha_256("helloworld",Sha_256.getSalt()));
        System.out.println(Sha_256.Sha_256("helloworld",Sha_256.getSalt()));
    }
}
