package edu.asu.cici.trajectory.server;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DBTools {
    private static DataSource dataSource = null;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static {
        try {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource)ctx.lookup("java:/comp/env/jdbc/digital-contact");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void compressedJsonResultSet(HttpServletRequest request, HttpServletResponse response, ResultSet resultSet)
            throws IOException, SQLException {

        try (Writer writer = CompressWriter.getWriter(request, response)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            String[] columns = new String[metaData.getColumnCount()];
            JDBCType[] types = new JDBCType[metaData.getColumnCount()];
            for (int i = 1; i <= columns.length; i++) {
                columns[i-1] = metaData.getColumnLabel(i);
                types[i-1] = JDBCType.valueOf(metaData.getColumnType(i));
            }

            writer.write("{\"success\": \"true\", \"message\": \"query ok\", \"data\": [");
            int count = 0;
            while (resultSet.next()) {
                if (count++ > 0) writer.write(",\n");
                writer.write("{");
                for (int j = 0; j < columns.length; j++) {
                    if (j > 0) writer.write(",");

                    switch (types[j]) {
                        case DOUBLE:
                            writer.write(String.format("\"%s\":%f", columns[j], resultSet.getDouble(j+1)));
                            break;
                        case INTEGER:
                            writer.write(String.format("\"%s\":%d", columns[j], resultSet.getInt(j+1)));
                            break;
                        case VARCHAR:
                            writer.write(String.format("\"%s\":\"%s\"", columns[j], resultSet.getInt(j+1)));
                            break;
                        case TIMESTAMP:
                            writer.write(String.format("\"%s\":\"%s\"", columns[j], simpleDateFormat.format(resultSet.getTimestamp(j+1))));
                            break;
                        default:
                    }
                }
                writer.write("}");
            }
            writer.write("]}");
        }
    }

    public static void trajectory(HttpServletRequest request, HttpServletResponse response, ResultSet resultSet)
            throws IOException, SQLException {
        try (Writer writer = CompressWriter.getWriter(request, response)) {
            List<Long> timestamps = new ArrayList<Long>();

            writer.write("[{\"path\":[");
            long count = 0, offset = Long.MAX_VALUE, timestamp = 0;
            while (resultSet.next()) {
                timestamp = resultSet.getTimestamp("date_time").getTime();
                if (timestamp < offset) {
                    offset = timestamp;
                }
                timestamps.add(timestamp);

                if (count++ == 0) {
                    writer.write(String.format("[%f,%f]", resultSet.getDouble("longitude"), resultSet.getDouble("latitude")));
                } else {
                    writer.write(String.format(",[%f,%f]", resultSet.getDouble("longitude"), resultSet.getDouble("latitude")));
                }


            }
            writer.write("],");

            writer.write("\"timestamp\":[");
            for(int i=0; i<timestamps.size(); i++) {
                if (i == 0) {
                    writer.write(String.valueOf((timestamps.get(i) - offset) / 1000));
                } else {
                    writer.write(String.format(",%d", (timestamps.get(i) - offset) / 1000));
                }
            }
            writer.write("], \"marked\": 0");

            writer.write("}]");
        }
    }

    public static void trajectoryJson(HttpServletRequest request, HttpServletResponse response, ResultSet resultSet)
            throws IOException, SQLException {
        try (Writer writer = CompressWriter.getWriter(request, response)) {

            writer.write("{\"type\": \"FeatureCollection\", \"features\": [{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [");
            long count = 0, timestamp = 0;
            while (resultSet.next()) {
                if (count++ == 0) {
                    writer.write(String.format("[%f,%f]", resultSet.getDouble("longitude"), resultSet.getDouble("latitude")));
                } else {
                    writer.write(String.format(",[%f,%f]", resultSet.getDouble("longitude"), resultSet.getDouble("latitude")));
                }
            }
            writer.write("]}}]}");
        }
    }

    public static void contact(HttpServletRequest request, HttpServletResponse response, ResultSet resultSet)
            throws IOException, SQLException {
        try (Writer writer = CompressWriter.getWriter(request, response)) {
            writer.write("[");
            long count = 0, offset = Long.MAX_VALUE, timestamp = 0;
            while (resultSet.next()) {
                if (count++ > 0) {
                    writer.write(",");
                }
                writer.write(String.format("{\"p\":[%f,%f],\"id\":%d,\"t\":%d}",
                        resultSet.getDouble("longitude"),
                        resultSet.getDouble("latitude"),
                        resultSet.getInt("target"),
                        resultSet.getTimestamp("date_time").getTime()));
            }
            writer.write("]");
        }
    }


    public static void contactJson(HttpServletRequest request, HttpServletResponse response, ResultSet resultSet)
            throws IOException, SQLException {
        try (Writer writer = CompressWriter.getWriter(request, response)) {
            writer.write("{\"type\": \"FeatureCollection\", \"features\": [");
            long count = 0, offset = Long.MAX_VALUE, timestamp = 0;
            while (resultSet.next()) {
                if (count++ > 0) {
                    writer.write(",");
                }
                writer.write(String.format(
                        "{\"type\":\"Feature\", \"properties\":{\"id\": \"%s\", \"time\":\"%s\"}, \"geometry\":{\"type\":\"Point\", \"coordinates\":[%f,%f]}}",
                        resultSet.getInt("target"),
                        simpleDateFormat.format(resultSet.getTimestamp("date_time")),
                        resultSet.getDouble("longitude"),
                        resultSet.getDouble("latitude")));
            }
            writer.write("]}");
        }
    }

}
