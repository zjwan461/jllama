package com.itsu.oa.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.req.FileDownloadReq;
import com.itsu.oa.controller.req.ModelReq;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.domain.model.ModelFile;
import com.itsu.oa.entity.BaseEntity;
import com.itsu.oa.entity.FileDownload;
import com.itsu.oa.entity.Model;
import com.itsu.oa.mapper.FileDownloadMapper;
import com.itsu.oa.service.ModelDownload;
import com.itsu.oa.service.ModelService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/mgn")
public class ModelMgnController {

    @Resource(name = "modelScopeModelDownload")
    private ModelDownload modelScopeModelDownload;

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

    @Resource
    private ModelService modelService;

    @Resource
    private FileDownloadMapper fileDownloadMapper;

    @Auth
    @GetMapping("/list")
    public R list(int page, int limit, String search) {
        IPage<Model> reqPage = new Page<>(page, limit);
        IPage<Model> resPage = modelService.page(reqPage, Wrappers.lambdaQuery(Model.class)
                .orderByDesc((SFunction<Model, Date>) BaseEntity::getCreateTime)
                .like(StrUtil.isNotBlank(search), (SFunction<Model, String>) Model::getName, search)
                .or()
                .like(StrUtil.isNotBlank(search), (SFunction<Model, String>) Model::getRepo, search));

        return R.success(resPage);
    }


    @Auth
    @PostMapping("/create")
    public R create(@RequestBody ModelReq modelReq) {
        Model model = new Model();
        BeanUtil.copyProperties(modelReq, model);
        model.setSaveDir(jllamaConfigProperties.getModel().getSaveDir() + "/" + modelReq.getDownloadPlatform() + "/" + model.getRepo());
        model.setCreateTime(new Date());
        model.setUpdateTime(new Date());
        modelService.save(model);
        return R.success(model);
    }

    @Auth
    @DeleteMapping("/del/{id}/{delFile}")
    public R delete(@PathVariable("id") Long id, @PathVariable("delFile") boolean delFile) {
        modelService.removeById(id);
        if (delFile) {
            fileDownloadMapper.delete(Wrappers.lambdaQuery(FileDownload.class)
                    .eq((SFunction<FileDownload, Long>) FileDownload::getModelId, id));
        }
        return R.success();
    }

    @Auth
    @PostMapping("/create-download")
    public R createDownload(@RequestBody FileDownloadReq fileDownloadReq) {
        Model model = modelService.getById(fileDownloadReq.getModelId());
        if (model == null) {
            throw new JException("非法的模型ID:" + fileDownloadReq.getModelId());
        }

        List<FileDownload> exists = fileDownloadMapper.selectList(Wrappers.lambdaQuery(FileDownload.class)
                .eq((SFunction<FileDownload, Long>) FileDownload::getModelId, model.getId()));
        boolean neverDl = exists.stream().noneMatch(x -> x.getFileName().equals(fileDownloadReq.getFileName()));
        if (neverDl) {
            FileDownload fileDownload = new FileDownload();
            fileDownload.setModelId(model.getId());
            fileDownload.setModelName(model.getName());
            fileDownload.setFileSize(fileDownloadReq.getFileSize());
            fileDownload.setFileName(fileDownloadReq.getFileName());
            fileDownload.setFilePath(jllamaConfigProperties.getModel().getSaveDir() + "/" + model.getDownloadPlatform() + "/" + model.getRepo());
            fileDownload.setCreateTime(new Date());
            fileDownload.setUpdateTime(new Date());
            fileDownloadMapper.insert(fileDownload);
        }
        return R.success();
    }

    @Auth
    @GetMapping("/dl/files")
    public R getRemoteFiles(String repo, String revision, String root) {
        if (StrUtil.isBlank(repo)) {
            throw new JException("模型仓库：repo必填");
        }
        String primaryStage = jllamaConfigProperties.getModel().getPrimaryStage();
        if (modelScopeModelDownload.isMatch(primaryStage)) {
            if (StrUtil.isBlank(revision)) {
                revision = "master";
            }
            if (StrUtil.isBlank(root)) {
                root = "";
            }
            List<ModelFile> list = modelScopeModelDownload.getFileList(repo, revision, root);
            return R.success(list);
        } else {
            throw new JException("暂不支持" + primaryStage + "的模型下载方式");
        }
    }

    @Auth
    @GetMapping(value = "/dl", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public Flux<String> download(String repo, String filename) {
        if (StrUtil.isBlank(repo)) {
            throw new JException("模型仓库：repo必填");
        }
        if (StrUtil.isBlank(filename)) {
            throw new JException("文件名必填");
        }
        String primaryStage = jllamaConfigProperties.getModel().getPrimaryStage();
        if (modelScopeModelDownload.isMatch(primaryStage)) {
            return modelScopeModelDownload.download(repo, filename);
        } else {
            throw new JException("暂不支持" + primaryStage + "的模型下载方式");
        }

    }

    @Auth
    @GetMapping(value = "/list-dl-file")
    public R listDownloadFiles(Long modelId) {
        List<FileDownload> list = fileDownloadMapper.selectList(Wrappers.lambdaQuery(FileDownload.class)
                .eq((SFunction<FileDownload, Long>) FileDownload::getModelId, modelId));
        return R.success(list);

    }

    @Auth
    @GetMapping(value = "/dl-percent", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public Flux<String> getDlFilePercent(String fileId) {
        FileDownload fileDownload = fileDownloadMapper.selectById(fileId);
        if (fileDownload == null) {
            return null;
        }
        return Flux.create(fluxSink -> {
            while (true) {
                try {
                    File file = new File(fileDownload.getFilePath(), fileDownload.getFileName());
                    if (!file.exists()) {
                        fluxSink.next("0%");
                        break;
                    }
                    double progress = (double) file.length() / fileDownload.getFileSize() * 100;
                    String progressStr = String.format("%.2f%%", progress);
                    fluxSink.next(progressStr);
                    if (progress >= 100.00) {
                        break;
                    }
                } catch (Exception e) {
                    fluxSink.error(e);
                }
            }
        });
    }
}
