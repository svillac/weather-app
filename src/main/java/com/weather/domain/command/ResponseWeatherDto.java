package com.weather.domain.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
 
@Data
@Builder
public class ResponseWeatherDto {

    @JsonProperty("dia")
    private long day;

    @JsonProperty("clima")
    private String weather;

}
