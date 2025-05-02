@echo off
setlocal enabledelayedexpansion

rem 先删除UI静态文件目录和构建目录
if exist src\main\resources\static (
    cd src\main\resources
    rd /s /q static
    cd ..\..\..
)
if exist build (
    rd /s /q build
)

rem 创建build目录
md build 2>nul

rem 进入ui目录
cd ui

rem 运行npm构建
call npm install
call npm run build

rem 复制dist目录到指定位置
xcopy /E /I dist ..\src\main\resources\static\app

rem 返回上一级目录
cd ..

rem 使用Maven进行项目打包并跳过测试
call mvn clean package -Dmaven.test.skip=true

rem 复制scripts目录下的文件到build目录
xcopy /Y scripts\* build\scripts\

rem 复制application.yml.template到build目录并重命名为application.yml
copy application.yml.template build\application.yml

rem 复制JAR文件到build目录
copy target\jllama-0.0.1-SNAPSHOT.jar build\jllama.jar

rem 复制llama目录到build目录
xcopy /E /I llama build\llama

rem 复制javafx-sdk目录到build目录
xcopy /E /I javafx-sdk build\javafx-sdk

move build\scripts\startup.bat build\
move build\scripts\startup_linux.sh build\
move build\scripts\startup_macos.sh build\
echo build success
endlocal