package com.trade;

import com.trade.bootstrap.AuthBootstrapCommand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

/**
 * 认证服务入口，通过显式命令选择一次性管理员初始化任务。
 */
@SpringBootApplication
@MapperScan("com.trade.dao")
public class TradeFlowAuthApplication {

    public static void main(String[] args) {

        DefaultApplicationArguments arguments = new DefaultApplicationArguments(args);
        // --trade.auth.command=bootstrap-admin 初始化超级管理员用户使用的
        List<String> commands = arguments.getOptionValues("trade.auth.command");

        if (commands == null) {
            SpringApplication.run(TradeFlowAuthApplication.class, args);
            return;
        }

        if (commands.size() != 1 || !"bootstrap-admin".equals(commands.get(0))) {
            throw new IllegalArgumentException("trade.auth.command 仅支持 bootstrap-admin，普通服务启动时请省略该参数");
        }

        int exitCode = AuthBootstrapCommand.execute(args);
        System.exit(exitCode);
    }
}