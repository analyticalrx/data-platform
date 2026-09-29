package com.analyticalrx.dataplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class DataplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataplatformApplication.class, args);
	}

}
