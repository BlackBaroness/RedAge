#!/bin/bash

# установка базового комбонента
apt install -y java-common

# удаление папки Java (на случай, если установка не чистая)
rm -R ~/bin/java

# создание папки
mkdir -p ~/bin/ && cd ~/bin/ && mkdir -p java && cd java || exit 1;

# скачивание пакета GraalVM 11
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.1.0/graalvm-ce-java11-linux-amd64-21.1.0.tar.gz

# распаковка
tar -xzf graalvm-ce-java11-linux-amd64-21.1.0.tar.gz

# копирование содержимого в папку
cp -r graalvm-ce-java11-21.1.0/. ~/bin/java/

# удаление пакета
rm graalvm-ce-java11-linux-amd64-21.1.0.tar.gz

# удаление ненужной папки
rm -R graalvm-ce-java11-21.1.0

# установка переменных в runtime
export PATH=~/bin/java/bin:$PATH
export JAVA_HOME=~/bin/java/bin

# установка переменных в .bash_profile
echo "export PATH=~/bin/java/bin:$PATH" >> .bash_profile
echo "export JAVA_HOME=~/bin/java/bin" >> .bash_profile

# аплодисменты
java -version

exit 0;