package com.itsu.jllama;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.List;

public class JsoupTest {

    @Test
    public void test1() throws Exception {

        String html = FileUtil.readUtf8String("C:\\Users\\1\\Desktop\\1.html");
        Document document = Jsoup.connect("https://github.com/ggml-org/llama.cpp/releases").get();
        JXDocument jxDocument = JXDocument.create(document);
        List<JXNode> jxNodes = jxDocument.selN("//*[@id=\"repo-content-pjax-container\"]/div/div[3]/section[1]/div/div[1]/div[3]/a/div/span");
        for(JXNode jxNode : jxNodes){
            Element element = jxNode.asElement();
            System.out.println(element.text());
        }

    }
}
