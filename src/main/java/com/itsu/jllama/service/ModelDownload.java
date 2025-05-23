package com.itsu.jllama.service;

import com.itsu.jllama.domain.model.ModelFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ModelDownload {

    List<ModelFile> getFileList(String repo, String revision, String root);

    Flux<String> download(String repo, String filename);

    void downloadAsync(String repo, String filename);

    boolean isMatch(String primaryStage);

}
