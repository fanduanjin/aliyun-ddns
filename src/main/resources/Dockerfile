FROM java:8
RUN mkdir /usr/lib/aliyun-ddns -p
WORKDIR /usr/lib/aliyun-ddns
COPY ./aliyun-ddns.jar /usr/lib/aliyun-ddns/
COPY ./aliyun-ddns.sh /usr/lib/aliyun-ddns/
RUN chmod -R 777 /usr/lib/aliyun-ddns/aliyun-ddns.sh
ENV ACCESS=阿里云access SECRET=阿里云secret DOMAIN=域名 IPV=4 CRON="*/15 * * * * *"
CMD ./aliyun-ddns.sh