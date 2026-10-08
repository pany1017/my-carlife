@echo off
echo 正在启动编译...

:: ==== 请把下面这行改成你刚才找到的 bin 文件夹的真实路径 ====
set JDK_PATH="C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot\bin"
:: ===================================================

:: 检查路径是否存在
if not exist %JDK_PATH%\java.exe (
    echo 【错误】找不到 JDK，请检查脚本里的路径是否正确！
    pause
    exit /b
)

:: 设置临时环境变量
set PATH=%JDK_PATH%;%PATH%
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8u504-b01

echo 正在清理旧文件...
call gradlew.bat clean

echo 正在编译 APK...
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo =================== 恭喜！编译成功 ===================
    echo 文件位置：app\build\outputs\apk\debug\CarLifeLauncher.apk
) else (
    echo.
    echo =================== 编译失败 ===================
    echo 可能是网络问题导致下载失败，或者是代码冲突。
)
pause