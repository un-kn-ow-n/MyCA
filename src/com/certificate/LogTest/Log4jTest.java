package com.certificate.LogTest;

import org.apache.log4j.Logger;

public class Log4jTest {
    public static void main(String[] args) {
        //创建记录日志的对象
        Logger log = Logger.getLogger(Log4jTest.class);

        //下面语句会根据log4j.properties中的日志级别输出
        log.debug(" debug信息");
        log.info(" info信息");
        log.warn(" warn信息");
        log.error(" error信息");
        log.fatal(" fatal信息");
    }
}

