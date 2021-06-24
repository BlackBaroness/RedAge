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

## Установка и настройка MySQL

```
apt install -y mariadb-server
mysql_secure_installation
nano /etc/mysql/mariadb.conf.d/50-server.cnf # заменить адрес на 0.0.0.0
systemctl restart mysql
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
