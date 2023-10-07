package com.certificate.servlet;

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
import java.util.Date;
import java.util.List;

@WebServlet("/readuser")
public class ReadUserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
//        resp.setHeader("Access-Control-Allow-Credentials", "true");
//        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        HttpSession session = req.getSession();
        String root = (String) session.getAttribute("root");
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        String encoded = req.getParameter("encoded").replace(" ", "+");
        String keyEncoded = req.getParameter("keyEncoded").replace(" ", "+");
        String keyStr, content;
        Logger log = Logger.getLogger(ReadUserServlet.class);
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
        long time = data.getLong("time");
        long now = (new Date()).getTime();
        if(time > now || (now - time > 60000)){
            log.error(" 请求时间不在可接受范围内。");
            return;
        }

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
            if (userRepository.find(new User(root, password))){
//                CertificateRepository certificateRepository = CertificateRepository.getInstance();
//                List<String> certificates = certificateRepository.findall();
//                jsonObject.put("res", "1");
//                jsonObject.put("certificates", certificates);
//                resp.getWriter().write(jsonObject.toString());
//                log.info(" 确认成功。");
//                return;
                List<String> users = userRepository.findall();
                jsonObject.put("res", "1");
                jsonObject.put("users", users);
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.info(" 获取用户列表成功。");
                return;
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
