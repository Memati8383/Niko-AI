@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Niko AI - Hizli Commit
color 0F

:: ESC Karakterini oluştur
for /F "tokens=1,2 delims=#" %%a in ('"prompt #$H#$E# & echo on & for %%b in (1) do rem"') do set ESC=%%b

set "G=%ESC%[32m"
set "R=%ESC%[31m"
set "Y=%ESC%[33m"
set "B=%ESC%[36m"
set "W=%ESC%[37m"
set "RESET=%ESC%[0m"

echo %B%==========================================%RESET%
echo %B%   NIKO AI - GELISMLS COMMIT SISTEMI      %RESET%
echo %B%==========================================%RESET%
echo.

git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 goto NO_GIT

echo %Y%[+] Mevcut durum:%RESET%
git status -s
echo.

set "msg="
set /p msg="Commit mesaji giriniz (Bos ise zaman damgasi): "

if "!msg!"=="" set "msg=Guncelleme %date% %time%"

echo.
echo %B%[1/4] Dosyalar hazirlaniyor...%RESET%
git add .

echo.
echo %B%[2/4] Yerel depoya kaydediliyor...%RESET%
git commit -m "!msg!"
if errorlevel 1 echo %Y%[-] Bilgi: Yeni degisiklik yok veya commit zaten guncel.%RESET%

echo.
echo %B%[3/4] Sunucuyla senkronize ediliyor - Pull...%RESET%
git pull origin main
if errorlevel 1 goto PULL_FAIL

echo.
echo %B%[4/4] GitHub'a gonderiliyor - Push...%RESET%
git push origin main
if errorlevel 1 goto PUSH_FAIL

echo.
echo %G%==========================================%RESET%
echo %G%   BASARILI! Degisiklikler yayinda.       %RESET%
echo %G%==========================================%RESET%
color 0A
goto END

:NO_GIT
echo %R% HATA: Burasi bir Git deposu degil.%RESET%
goto EXIT_PAUSE

:PULL_FAIL
echo %R% HATA: Senkronizasyon basarisiz. Cakisma olabilir.%RESET%
echo %Y% IPUCU: Manuel olarak git status bakmaniz gerekebilir.%RESET%
color 0C
goto EXIT_PAUSE

:PUSH_FAIL
echo %R% HATA: Gonderme islemi basarisiz.%RESET%
echo %R% Internet baglantinizi kontrol edin.%RESET%
color 0C
goto EXIT_PAUSE

:EXIT_PAUSE
echo.
pause

:END
echo.
echo %W%Cikmak icin bir tusa basin...%RESET%
pause >nul
:EXIT
