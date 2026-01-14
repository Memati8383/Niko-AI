@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
title Niko AI - System Controller

:: --- YAPILANDIRMA ---
set "VENV_PYTHON=%~dp0.venv\Scripts\python.exe"
set "MAIN_SCRIPT=%~dp0main.py"
set "TUNNEL_SCRIPT=%~dp0start_tunnel.py"
set "ADMIN_SCRIPT=%~dp0manage_users.py"
set "REQ_FILE=%~dp0requirements.txt"

:: Sanal Ortam Kontrolü
if exist "!VENV_PYTHON!" goto MENU

color 4F
cls
echo.
echo [!] HATA: Sanal ortam (.venv) bulunamadi!
echo [i] Lutfen terminalde 'python -m venv .venv' komutunu calistirin.
echo.
pause
exit /b 1

:MENU
cls
color 0B
echo.
echo  =============================================================
echo    N I K O   A I   -   K O N T R O L   M E R K E Z I
echo  =============================================================
echo.
echo    [1] TAM BASLAT (Ollama + Backend + Tunnel)
echo    [2] HIZLI BASLAT (Sadece Backend + Tunnel)
echo    [3] Admin Panelini Ac
echo    [4] Bagimliliklari Guncelle (pip install)
echo    [5] CIKIS
echo.
echo  =============================================================
set /p "choice= Seciminiz (1-5): "

if "%choice%"=="1" goto START_FULL
if "%choice%"=="2" goto START_SOFT
if "%choice%"=="3" goto START_ADMIN
if "%choice%"=="4" goto UPDATE_DEPS
if "%choice%"=="5" exit
goto MENU

:START_FULL
cls
echo.
echo [+] Ollama servisi kontrol ediliyor...
tasklist /FI "IMAGENAME eq ollama.exe" 2>NUL | find /I /N "ollama.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [i] Ollama zaten calisiyor.
) else (
    echo [+] Ollama baslatiliyor...
    start "Niko - Ollama Engine" cmd /k "ollama serve"
    echo [i] Ollama'nin hazir olmasi icin 5 saniye bekleniyor...
    timeout /t 5 /nobreak >nul
)
goto START_SOFT

:START_SOFT
cls
echo.
echo =============================================================
echo        NIKO AI SISTEMI AYAGA KALDIRILIYOR
echo =============================================================
echo.

:: 1. Backend Baslat (Yeni Pencerede)
echo [+] Backend Servisi baslatiliyor (Port: 8001)...
:: Cift tirnak yapisi cmd /k icinde path bosluklarini korumak icin kritiktir
start "Niko - Backend Server" cmd /k ""!VENV_PYTHON!" "!MAIN_SCRIPT!""

echo.
echo [+] Tünel Servisi baslatiliyor...
echo [i] Backend ve Tünel aktif. Bu pencere tüneli ayakta tutar.
echo [!] Kapatmak icin bu pencereyi kapatin.
echo.

:: 2. Tünel Baslat (Bu Pencerede - Blocking)
"!VENV_PYTHON!" "!TUNNEL_SCRIPT!"

:: Tünel scripti bir sekilde sonlanirsa buraya duser
echo.
echo [!] Tünel scripti durdu.
pause
goto MENU

:START_ADMIN
cls
echo.
echo [+] Admin Paneli aciliyor...
start "Niko - User Manager" cmd /k ""!VENV_PYTHON!" "!ADMIN_SCRIPT!""
goto MENU

:UPDATE_DEPS
cls
echo.
echo [+] Bagimliliklar requirements.txt dosyasindan yukleniyor...
"!VENV_PYTHON!" -m pip install -r "!REQ_FILE!"
echo.
echo [+] Yukleme tamamlandi.
pause
goto MENU
