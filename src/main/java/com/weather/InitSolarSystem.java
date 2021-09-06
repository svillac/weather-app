package com.weather;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import com.weather.domain.use_case.planet.Motion;
import com.weather.domain.use_case.planet.Planet;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Configuration
@Log4j2
public class InitSolarSystem {

    private List<Planet> planetsInSolarSystem;

    public static final LocalDate  BEGAN_DATE_SOLAR_SYSTEM = LocalDate.now();

    public static final double INITIAL_DEGREE_IN_X = 0;

    public static String schedule = "";

    /**
     * Se agregan todos los planetas que estan en el sistema solar con sus respectivos datos
     */
    @PostConstruct
    private void postConstruct() {
        planetsInSolarSystem = List.of(
            Planet.builder()
                    .name("Vulcano")
                    .radiusInKilometers(1000)
                    .motion(Motion.ANTICLOCKWISE)
                    .angularVelocityPerDay(5)
                    .build()
                    .calculePAndF(),
            Planet.builder()
                .name("Ferengi")
                .radiusInKilometers(500)
                .motion(Motion.CLOCKWISE)
                .angularVelocityPerDay(1)
                .build()
                .calculePAndF(),
            Planet.builder()
                .name("Batasoide")
                .radiusInKilometers(2000)
                .motion(Motion.CLOCKWISE)
                .angularVelocityPerDay(3)
                .build()
                .calculePAndF());
        log.info("Init solar system: {}", this::toString);
    }

    /*@PostConstruct
    private void postConstruct() {
        planetsInSolarSystem = List.of(
            Planet.builder()
                    .name("Vulcano")
                    .radiusInKilometers(1)
                    .motion(Motion.CLOCKWISE)
                    .angularVelocityPerDay(0)
                    .build()
                    .calculePAndF(),
            Planet.builder()
                .name("Ferengi")
                .radiusInKilometers(2)
                .motion(Motion.CLOCKWISE)
                .angularVelocityPerDay(90)
                .build()
                .calculePAndF(),
            Planet.builder()
                .name("Batasoide")
                .radiusInKilometers(3)
                .motion(Motion.CLOCKWISE)
                .angularVelocityPerDay(120)
                .build()
                .calculePAndF());
        log.info("Init solar system: {}", this::toString);
    }*/
}
