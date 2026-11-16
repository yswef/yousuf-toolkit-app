@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "TAG="
set /p "TAG=Tag (v0.1.0-test.1 | v0.2.0-beta.1 | v1.0.0): "
if not defined TAG exit /b 1

set "KIND="
echo !TAG!| findstr /r /c:"^v[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*-test\.[0-9][0-9]*$" >nul && set "KIND=test"
echo !TAG!| findstr /r /c:"^v[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*-beta\.[0-9][0-9]*$" >nul && set "KIND=beta"
echo !TAG!| findstr /r /c:"^v[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*$" >nul && set "KIND=stable"
if not defined KIND (
  echo [error] invalid tag format
  exit /b 1
)

for /f "delims=" %%b in ('git rev-parse --abbrev-ref HEAD') do set "BR=%%b"
if "!KIND!"=="stable" (
  if /i not "!BR!"=="main" (
    echo [error] stable tags must be created on main
    exit /b 1
  )
) else (
  if /i not "!BR!"=="dev" (
    echo [error] test and beta tags must be created on dev
    exit /b 1
  )
)

set "DIRTY="
for /f "delims=" %%s in ('git status --porcelain') do set "DIRTY=1"
if defined DIRTY (
  echo [error] working tree is not clean
  exit /b 1
)

git fetch -q origin
if errorlevel 1 exit /b 1
for /f "delims=" %%h in ('git rev-parse HEAD') do set "LOCAL=%%h"
for /f "delims=" %%h in ('git rev-parse origin/!BR!') do set "REMOTE=%%h"
if not "!LOCAL!"=="!REMOTE!" (
  echo [error] branch !BR! is not in sync with origin
  exit /b 1
)

git tag -a "!TAG!" -m "!TAG!"
if errorlevel 1 exit /b 1
git push origin "!TAG!"
if errorlevel 1 exit /b 1
echo tag !TAG! pushed
exit /b 0
