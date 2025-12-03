package org.th;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MyTogetherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyTogetherApplication.class, args);
    }
}