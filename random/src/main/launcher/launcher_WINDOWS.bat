@echo off
REM Expansion not relevant yet, just good practice to have it here
setlocal enabledelayedexpansion

echo ===== Launching the Universal Pokémon Randomizer FVX at %date% %time% ===== >> console_output.log
echo ===== Launching the Universal Pokémon Randomizer FVX at %date% %time%

REM Change to the directory of the launcher
cd /d "%~dp0"
if errorlevel 1 (
    echo Error: cannot change the working directory to the launcher folder: %~dp0 >> console_output.log
    echo Error: cannot change the working directory to the launcher folder: %~dp0
    exit /b 1
)

REM Path to embedded Java
set "JAVA=java\bin\java.exe"

REM Detect embedded Java missing
if not exist "%JAVA%" (
    echo Error: embedded Java runtime not found at: %JAVA% >> console_output.log
    echo Error: embedded Java runtime not found at: %JAVA%
    exit /b 1
)

REM Detect Java not runnable
"%JAVA%" -version >nul 2>&1
if errorlevel 1 (
    echo Error: embedded Java runtime exists but cannot be executed: %JAVA% >> console_output.log
    echo Error: embedded Java runtime exists but cannot be executed: %JAVA%
    exit /b 1
)

REM Launch FVX
"%JAVA%" -Xmx4608M -jar UPR-FVX.jar please-use-the-launcher
