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
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

@WebServlet("/delete")
public class DeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String user = (String) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect("login.html");
        } else {
            resp.sendRedirect("delete.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPost(req, resp);
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        String encoded = req.getParameter("encoded").replace(" ", "+");
        String keyEncoded = req.getParameter("keyEncoded").replace(" ", "+");
        String keyStr, content;
        Logger log = Logger.getLogger(DeleteServlet.class);
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
        String password = data.getString("password");
        String checkcode = data.getString("checkcode");
        String serial = data.getString("serial");
        long time = data.getLong("time");
        long now = (new Date()).getTime();
        if(time > now || (now - time > 60000)){
            log.error(" 请求时间不在可接受范围内。");
            return;
        }
        String user = (String) session.getAttribute("user");
        String answer = (String) session.getAttribute("checkcode");
        JSONObject jsonObject =  new JSONObject();
        if(checkcode == null || !checkcode.equals(answer)){
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
                if (userRepository.find(new User(user, password))) {
                    CertificateRepository certificateRepository = CertificateRepository.getInstance();
                    List<String> certificates = certificateRepository.findall(user);
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
                        log.warn(" 证书删除成功。");
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
            } else {
                jsonObject.put("res", "-1");
                jsonObject.put("error", "账号冻结！");
                resp.getWriter().write(EncDec.EncandSign(jsonObject.toString(), keyStr));
                log.error(" 用户 " + user + " 已被删除。");
                return;
            }
        }
//        else {
//            CertificateRepository certificateRepository = CertificateRepository.getInstance();
//            List<String> certificates = certificateRepository.findall(user);
//            String index = req.getParameter("index");
//            try{
//                Integer i = Integer.valueOf(index);
//                if(i >= 1 && i <= certificates.size()){
//                    certificateRepository.withdrawCertificate(certificates.get(i - 1).split(":")[0]);
//                    PrintWriter out = resp.getWriter();
//                    out.print("<html lang='zh'><script language='javascript'>alert('撤销证书成功。');window.location.href='delete.html';</script></html>");
//                    log.info(" 证书撤销成功。");
//                    return;
//                }else{
//                    log.fatal(" 序号范围不正确。");
//                    throw new RuntimeException();
//                }
//            }catch(NumberFormatException e){
//                log.fatal(" 序号格式错误。");
//                throw new RuntimeException();
//            }
//        }
    }
}
