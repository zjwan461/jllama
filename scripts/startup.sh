#!/bin/bash

# 启动 Java 应用程序
nohup java -jar -Dspring.config.location=./application.yml jllama.jar > /dev/null 2>&1 &
echo "Jllama启动中..."

# 等待 5 秒
sleep 5

# 使用默认浏览器打开指定 URL
xdg-open "http://127.0.0.1:21434/app"