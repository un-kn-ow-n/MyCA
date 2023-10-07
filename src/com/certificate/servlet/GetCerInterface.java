package com.certificate.servlet;

import com.certificate.repository.CertificateRepository;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet("/getcer")
public class GetCerInterface extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
//        req.setCharacterEncoding("utf-8");
//        resp.setContentType("text/html; charset=utf-8");
//        resp.setCharacterEncoding("utf-8");
//        String C = req.getParameter("C");
//        String S = req.getParameter("S");
//        String L = req.getParameter("L");
//        String CN = req.getParameter("CN");
//        String subject = String.format("C=%s,S=%s,L=%s,CN=%s", C, S, L, CN);
//        String serial;
//        JSONObject jsonObject = new JSONObject();
//        CertificateRepository certificateRepository = CertificateRepository.getInstance();
//        if ((serial = certificateRepository.findbysubject(subject)) == null) {
//            jsonObject.put("res", "-1");
//            jsonObject.put("error", "无此主体的证书，请重新输入。");
//            resp.getWriter().write(jsonObject.toString());
//            return;
//        } else {
//            String path = String.format("d://certificates/%s.dc", serial);
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
//            String temp;
//            String certificate = "";
//            while ((temp = bufferedReader.readLine()) != null) {
//                certificate += temp;
//            }
//            bufferedReader.close();
//            jsonObject.put("res", "1");
//            jsonObject.put("certificate", certificate);
//            resp.getWriter().write(jsonObject.toString());
//            return;
//        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        String C = req.getParameter("C");
        String S = req.getParameter("S");
        String L = req.getParameter("L");
        String CN = req.getParameter("CN");
        String subject = String.format("C=%s,S=%s,L=%s,CN=%s", C, S, L, CN);
        String serial;
        JSONObject jsonObject = new JSONObject();
        Logger log = Logger.getLogger(GetCerInterface.class);
        CertificateRepository certificateRepository = CertificateRepository.getInstance();
        if ((serial = certificateRepository.findbysubject(subject)) == null) {
            jsonObject.put("res", "-1");
            jsonObject.put("error", "无此主体的证书，请重新输入。");
            jsonObject.put("subject", subject);
            resp.getWriter().write(jsonObject.toString());
            log.warn(" 无此主体的证书。");
            return;
        } else {
            String path = String.format("d://certificates/%s.dc", serial);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String temp;
            String certificate = "";
            while ((temp = bufferedReader.readLine()) != null) {
                certificate += temp + "\n";
            }
            bufferedReader.close();
            jsonObject.put("res", "1");
            jsonObject.put("subject", subject);
            jsonObject.put("certificate", certificate);
            resp.getWriter().write(jsonObject.toString());
            log.info(" 证书下载成功。");
            return;
        }
    }
}
