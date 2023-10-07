package com.certificate.genDC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class MyDC {

    private BigInteger SerialNumber;
    private String IssuerDN;
    private Date NotBefore;
    private Date NotAfter;
    private String SubjectDN;
    private PublicKey PublicKey;
    private String SignatureAlgorithm;
    private String signature;
    private final SimpleDateFormat sft = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public MyDC(String content) throws Exception{
        String[] lines = content.split("\n");
        this.SerialNumber = new BigInteger(lines[0].split(": ")[1]);
        this.IssuerDN = lines[1].split(": ")[1];
        this.NotBefore = sft.parse(lines[2].split(": ")[1]);
        this.NotAfter = sft.parse(lines[3].split(": ")[1]);
        this.SubjectDN = lines[4].split(": ")[1];
        this.PublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(lines[5].split(": ")[1])));
        this.SignatureAlgorithm = lines[6].split(": ")[1];
        this.signature = lines[7].split(": ")[1].replaceAll("\n", "");
    }

    public MyDC(String subject, PublicKey pk_subject, PrivateKey sk_ca, int days){
        this.SerialNumber = BigInteger.valueOf(System.currentTimeMillis());
        this.IssuerDN = "C=CN,S=HRB,L=HRB,CN=HIT";
        long start = System.currentTimeMillis();
        long end = start + ((long) days) * 24 * 60 * 60 * 1000;
        this.NotBefore = new Date(start);
        this.NotAfter = new Date(end);
        this.SubjectDN = subject;
        this.PublicKey = pk_subject;
        this.SignatureAlgorithm = "SHA256withRSA";
        try {
            String content = this.SerialNumber + this.IssuerDN + sft.format(this.NotBefore) + sft.format(this.NotAfter) +
                this.SubjectDN + new String(Base64.getEncoder().encode(this.PublicKey.getEncoded())) + this.SignatureAlgorithm;
            Signature sign = Signature.getInstance(this.SignatureAlgorithm);
            sign.initSign(sk_ca);
            sign.update(content.getBytes());
            this.signature = new String(Base64.getEncoder().encode(sign.sign()));

//            Signature verify = Signature.getInstance(this.SignatureAlgorithm);
//            verify.initVerify(pk_subject);
//            verify.update(content.getBytes());
//            System.out.println(verify.verify(Base64.getDecoder().decode(this.signature)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger getSerialNumber() {
        return SerialNumber;
    }

    public String getIssuerDN() {
        return IssuerDN;
    }

    public Date getNotBefore() {
        return NotBefore;
    }

    public Date getNotAfter() {
        return NotAfter;
    }

    public String getSubjectDN() {
        return SubjectDN;
    }

    public java.security.PublicKey getPublicKey() {
        return PublicKey;
    }

    public String getSignatureAlgorithm() {
        return SignatureAlgorithm;
    }

    public String getSignature() {
        return signature;
    }

    public boolean export(String path){
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(this.toString());
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String toString(){
        return "SerialNumber: " + this.SerialNumber.toString() +
                "\nIssuerDN: " + this.IssuerDN +
                "\nNotBefore: " + sft.format(this.NotBefore) +
                "\nNotAfter: " + sft.format(this.NotAfter) +
                "\nSubjectDN: " + this.SubjectDN +
                "\nPublicKey: " + new String(Base64.getEncoder().encode(this.PublicKey.getEncoded())) +
                "\nSignatureAlgorithm: " + this.SignatureAlgorithm +
                "\nSignature: " + this.signature;
    }

    public void checkValidity() throws CertificateNotYetValidException, CertificateExpiredException {
        Date now = new Date();
        if(now.before(this.NotBefore)){
            throw new CertificateNotYetValidException();
        } else if(now.after(this.NotAfter)){
            throw new CertificateExpiredException();
        }
    }

    public void verify(PublicKey pk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IllegalArgumentException {
        Signature sign = Signature.getInstance(this.SignatureAlgorithm);
        sign.initVerify(pk);
        String content = this.SerialNumber + this.IssuerDN + sft.format(this.NotBefore) + sft.format(this.NotAfter) +
                this.SubjectDN + new String(Base64.getEncoder().encode(this.PublicKey.getEncoded())) + this.SignatureAlgorithm;
        sign.update(content.getBytes());
        if(!sign.verify(Base64.getDecoder().decode(this.signature))){
            throw new SignatureException();
        }
    }
}
