package com.certificate.genDC;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.util.Base64;

public class MyDCRootGen {
    public static void main(String[] args) {
        String subject = "CN=*.hit.edu.cn," +
                "O=HRB," +
                "S=HLJ," +
                "C=CN";
        try {
            KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PublicKey pk = kp.getPublic();
            PrivateKey sk = kp.getPrivate();
            MyDC dc = new MyDC(subject, pk, sk, 365 * 6 + 366 * 2);
            dc.export("d://certificates/root.dc");
            FileWriter fileWriter = new FileWriter("sign_sk");//生成后会经过处理以实现保密
            fileWriter.write(new String(Base64.getEncoder().encode(sk.getEncoded())));
            fileWriter.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
