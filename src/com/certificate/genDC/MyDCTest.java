package com.certificate.genDC;

import java.security.*;

public class MyDCTest {
    public static void main(String[] args) {
        String subject = "CN=*.hit.edu.cn," +
                "O=HIT," +
                "S=Harbin," +
                "C=CN";
        try {
            KeyPair kp1 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            KeyPair kp2 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PublicKey pk1 = kp1.getPublic(), pk2 = kp2.getPublic();
            PrivateKey sk1 = kp1.getPrivate(), sk2 = kp2.getPrivate();
            MyDC dc = new MyDC(subject, pk1, sk2, 5);
            dc.export("D://test.dc");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}

