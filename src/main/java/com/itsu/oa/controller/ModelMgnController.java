package com.itsu.oa.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.itsu.oa.entity.Settings;
import com.itsu.oa.service.FileDownloadService;
import com.itsu.oa.service.ModelDownload;
import com.itsu.oa.service.ModelService;
import com.itsu.oa.service.SettingsService;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/mgn")
public class ModelMgnController {

    @Resource(name = "modelScopeModelDownload")
    private ModelDownload modelScopeModelDownload;

    @Resource
    private ModelService modelService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private SettingsService settingsService;

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

    @Auth
    @GetMapping("/list")
    public R list(int page, int limit, String search) {
        String importDir = settingsService.getCachedSettings().getModelSaveDir() + File.separator + "import";
        IPage<Model> reqPage = new Page<>(page, limit);
        IPage<Model> resPage = modelService.page(reqPage, Wrappers.lambdaQuery(Model.class)
                .orderByDesc((SFunction<Model, Date>) BaseEntity::getCreateTime)
                .like(StrUtil.isNotBlank(search), (SFunction<Model, String>) Model::getName, search)
                .or()
                .like(StrUtil.isNotBlank(search), (SFunction<Model, String>) Model::getRepo, search));
        resPage.getRecords().forEach(x -> {
            x.setImportDir(importDir);
        });
        return R.success(resPage);
    }


    @Auth
    @PostMapping("/create")
    public R create(@RequestBody ModelReq modelReq) {
        Model exist = modelService.getOne(Wrappers.lambdaQuery(Model.class).eq((SFunction<Model, String>) Model::getName, modelReq.getName()));
        if (exist != null) {
            return R.success(exist);
        }
        Model model = new Model();
        BeanUtil.copyProperties(modelReq, model);
        Settings settings = settingsService.getCachedSettings();
        model.setSaveDir(settings.getModelSaveDir() + "/" + modelReq.getDownloadPlatform() + "/" + model.getRepo());
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
            List<FileDownload> files = fileDownloadService.list((Wrappers.lambdaQuery(FileDownload.class)
                    .eq((SFunction<FileDownload, Long>) FileDownload::getModelId, id)));
            files.forEach(x -> {
                File file = new File(x.getFilePath() + "/" + x.getFileName());
                if (file.exists()) {
                    FileUtil.del(file);
                }
                fileDownloadService.removeById(x.getId());
            });
        }
        return R.success();
    }

    @Auth
    @DeleteMapping("/del-file/{id}")
    public R deleteFile(@PathVariable("id") Long id) {
        FileDownload fileDownload = fileDownloadService.getById(id);
        if (fileDownload != null) {
            File file = new File(fileDownload.getFilePath(), fileDownload.getFileName());
            FileUtil.del(file);
            fileDownloadService.removeById(id);
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

        List<FileDownload> exists = fileDownloadService.list(Wrappers.lambdaQuery(FileDownload.class)
                .eq((SFunction<FileDownload, Long>) FileDownload::getModelId, model.getId()));
        boolean neverDl = exists.stream().noneMatch(x -> x.getFileName().equals(fileDownloadReq.getFileName()));
        if (neverDl) {
            FileDownload fileDownload = new FileDownload();
            fileDownload.setModelId(model.getId());
            fileDownload.setModelName(model.getName());
            fileDownload.setFileSize(fileDownloadReq.getFileSize());
            fileDownload.setFileName(fileDownloadReq.getFileName());
            fileDownload.setType("download");
            Settings settings = settingsService.getCachedSettings();
            fileDownload.setFilePath(settings.getModelSaveDir() + "/" + model.getDownloadPlatform() + "/" + model.getRepo());
            fileDownload.setCreateTime(new Date());
            fileDownload.setUpdateTime(new Date());
            fileDownloadService.save(fileDownload);
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
    @GetMapping(value = "/dl")
    public R download(String repo, String filename) {
        if (StrUtil.isBlank(repo)) {
            throw new JException("模型仓库：repo必填");
        }
        if (StrUtil.isBlank(filename)) {
            throw new JException("文件名必填");
        }
        String primaryStage = jllamaConfigProperties.getModel().getPrimaryStage();
        if (modelScopeModelDownload.isMatch(primaryStage)) {
            modelScopeModelDownload.downloadAsync(repo, filename);
        } else {
            throw new JException("暂不支持" + primaryStage + "的模型下载方式");
        }

        return R.success();
    }

    @Auth
    @GetMapping(value = "/list-dl-file")
    public R listDownloadFiles(Long modelId) {
        List<FileDownload> list = fileDownloadService.list(Wrappers.lambdaQuery(FileDownload.class)
                .eq((SFunction<FileDownload, Long>) FileDownload::getModelId, modelId));
        list.forEach(x -> {
            File file = new File(x.getFilePath(), x.getFileName());
            if (file.exists()) {
                double progress = (double) file.length() / x.getFileSize() * 100;
                x.setPercent(String.format("%.2f%%", progress));
            }
        });
        return R.success(list);

    }

    @Auth
    @GetMapping(value = "/dl-percent", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public Flux<String> getDlFilePercent(String fileId) {
        FileDownload fileDownload = fileDownloadService.getById(fileId);
        if (fileDownload == null) {
            return null;
        }
        return Flux.create(fluxSink -> {
            threadPoolTaskExecutor.submit(() -> {
                long last = new Date().getTime();
                while (true) {
                    try {
                        File file = new File(fileDownload.getFilePath(), fileDownload.getFileName());
                        if (!file.exists()) {
                            fluxSink.next("0%");
                            break;
                        }
                        double progress = (double) file.length() / fileDownload.getFileSize() * 100;
                        String progressStr = String.format("%.2f%%", progress);
                        if (progress >= 100.00) {
                            fluxSink.next(progressStr);
                            break;
                        }
                        long current = new Date().getTime();
                        if (current - last > 1000) {
                            fluxSink.next(progressStr);
                            last = current;
                        }
                    } catch (Exception e) {
                        fluxSink.error(e);
                        break;
                    }
                }
                fluxSink.complete();
            });

        });
    }


    @Auth
    @GetMapping("/list-model")
    public R listModelInfo() {
        List<Model> modelList = modelService.list();
        return R.success(modelList);
    }

    @Auth
    @GetMapping("/list-download-file")
    public R listFileInfo(Long modelId) {
        List<FileDownload> fileDownloadList = fileDownloadService.list(new QueryWrapper<FileDownload>().eq("model_id", modelId));
        return R.success(fileDownloadList);
    }


    @Auth
    @PostMapping("/import-file")
    public R importFile(@RequestParam("file") MultipartFile file, @RequestParam("modelId") Long modelId) {
        Settings settings = settingsService.getCachedSettings();
        String modelSaveDir = settings.getModelSaveDir();
        if (file.isEmpty()) {
            return R.fail("请选择一个文件上传");
        }

        try {
            Path uploadPath = Paths.get(modelSaveDir + File.separator + "import");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Model model = modelService.getById(modelId);
            if (model != null) {
                FileDownload fileEntity = new FileDownload();
                fileEntity.setFileName(file.getOriginalFilename());
                fileEntity.setFileSize(file.getSize());
                fileEntity.setFilePath(modelSaveDir + File.separator + "import");
                fileEntity.setModelName(model.getName());
                fileEntity.setModelId(modelId);
                fileEntity.setType("import");
                fileDownloadService.save(fileEntity);
            }

        } catch (Exception e) {
            throw new JException("文件导入失败", e);
        }
        return R.success();

    }
}
