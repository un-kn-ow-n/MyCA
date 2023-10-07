package com.certificate.servlet;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
//        session.setAttribute("user", null);
        String user = (String) session.getAttribute("user");
        session.invalidate();
//        session.setAttribute("message", "成功登出。");
        resp.sendRedirect("login.html");
        Logger log = Logger.getLogger(LogoutServlet.class);
        if(user != null){
            log.info("用户 " + user + " 成功登出。");
        }
        return;
    }
}
