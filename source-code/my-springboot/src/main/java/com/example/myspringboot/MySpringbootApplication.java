package com.example.myspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MySpringbootApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(MySpringbootApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
