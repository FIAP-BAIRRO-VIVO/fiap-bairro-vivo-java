package br.com.bairrovivo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BairroVivoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BairroVivoApplication.class, args);
    }
}
