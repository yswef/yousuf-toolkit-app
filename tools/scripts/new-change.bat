@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "M="
set "F="
set /p "M=Model name: "
set /p "F=Feature: "
if not defined M exit /b 1
if not defined F exit /b 1

for /f %%d in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd"') do set "D=%%d"
set "M=!M: =-!"
set "F=!F: =-!"
set "NAME=!D!_!M!_!F!"
set "FILE=docs\dev\changes\!NAME!.md"

if exist "!FILE!" (
  echo [error] file already exists
  exit /b 1
)

> "!FILE!" echo # change
>> "!FILE!" echo.
>> "!FILE!" echo - model: !M!
>> "!FILE!" echo - feature: !F!
>> "!FILE!" echo - date: !D!
>> "!FILE!" echo - files:
>> docs\dev\CHANGELOG.md echo - !D! ^| !M! ^| !F! ^| changes/!NAME!.md
echo created !FILE!
exit /b 0
