package com.weather.domain.model.planet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Document
@Builder
public class GeometricWeather {

    @Id
    private String id;

    private LocalDate date;

    private List<CartesianPoint> listPoints;

    private Set<Line> lines;

    private Double perimeter;

    private String weather;

}
