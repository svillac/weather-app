package com.weather.infraestructure.repository;

import java.time.LocalDate;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.weather.domain.model.planet.GeometricWeather;
import reactor.core.publisher.Flux;

public interface GeometricWeatherRepository extends ReactiveMongoRepository<GeometricWeather, String> {

    Flux<GeometricWeather> findByDate(LocalDate date);

    Flux<GeometricWeather> findByWeather(String weather);

}
