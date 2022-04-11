#!/bin/bash
echo "$CRON"

*/15 * * * * *
java -jar aliyun-ddns.jar access=$ACCESS secret=$SECRET domain=$DOMAIN ipv=$IPV cron="$CRON"