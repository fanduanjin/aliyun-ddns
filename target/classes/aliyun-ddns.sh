#!/bin/bash
echo "$CRON"
java -jar aliyun-ddns.jar access=$ACCESS secret=$SECRET domain=$DOMAIN ipv=$IPV cron="$CRON"