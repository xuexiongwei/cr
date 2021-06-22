package com.example.copyresult;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CopyResultApplication {

    public static void main(String[] args) {
        SpringApplication.run(CopyResultApplication.class, args);
    }

}
