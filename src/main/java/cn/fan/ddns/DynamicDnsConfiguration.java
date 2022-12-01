package cn.fan.ddns;

import lombok.Data;

/**
 * @author fanduanjin
 * @Description
 * @Date 2022/11/30
 * @Created by fanduanjin
 */
@Data
public class DynamicDnsConfiguration {

    public static final String CONFIG_FILE_NAME = "dns.conf.json";

    /**
     * 默认每15秒刷新
     */
    public static final String DEFAULT_CRON = "0/15 * * * * *";
    public static final Long DEFAULT_TTL = 600L;
    private String access;
    private String secret;
    private String domain;
    private String cron;
    private Long ttl;
    /**
     * 二级域名
     */
    private String[] secondDomain;

}
