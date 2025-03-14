package com.itsu.oa;

import com.itsu.oa.domain.model.ModelFile;
import com.itsu.oa.service.ModelDownload;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class JllamaApplicationTests {

    @Resource
    private ModelDownload modelDownload;

    @Test
    void test1() {
        List<ModelFile> list = modelDownload.getFileList("unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "master", "");
        for (ModelFile modelFile : list) {
            System.out.println(modelFile);
        }
    }

}
