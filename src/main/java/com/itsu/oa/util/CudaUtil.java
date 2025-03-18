package com.itsu.oa.util;

import com.itsu.oa.core.sys.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Future;

@Component
@Slf4j
public class CudaUtil {

    @Resource
    private ScriptRunner scriptRunner;

    public boolean isCudaAvailable(Platform platform) {
        String cudaVersionScript = "";
        ScriptRunner.SCRIPT_TYPE scriptType = null;
        if (platform == Platform.WINDOWS) {
            cudaVersionScript = "cuda-version.bat";
            scriptType = ScriptRunner.SCRIPT_TYPE.BAT;
        } else if (platform == Platform.LINUX || platform == Platform.MAC) {
            cudaVersionScript = "cuda-version.sh";
            scriptType = ScriptRunner.SCRIPT_TYPE.BASH;
        }
        // Check if CUDA is available on the system
        Future<ScriptRunner.ScriptResp> future = scriptRunner.runScript(System.getProperty("user.dir") + "/scripts/" + cudaVersionScript,
                scriptType);
        try {
            ScriptRunner.ScriptResp scriptResp = future.get();
            return scriptResp.getProcess().waitFor() == 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
