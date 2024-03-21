FROM openjdk:8

WORKDIR /usr/local/aliyun-ddns
ADD target/aliyun-ddns-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/aliyun-ddns/aliyun-ddns.jar

ENTRYPOINT ["java","-jar","/usr/local/aliyun-ddns/aliyun-ddns.jar"]