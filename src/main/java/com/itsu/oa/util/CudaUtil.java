package com.itsu.oa.util;

import com.itsu.oa.core.sys.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CudaUtil {

    @Resource
    private ScriptRunner scriptRunner;

    public boolean isCudaAvailable(Platform platform) {
        String cudaVersionScript = "";
        String scriptDir = "";
        if (platform == Platform.WINDOWS) {
            cudaVersionScript = "cuda-version.bat";
            scriptDir = "cmd";
        } else if (platform == Platform.LINUX || platform == Platform.MAC) {
            cudaVersionScript = "cuda-version.sh";
            scriptDir = "bash";
        }
        // Check if CUDA is available on the system

        try {
            scriptRunner.runScript(scriptDir, System.getProperty("user.dir") + "/scripts/" + cudaVersionScript,false);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
