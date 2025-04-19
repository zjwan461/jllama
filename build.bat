@echo off
setlocal enabledelayedexpansion

rem 创建build目录
md build 2>nul

rem 进入ui目录
cd ui

rem 运行npm构建
call npm install
call npm run build

rem 复制dist目录到指定位置
xcopy /E /I dist ..\src\java\resources\static

rem 重命名dist目录为app
ren ..\src\java\resources\static\dist app

rem 返回上一级目录
cd ..

rem 使用Maven进行项目打包并跳过测试
call mvn clean package -Dmaven.test.skip=true

rem 复制scripts目录下的文件到build目录
xcopy /Y scripts\* build\

rem 为build目录下的文件添加可执行权限（在Windows中可执行文件有特定扩展名，此步无实际作用，可省略）
rem 假设要处理的是.cmd或.exe文件，可添加如下代码
for %%f in (build\*) do (
    if "%%~xf"==".cmd" (
        attrib +x "%%f"
    )
    if "%%~xf"==".exe" (
        attrib +x "%%f"
    )
)

rem 复制application.yml.template到build目录并重命名为application.yml
copy application.yml.template build\application.yml

rem 复制JAR文件到build目录
copy target\jllama-0.0.1-SNAPSHO.jar build\jllama.jar

rem 复制llmama目录到build目录
xcopy /E /I llmama build\llmama

endlocal