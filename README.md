# aliyun-ddns
## 利用阿里云API实现DDNS功能 __需要安装Docker__
> 1. 实现动态域名解析
> 2. 支持IPv4和IPv6
> 3. 支持多记录
## 使用教程
### 宿主机中创建配置文件 /root/aliyun-ddns/dns.conf.json
#### 配置文件模板 解析
> 以下配置解析 dev-ddns.you.domain 域名
```json
{
  "cron": "0/15 * * * * *",
  "access": "access",
  "secret": "secret",
  "ttl": 600,
  "domain": "you.domain",
  "secondDomain": [
    "dev-ddns"
  ]
}
```

### 运行Docker容器

```bash
docker run --name=aliyun-ddns --net=host -v /root/aliyun-ddns/dns.conf.json:/usr/local/aliyun-ddns/dns.conf.json --restart=always -d fanduanjin/aliyun-ddns
```
#### 参数说明
> --name 指定容器名称
> --net=host 指定容器网络模式为host模式
> -v 指定配置文件挂载路径
> --restart=always 指定容器重启策略为始终重启
> -d 指定容器运行在后台模式