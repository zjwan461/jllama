package com.itsu.oa.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class FileDownloader {

    private static final int BUFFER_SIZE = 4096;

    public static Flux<String> downloadFileFlux(String fileUrl, String savePath) {
        return Flux.create(fluxSink -> {
            HttpURLConnection httpConn = null;
            try {
                URL url = new URL(fileUrl);
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("GET");

                // 检查本地文件是否存在，获取已下载的字节数
                File file = new File(savePath);
                long downloadedBytes = 0;
                if (file.exists()) {
                    downloadedBytes = file.length();
                    // 设置 Range 请求头，从已下载的位置继续下载
                    httpConn.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
                }

                int responseCode = httpConn.getResponseCode();

                // 检查响应状态码
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    // 获取文件的总大小
                    long fileSize = httpConn.getContentLengthLong() + downloadedBytes;

                    // 打开输入流和输出流
                    try (InputStream inputStream = httpConn.getInputStream();
                         RandomAccessFile outputStream = new RandomAccessFile(file, "rw")) {

                        // 将文件指针移动到已下载的位置
                        outputStream.seek(downloadedBytes);

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadedBytes += bytesRead;

                            // 打印下载进度
                            double progress = (double) downloadedBytes / fileSize * 100;
                            fluxSink.next(String.format("%.2f%%", progress));
                        }
                    }
                    log.info("File:{} downloaded successfully.", file.getName());
                } else {
                    log.error("Error: HTTP response code {} ",responseCode);
                }

            } catch (IOException e) {
                fluxSink.error(e);
            } finally {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
                fluxSink.complete();
            }
        });
    }

    public static void downloadFile(String fileUrl, String savePath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        // 检查本地文件是否存在，获取已下载的字节数
        File file = new File(savePath);
        long downloadedBytes = 0;
        if (file.exists()) {
            downloadedBytes = file.length();
            // 设置 Range 请求头，从已下载的位置继续下载
            httpConn.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
        }

        int responseCode = httpConn.getResponseCode();

        // 检查响应状态码
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
            // 获取文件的总大小
            long fileSize = httpConn.getContentLengthLong() + downloadedBytes;

            // 打开输入流和输出流
            try (InputStream inputStream = httpConn.getInputStream();
                 RandomAccessFile outputStream = new RandomAccessFile(file, "rw")) {

                // 将文件指针移动到已下载的位置
                outputStream.seek(downloadedBytes);

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;

                    // 打印下载进度
                    double progress = (double) downloadedBytes / fileSize * 100;
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Download progress: %.2f%%\n", progress));
                    }
                }
            }
            log.info("File:{} downloaded successfully.", file.getName());
        } else {
            log.error("Error: HTTP response code {} ",responseCode);
        }
        httpConn.disconnect();
    }

    public static void main(String[] args) {
        String fileUrl = "https://modelscope.cn/models/unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF/resolve/master/DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf"; // 替换为实际的文件下载链接
        String savePath = "D:/models/DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf"; // 替换为实际的保存路径
//        try {
//            downloadFile(fileUrl, savePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Flux<String> progressFlux = downloadFileFlux(fileUrl, savePath);
        progressFlux.subscribe(
                progress -> System.out.println("Read progress: " + progress),
                error -> System.err.println("Error reading file: " + error.getMessage()),
                () -> System.out.println("File reading completed.")
        );
    }
}