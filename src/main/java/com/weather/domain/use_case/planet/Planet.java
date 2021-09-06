package com.weather.domain.use_case.planet;


import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Planet {

    private String name;

    private double radiusInKilometers;

    private double radiusInMeters;

    private Motion motion;

    private double angularVelocityPerDay;

    private double angularVelocityPerSecond;

    private double period;

    private double frequency;

    private double tangentialVelocity;

    private double circlePerimeter;

    public Planet calculePAndF(){
        angularVelocityPerSecond = angularVelocityPerDay/86400;
        period = 360/ angularVelocityPerSecond;//Segundos
        radiusInMeters = radiusInKilometers * 1000;
        tangentialVelocity = (2*Math.PI* radiusInMeters)/period;
        circlePerimeter = 2*Math.PI*radiusInMeters;
        return this;
    }

}
