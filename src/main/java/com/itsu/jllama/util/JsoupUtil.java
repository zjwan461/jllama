package com.itsu.jllama.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.itsu.jllama.core.exception.JException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsoupUtil {

    public static String getValue(String url, String xpath) {
        return getValue(url, xpath, null, null);
    }


    public static String getValue(String url, String xpath, String proxyId, Integer proxyPort) {
        List<String> values = getValues(url, xpath, proxyId, proxyPort);
        if (CollUtil.isEmpty(values)) {
            return "";
        }
        return values.get(0);
    }

    public static List<String> getValues(String url, String xpath, String proxyIp, Integer proxyPort) {
        Document document = null;
        try {
            if (StrUtil.isNotBlank(proxyIp) && proxyPort != null) {
                document = Jsoup.connect(url).proxy(proxyIp, proxyPort).get();
            } else {
                document = Jsoup.connect(url).get();
            }
        } catch (IOException e) {
            throw new JException("尝试连接:" + url + "失败");
        }
        JXDocument jxDocument = JXDocument.create(document);
        List<JXNode> jxNodes = jxDocument.selN(xpath);
        List<String> values = new ArrayList<>();
        for (JXNode jxNode : jxNodes) {
            Element element = jxNode.asElement();
            String text = element.text();
            if (StrUtil.isNotBlank(text)) {
                values.add(text);
            }
        }
        return values;
    }
}
