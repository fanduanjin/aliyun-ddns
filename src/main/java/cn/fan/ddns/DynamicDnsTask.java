package cn.fan.ddns;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.*;
import com.sun.istack.NotNull;
import org.bouncycastle.util.IPAddress;
import com.aliyun.teaopenapi.models.Config;

import java.sql.Struct;
import java.util.Collections;
import java.util.List;

/**
 * @author fanduanjin
 * @program aliyun-ddns
 * @Classname
 * @Description
 * @Date 2022/4/10
 * @Created by fanduanjin
 */
public class DynamicDnsTask implements Runnable {

    String access;
    String secret;

    String domain;
    String rr;
    Long ttl;
    Client client;

    public DynamicDnsTask(String access, String secret, String domain, String rr, Long ttl) {
        this.access = access;
        this.secret = secret;
        this.domain = domain;
        this.rr = rr;
        this.ttl = ttl;
        Config config = new Config();
        config.setAccessKeyId(this.access);
        config.setAccessKeySecret(this.secret);
        try {
            this.client = new Client(config);
        } catch (Exception e) {
            StaticLog.error("{}创建client失败:{}{}", LogTool.READ, e.getMessage(), LogTool.RESET);
            System.exit(-1);
        }
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        /**
         * 查询当前域名下所有解析记录
         */
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> records =
                describeDomainRecords();
        if (records == null) {
            return;
        } else if (records.isEmpty()) {
            StaticLog.warn("{}警告:云解析没有配置任何记录{}", LogTool.YELLOW, LogTool.RESET);
            return;
        }

        String type;
        String ipAddressStr;
        for (DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record : records) {
            type = record.getType();
            if (type.equals("A")) {
                //IPV4
                ipAddressStr = getLocalPublicIp4();
            } else if (type.equals("AAAA")) {
                //IPV6
                ipAddressStr = getLocalPublicIp6();
            } else {
                //其他记录类型跳过不处理
                continue;
            }
            //ipAddressStr==null 获取外网ip异常 跳过
            if (ipAddressStr == null) {
                continue;
            }
            if (ipAddressStr.equals(record.value)) {
                //解析ip 和 当前公网ip一样跳过
                continue;
            }

            //解析ip和公网ip 不一样 更新
            updateDomainRecord(record.recordId, type, ipAddressStr);

        }

        StaticLog.info("{}dynamic dns end {}.{} 用时[{}ms] {}", LogTool.CYAN, rr, domain,
                System.currentTimeMillis() - start,
                LogTool.RESET);
    }

    public String getLocalPublicIp4() {
        try {
            return HttpUtil.get("4.ipw.cn");
        } catch (Exception e) {
            return null;
        }
    }

    public String getLocalPublicIp6() {
        try {
            return HttpUtil.get("6.ipw.cn");
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 获取域名下所有解析记录
     *
     * @return null 报错 is empty 云服务器没有配置
     */
    public List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> describeDomainRecords() {

        DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
        request.domainName = domain;
        request.RRKeyWord = rr;
        try {
            DescribeDomainRecordsResponse response = client.describeDomainRecords(request);
            return response.getBody().domainRecords.record;
        } catch (Exception e) {
            StaticLog.warn("{}警告:获取解析记录列表异常{}{}", LogTool.YELLOW, e.getMessage(), LogTool.RESET);
            return null;
        }
    }


    /**
     * 更新域名解析的ip地址
     *
     * @param recordId
     * @param value
     * @return
     */
    public boolean updateDomainRecord(String recordId, String type, String value) {
        UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
        updateDomainRecordRequest.recordId = recordId;
        updateDomainRecordRequest.TTL = ttl;
        updateDomainRecordRequest.type = type;
        updateDomainRecordRequest.RR = rr;
        updateDomainRecordRequest.value = value;
        try {
            this.client.updateDomainRecord(updateDomainRecordRequest);
            StaticLog.info("{}dynamic dns 已修改 {}.{} - {} {}", LogTool.CYAN, rr, domain, value, LogTool.RESET);
            return true;
        } catch (Exception e) {
            StaticLog.error("{}修改DNS解析记录失败 : {} {}", LogTool.READ, e.getMessage(), LogTool.RESET);
            return false;
        }
    }


}
