## Сборка:

- Во всех проектах прописать переменную desktop под название рабочего стола
- Собирать через ALL проект: clean install package

## Настройка окружения:

```
apt update
apt upgrade
apt install screen
apt install java-common
wget https://corretto.aws/downloads/latest/amazon-corretto-11-x64-linux-jdk.deb
dpkg --install amazon-corretto-11-x64-linux-jdk.deb
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