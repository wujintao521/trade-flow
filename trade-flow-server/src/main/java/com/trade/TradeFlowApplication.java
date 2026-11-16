package com.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.trade.dao")
public class TradeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeFlowApplication.class, args);
    }
}
