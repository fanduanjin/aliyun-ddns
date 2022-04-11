package cn.fan.ddns;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.aliyun.alidns20150109.models.*;
import org.bouncycastle.util.IPAddress;

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
public class DdnsTask implements Runnable {
    private String localIp;
    private static final String MSG_RECORD_TEMPLATE = "记录类型[{}]  主机记录[{}]  记录值[{}]";

    @Override
    public void run() {
        StaticLog.info(BootStrap.MSG_TEMPLATE, "原主机ip地址 : " + localIp);
        String nowIp = BootStrap.getPublicIp();
        StaticLog.info(BootStrap.MSG_TEMPLATE, "现在主机ip地址 : " + nowIp);
        boolean isEquals = localIp != null && localIp.equals(nowIp);
        localIp = nowIp;
        if (isEquals) {
            StaticLog.info(BootStrap.MSG_TEMPLATE, "原主机IP地址与现在主机IP地址一样 不需要更新IP");
            return;
        }
        //IP不一样 更新域名解析
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> records =
                describeDomainRecords();
        if (records == null || records.isEmpty()) {
            StaticLog.warn(BootStrap.MSG_TEMPLATE, "警告 没有获取到解析记录，或者云平台没有配置任何解析记录");
            return;
        }

        for (DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record : records) {
            String value = record.getValue();
            if (value.equals(localIp)) {
                return;
            }
            String recordId = record.getRecordId();
            String rr = record.getRR();
            updateDomainRecord(recordId, rr, localIp);
        }

    }

    List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> describeDomainRecords() {

        DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
        request.domainName = BootStrap.domain;
        request.type = BootStrap.recordType;
        try {
            DescribeDomainRecordsResponse response = BootStrap.client.describeDomainRecords(request);
            return response.getBody().domainRecords.record;
        } catch (Exception e) {
            StaticLog.warn(BootStrap.MSG_TEMPLATE, "获取阿里DNS 绑定信息失败 " + e.getMessage());
            return null;
        }
    }

    boolean updateDomainRecord(String recordId, String rr, String value) {
        UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
        updateDomainRecordRequest.recordId = recordId;
        updateDomainRecordRequest.RR = rr;
        updateDomainRecordRequest.type = BootStrap.recordType;
        updateDomainRecordRequest.value = value;
        try {
            BootStrap.client.updateDomainRecord(updateDomainRecordRequest);
            StaticLog.info(BootStrap.MSG_TEMPLATE, "修改DNS解析成功" + StrUtil.format(MSG_RECORD_TEMPLATE,
                    BootStrap.recordType
                    , rr,
                    value));
            return true;
        } catch (Exception e) {
            StaticLog.error(BootStrap.MSG_TEMPLATE, StrUtil.format(MSG_RECORD_TEMPLATE, BootStrap.recordType, rr,
                    value));
            StaticLog.error(BootStrap.MSG_TEMPLATE, "修改DNS解析记录失败 : " + e.getMessage());
            return false;
        }
    }

}
