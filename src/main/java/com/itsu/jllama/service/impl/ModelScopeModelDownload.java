package com.itsu.jllama.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsu.jllama.config.JllamaConfigProperties;
import com.itsu.jllama.core.component.MessageQueue;
import com.itsu.jllama.core.component.Msg;
import com.itsu.jllama.core.exception.JException;
import com.itsu.jllama.domain.model.ModelFile;
import com.itsu.jllama.domain.model.ModelScopeModelFile;
import com.itsu.jllama.service.ModelDownload;
import com.itsu.jllama.util.FileDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service(value = "modelScopeModelDownload")
public class ModelScopeModelDownload implements ModelDownload {

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private MessageQueue messageQueue;

    @Override
    public List<ModelFile> getFileList(String repo, String revision, String root) {
        String prefix = jllamaConfigProperties.getModel().getModelScopeFileListUriPrefix();
        String respJson = HttpUtil.get(prefix + "/" + repo + "/repo/files?Revision=" + revision + "&Root=" + root);
        try {
            JSONObject jobj = JSONUtil.parseObj(respJson);
            if (jobj.getInt("Code") == 200) {
                JSONArray list = jobj.getByPath("Data.Files", JSONArray.class);
                List<ModelFile> res = new ArrayList<>();
                for (Object o : list) {
                    JSONObject item = (JSONObject) o;
                    ModelFile modelFile = new ModelScopeModelFile();
                    BeanUtil.copyProperties(item, modelFile);
                    res.add(modelFile);
                }
                return res;
            }
        } catch (Exception e) {
            throw new JException("获取模型文件列表失败", e);
        }
        return null;
    }

    @Override
    public Flux<String> download(String repo, String filename) {
        String prefix = jllamaConfigProperties.getModel().getModelScopeFileDownloadUriPrefix();
        String saveDir = jllamaConfigProperties.getModel().getSaveDir();
        String url = prefix + "/" + repo + "/resolve/master/" + filename;
        String modelDir = saveDir + "/" + jllamaConfigProperties.getModel().getPrimaryStage() + "/" + repo;
        FileUtil.mkdir(modelDir);
        String fileFullPath = modelDir + "/" + filename;
        return FileDownloader.downloadFileFlux(url, fileFullPath);
    }


    @Override
    public void downloadAsync(String repo, String filename) {
        String prefix = jllamaConfigProperties.getModel().getModelScopeFileDownloadUriPrefix();
        String saveDir = jllamaConfigProperties.getModel().getSaveDir();
        String url = prefix + "/" + repo + "/resolve/master/" + filename;
        String modelDir = saveDir + "/" + jllamaConfigProperties.getModel().getPrimaryStage() + "/" + repo;
        FileUtil.mkdir(modelDir);
        String fileFullPath = modelDir + "/" + filename;
        threadPoolTaskExecutor.submit(() -> {
            Msg msg = new Msg();
            msg.setTitle("AI模型下载提醒");
            try {
                FileDownloader.downloadFile(url, fileFullPath);
                msg.setContent(filename + "下载完成");
                msg.setStatus(Msg.Status.success);
            } catch (IOException e) {
                log.error(e.getMessage());
                msg.setContent(filename + "下载失败");
                msg.setStatus(Msg.Status.error);
            } finally {
                msg.setCreateTime(LocalDateTime.now());
                try {
                    messageQueue.put(msg);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean isMatch(String primaryStage) {
        return "modelScope".equals(primaryStage);
    }
}
