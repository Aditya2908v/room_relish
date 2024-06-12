package org.example.roomrelish;

import org.jetbrains.annotations.TestOnly;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class CardDetailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDetailsApplication.class, args);
    }

}
