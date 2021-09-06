package com.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.modelmapper.ModelMapper;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableScheduling
@EnableSwagger2
public class WeatherAppApplication {

	public static void main(String[] args) { SpringApplication.run(WeatherAppApplication.class, args); }

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
