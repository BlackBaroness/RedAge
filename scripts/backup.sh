#!/bin/bash

# укажите все переменные ниже (данные от хранилища)
USER=spacecore21625
PASSWORD=bGHQPwk3OOtG

# начало отсчёта
START_TIME=$(date +%s)

# создание папки логов
mkdir -p backupLogs

# установка нужных утилит
apt install -y p7zip-full lftp &&

# сборка всех серверов в архив
mysqldump -u root -pPassword --all-databases > databases.sql &&
TIME=$(date +%F-%T)
7z a -t7z -m0=lzma -mx=9 -mfb=64 -md=32m -ms=on "$TIME".7z proxy lobby server-1 databases.sql 2> backupLogs/"$TIME".log &&

# отправка
lftp ftp://$USER:$PASSWORD@backup.s1.fsn.spacecore.pro:21 -e "set ftp:ssl-allow no; put -O / $TIME.7z; quit" &&

# чистка мусора
rm "$TIME".7z
rm databases.sql

# вывод логов
END_TIME=$(date +%s)
TOTAL_TIME=$(( $END_TIME - $START_TIME ))
MINUTES=$((total_time / 60))
SECONDS=$((total_time % 60))
LOGSTRING="Backup completed in $MINUTES:$SECONDS"

echo ""
echo "$LOGSTRING"
echo ""  >> backupLogs/"$TIME".log
echo "$LOGSTRING" >> backupLogs/"$TIME".log

exit 0; 

