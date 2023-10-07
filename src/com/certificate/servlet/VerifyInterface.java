package com.certificate.servlet;

import com.certificate.genDC.MyDC;
import com.certificate.repository.CertificateRepository;
import com.certificate.verify.verify;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyInterface extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPost(req, resp);
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        JSONObject jsonObject = new JSONObject();
        Logger log = Logger.getLogger(VerifyInterface.class);
        String cert = (String) req.getParameter("cert");
        try {
            MyDC dc = new MyDC(cert);
            if(verify.verify(dc, CertificateRepository.getInstance().getCRL())){
                jsonObject.put("res", "1");
                jsonObject.put("cert", cert);
                resp.getWriter().write(jsonObject.toString());
                log.info(" 证书验证成功。");
                return;
            } else {
                jsonObject.put("res", "0");
                jsonObject.put("error", "证书未通过验证！");
                jsonObject.put("cert", cert);
                resp.getWriter().write(jsonObject.toString());
                log.warn(" 证书未通过验证！");
                return;
            }
        } catch (Exception e) {
            jsonObject.put("res", "0");
            jsonObject.put("error", "证书格式错误！");
            jsonObject.put("cert", cert);
            resp.getWriter().write(jsonObject.toString());
            log.warn(" 证书格式错误。");
            return;
        }
    }
}
