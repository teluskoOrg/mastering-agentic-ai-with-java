package com.telusko.springbootpdfrag;

import com.telusko.springbootpdfrag.service.IngestionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IngestionProperties.class)
public class SpringbootPdfRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootPdfRagApplication.class, args);
    }

}
