package com.zientarski.multiroom;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpAudioServer implements HttpHandler {
    private static final Pattern DLNA_PATTERN = Pattern.compile("^/DLNA/([0-9A-Za-z]+)$");
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("^bytes=(\\d+)-(\\d*)$");

    private final File audioFile;
    private HttpServer server;
    private final static int PORT = 49200;

    public HttpAudioServer(final File audioFile) {
        this.audioFile = audioFile;
    }

    public void listen() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", this);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public void close() {
        server.stop(0);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            Matcher matcher = DLNA_PATTERN.matcher(httpExchange.getRequestURI().toString());
            if (matcher.matches()) {
                String objectId = matcher.group(1);
                handleDLNARequest(httpExchange, objectId);
            } else {
                System.out.println("Unhandled request path: " + httpExchange.getRequestURI());
                httpExchange.sendResponseHeaders(404, 0);
                OutputStream os = httpExchange.getResponseBody();
                os.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleDLNARequest(HttpExchange httpExchange, String objectId) throws IOException, InterruptedException {
        String range = httpExchange.getRequestHeaders().getFirst("Range");
        final Long start;
        final Long end;
        if (range != null) {
            Matcher matcher = RANGE_HEADER_PATTERN.matcher(range);
            if (matcher.matches()) {
                String startStr = matcher.group(1);
                start = Long.parseLong(startStr);

                String endStr = matcher.group(2);
                if (endStr.length() > 0) {
                    end = Long.parseLong(endStr);
                } else {
                    end = audioFile.length();
                }
            } else {
                System.out.println("Requested range did not match expected pattern:" + range);
                throw new RuntimeException();
            }
        } else {
            start = 0L;
            end = audioFile.length();
        }
        System.out.println("objectId=" + objectId + ", range:" + start + "-" + end + " (requested " + range + ")");
        if (start > end) {
            httpExchange.sendResponseHeaders(416, 0);
            httpExchange.getResponseBody().close();
        } else {
            httpExchange.getResponseHeaders().add("Content-Type", getContentType());
            // As it seems this is not really needed
            // httpExchange.getResponseHeaders().add("Content-Range", "bytes " + start + "-" + end + "/*");
            httpExchange.sendResponseHeaders(206, 0);
            try (final OutputStream os = httpExchange.getResponseBody()) {
                if (audioFile.getName().endsWith("ogg")) {
                    copyEndlessStream(audioFile, os, 1460);
                } else {
                    try (final InputStream is = new FileInputStream(audioFile)) {
                        copyStream(is, os, 1460, start, end);
                    }
                }
            }
        }
    }

    private String getContentType() {
        if (audioFile.getName().endsWith("wav")) {
            return "audio/wav";
        }
        if (audioFile.getName().endsWith("mp3")) {
            return "audio/mpeg";
        }
        if (audioFile.getName().endsWith("ogg")) {
            return "audio/ogg";
        }
        throw new IllegalArgumentException("Unhandled file type: " + audioFile.getName());
    }

    private static void copyStream(InputStream from, OutputStream to, int bufferSize, Long start, Long end) throws IOException {
        int left = Integer.MAX_VALUE;
        if (start != null) {
            from.skip(start);
            if (end != null) {
                left = (int) (end - start + 1);
            }
        }
        byte[] buffer = new byte[bufferSize];
        int read;
        while (left > 0 && (read = from.read(buffer, 0, Math.min(left, buffer.length))) > 0) {
            to.write(buffer, 0, read);
            left -= read;
        }
    }

    // Work in progress. So far repeats the file over and over again.
    private static void copyEndlessStream(File from, OutputStream to, int bufferSize) throws IOException, InterruptedException {
        byte[] buffer = new byte[bufferSize];
        final LoopableFileInputStream input = new LoopableFileInputStream(from);
        for (int i = 0; i < 30000; i++) {
            input.read(buffer);
            to.write(buffer);
            //Thread.sleep(2000);
        }
        input.close();
    }
}
