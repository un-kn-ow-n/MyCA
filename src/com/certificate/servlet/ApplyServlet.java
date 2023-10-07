package com.certificate.servlet;

import com.certificate.entity.Certificate;
import com.certificate.entity.User;
import com.certificate.genDC.MyDC;
import com.certificate.genKeyPair.KeyOperation;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.UserRepository;
import com.certificate.test.EncDec;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/apply")
@MultipartConfig
public class ApplyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String user = (String) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect("login.html");
        } else {
            resp.sendRedirect("apply.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession();
        String user = (String) session.getAttribute("user");
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
//        resp.setHeader("Access-Control-Allow-Credentials", "true");
////        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8080/apply");
//        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        JSONObject jsonObject = new JSONObject();
        Logger log = Logger.getLogger(ApplyServlet.class);
        String keyStr = null;
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

            String encoded = (String) params.get("encoded");
            String keyEncoded = (String) params.get("keyEncoded");
            String content;
            try {
                keyStr = EncDec.RSADec(keyEncoded);
                content = EncDec.AESDec(encoded, keyStr);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(" 通讯异常。");
                return;
            }

            JSONObject data = JSONObject.fromObject(content);
            String password = data.getString("password");
            String checkcode = data.getString("checkcode");
            String C = data.getString("C");
            String S = data.getString("S");
            String L = data.getString("L");
            String CN = data.getString("CN");
            long time = data.getLong("time");
            long now = (new Date()).getTime();
            if(time > now || (now - time > 60000)){
                log.error(" 请求时间不在可接受范围内。");
                return;
            }
            String answer = (String) session.getAttribute("checkcode");
            if(checkcode == null || !checkcode.equals(answer)){
                jsonObject.put("res", "0");
                jsonObject.put("error", "验证码错误！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.warn(" 验证码错误。");
                return;
            }
            session.removeAttribute("checkcode");
            byte[] publickey = ((InputStream) params.get("publickey")).readAllBytes();
            PublicKey pk = KeyOperation.getPublicKey(new String(publickey));
            if(user == null){
                jsonObject.put("res", "-1");
                jsonObject.put("error", "请先登录！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.warn(" 未登录。");
                return;
            } else {
                UserRepository userRepository = UserRepository.getInstance();
                String valid = userRepository.isvalid(user);
                if(valid == null){
                    jsonObject.put("res", "-1");
                    jsonObject.put("error", "账号被删除！");
                    resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                    log.error(" 用户 " + user + " 已被删除。");
                    return;
                } else if(!valid.equals("NO")) {
                    if (userRepository.find(new User(user, password))) {
                        int days = 365;
                        String subject = String.format("C=%s,S=%s,L=%s,CN=%s", C, S, L, CN);
                        CertificateRepository certificateRepository = CertificateRepository.getInstance();
                        if (certificateRepository.findbysubject(subject) != null) {
                            jsonObject.put("res", "0");
                            jsonObject.put("error", "已存在该申请人!");
                            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                            log.warn(" 申请人 " + subject + " 已存在。");
                            return;
                        } else {
//                            DC dc = new DC();
                            PrivateKey sk = null;
                            try {
//                                InputStream is = new FileInputStream("d://key/sign_sk");
                                BufferedReader bf = new BufferedReader(new FileReader("d://key/sign_sk_base64"));
                                String line, sk_content = "";
                                while ((line = bf.readLine()) != null){
                                    sk_content += line;
                                }
                                sk = KeyOperation.getPrivateKey(sk_content);
                            } catch (Exception e) {
                                log.fatal(" 私钥文件位置或格式不正确。");
                                throw new RuntimeException("私钥文件位置或格式不正确！");
                            }
                            MyDC cert = new MyDC(subject, pk, sk, days);
                            cert.export(String.format("d://certificates/%s.dc", cert.getSerialNumber().toString()));
//                            X509Certificate cert = dc.generateDC(subject, pk, sk, days);
//                            dc.Cer_exporter(String.format("d://certificates/%s.cer", cert.getSerialNumber().toString()));
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                            certificateRepository.addCertificate(new Certificate(cert.getSerialNumber().toString(), subject, user, simpleDateFormat.format(cert.getNotBefore()), simpleDateFormat.format(cert.getNotAfter())));
                            jsonObject.put("res", "1");
                            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                            log.info(" 证书申请成功。");
                            return;
                        }
                    } else {
                        jsonObject.put("res", "0");
                        jsonObject.put("error", "密码错误！");
                        resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                        log.warn(" 密码错误。");
                        return;
                    }
                } else {
                    jsonObject.put("res", "-1");
                    jsonObject.put("error", "账号冻结！");
                    resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                    log.error(" 用户 " + user + " 已被删除。");
                    return;
                }
            }
        } catch (FileUploadException e) {
            log.fatal(" 文件上传错误。");
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            jsonObject.put("res", "0");
            jsonObject.put("error", "公钥格式不正确！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.error(" 公钥格式不正确。");
            return;
        }
    }
}
