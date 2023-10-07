package com.certificate.servlet;

import com.certificate.entity.User;
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
import java.security.Signature;
import java.util.Date;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final int MAX_DECRYPT_BLOCK = 128;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.sendRedirect("signup.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String encoded = req.getParameter("encoded").replace(" ", "+");
        String keyEncoded = req.getParameter("keyEncoded").replace(" ", "+");
        String keyStr, content;
        Logger log = Logger.getLogger(SignupServlet.class);
        try {
            keyStr = EncDec.RSADec(keyEncoded);
            content = EncDec.AESDec(encoded, keyStr);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(" 通讯异常。");
            return;
        }
        HttpSession session = req.getSession();
        JSONObject data = JSONObject.fromObject(content);
        String id = data.getString("id");
        String password = data.getString("password");
        String checkcode = data.getString("checkcode");
        long time = data.getLong("time");
        long now = (new Date()).getTime();
        if(time > now || (now - time > 60000)){
            log.error(" 请求时间不在可接受范围内。");
            return;
        }
        String answer = (String) session.getAttribute("checkcode");
        User user = new User(id, password);
        UserRepository userRepository = UserRepository.getInstance();
        JSONObject jsonObject = new JSONObject();
        if(checkcode == null || !checkcode.equals(answer)) {
            jsonObject.put("res", "-1");
            jsonObject.put("error", "验证码错误！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 验证码错误。");
            return;
        }
        if(userRepository.addUser(user)){
            jsonObject.put("res", "1");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.info(" 成功注册账号 " + user.getId() + "。");
            return;
        } else {
            jsonObject.put("res", "-1");
            jsonObject.put("error", "该用户名已存在，注册失败。");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 用户名 " + user.getId() + " 已存在，注册失败。");
            return;
        }
    }
}
