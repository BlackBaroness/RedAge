#!/bin/bash

# укажите все переменные ниже (данные от хранилища)
USER=spacecore21625
PASSWORD=bGHQPwk3OOtG

# начало отсчёта
START_TIME=$(date +%s)

# установка нужных утилит
apt install -y p7zip-full lftp &&

# сборка всех серверов в архив
mysqldump -u root -pPassword --all-databases > databases.sql &&
TIME=$(date +%F-%T)
7z a "$TIME".7z proxy lobby server-1 databases.sql &&

# отправка
lftp ftp://$USER:$PASSWORD@backup.s1.fsn.spacecore.pro:21 -e "set ftp:ssl-allow no; put -O / $TIME.7z; quit" &&

# чистка мусора
rm "$TIME".7z
rm databases.sql

# вывод логов
END_TIME=$(date +%s)
TOTAL_TIME=$(( $END_TIME - $START_TIME ))
MINUTES=$((TOTAL_TIME / 60))
SECONDS=$((TOTAL_TIME % 60))

echo ""
echo "Backup completed in $MINUTES:$SECONDS"

exit 0; 

