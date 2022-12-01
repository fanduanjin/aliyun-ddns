package cn.fan.ddns;

import cn.hutool.Hutool;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronException;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.Scheduler;
import cn.hutool.cron.pattern.CronPattern;
import cn.hutool.cron.pattern.CronPatternUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.Setting;
import cn.hutool.system.SystemUtil;
import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.*;
import com.aliyun.teaopenapi.models.Config;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * @author fanduanjin
 * @program aliyun-ddns
 * @Classname
 * @Description
 * @Date 2022/4/10
 * @Created by fanduanjin
 */
public class BootStrap {


    public static void main(String[] args) {
        StaticLog.info("{}启动阿里云动态域名工具{}", LogTool.GREEN, LogTool.RESET);
        //获取当前执行文件所在目录
        String userDir = System.getProperty("user.dir");
        StaticLog.info("{}当前程序所在目录:{}{}", LogTool.GREEN, userDir, LogTool.RESET);
        //根据执行文件目录找配置文件
        String configFilePath = userDir + File.separatorChar + DynamicDnsConfiguration.CONFIG_FILE_NAME;
        //读取配置文件
        DynamicDnsConfiguration configuration = readConfiguration(configFilePath);

        //根据配置添加定时任务
        for (String rr : configuration.getSecondDomain()) {
            CronUtil.schedule(configuration.getCron(), new DynamicDnsTask(configuration.getAccess(),
                    configuration.getSecret(), configuration.getDomain(), rr, configuration.getTtl()));
        }
        //开启秒级定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }


    public static DynamicDnsConfiguration readConfiguration(String configFilePath) {

        if (!FileUtil.exist(configFilePath)) {
            //不存在配置文件 生成一个默认的
            DynamicDnsConfiguration configuration = new DynamicDnsConfiguration();
            configuration.setCron(DynamicDnsConfiguration.DEFAULT_CRON);
            configuration.setTtl(DynamicDnsConfiguration.DEFAULT_TTL);
            configuration.setAccess("access");
            configuration.setSecret("secret");
            configuration.setDomain("you.domain");
            configuration.setSecondDomain(new String[]{"dev-ddns"});
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(configuration), configFilePath);
            StaticLog.warn("{}已生成默认配置文件，请修改后再启动！{}", LogTool.YELLOW, LogTool.RESET);
            System.exit(1);
        }
        //读取配置文件
        String configJsonString = FileUtil.readString(configFilePath, CharsetUtil.CHARSET_UTF_8);
        //将字符串转化为pojo
        DynamicDnsConfiguration configuration = JSONUtil.toBean(configJsonString, DynamicDnsConfiguration.class);
        StaticLog.info("{}读取配置文件成功:\n{}{}", LogTool.GREEN, configJsonString, LogTool.RESET);
        return configuration;
    }

}
