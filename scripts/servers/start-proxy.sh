#!/bin/bash

apt install -y screen ipset

if ! screen -S proxy -X select . > /dev/null;
then screen -dmS proxy java -jar InsaneProxy.jar; fi

exit 0;