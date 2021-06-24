#!/bin/bash

# получение приветствия
echo Введите приветствие для SSH
read -r GREETING

# обновление всех пакетов
apt-get -y update && apt-get -y dist-upgrade

# очистка приветствия
cat /dev/null > /etc/motd

# установка figlet
apt install -y figlet

# установка приветствия
echo "figlet -ct -C utf8 -f banner $GREETING" > /etc/profile.d/salute.sh

# аплодисменты
echo "Приветствие '$GREETING' установлено"

exit 0;