package com.weather.domain.command;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherStatDto {
    @JsonProperty("periodos")
    private Map<String, Long> countWeather;
    @JsonProperty("picoMaximoLluvia")
    private String maxRaining;

}
