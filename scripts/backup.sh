#!/bin/bash

# укажите все переменные ниже (данные от хранилища)
USER=spacecore21625
PASSWORD=bGHQPwk3OOtG

# установка нужных утилит
apt install -y p7zip-full lftp

# сборка всех серверов в архив
TIME=$(date +%F-%T)
7z a "$TIME".7z proxy lobby server-1

# отправка
lftp ftp://$USER:$PASSWORD@backup.s1.fsn.spacecore.pro:21 -e "set ftp:ssl-allow no; put -O / $TIME.7z; quit"

# чистка мусора
rm "$TIME".7z

exit 0;