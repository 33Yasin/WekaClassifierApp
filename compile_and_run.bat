@echo off
echo WEKA Classification Project - Compile and Run Script
echo =====================================================

:: Bin klasörü oluştur
if not exist bin mkdir bin

:: Derleme
echo Derleniyor...
javac -cp "lib/weka.jar" -d bin src/main/java/com/weka/classification/*.java

if %ERRORLEVEL% neq 0 (
    echo HATA: Derleme basarisiz!
    pause
    exit /b 1
)

echo Derleme tamamlandi.

:: Çalıştırma
echo Uygulama baslatiliyor...
java --add-opens java.base/java.lang=ALL-UNNAMED -cp "bin;lib/weka.jar" com.weka.classification.WekaClassificationGUI

pause