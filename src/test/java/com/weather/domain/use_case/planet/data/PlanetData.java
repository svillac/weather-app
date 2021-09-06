package com.weather.domain.use_case.planet.data;

import java.util.List;

import com.weather.domain.use_case.planet.Motion;
import com.weather.domain.use_case.planet.Planet;

public class PlanetData {
    static List<Planet> getPlanets(){
        return List.of(
            Planet.builder()
                .name("Vulcano")
                .radiusInKilometers(1000)
                .motion(Motion.ANTICLOCKWISE)
                .angularVelocityPerDay(5)
                .build(),
            Planet.builder()
                .name("Ferengi")
                .radiusInKilometers(500)
                .motion(Motion.CLOCKWISE)
                .angularVelocityPerDay(1)
                .build(),
            Planet.builder()
                .name("Batasoide")
                .radiusInKilometers(2000)
                .motion(Motion.CLOCKWISE)
                .angularVelocityPerDay(3)
                .build());
    }
}
