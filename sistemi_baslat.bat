@echo off
TITLE Niko AI - Advanced System Dashboard
SETLOCAL EnableDelayedExpansion

:: --- CONFIGURATION ---
SET PYTHON_EXE=.venv\Scripts\python.exe
SET MAIN_SCRIPT=main.py
SET TUNNEL_SCRIPT=start_tunnel.py
SET ADMIN_SCRIPT=manage_users.py
:: ---------------------

:MENU
cls
color 0B
echo.
echo  ==========================================================
echo            N I K O   A I   -   S Y S T E M   H U B
echo  ==========================================================
echo.
echo    [1] TAM SISTEM BASLAT (Ollama + Backend + Tunnel)
echo    [2] Sadece Backend ve Tunel Baslat
echo    [3] Kullanici Yonetim Panelini Ac (Admin)
echo    [4] Bagimliliklari Kontrol Et / Guncelle
echo    [5] Cikis
echo.
echo  ==========================================================
set /p choice=" Seciminizi yapin (1-5): "

if "%choice%"=="1" goto FULL_START
if "%choice%"=="2" goto SOFT_START
if "%choice%"=="3" goto ADMIN_PANEL
if "%choice%"=="4" goto DEPS
if "%choice%"=="5" exit
goto MENU

:FULL_START
cls
echo [+] Ollama servisi kontrol ediliyor...
tasklist /FI "IMAGENAME eq ollama.exe" 2>NUL | find /I /N "ollama.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [!] Ollama zaten calisiyor.
) else (
    echo [+] Ollama baslatiliyor...
    start "Niko - Ollama Engine" cmd /k "ollama serve"
    timeout /t 5 /nobreak > nul
)
goto SOFT_START

:SOFT_START
cls
echo ==========================================================
echo        NIKO AI SISTEMI AYAGA KALDIRILIYOR
echo ==========================================================
echo.

:: Check Virtual Env
if not exist "%PYTHON_EXE%" (
    color 0C
    echo [!] HATA: Sanal ortam (.venv) bulunamadi.
    echo [*] Lutfen 'python -m venv .venv' komutuyla olusturun.
    pause
    goto MENU
)

:: Start Backend
echo [+] Backend baslatiliyor (Port: 8001)...
start "Niko - Backend Server" cmd /k "%PYTHON_EXE% %MAIN_SCRIPT%"
timeout /t 5 /nobreak > nul

:: Start Tunnel
echo [+] Cloudflare Tunel aktif ediliyor...
echo [*] URL'ler otomatik olarak GitHub ve yerel dosyalarda guncellenecek.
echo.
echo [!] DIKKAT: Sistemi kapatmak isterseniz bu pencereyi veya digerlerini kapatin.
echo.
"%PYTHON_EXE% %TUNNEL_SCRIPT%"
pause
goto MENU

:ADMIN_PANEL
cls
echo [+] Kullanici Yonetim Paneli aciliyor...
start "Niko - User Manager" cmd /k "%PYTHON_EXE% %ADMIN_SCRIPT%"
pause
goto MENU

:DEPS
cls
echo [+] Bagimliliklar kontrol ediliyor...
"%PYTHON_EXE%" -m pip install -r requirements.txt --quiet --no-warn-script-location --disable-pip-version-check
echo.
echo [+] Tamamlandi.
pause
goto MENU
