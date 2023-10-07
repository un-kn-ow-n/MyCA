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
import java.util.Date;

@WebServlet("/modify_psw")
public class ModifyPswServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        HttpSession session = req.getSession();
        String user = (String) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect("login.html");
        } else {
            resp.sendRedirect("modifypsw.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
//        resp.setHeader("Access-Control-Allow-Credentials", "true");
////        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8080/modify_psw");
//        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        String encoded = req.getParameter("encoded").replace(" ", "+");
        String keyEncoded = req.getParameter("keyEncoded").replace(" ", "+");
        String keyStr, content;
        Logger log = Logger.getLogger(ModifyPswServlet.class);
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
        String opsw = data.getString("oldpassword");
        String npsw = data.getString("newpassword");
        String cnpsw = data.getString("confirmnewpassword");
        String checkcode = data.getString("checkcode");
        long time = data.getLong("time");
        long now = (new Date()).getTime();
        if(time > now || (now - time > 60000)){
            log.error(" 请求时间不在可接受范围内。");
            return;
        }

        String user = (String) session.getAttribute("user");
        String answer =(String) session.getAttribute("checkcode");
        JSONObject jsonObject = new JSONObject();
        if (checkcode == null || ! checkcode.equals(answer)){
            jsonObject.put("res", "0");
            jsonObject.put("error", "验证码错误！");
            resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
            log.warn(" 验证码错误。");
            return;
        }
        session.removeAttribute("checkcode");
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
                if (userRepository.find(new User(user, opsw))) {
                    if (npsw.equals(cnpsw)) {
                        userRepository.modifyPsw(new User(user, npsw));
                        jsonObject.put("res", "1");
                        resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                        log.info(" 用户 " + user + " 修改密码成功。");
                        return;
                    } else {
                        jsonObject.put("res", "0");
                        jsonObject.put("error", "两次输入的新密码不一致！");
                        resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                        log.warn(" 两次输入的新密码不一致。");
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
    }
}
