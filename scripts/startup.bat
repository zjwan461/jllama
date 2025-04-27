start javaw -jar -Dspring.config.location=./application.yml jllama.jar
echo Jllama启动中...
timeout /t 5 /nobreak > nul
start "" "http://127.0.0.1:21434/app"