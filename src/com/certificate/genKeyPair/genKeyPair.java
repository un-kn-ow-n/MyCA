package com.certificate.genKeyPair;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.util.Base64;

public class genKeyPair {

        public static void main(String[] args) {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PublicKey pk = keyPair.getPublic();
            PrivateKey sk = keyPair.getPrivate();
            FileWriter fw1 = new FileWriter("pk_base64"), fw2 = new FileWriter("sk_base64");
            fw1.write(new String(Base64.getEncoder().encode(pk.getEncoded())));
            fw2.write(new String(Base64.getEncoder().encode(sk.getEncoded())));
            fw1.close();
            fw2.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
