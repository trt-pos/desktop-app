@echo off

REM Assign the value random password to the password variable
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
set alfanum=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
set rustdesk_pw=
for /L %%b in (1, 1, 12) do (
    set /A rnd_num=!RANDOM! %% 62
    for %%c in (!rnd_num!) do (
        set rustdesk_pw=!rustdesk_pw!!alfanum:~%%c,1!
    )
)

REM Get your config string from your Web portal and Fill Below
set rustdesk_cfg="0nI9Mndp9mczkka0ljZKplVrclYWlnatJGeYpkYXVUUiZXREREOohDOPl2V6ZlI6ISeltmIsIiI6ISawFmIsIiI6ISehxWZyJCLi02bj5idwRXZsJWY0Rmb19mcus2clRGdzVnciojI0N3boJye"

REM ############################### Please Do Not Edit Below This Line #########################################

if not exist C:\Temp\ md C:\Temp\
cd C:\Temp\

curl -L "https://github.com/rustdesk/rustdesk/releases/download/1.4.0/rustdesk-1.4.0-x86_64.exe" -o rustdesk.exe

rustdesk.exe --silent-install
timeout /t 20

rustdesk.exe --config %rustdesk_cfg%
rustdesk.exe --password %rustdesk_pw%

cd "C:\Program Files\RustDesk\"
rustdesk.exe --install-service
timeout /t 5

for /f "delims=" %%i in ('rustdesk.exe --get-id ^| more') do set rustdesk_id=%%i

echo ...............................................
REM Show the value of the ID Variable
echo RustDesk ID: %rustdesk_id%

REM Show the value of the Password Variable
echo Password: %rustdesk_pw%
echo ...............................................
