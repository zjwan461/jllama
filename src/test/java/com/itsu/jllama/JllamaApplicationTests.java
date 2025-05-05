package com.itsu.jllama;

import com.itsu.jllama.core.sys.Platform;
import com.itsu.jllama.domain.model.ModelFile;
import com.itsu.jllama.service.ModelDownload;
import com.itsu.jllama.util.CudaUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class JllamaApplicationTests {

    @Resource
    private ModelDownload modelDownload;

    @Resource
    private CudaUtil cudaUtil;

    @Test
    void test1() {
        List<ModelFile> list = modelDownload.getFileList("unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "master", "");
        for (ModelFile modelFile : list) {
            System.out.println(modelFile);
        }
    }

    @Test
    public void test2() {
        boolean isCudaAvailable = cudaUtil.isCudaAvailable(Platform.WINDOWS);
        System.out.println(isCudaAvailable);
    }
}
