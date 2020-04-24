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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class GeoLife extends HttpServlet {

    enum ReturnDataType {
        JSON,
        TRAJECTORY,
        CONTACT
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
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

    private void trajectory(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String userId = request.getParameter("objectId");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String format = request.getParameter("format");

        if (userId == null || start == null || end == null) {
            throw new ServletException("Illegal Argument Exception");
        }

        String sql = String.format("SELECT longitude, latitude, date_time, trans_type FROM geolife " +
                "WHERE target = '%s' " +
                "AND (date_time BETWEEN '%s' AND '%s') " +
                "ORDER BY date_time ASC", userId, start, end);

        System.out.println(sql);
        this.query(request, response, sql, ReturnDataType.TRAJECTORY, (format != null && format.equals("json")));
    }

    private void buffer(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String userId = request.getParameter("objectId");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String radius = request.getParameter("radius");
        String format = request.getParameter("format");

        if (userId == null || start == null || end == null) {
            throw new ServletException("Illegal Argument Exception");
        }

        if (radius == null || radius.trim().isEmpty()) {
            radius = "0.0001";
        }

        String sql = String.format("SELECT ST_AsGeoJSON(ST_Buffer(ST_MakeLine(geom ORDER BY date_time ASC), %s)) as buffer FROM geolife " +
                "WHERE target = '%s' " +
                "AND (date_time BETWEEN '%s' AND '%s')", radius, userId, start, end);

        System.out.println(sql);

        try (Connection connection = DBTools.getConnection()){
            try(Statement statement = connection.createStatement()){
                try(ResultSet resultSet = statement.executeQuery(sql)){
                    resultSet.next();
                    String json = resultSet.getString("buffer");
                    CompressWriter.write(request,response, json);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private void contact(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String userId = request.getParameter("objectId");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String radius = request.getParameter("radius");
        String format = request.getParameter("format");

        if (userId == null || start == null || end == null) {
            throw new ServletException("Illegal Argument Exception");
        }

        if (radius == null || radius.trim().isEmpty()) {
            radius = "0.0001";
        }

        String sql = String.format("SELECT target, longitude, latitude, date_time, trans_type FROM geolife g " +
                "WHERE target != '%s' " +
                "AND (g.date_time BETWEEN '%s' AND '%s')" +
                "AND ST_Contains(" +
                "(SELECT ST_Buffer(ST_MakeLine(geom ORDER BY date_time ASC), %s) FROM geolife " +
                    "WHERE target = '%s' " +
                    "AND (date_time BETWEEN '%s' AND '%s')), g.geom)", userId, start, end, radius, userId, start, end);

        System.out.println(sql);
        this.query(request, response, sql, ReturnDataType.CONTACT, (format != null && format.equals("json")));
    }

    private void query(HttpServletRequest request, HttpServletResponse response, String sql, ReturnDataType returnType, boolean isJson) throws ServletException {
        try (Connection connection = DBTools.getConnection()){
            try(Statement statement = connection.createStatement()){
                try(ResultSet resultSet = statement.executeQuery(sql)){
                    switch (returnType) {
                        case TRAJECTORY:
                            if (isJson) {
                                DBTools.trajectoryJson(request, response, resultSet);
                            } else {
                                DBTools.trajectory(request, response, resultSet);
                            }
                            return;
                        case CONTACT:
                            if (isJson) {
                                DBTools.contactJson(request, response, resultSet);
                            } else {
                                DBTools.contact(request, response, resultSet);
                            }
                            return;
                        case JSON:
                        default:
                            DBTools.compressedJsonResultSet(request, response, resultSet);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

 }
