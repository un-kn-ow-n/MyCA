package com.certificate.test;

import com.certificate.genKeyPair.KeyOperation;
import net.sf.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class EncDec {

    private static String sk_path = "d://key/sign_sk_base64";
    private static String ALGORITHMSTR = "AES/ECB/PKCS7Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static PrivateKey getPrivateKey(){
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(sk_path));
            String temp;
            String content = "";
            while ((temp = bufferedReader.readLine()) != null) {
                content += temp;
            }
            bufferedReader.close();
            return KeyOperation.getPrivateKey(content);
        } catch (InvalidKeySpecException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String RSADec(String c) throws Exception{
        PrivateKey sk = getPrivateKey();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, sk);
        return new String(cipher.doFinal(Base64.getDecoder().decode(c)));

    }

    public static String AESDec(String c, String keyStr) throws Exception{
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        Key key = new SecretKeySpec(keyStr.getBytes(), "AES");
//        KeyGenerator keygen = KeyGenerator.getInstance("AES");
//        keygen.init(128, new SecureRandom(c.getBytes()));
//        SecretKey secretKey = keygen.generateKey();
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(c)));
    }

    public static String AESEnc(String m, String keyStr){
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
            Key key = new SecretKeySpec(keyStr.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
//            return new String(Base64.getDecoder().decode(cipher.doFinal(m.getBytes())));
            return new String(Base64.getEncoder().encode(cipher.doFinal(m.getBytes("utf-8"))));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String Sign(String c){
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(getPrivateKey());
            sign.update(c.getBytes());
            return new String(Base64.getEncoder().encode(sign.sign()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static String EncandSign(String m, String keyStr){
        JSONObject data = new JSONObject();
        String c = null;
        try {
            c = AESEnc(m, keyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("encoded", c);
        data.put("fingerprint", Sign(c));
        return  data.toString();
    }

}
