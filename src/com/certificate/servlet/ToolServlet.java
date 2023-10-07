package com.certificate.servlet;

import com.certificate.entity.Certificate;
import com.certificate.repository.CertificateRepository;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.List;

@WebServlet("/tool")
public class ToolServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("tool.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPost(req, resp);
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        String checkcode = req.getParameter("checkcode");
        String answer = (String) session.getAttribute("checkcode");
        Logger log = org.apache.log4j.Logger.getLogger(ToolServlet.class);
        if(checkcode == null || !checkcode.equals(answer)){
            PrintWriter out = resp.getWriter();
            out.print("<html lang='zh'><head><meta charset='UTF-8'><script language='javascript'>alert('验证码错误。');window.location.href='tool.html';</script></head></html>");
            log.warn(" 验证码错误。");
            return;
        } else {
            session.removeAttribute("checkcode");
            String option = req.getParameter("option");
            resp.setContentType("application/x-msdownload");
            if(option != null && option.equals("1"))            {
                resp.setHeader("Content-Disposition", "attachment;filename=ToolandFingerprint.zip");
                OutputStream outputStream = resp.getOutputStream();
                String path = "d://tools/TaF.zip";
                InputStream inputStream = new FileInputStream(path);
                int temp;
                while ((temp = inputStream.read()) != -1) {
                    outputStream.write(temp);
                }
                inputStream.close();
                outputStream.close();
                log.info(" 工具下载成功。");
                return;
            } else if(option != null && option.equals("2")){
                resp.setHeader("Content-Disposition", "attachment;filename=crl");
                OutputStream outputStream = resp.getOutputStream();
                CertificateRepository certificateRepository = CertificateRepository.getInstance();
                List<String> crl = certificateRepository.getCRL();
                for(String temp : crl){
                    temp += "\n";
                    outputStream.write(temp.getBytes());
                }
                outputStream.close();
                log.info(" crl下载成功。");
                return;
            }
        }
    }
}
