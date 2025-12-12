package com.mercadolocalia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.mercadolocalia")
@SpringBootApplication
public class MercadolocaliaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MercadolocaliaApplication.class, args);
	}

}
