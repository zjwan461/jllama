#!/bin/bash

# 启动 Java 应用程序
nohup java -jar -Dspring.config.location=./application.yml jllama.jar > /dev/null 2>&1 &
echo "Jllama is starting..."

# 等待 5 秒
sleep 5
echo "Jllama is started."