package com.itsu.oa.util;

import jcuda.driver.JCudaDriver;

public class CudaUtil {

    public static boolean isCuda() {
        int result = -1;
        try {
            result = JCudaDriver.cuInit(0);
        } catch (Throwable e) {
//            log.warn("not cuda env, detail: {}", e.getMessage());
            e.printStackTrace();
        }
        return result == 0;
    }

    public static String cudaVersion() {
        int[] driverVersion = new int[1];
        JCudaDriver.cuDriverGetVersion(driverVersion);
        int majorDriver = driverVersion[0] / 1000;
        int minorDriver = (driverVersion[0] % 1000) / 10;
//        log.info("CUDA 驱动版本: " + majorDriver + "." + minorDriver);
        System.out.println("CUDA 驱动版本: " + majorDriver + "." + minorDriver);
        return majorDriver + "." + minorDriver;
    }

    public static void main(String[] args) {
        isCuda();
        cudaVersion();
    }
}
