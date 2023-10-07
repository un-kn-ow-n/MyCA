package com.certificate.verify;

import com.certificate.genDC.MyDC;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class verify {

    private static String pk_base64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAioCslqlD6fXtBDvnvAyIo3VlqjbYEdf1gl/piZ7DNLB6wL4xrQerNNXQ83gzMsij64LkPHjaChY4F+vmFDl3JOJJeSzB20NK9NQ6ifQgVD9iFyBtuiaYSj/KtWFRWjXXtoJEu1OzMvliaGxOrZRTYr0d2S74JcxQHKVQGvajzKo4EaAgyai7R9jFL0gK7/SdX2t+dtTGL7ocxoMcoh5+ivbaZTjMv0Uu6ARfAnFRpDwxDfZyTn7YQxqlsaHrAaHG8jeBL1Rsip+H5CINnOApOnKNbiuxx9if7G+W+fO26iqYWUoBDAFoDpjJecQr474lyHU6hNywVzi4MiZKGcO6mwIDAQAB";

    public static PublicKey getPublicKey(String pk_base64) throws Exception{
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pk_base64)));
    }

    public static boolean verify(MyDC cert, List<String> crl){
        if(cert == null){
            System.out.println("cert is null!");
            return false;
        }

        PublicKey pk = null;
        try {
            pk = getPublicKey(pk_base64);
        } catch (Exception e) {
            System.out.println("pkPath error or pk syntax error!");
            throw new RuntimeException(e);
        }
        String serialnumber = cert.getSerialNumber().toString();
        if(crl != null && crl.contains(serialnumber)){
            System.out.println("cer deleted!");
            return false;
        } else {
            try {
                cert.checkValidity();
            } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                System.out.println("not in validtime!");
                return false;
            }
            try {
                cert.verify(pk);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("sign-algorithm not supported!");
                return false;
            } catch (InvalidKeyException e) {
                System.out.println("pk syntax error!");
                throw new RuntimeException(e);
            } catch (SignatureException e) {
                System.out.println("sign error!!!");
                return false;
            }
            return true;
        }
    }

    public static List<String> getCRL(String crlPath){
        List<String> crl = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(crlPath));
            String serial;
            while ((serial = bufferedReader.readLine()) != null){
                crl.add(serial);
            }
            return crl;
        } catch (FileNotFoundException e) {
            System.out.println("crlPath error! crl = null.");
            return null;
        } catch (IOException e) {
            System.out.println("I/O exception! crl = null.");
            return null;
        }
    }
}
