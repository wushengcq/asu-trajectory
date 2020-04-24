package edu.asu.cici.trajectory.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

public class CompressWriter {

    public static BufferedWriter getWriter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out;
        String encodings = request.getHeader("Accept-Encoding");
        String encodeFlag = request.getParameter("encoding");
        if ((encodings != null) && (encodings.indexOf("G") != -1) && !"none".equals(encodeFlag)) {
            OutputStream out1 = response.getOutputStream();
            out = new PrintWriter(new GZIPOutputStream(out1), false);
            response.setHeader("Content-Encoding", "gzip");
        } else {
            out = response.getWriter();
        }
        return new BufferedWriter(out);
    }

    public static void write(HttpServletRequest request, HttpServletResponse response, String data) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = getWriter(request, response);
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
