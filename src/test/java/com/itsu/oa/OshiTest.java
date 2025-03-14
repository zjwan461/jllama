package com.itsu.oa;

import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;

public class OshiTest {

    @Test
    public void test() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        // 获取所有的显卡信息
        List<GraphicsCard> graphicsCards = hal.getGraphicsCards();
        if (graphicsCards.isEmpty()) {
            System.out.println("未检测到 GPU 信息。");
        } else {
            for (GraphicsCard graphicsCard : graphicsCards) {
                System.out.println("GPU 名称: " + graphicsCard.getName());
                System.out.println("GPU 显存: " + graphicsCard.getVRam() / (1024 * 1024 * 1024) + " GB");
                System.out.println("GPU 驱动版本: " + graphicsCard.getVersionInfo());
                System.out.println("------------------------------");
            }
        }
    }
}
