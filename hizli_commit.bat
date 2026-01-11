@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Niko AI - Hizli Commit
color 0F

:: ESC Karakterini oluştur (Renkler için)
for /F "tokens=1,2 delims=#" %%a in ('"prompt #$H#$E# & echo on & for %%b in (1) do rem"') do set ESC=%%b

:: Renk Tanımlamaları
set "G=%ESC%[32m"
set "R=%ESC%[31m"
set "Y=%ESC%[33m"
set "B=%ESC%[36m"
set "W=%ESC%[37m"
set "RESET=%ESC%[0m"

echo %B%==========================================%RESET%
echo %B%   NIKO AI - GELİŞMİŞ COMMİT SİSTEMİ      %RESET%
echo %B%==========================================%RESET%
echo.

:: Git kontrolü
git rev-parse --is-inside-work-tree >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo %R% HATA: Burası bir Git deposu değil.%RESET%
    pause
    exit /b
)

:: Değişiklikleri göster
echo %Y%[+] Mevcut durum:%RESET%
git status -s
echo.

:: Kullanıcıdan mesaj al
set /p msg="Commit mesajı girin (Boş ise zaman damgası): "

:: Mesaj boşsa zaman damgası ata
if "!msg!"=="" (
    set "msg=Guncelleme %date% %time%"
)

echo.
echo %B%[1/4] Dosyalar hazırlanıyor...%RESET%
git add .

echo.
echo %B%[2/4] Yerel depoya kaydediliyor...%RESET%
git commit -m "!msg!"

:: Commit başarısız olsa bile devam edebiliriz (belki önceden yapılmış ama push edilmemiş commitler vardır)
if %ERRORLEVEL% NEQ 0 (
    echo %Y%[-] Bilgi: Yeni değişiklik bulunamadı veya commit zaten güncel.%RESET%
)

echo.
echo %B%[3/4] Sunucuyla senkronize ediliyor (Pull)...%RESET%
git pull origin main --rebase

if %ERRORLEVEL% NEQ 0 (
    echo %R% HATA: Senkronizasyon başarısız. Çakışma (Conflict) olabilir.%RESET%
    echo %Y% İPUCU: Manuel olarak 'git status' bakmanız gerekebilir.%RESET%
    color 0C
    pause
    exit /b
)

echo.
echo %B%[4/4] GitHub'a gönderiliyor (Push)...%RESET%
git push origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo %G%==========================================%RESET%
    echo %G%   BAŞARILI! Değişiklikler yayında.       %RESET%
    echo %G%==========================================%RESET%
    color 0A
) else (
    echo.
    echo %R%==========================================%RESET%
    echo %R%   HATA! Gönderme işlemi başarısız.      %RESET%
    echo %R%   İnternet bağlantınızı kontrol edin.    %RESET%
    echo %R%==========================================%RESET%
    color 0C
)

echo.
echo %W%Çıkmak için bir tuşa basın...%RESET%
pause >nul
