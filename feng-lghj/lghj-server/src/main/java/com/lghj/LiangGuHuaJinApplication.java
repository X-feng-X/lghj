package com.lghj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling // 开启定时任务
@EnableTransactionManagement // 开启注解方式的事务管理
@EnableAspectJAutoProxy(exposeProxy = true) // 暴露代理对象
@SpringBootApplication
@MapperScan("com.lghj.mapper")
public class LiangGuHuaJinApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiangGuHuaJinApplication.class, args);
    }
}
