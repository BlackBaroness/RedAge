## Сборка:

- Во всех проектах прописать переменную desktop под название рабочего стола
- Собирать через ALL проект: clean install package

# Настройка дистрибутива

## Проброс ключей:

```
У СЕБЯ
ssh-keygen -t rsa -q -N '' -f ~/.ssh/id_rsa
scp -P ПОРТ ~/.ssh/id_rsa.pub ЮЗЕР@АДРЕС:~

НА СЕРВЕРЕ
[ -d ~/.ssh ] || (mkdir ~/.ssh; chmod 711 ~/.ssh)
cat ~/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
rm ~/id_rsa.pub
```

## Первоначальные действия

```
apt update
apt-get dist-upgrade
cat /dev/null > /etc/motd
apt install figlet
echo "figlet -ct -C utf8 -f banner матвей лох" > /etc/profile.d/salute.sh
```

## Создание юзера

```
useradd [user_name]
usermod -aG sudo [user_name]
nano /etc/passwd # выдать 0:0 и сменить на bash
```

## Установка Screen:

```
apt install screen
```

## Установка Java 11:

```
## БАЗОВОЕ
apt install java-common

## AMAZON
wget https://corretto.aws/downloads/latest/amazon-corretto-11-x64-linux-jdk.deb
dpkg --install amazon-corretto-11-x64-linux-jdk.deb

## GRAALVM
cd /bin/
mkdir java
cd java
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.1.0/graalvm-ce-java11-linux-amd64-21.1.0.tar.gz
tar -xzf graalvm-ce-java11-linux-amd64-21.1.0.tar.gz
cp -r graalvm-ce-java11-21.1.0/. /bin/java/
export JAVA_HOME=/java/bin
rm graalvm-ce-java11-linux-amd64-21.1.0.tar.gz
rm -R graalvm-ce-java11-21.1.0
export PATH=/bin/java/bin:$PATH
export JAVA_HOME=/bin/java/bin
```

## Установка IPSET

```
apt install module-assistant
module-assistant auto-install xtables-addons-source
```

## Установка и настройка MySQL

```
apt install mariadb-server
mysql_secure_installation
mysql -u root -p

CREATE DATABASE advanced_bans;
CREATE DATABASE bauth_data;
CREATE DATABASE donate;
CREATE DATABASE donate_money;

GRANT ALL ON *.* TO 'local_user'@'localhost' IDENTIFIED BY 'password' WITH GRANT OPTION;
GRANT ALL ON bauth_data.* TO 'GwKqwDRZ5v2uS4WNI86I'@'31.31.196.189' IDENTIFIED BY '664L5LmjUq9Ud39ZHCj2' WITH GRANT OPTION;
GRANT ALL ON donate.* TO 'GwKqwDRZ5v2uS4WNI86I'@'31.31.196.189' IDENTIFIED BY '664L5LmjUq9Ud39ZHCj2' WITH GRANT OPTION;
GRANT ALL ON donate_money.* TO 'GwKqwDRZ5v2uS4WNI86I'@'31.31.196.189' IDENTIFIED BY '664L5LmjUq9Ud39ZHCj2' WITH GRANT OPTION;
```

## Настройка SSH

```
nano /etc/ssh/sshd_config
Смена порта: Разкоментировать #port = 22 и сменить на любой порт 2 в степени 16 (0-65535)
(скоро допишу остальное)
```
