package edu.asu.cici.trajectory.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GeoLife extends HttpServlet {
    private String queryByUser = null;
    private String queryByUserAndTime = null;
    private String queryByTime = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.queryByUser = config.getInitParameter("queryByUser");
        this.queryByUserAndTime = config.getInitParameter("queryByUserAndTime");
        this.queryByTime = config.getInitParameter("queryByTime");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        this.doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String path = request.getRequestURI();
        String methodName = path.substring(path.lastIndexOf("/")+1);
        System.out.println(methodName);

        try {
            Method method = this.getClass().getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(this, request, response);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private void queryByUser(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String userId = request.getParameter("userId");
        String sql = String.format(this.queryByUser, userId);
        this.query(request, response, sql);
    }

    private void queryByUserAndTime(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String userId = request.getParameter("userId");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String sql = String.format(this.queryByUserAndTime, userId, start, end);
        this.query(request, response, sql);
    }

    private void queryByTime(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String sql = String.format(this.queryByTime, start, end);
        this.query(request, response, sql);
    }

    private void query(HttpServletRequest request, HttpServletResponse response, String sql) throws ServletException {
        try (Connection connection = DBTools.getConnection()){
            try(Statement statement = connection.createStatement()){
                try(ResultSet resultSet = statement.executeQuery(sql)){
                    DBTools.compressedJsonResultSet(request, response, resultSet);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

 }
