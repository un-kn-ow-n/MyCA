package com.certificate.servlet;

import com.certificate.repository.CertificateRepository;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@WebServlet("/userdownload")
public class UserCertDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String user = (String) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect("login.html");
        } else {
            resp.sendRedirect("read.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        String user = (String) session.getAttribute("user");
        Logger log = Logger.getLogger(UserCertDownloadServlet.class);
        if(user == null){
            resp.sendRedirect("login.html");
        } else {
            CertificateRepository certificateRepository = CertificateRepository.getInstance();
            List<String> certificates = certificateRepository.findall(user);
            String index = req.getParameter("index");
            try{
                Integer i = Integer.valueOf(index);
                if(i >= 1 && i <= certificates.size()){
                    String serial = certificates.get(i - 1).split(":")[0];
                    resp.setContentType("application/x-msdownload");
                    resp.setHeader("Content-Disposition","attachment;filename=" + serial + ".dc");
                    OutputStream outputStream = resp.getOutputStream();
                    String path = String.format("d://certificates/%s.dc", serial);
                    InputStream inputStream = new FileInputStream(path);
                    int temp;
                    while((temp = inputStream.read()) != -1){
                        outputStream.write(temp);
                    }
                    inputStream.close();
                    outputStream.close();
                    log.info(" 证书下载成功。");
                }else{
//                    System.out.println("序号范围不正确！");
//                    session.setAttribute("message", "序号范围不正确！");
//                    resp.sendRedirect("/confirm");
                    log.fatal(" 序号范围不正确。");
                    throw new RuntimeException();
                }
            }catch(NumberFormatException e){
//                System.out.println("序号格式错误！");
//                session.setAttribute("message", "序号格式错误！");
//                resp.sendRedirect("/confirm");
                log.fatal(" 序号格式错误。");
                throw new RuntimeException();
            }
        }
    }
}
