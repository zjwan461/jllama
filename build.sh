#!/usr/bin/bash
set -ex

# 定义变量
BUILD_DIR="build"
UI_DIR="ui"
RESOURCES_DIR="src/java/resources/static"
SCRIPTS_DIR="scripts"
TARGET_DIR="target"
JAR_FILE="jllama-0.0.1-SNAPSHO.jar"
TEMPLATE_FILE="application.yml.template"
LLMAMA_DIR="llmama"

# 创建build目录
mkdir -p "$BUILD_DIR"

# 构建前端项目
if [ -d "$UI_DIR" ]; then
    cd "$UI_DIR"
    npm install
    npm run build
    if [ -d "dist" ]; then
        cp -r dist "../$RESOURCES_DIR"
        mv "../$RESOURCES_DIR/dist" "../$RESOURCES_DIR/app"
    fi
    cd ..
fi

# 构建后端项目
mvn clean package -Dmaven.test.skip=true

# 复制脚本文件
if [ -d "$SCRIPTS_DIR" ]; then
    cp "$SCRIPTS_DIR"/* "$BUILD_DIR/"
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
if [ -d "$LLMAMA_DIR" ]; then
    cp -r "$LLMAMA_DIR" "$BUILD_DIR/$LLMAMA_DIR"
fi