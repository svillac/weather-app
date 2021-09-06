package com.weather.presentation.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weather.domain.command.ResponseWeatherDto;
import com.weather.domain.command.WeatherStatDto;
import com.weather.domain.model.planet.GeometricWeather;
import com.weather.domain.use_case.stats.WeatherService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @ApiOperation(value = "Retornar informacion climatica", notes = "Devuelve toda la informacion almacenada en "
        + "base de datos, contiene la informacion almacenada por fechas y la informacion de las lineas rectas "
        + "asociada a esta")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Climas obtenidos satisfactoriamente", response = GeometricWeather.class,
            responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping("/traerTodosLosClimasGuardados")
    private Flux<GeometricWeather> getAllWeatherStored(){
        return weatherService.getAllGeometricWeather();
    }

    @ApiOperation(value = "Responde preguntas", notes = " Responde las preguntas planteadas en el ejercicio "
        + "cuantos periodos de sequia habra? cuantos periodos de lluvia habra ? cual es el pico de lluvia ?")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Preguntas obtenidas satisfactoriamente", response = WeatherStatDto.class),
        @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping("/responderPreguntas")
    private ResponseEntity<Mono<WeatherStatDto>> responseAnswer(@RequestParam long desde,
                                                                @RequestParam long hasta){
        return Optional.of(desde > hasta)
            .filter(e -> !e)
            .map(e -> ResponseEntity.ok(weatherService.responseAnswer(desde, hasta)))
            .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    @ApiOperation(value = "Obtiene el clima por dia", notes = "Dato un dia en el query param este endpoint "
        + "debe retornar el estado del clima para el dia puntual")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Clima obtenido satisfactoriamente", response = ResponseWeatherDto.class,
            responseContainer = "List")
    })
    @GetMapping("/clima")
    private ParallelFlux<ResponseWeatherDto> getWeatherOfDay(@RequestParam long dia){
        return weatherService.getWeatherOfDay(dia);
    }


}
