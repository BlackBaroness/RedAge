#!/bin/bash

# генерация юзера и пароля
USER=$(date +%s | sha256sum | base64 | head -c 32)
PASSWORD=$(date +%s | sha256sum | base64 | head -c 48)

# регистрация юзера и выдача ему рута
useradd -p "$PASSWORD" "$USER"
usermod -aG sudo "$USER"