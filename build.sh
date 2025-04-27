#!/bin/bash
set -ex

# 定义变量
BUILD_DIR="build"
UI_DIR="ui"
RESOURCES_DIR="src/main/resources/static"
SCRIPTS_DIR="scripts"
TARGET_DIR="target"
JAR_FILE="jllama-0.0.1-SNAPSHOT.jar"
TEMPLATE_FILE="application.yml.template"
LLAMA_DIR="llama"

# 先删除UI静态文件目录和build目录
rm -rf "$RESOURCES_DIR"
rm -rf "$BUILD_DIR"

# 创建build目录
mkdir -p "$BUILD_DIR"

# 构建前端项目
if [ -d "$UI_DIR" ]; then
    cd "$UI_DIR"
    npm install
    npm run build
    if [ -d "dist" ]; then
        mkdir -p "../$RESOURCES_DIR/app"
        cp -rf dist/ "../$RESOURCES_DIR/app"
#        mv "../$RESOURCES_DIR/dist" "../$RESOURCES_DIR/app"
    fi
    cd ..
fi

# 构建后端项目
mvn clean package -Dmaven.test.skip=true

# 复制脚本文件
if [ -d "$SCRIPTS_DIR" ]; then
    mkdir -p "$BUILD_DIR/scripts"
    cp "$SCRIPTS_DIR"/* "$BUILD_DIR/scripts/"
    chmod +x "$BUILD_DIR"/*
fi

# 复制配置文件
if [ -f "$TEMPLATE_FILE" ]; then
    cp "$TEMPLATE_FILE" "$BUILD_DIR/application.yml"
fi

# 复制JAR文件
if [ -f "$TARGET_DIR/$JAR_FILE" ]; then
    cp "$TARGET_DIR/$JAR_FILE" "$BUILD_DIR/jllama.jar"
fi

# 复制llmama目录
if [ -d "$LLAMA_DIR" ]; then
    cp -r "$LLAMA_DIR" "$BUILD_DIR/$LLAMA_DIR"
fi

mv build/scripts/startup.bat build/startup.bat
mv build/scripts/startup.sh build/startup.sh

echo "build success"