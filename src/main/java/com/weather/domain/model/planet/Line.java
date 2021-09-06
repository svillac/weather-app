package com.weather.domain.model.planet;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.HashCodeExclude;

/**
 * Estos son los atributos para la ecuacion de la recta dada por
 * f(x) = mx + b
 */
@Data
@Builder
@EqualsAndHashCode
public class Line {

    private double m;

    private double a;

    private double b;

    private double c;

}
