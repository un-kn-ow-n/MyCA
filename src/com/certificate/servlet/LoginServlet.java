//package com.certificate.servlet;
//
//import com.certificate.entity.User;
//import com.certificate.repository.UserRepository;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//@WebServlet("/user")
//public class UserServlet extends HttpServlet {
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setContentType("text/html;charset=UTF-8");
//        resp.sendRedirect("login.jsp");
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        HttpSession session = req.getSession();
//        String method = req.getParameter("method");
//        String id = req.getParameter("id");
//        String password = req.getParameter("password");
//        User user = new User(id, password);
//        resp.setContentType("text/html;charset=UTF-8");
//        PrintWriter out = resp.getWriter();
//        UserRepository userRepository = UserRepository.getInstance();
//        switch(method){
//            case "signup":
//                if(userRepository.addUser(user)){
//                    req.setAttribute("message", "注册成功，请登录。");
//                    req.getRequestDispatcher("login.jsp").forward(req, resp);
//                } else {
//                    req.setAttribute("message", "该用户名已存在，注册失败。");
//                    req.getRequestDispatcher("signup.jsp").forward(req, resp);
//                }
//                break;
//            case "login":
//                if(userRepository.find(user)){
//                    session.setAttribute("user", user);
////                    req.setAttribute("message", null);
//                    resp.sendRedirect("certificate.jsp");
//                } else {
//                    session.setAttribute("user", null);
////                    req.setCharacterEncoding("UTF-8");
////                    session.setAttribute("message", "id或password错误。");
//                    req.setAttribute("message", "id或password错误。");
//                    req.getRequestDispatcher("login.jsp").forward(req, resp);
////                    resp.sendRedirect("login.jsp");
//                }
//                break;
//            default:
//                throw new IllegalStateException("Unexpected value: " + method);
//        }
//    }
//}
package com.certificate.servlet;

import com.certificate.entity.User;
import com.certificate.repository.UserRepository;
import com.certificate.test.EncDec;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final int MAX_DECRYPT_BLOCK = 128;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.sendRedirect("login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String encoded = req.getParameter("encoded").replace(" ", "+");
        String keyEncoded = req.getParameter("keyEncoded").replace(" ", "+");
        String keyStr, content;
        Logger log = Logger.getLogger(LoginServlet.class);
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
        String valid = userRepository.isvalid(id);
        JSONObject jsonObject = new JSONObject();
        if (checkcode == null || ! checkcode.equals(answer)){
            jsonObject.put("res", "-1");
            jsonObject.put("error", "验证码错误！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 验证码错误。");
            return;
        }
        session.removeAttribute("checkcode");
        if(valid == null){
            jsonObject.put("res", "-1");
            jsonObject.put("error", "用户名或密码不正确！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 用户名或密码不正确，登录失败。");
            return;
        } else if (valid.equals("YES")) {
            if (userRepository.find(user)) {
                session.removeAttribute("root");
                session.setAttribute("user", user.getId());
                jsonObject.put("res", "1");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.info(" 用户 " + user.getId() +" 成功登录。");
                return;
            } else {
                jsonObject.put("res", "-1");
                jsonObject.put("error", "用户名或密码不正确！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.warn(" 用户名或密码不正确，登录失败。");
                return;
            }
        } else if (valid.equals("NO")){
            jsonObject.put("res", "-1");
            jsonObject.put("error", "账号冻结！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 用户 " + user.getId() +" 被冻结。");
            return;
        } else if (valid.equals("FOREVER")){
            if (userRepository.find(user)) {
                session.setAttribute("user", user.getId());
                session.setAttribute("root", user.getId());
                jsonObject.put("res", "0");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.info(" root用户登录。");
                return;
            } else {
                jsonObject.put("res", "-1");
                jsonObject.put("error", "用户名或密码不正确！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.warn(" 用户名或密码不正确，登录失败。");
                return;
            }
        }
    }
}
