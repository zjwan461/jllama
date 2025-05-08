package com.itsu.jllama.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsu.jllama.core.exception.JException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class GradioUtil {

    public static synchronized String api(String apiUrl, Object requestBody) {
        String eventId = postEventId(apiUrl, requestBody);
        return getData(apiUrl, eventId);
    }

    public static synchronized Flux<String> apiWithFlux(String apiUrl, Object requestBody) {
        String eventId = postEventId(apiUrl, requestBody);
        return getDataFlux(apiUrl, eventId);
    }

    private static Flux<String> getDataFlux(String apiUrl, String eventId) {
        return Flux.create((fluxSink -> {
            Thread t = new Thread(() -> {
                HttpURLConnection httpConn = null;
                try {
                    URL url = new URL(apiUrl + "/" + eventId);
                    httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("GET");
                    int responseCode = httpConn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        log.error("请求gradio server失败");
                        throw new JException("请求gradio server失败");
                    } else {
                        try (InputStream inputStream = httpConn.getInputStream();
                             InputStreamReader isr = new InputStreamReader(inputStream);
                             BufferedReader br = new BufferedReader(isr)) {
                            String line = null;
                            boolean complete = false;
                            boolean error = false;
                            while ((line = br.readLine()) != null) {
                                line = StrUtil.trim(line);
                                if (StrUtil.isBlank(line)) {
                                    continue;
                                }
                                log.info(line);
                                if (line.startsWith("event:")) {
                                    String status = line.substring(6).trim();
                                    if ("complete".equals(status)) {
                                        complete = true;
                                    } else if ("error".equals(status)) {
                                        error = true;
                                    }
                                } else if (line.startsWith("data:")) {
                                    String data = line.substring(5).trim();
                                    if (complete) {
                                        fluxSink.next(data);
                                        break;
                                    } else if (error) {
                                        throw new JException("请求gradio失败");
                                    } else {
                                        fluxSink.next(data);
                                    }
                                } else {
                                    log.warn("unsupported response line: {}", line);
                                    fluxSink.next(line);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    fluxSink.error(e);
                } finally {
                    if (httpConn != null) {
                        httpConn.disconnect();
                    }
                    fluxSink.complete();
                }
            }, "gradio-get-thread");
            t.start();
        }));
    }

    private static String getData(String apiUrl, String eventId) {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(apiUrl + "/" + eventId);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new JException("请求gradio server失败");
            }
            try (InputStream inputStream = httpConn.getInputStream();
                 InputStreamReader isr = new InputStreamReader(inputStream);
                 BufferedReader br = new BufferedReader(isr)) {
                String line = null;
                boolean complete = false;
                boolean error = false;
                while ((line = br.readLine()) != null) {
                    line = StrUtil.trim(line);
                    if (StrUtil.isBlank(line)) {
                        continue;
                    }
                    log.info(line);
                    if (line.startsWith("event:")) {
                        String status = line.substring(6).trim();
                        if ("complete".equals(status)) {
                            complete = true;
                        } else if ("error".equals(status)) {
                            error = true;
                        }
                    } else if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if (complete) {
                            return data;
                        } else if (error) {
                            throw new JException("请求gradio失败");
                        }
                    } else {
                        log.warn("unsupported response line: {}", line);
                    }
                }

            }
            return null;
        } catch (JException e) {
            throw e;
        } catch (Exception e) {
            throw new JException("请求gradio server失败");
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    private static String postEventId(String apiUrl, Object requestBody) {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(apiUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("content-type", "application/json");
            // 启用输出流以发送请求体
            httpConn.setDoOutput(true);
            if (requestBody != null) {
                // 获取输出流并写入请求体数据
                DataOutputStream wr = new DataOutputStream(httpConn.getOutputStream());
                wr.writeBytes(JSONUtil.toJsonStr(requestBody));
                wr.flush();
                wr.close();

            }
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new JException("请求gradio server失败");
            }
            InputStream inputStream = httpConn.getInputStream();
            String resp = IoUtil.readUtf8(inputStream);
            log.info("resp:{}", resp);
            JSONObject result = JSONUtil.parseObj(resp);
            String eventId = result.getStr("event_id");
            if (StrUtil.isBlank(eventId)) {
                throw new JException("请求gradio server失败，eventId is null");
            }
            return eventId;
        } catch (JException e) {
            throw e;
        } catch (Exception e) {
            throw new JException("请求gradio server失败");
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    public static void main(String[] args) {
//        Map<String, Object> map = new HashMap<>();
//        List data = new ArrayList();
//        data.add("Aya-23-8B-Chat");
//        map.put("data", data);
        String str = "{\n" +
                "  \"data\": [\n" +
                "    \"zh\",\n" +
                "    \"Aya-23-8B-Chat\",\n" +
                "    \"Hello!!\",\n" +
                "    \"lora\",\n" +
                "    [],\n" +
                "    \"none\",\n" +
                "    \"bnb\",\n" +
                "    \"default\",\n" +
                "    \"none\",\n" +
                "    \"auto\",\n" +
                "    \"Supervised Fine-Tuning\",\n" +
                "    \"data\",\n" +
                "    [\n" +
                "      \"identity\"\n" +
                "    ],\n" +
                "    \"5e-5\",\n" +
                "    \"3.0\",\n" +
                "    \"1.0\",\n" +
                "    \"100000\",\n" +
                "    \"bf16\",\n" +
                "    2048,\n" +
                "    2,\n" +
                "    8,\n" +
                "    0,\n" +
                "    \"cosine\",\n" +
                "    5,\n" +
                "    100,\n" +
                "    0,\n" +
                "    0,\n" +
                "    \"{\\\"optim\\\": \\\"adamw_torch\\\"}\",\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    [\n" +
                "      \"none\"\n" +
                "    ],\n" +
                "    2,\n" +
                "    \"all\",\n" +
                "    \"\",\n" +
                "    8,\n" +
                "    16,\n" +
                "    0,\n" +
                "    0,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    0.1,\n" +
                "    0,\n" +
                "    \"sigmoid\",\n" +
                "    [],\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    16,\n" +
                "    200,\n" +
                "    2,\n" +
                "    \"all\",\n" +
                "    false,\n" +
                "    16,\n" +
                "    200,\n" +
                "    32,\n" +
                "    \"all\",\n" +
                "    false,\n" +
                "    \"layer\",\n" +
                "    \"ascending\",\n" +
                "    50,\n" +
                "    0.05,\n" +
                "    false,\n" +
                "    \"llamafactory\",\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    \"cloud\",\n" +
                "    \"train_2025-05-08-10-27-09\",\n" +
                "    \"2025-05-08-10-27-09.yaml\",\n" +
                "    1,\n" +
                "    \"none\",\n" +
                "    false\n" +
                "  ]\n" +
                "}";
        JSONObject data = JSONUtil.parseObj(str);
        String result = api("http://10.100.216.70:7860/gradio_api/call/preview_train", data);
        System.out.println(result);
    }
}
