package com.weather.domain.model.planet;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartesianPoint {

    private String name;

    private double x;

    private double y;

}
