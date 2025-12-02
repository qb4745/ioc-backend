package com.cambiaso.ioc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.cambiaso.ioc.persistence.entity")
@EnableJpaRepositories("com.cambiaso.ioc.persistence.repository")
@EnableScheduling
public class IocbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(IocbackendApplication.class, args);
	}

}
