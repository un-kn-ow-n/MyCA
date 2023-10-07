package com.certificate.servlet;

import com.certificate.entity.Certificate;
import com.certificate.entity.User;
import com.certificate.genDC.MyDC;
import com.certificate.genKeyPair.KeyOperation;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.UserRepository;
import com.certificate.test.EncDec;
import com.certificate.verify.verify;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/verify")
public class VerifyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("verify.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
//        resp.setHeader("Access-Control-Allow-Credentials", "true");
////        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8080/apply");
//        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        JSONObject jsonObject = new JSONObject(), withFingerPrint = new JSONObject();
        Logger log = Logger.getLogger(VerifyServlet.class);
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            List items = upload.parseRequest(req);
            Map params = new HashMap();
            for(Object object:items){
                FileItem fileItem = (FileItem) object;
                if (fileItem.isFormField()) {
                    //这里就相当与FormData中的key,value
                    params.put(fileItem.getFieldName(), fileItem.getString("utf-8"));//如果你页面编码是utf-8的
                } else {
                    params.put(fileItem.getFieldName(), fileItem.getInputStream());
                }
            }

            //使用params.get获取参数值 send_time就是key
            String checkcode = (String) params.get("checkcode");
            String time = (String) params.get("time");
            String answer = (String) session.getAttribute("checkcode");
            if(checkcode == null || !checkcode.equals(answer)){
                jsonObject.put("res", "0");
                jsonObject.put("error", "验证码错误！");
                jsonObject.put("ts", time);
                String result = new String(Base64.getEncoder().encode(jsonObject.toString().getBytes("utf-8")));
                withFingerPrint.put("messa", result);
                withFingerPrint.put("fingerprint", EncDec.Sign(result));
                resp.getWriter().write(withFingerPrint.toString());
                log.warn(" 验证码错误。");
                return;
            }
            session.removeAttribute("checkcode");
            byte[] cert = ((InputStream) params.get("cert")).readAllBytes();
            try {
                MyDC dc = new MyDC(new String(cert));
                if(verify.verify(dc, CertificateRepository.getInstance().getCRL())){
                    jsonObject.put("res", "1");
                    jsonObject.put("ts", time);
                    String result = new String(Base64.getEncoder().encode(jsonObject.toString().getBytes("utf-8")));
                    withFingerPrint.put("messa", result);
                    withFingerPrint.put("fingerprint", EncDec.Sign(result));
                    resp.getWriter().write(withFingerPrint.toString());
                    log.info(" 证书验证成功。");
                    return;
                } else {
                    jsonObject.put("res", "0");
                    jsonObject.put("error", "证书未通过验证！");
                    jsonObject.put("ts", time);
                    String result = new String(Base64.getEncoder().encode(jsonObject.toString().getBytes("utf-8")));
                    withFingerPrint.put("messa", result);
                    withFingerPrint.put("fingerprint", EncDec.Sign(result));
                    resp.getWriter().write(withFingerPrint.toString());
                    log.warn(" 证书未通过验证！");
                    return;
                }
            } catch (Exception e) {
                jsonObject.put("res", "0");
                jsonObject.put("error", "证书格式错误！");
                jsonObject.put("ts", time);
                String result = new String(Base64.getEncoder().encode(jsonObject.toString().getBytes("utf-8")));
                withFingerPrint.put("messa", result);
                withFingerPrint.put("fingerprint", EncDec.Sign(result));
                resp.getWriter().write(withFingerPrint.toString());
                log.warn(" 证书格式错误。");
                return;
            }
        } catch (FileUploadException e) {
            log.fatal(" 文件上传错误。");
            throw new RuntimeException(e);
        }
    }
}
