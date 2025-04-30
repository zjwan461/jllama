package com.itsu.oa.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.itsu.oa.core.exception.JException;
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
        List<String> values = getValues(url, xpath);
        if (CollUtil.isEmpty(values)) {
            return "";
        }
        return values.get(0);
    }

    public static List<String> getValues(String url, String xpath) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
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
