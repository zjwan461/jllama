package com.itsu.jllama.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsu.jllama.core.exception.JException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GradioUtil {

    public static synchronized String api(String apiUrl, Object requestBody) {
        String eventId = postEventId(apiUrl, requestBody);
        return getData(apiUrl, eventId);
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
                        log.warn("unsupported response line: {}" , line);
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
        Map<String, Object> map = new HashMap<>();
        List data = new ArrayList();
        data.add("Aya-23-8B-Chat");
        map.put("data", data);
        String result = api("http://10.100.216.70:7860/gradio_api/call/get_model_info", map);
        System.out.println(result);
    }
}
