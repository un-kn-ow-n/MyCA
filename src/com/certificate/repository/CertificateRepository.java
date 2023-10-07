package com.certificate.repository;

import com.certificate.Sha_256.Sha_256;
import com.certificate.entity.Certificate;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificateRepository {
    private static Connection connection = null;
//    private static UserRepository instance = new UserRepository();
    private static CertificateRepository instance = new CertificateRepository();

    private CertificateRepository(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/certificates?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT";
            String db_user = "root";
            String db_password = "HITAsugo48";
            connection = DriverManager.getConnection(url, db_user, db_password);
        } catch (ClassNotFoundException e) {
            throw(new RuntimeException(e));
        } catch (SQLException throwables) {
            try {
                connection.close();
            } catch (SQLException e) {

            } finally {
//                System.out.println("数据库连接失败。");
                throw(new RuntimeException(throwables));
            }
        }
    }

    public boolean addCertificate(Certificate certificate){
        PreparedStatement preparedStatement;
        String sql = "insert into certificates values(?, ?, ?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, certificate.getId());
            preparedStatement.setString(2, certificate.getSubject());
            preparedStatement.setString(3, certificate.getUser());
            preparedStatement.setString(4, certificate.getStart());
            preparedStatement.setString(5, certificate.getEnd());
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
//            System.out.println("已存在该申请者的证书。");
            return false;
        }
    }

//    public boolean withdrawCertificate(Certificate certificate){
//        PreparedStatement preparedStatement;
//        String sql = "delete from certificates where id = ?";
//        try {
//            preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1, certificate.getId());
//        }catch (SQLException throwables) {
//            throw(new RuntimeException(throwables));
//        }
//        try {
//            System.out.println(preparedStatement.executeUpdate());
//            return true;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public boolean withdrawUser(String user){
//        System.out.println(id + "这是序列号");
        ResultSet resultSet;
        PreparedStatement preparedStatement;
        String sql = "select * from certificates where user = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String serial = resultSet.getString("id");
                File file = new File(String.format("d://certificates/%s.cer", serial));
                file.delete();
                String sql0 = "insert into crl values(?)";
                PreparedStatement preparedStatement0 = connection.prepareStatement(sql0);
                preparedStatement0.setString(1, serial);
                preparedStatement0.executeUpdate();
            }
            String sql1 = "delete from certificates where user = ?";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, user);
            preparedStatement1.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean withdrawCertificate(String id){
//        System.out.println(id + "这是序列号");
        PreparedStatement preparedStatement;
        String sql = "delete from certificates where id = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }

        try {
            preparedStatement.executeUpdate();
            File file = new File(String.format("d://certificates/%s.cer", id));
            file.delete();
            String sql1 = "insert into crl values(?)";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, id);
            preparedStatement1.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

//    public boolean find(Certificate certificate){
////        System.out.println(user);
//        PreparedStatement preparedStatement = null;
//        ResultSet resultSet = null;
//        String sql = "select * from certificates where id = ?";
//        try {
//            preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1, certificate.getId());
//        }catch (SQLException throwables) {
//            throw(new RuntimeException(throwables));
//        }
//        try {
//            resultSet = preparedStatement.executeQuery();
//            if(resultSet.next()){
//                String subject = resultSet.getString("subject");
//                if(subject.equals(certificate.getSubject())){
//                    return true;
//                } else {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        } catch (SQLException throwables) {
//            throw(new RuntimeException(throwables));
//        }
//    }

    public String findbysubject(String subject){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from certificates where subject = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, subject);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("id");
            } else {
                return null;
            }
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }

    public List<String> findall(){
        List<String> result = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from certificates";
        try {
            preparedStatement = connection.prepareStatement(sql);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                result.add(resultSet.getString("id") + ":" + resultSet.getString("subject") + " " + resultSet.getString("start") + "->" + resultSet.getString("end"));
            }
            return result;
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }

    public List<String> findall(String user){
        List<String> result = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from certificates where user = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
//            if(resultSet.next()){
//                result.add(resultSet.getString("id") + ":" + resultSet.getString("subject"));
////                return resultSet.getString("id");
//            } else {
////                return null;
//                return
//            }
            while(resultSet.next()){
                result.add(resultSet.getString("id") + ":" + resultSet.getString("subject") + " " + resultSet.getString("start") + "->" + resultSet.getString("end"));
            }
            return result;
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }

    public List<String> getCRL(){
        List<String> result = new ArrayList<>();
        PreparedStatement preparedStatement;
        String sql = "select * from crl";
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                result.add(resultSet.getString("id"));
            }
            return result;
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public boolean exportCRL(String path){
        try {
            Writer writer = new FileWriter(path);
            for(String serial : getCRL()){
                writer.write(serial + "\n");
            }
            writer.close();
            return true;
        } catch (IOException e) {
//            System.out.println("输出路径错误！");
            return false;
        }
    }

    public static CertificateRepository getInstance(){
        return instance;
    }

    public static void main(String[] args) {
        getInstance().exportCRL("d://crl");
    }
}
