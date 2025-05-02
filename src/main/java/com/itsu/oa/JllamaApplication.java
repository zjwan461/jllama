package com.itsu.oa;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.mapper.SysInfoMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Objects;

@SpringBootApplication
public class JllamaApplication extends Application {

    private static String port;
    private static String[] args;

    public static void main(String[] args) {
        JllamaApplication.args = args;
        SpringApplication.run(JllamaApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(SysInfoMapper sysInfoMapper, BeanFactory beanFactory, Environment environment) {
        JllamaApplication.port = environment.getProperty("server.port");
        return args -> {
            SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class).last("limit 1"));
            if (sysInfo != null) {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SysInfo.class);
                beanDefinitionBuilder.addPropertyValue("platform", sysInfo.getPlatform())
                        .addPropertyValue("osArch", sysInfo.getOsArch())
                        .addPropertyValue("gpuPlatform", sysInfo.getGpuPlatform())
                        .addPropertyValue("cppVersion", sysInfo.getCppVersion())
                        .addPropertyValue("factoryVersion", sysInfo.getFactoryVersion())
                        .addPropertyValue("selfVersion", sysInfo.getSelfVersion())
                        .setScope(BeanDefinition.SCOPE_SINGLETON);
                AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                registry.registerBeanDefinition("sysInfo", beanDefinition);
                System.out.println(SpringUtil.getBean("sysInfo", SysInfo.class));
            }

            launch(JllamaApplication.args);
        };

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建 WebView 组件
        WebView webView = new WebView();
        // 获取 WebView 的 WebEngine，用于加载和管理网页
        WebEngine webEngine = webView.getEngine();
        // 加载指定的网页
        webEngine.load("http://127.0.0.1:" + port + "/app");

        // 创建一个 StackPane 布局，并将 WebView 添加到其中
        StackPane root = new StackPane();
        root.getChildren().add(webView);

        // 创建场景，设置场景的宽度和高度
        Scene scene = new Scene(root, 1366, 768);

        // 设置舞台的标题
        primaryStage.setTitle("jllama");
        // 将场景设置到舞台上
        primaryStage.setScene(scene);
        // 设置图标
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/logo.png")));
        primaryStage.getIcons().add(icon);

//        configureHide(primaryStage);
        // 显示舞台
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        int exit = SpringApplication.exit(SpringUtil.getApplicationContext());
        System.exit(exit);
    }

//    public void configureHide(Stage primaryStage) {
//        // 先添加系统托盘图标
//        URL url = JllamaApplication.class.getResource("/assets/logo.png");
//        // 创建系统托盘
//        FXTrayIcon fxTrayIcon = new FXTrayIcon(primaryStage, url);
//        // 添加系统托盘菜单
//        javafx.scene.control.MenuItem exitItem = new MenuItem("exit");
//        fxTrayIcon.addMenuItem(exitItem);
//        exitItem.setOnAction(actionEvent -> {
//            // 退出程序
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要退出jllama?");
//            Optional<ButtonType> optional = alert.showAndWait();
//            if (optional.isPresent() && optional.get() == ButtonType.OK) {
//                Platform.exit();
//                System.exit(0);
//            } else {
//                actionEvent.consume();
//            }
//        });
//
//        primaryStage.setOnCloseRequest(event -> {
//
//            Platform.setImplicitExit(false);
//            // 先判断是否支持系统托盘
//            if (FXTrayIcon.isSupported()) {
//                if (!fxTrayIcon.isMenuShowing()) {
//                    // 显示系统托盘
//                    fxTrayIcon.show();
//                }
//            } else { // 如果不支持，关闭窗口直接退出程序
//                System.exit(0);
//            }
//        });
//    }

}
