package cn.fan.ddns;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.aliyun.alidns20150109.models.*;
import org.bouncycastle.util.IPAddress;

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
public class DdnsTask implements Runnable {

    @Override
    public void run() {
        String localIp = BootStrap.getPublicIp();
        if (StrUtil.isEmpty(localIp)) {
            StaticLog.warn(BootStrap.MSG_TEMPLATE, "警告 没有获取到本地ip");
            return;
        }
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> records =
                DdnsUtil.describeDomainRecords();
        if (records == null || records.isEmpty()) {
            StaticLog.warn(BootStrap.MSG_TEMPLATE, "警告 没有获取到解析记录，或者云平台没有配置任何解析记录");
            return;
        }
        long ttl = -2;
        for (DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record : records) {
            String value = record.getValue();
            if (value.equals(localIp)) {
                return;
            }
            if (ttl == -2) {
                //ttl等于-2说明还没有获取过ttl 只获取一边就可以了
                ttl = DdnsUtil.getInstanceTtl();
            } else if (ttl == -1) {
                //ttl等于-1 没有获取成功
                return;
            }
            String recordId = record.getRecordId();
            String rr = record.getRR();
            DdnsUtil.updateDomainRecord(recordId, rr, localIp, ttl);
        }

    }


}
