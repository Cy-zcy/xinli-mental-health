@echo off
echo 正在测试后端项目编译...
cd /d "%~dp0"
mvn clean compile
if %errorlevel% equ 0 (
    echo 编译成功！
) else (
    echo 编译失败，请检查错误信息
)
pause
