@echo off
chcp 65001 >nul
title Niko AI - Hizli Commit
color 0A

echo ==========================================
echo    NIKO AI - HIZLI COMMİT ve PUSH
echo ==========================================
echo.

:: Durumu göster
git status -s
echo.

:: Kullanıcıdan mesaj al
set /p msg="Commit mesaji girin (Enter'a basarsaniz tarih saat atilir): "

:: Mesaj boşsa otomatik tarih saat ata
if "%msg%"=="" (
    set msg=Guncelleme %date% %time%
)

echo.
echo [1/3] Dosyalar ekleniyor...
git add .

echo.
echo [2/3] Commitleniyor: "%msg%"
git commit -m "%msg%"

echo.
echo [3/3] GitHub'a gonderiliyor...
git push origin main

echo.
if %ERRORLEVEL% EQU 0 (
    echo BASARILI! Her sey gonderildi.
) else (
    color 0C
    echo HATA! Gonderilemedi. Lutfen interneti veya cakismalari kontrol edin.
)

echo.
pause
