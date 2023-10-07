package com.certificate.servlet;

import com.certificate.entity.User;
import com.certificate.repository.CertificateRepository;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

@WebServlet("/query")
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("query.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        CertificateRepository certificateRepository = CertificateRepository.getInstance();
        String C, S, L, CN, subject, serial;
        C = req.getParameter("C");
        S = req.getParameter("S");
        L = req.getParameter("L");
        CN = req.getParameter("CN");
        subject = String.format("C=%s,S=%s,L=%s,CN=%s", C, S, L, CN);
        String checkcode = req.getParameter("checkcode");
        HttpSession session = req.getSession();
        String answer = (String) session.getAttribute("checkcode");
        Logger log = Logger.getLogger(QueryServlet.class);
        if(checkcode == null || !checkcode.equals(answer)){
            PrintWriter out = resp.getWriter();
            out.print("<html lang='zh'><head><meta charset='UTF-8'><script language='javascript'>alert('验证码错误。');window.location.href='query.html';</script></head></html>");
            log.warn(" 验证码错误。");
            return;
        }
        session.removeAttribute("checkcode");
        if ((serial = certificateRepository.findbysubject(subject)) == null) {
//            System.out.println("无此主体的证书！");
//            session.setAttribute("message", "无此主体的证书！");
            PrintWriter out = resp.getWriter();
            out.print("<html lang='zh'><head><meta charset='UTF-8'><script language='javascript'>alert('无此主体的证书，请重新输入。');window.location.href='query.html';</script></head></html>");
            log.warn(" 无此主体的证书。");
            return;
        } else {
            resp.setContentType("application/x-msdownload");
            resp.setHeader("Content-Disposition", "attachment;filename=" + serial + ".dc");
            OutputStream outputStream = resp.getOutputStream();
            String path = String.format("d://certificates/%s.dc", serial);
            InputStream inputStream = new FileInputStream(path);
            int temp;
            while ((temp = inputStream.read()) != -1) {
                outputStream.write(temp);
            }
            inputStream.close();
            outputStream.close();
            log.info(" 证书下载成功。");
        }
    }
}
