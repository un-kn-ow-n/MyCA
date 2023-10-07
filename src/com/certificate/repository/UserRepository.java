package com.certificate.repository;

import com.certificate.Sha_256.Sha_256;
import com.certificate.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository{

    private static Connection connection = null;
    private static UserRepository instance = new UserRepository();

    private UserRepository(){
//        System.out.println("init!");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT";
            String db_user = "root";
            String db_password = "HITAsugo48";
            connection = DriverManager.getConnection(url, db_user, db_password);
        } catch (ClassNotFoundException e) {
//            System.out.println("找不到JDBC驱动。");
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

    public boolean addUser(User user){
        PreparedStatement preparedStatement;
        String sql = "insert into users values(?, ?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(sql);
            String salt = Sha_256.getSalt();
            String password_encoded = Sha_256.Sha_256(user.getPassword(), salt);
            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, password_encoded);
            preparedStatement.setString(3, salt);
            preparedStatement.setString(4, "YES");
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
//            System.out.println("用户id不能相同。");
            return false;
        }
    }

    public boolean modifyPsw(User user){
        PreparedStatement preparedStatement;
        String sql = "update users set password = ?, salt = ? where id = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            String salt = Sha_256.getSalt();
            String password_encoded = Sha_256.Sha_256(user.getPassword(), salt);
            preparedStatement.setString(3, user.getId());
            preparedStatement.setString(1, password_encoded);
            preparedStatement.setString(2, salt);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String isvalid(String id){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from users where id = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("valid");
            } else {
                return null;
            }
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }
    public boolean find(User user){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from users where id = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getId());
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                String password_hashed = resultSet.getString("password");
                String salt = resultSet.getString("salt");
                if(Sha_256.Sha_256(user.getPassword(), salt).equals(password_hashed)){
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }

    public List<String> findall(){
        List<String> result = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from users";
        try {
            preparedStatement = connection.prepareStatement(sql);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                result.add(resultSet.getString("id") + ":" + resultSet.getString("valid"));
            }
            return result;
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }

    public boolean changevalid(String id){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from users where id = ?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
        }catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
        try {
            resultSet = preparedStatement.executeQuery();
//            while(resultSet.next()){
//                result.add(resultSet.getString("id") + ":" + resultSet.getString("valid"));
//            }
//            return result;
            resultSet.next();
            String valid = resultSet.getString("valid");
            String toset;
            switch(valid){
                case "YES":
                    toset = "NO";
                    break;
                case "NO":
                    toset = "YES";
                    break;
                case "FOREVER":
                    return false;
                default:
                    throw new RuntimeException();
            }
            String newsql = "update users set valid = ? where id = ?";
            PreparedStatement newpreparedStatement = connection.prepareStatement(newsql);
            newpreparedStatement.setString(1, toset);
            newpreparedStatement.setString(2, id);
            return newpreparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw(new RuntimeException(throwables));
        }
    }

    public boolean delete(String id){
        String sql = "delete from users where id = ? and valid != 'FOREVER'";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public static UserRepository getInstance(){
        return instance;
    }
}
