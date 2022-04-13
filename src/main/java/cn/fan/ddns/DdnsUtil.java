package cn.fan.ddns;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.aliyun.alidns20150109.models.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author fanduanjin
 * @program aliyun-ddns
 * @Classname
 * @Description
 * @Date 2022/4/13
 * @Created by fanduanjin
 */
public class DdnsUtil {
    private static final String MSG_RECORD_TEMPLATE = "记录类型[{}]  主机记录[{}]  记录值[{}]  ttl[{}]";


    /**
     * 获取域名解析记录
     *
     * @return
     */
    public static List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> describeDomainRecords() {

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


    /**
     * 更新域名解析的ip地址
     *
     * @param recordId
     * @param rr
     * @param value
     * @return
     */
    public static boolean updateDomainRecord(String recordId, String rr, String value,long ttl) {
        UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
        updateDomainRecordRequest.recordId = recordId;
        updateDomainRecordRequest.RR = rr;
        updateDomainRecordRequest.type = BootStrap.recordType;
        updateDomainRecordRequest.TTL=ttl;
        updateDomainRecordRequest.value = value;
        try {
            BootStrap.client.updateDomainRecord(updateDomainRecordRequest);
            StaticLog.info(BootStrap.MSG_TEMPLATE, "修改DNS解析成功" + StrUtil.format(MSG_RECORD_TEMPLATE,
                    BootStrap.recordType
                    , rr,
                    value,ttl));
            return true;
        } catch (Exception e) {
            StaticLog.error(BootStrap.MSG_TEMPLATE, StrUtil.format(MSG_RECORD_TEMPLATE, BootStrap.recordType, rr,
                    value));
            StaticLog.error(BootStrap.MSG_TEMPLATE, "修改DNS解析记录失败 : " + e.getMessage());
            return false;
        }
    }

    private static long getInstanceTtl(String instanceId) {
        DescribeDnsProductInstanceRequest request = new DescribeDnsProductInstanceRequest();
        request.instanceId = instanceId;
        try {

            DescribeDnsProductInstanceResponse response = BootStrap.client.describeDnsProductInstance(request);
            return response.body.TTLMinValue;
        } catch (Exception e) {
            StaticLog.error( BootStrap.MSG_TEMPLATE,e.getMessage());
            return -1;
        }
    }

    private static String getInstanceId(){
        DescribeDomainInfoRequest request=new DescribeDomainInfoRequest();
        request.domainName=BootStrap.domain;
        try {
            DescribeDomainInfoResponse response=BootStrap.client.describeDomainInfo(request);
            return response.getBody().instanceId;
        } catch (Exception e) {
            StaticLog.error(BootStrap.MSG_TEMPLATE,e.getMessage());
            return null;
        }
    }

    /**
     * 秒
     * 免费版      [600 - 86400]
     * 个人版      [600 - 86400]
     * 企业标准版   [60 - 86400]
     * 企业旗舰版   [1 - 86400]
     * @return
     */
    public static long getInstanceTtl(){
        String instanceId=getInstanceId();
        if(instanceId==null){
            //等于NULL 是免费版 或者是请求失败了
            return 600;
        }else{
            //阿里云接口有问题，获取不了最小值
            //return getInstanceTtl(instanceId);
            return 60;
        }
    }


}
