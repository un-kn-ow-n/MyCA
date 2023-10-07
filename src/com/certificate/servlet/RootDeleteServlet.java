package com.certificate.servlet;

import com.certificate.entity.Certificate;
import com.certificate.entity.User;
import com.certificate.repository.CertificateRepository;
import com.certificate.repository.UserRepository;
import com.certificate.test.EncDec;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

@WebServlet("/rootdelete")
public class RootDeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        HttpSession session = req.getSession();
        String root = (String) session.getAttribute("root");
        if(root == null){
            resp.sendRedirect("/logout");
        } else {
            resp.sendRedirect("rootdelete.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        HttpSession session = req.getSession();
        String encoded = req.getParameter("encoded").replace(" ", "+");
        String keyEncoded = req.getParameter("keyEncoded").replace(" ", "+");
        String keyStr, content;
        Logger log = Logger.getLogger(RootDeleteServlet.class);
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
        String serial = data.getString("serial");
        long time = data.getLong("time");
        long now = (new Date()).getTime();
        if(time > now || (now - time > 60000)){
            log.error(" 请求时间不在可接受范围内。");
            return;
        }

        String root = (String) session.getAttribute("root");

        String answer = (String) session.getAttribute("checkcode");
        JSONObject jsonObject =  new JSONObject();
        if(root == null){
            jsonObject.put("res", "-1");
            jsonObject.put("error", "请先登录！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 未登录。");
            return;
        } else {
            if(checkcode == null || !checkcode.equals(answer)){
                jsonObject.put("res", "0");
                jsonObject.put("error", "验证码错误！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.warn(" 验证码错误。");
                return;
            }
            session.removeAttribute("checkcode");

            UserRepository userRepository = UserRepository.getInstance();
            String valid = userRepository.isvalid(root);
            if(valid == null || !valid.equals("FOREVER")){
                jsonObject.put("res", "-1");
                jsonObject.put("error", "非管理员用户！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.error(" 非管理员用户。");
                return;
            } else {
                if (userRepository.find(new User(root, password))) {
                    CertificateRepository certificateRepository = CertificateRepository.getInstance();
                    List<String> certificates = certificateRepository.findall();
                    boolean flag = false;
                    for(int i = 0; i < certificates.size(); i++){
                        if(certificates.get(i).split(":")[0].equals(serial)){
                            certificateRepository.withdrawCertificate(serial);
                            flag = true;
                            break;
                        }
                    }
                    if(flag){
                        jsonObject.put("res", "1");
                        resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                        log.info(" 证书删除成功。");
                        return;
                    } else {
                        jsonObject.put("res", "0");
                        jsonObject.put("error", "不存在此序列号的证书！");
                        resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                        log.warn(" 不存在此序列号的证书。");
                        return;
                    }
                } else {
                    jsonObject.put("res", "0");
                    jsonObject.put("error", "密码错误！");
                    resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                    log.warn(" 密码错误。");
                    return;
                }
            }
        }
    }
}
