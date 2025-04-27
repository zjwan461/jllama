#!/bin/bash
nohup java -jar -Dspring.config.location=./application.yml jllama.jar > /dev/null 2>&1 &