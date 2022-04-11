package cn.fan.ddns;

import cn.hutool.Hutool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronException;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.pattern.CronPattern;
import cn.hutool.cron.pattern.CronPatternUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponse;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import com.aliyun.teaopenapi.models.Config;

import java.util.List;


/**
 * @author fanduanjin
 * @program aliyun-ddns
 * @Classname
 * @Description
 * @Date 2022/4/10
 * @Created by fanduanjin
 */
public class BootStrap {

    public static final String MSG_TEMPLATE = "---------------{}\n";
    /**
     * 定时任务表达式 15秒
     */
    private static String cronStr;

    private static int ipVersion = 4;
    /**
     * 域名
     */
    public static String domain;
    /**
     * 记录类型
     */
    public static String recordType;
    private static String CONFIG_TEMPLATE = "---------------配置参数: access={} secret={} domain={} ipv={}";
    public static Client client;
    private static Config config;


    public static void main(String[] args) {
        StaticLog.info(MSG_TEMPLATE, "启动阿里云动态域名");
        StaticLog.info(MSG_TEMPLATE, "运行方式 如 : java -jar aliyun-ddns.jar access=阿里云access secret=阿里云secret domain=域名");
        StaticLog.info(MSG_TEMPLATE);
        initArgs(args);
        CronUtil.schedule(cronStr, new DdnsTask());
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
        StaticLog.info(MSG_TEMPLATE, "域名工具启动成功 -_- " + cronStr);
    }


    static void initArgs(String[] args) {
        config = new Config();
        for (String arg : args) {
            String[] argArr = arg.split("=");
            switch (argArr[0]) {
                case "access":
                    config.accessKeyId = argArr[1];
                    break;
                case "secret":
                    config.accessKeySecret = argArr[1];
                    break;
                case "domain":
                    domain = argArr[1];
                    break;
                case "ipv":
                    ipVersion = Integer.valueOf(argArr[1]);
                    break;
                case "cron":
                    cronStr = argArr[1];
                    break;
                default:
                    break;
            }
        }

        volatileParam();
        StaticLog.info(CONFIG_TEMPLATE, config.accessKeyId, config.accessKeySecret, domain, ipVersion);
        StaticLog.info(MSG_TEMPLATE, "记录类型 : " + recordType);
        try {
            client = new Client(config);
        } catch (Exception e) {
            StaticLog.error(MSG_TEMPLATE, "初始化SDK客户端出错:" + e.getMessage());
            System.exit(0);
        }
    }

    static void volatileParam() {
        if (StrUtil.isEmpty(config.accessKeyId)) {
            StaticLog.error(MSG_TEMPLATE, "参数 access 是必须的");
            System.exit(0);
        }
        if (StrUtil.isEmpty(config.accessKeySecret)) {
            StaticLog.error(MSG_TEMPLATE, "参数 secret 是必须的");
            System.exit(0);
        }
        if (StrUtil.isEmpty(domain)) {
            StaticLog.error(MSG_TEMPLATE, "参数 domain 是必须的");
            System.exit(0);
        }
        if (ipVersion != 4 && ipVersion != 6) {
            StaticLog.error(MSG_TEMPLATE, "参数 ipv 只能是 4或6 是必须的");
            System.exit(0);
        }
        if (4 == ipVersion) {
            recordType = "A";
        }
        if (6 == ipVersion) {
            recordType = "AAAA";
        }
        if (StrUtil.isEmpty(cronStr)) {
            cronStr = "*/15 * * * * *";
            StaticLog.warn(MSG_TEMPLATE, "未配置参数 cron 启动默认 : " + cronStr);
        }
        StaticLog.warn("cron 没有做格式验证");


    }

    static String getPublicIp() {
        String publicIp = null;
        switch (ipVersion) {
            case 4:
                publicIp = HttpUtil.get("http://ip.3322.net");
                StaticLog.info(MSG_TEMPLATE, "获取 4代IP : ");
                break;
            case 6:
                publicIp = HttpUtil.get("https://ipv6.ipw.cn/api/ip/myip");
                StaticLog.info(MSG_TEMPLATE, "获取 6代IP : ");
                break;
            default:
        }
        return publicIp;
    }


}
