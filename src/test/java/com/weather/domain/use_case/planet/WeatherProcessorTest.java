package com.weather.domain.use_case.planet;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.weather.InitSolarSystem;
import com.weather.domain.model.planet.GeometricWeather;
import com.weather.infraestructure.repository.GeometricWeatherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WeatherProcessorTest {

    @Mock
    private GeometricWeatherRepository mockRepository;

    @Mock
    private InitSolarSystem mockInitSolarSystem;

    @InjectMocks
    private WeatherProcessor weatherProcessorUnderTest;

    @Test
    void given360DaysInTheFutureAndOnlySpecificDateFalseTheCalculeAllDaysSinceInitOfTimes() {
        //given
        given(mockRepository.findByDate(any())).willReturn(Flux.empty());
        given(mockInitSolarSystem.getPlanetsInSolarSystem()).willReturn(getPlanets());
        given(mockRepository.save(any())).willReturn(getGeometricWeather());
        //when
        StepVerifier.create(weatherProcessorUnderTest.movePlanetsInTheTime(0,0,3,
            Optional.empty(), true))
            .assertNext(it -> {
                //then
                assertThat(it).isNotNull();
                assertThat(it.getId()).isEqualTo("id");
                then(this.mockRepository).should().save(any());
                then(this.mockRepository).should().findByDate(any());
            })
            .expectComplete()
            .verify();
    }

    @Test
    void given25DaysInTheFutureAndOnlySpecificDateFalseTheCalculeAllDaysSinceInitOfTimes() {
        given(mockRepository.findByDate(any())).willReturn(Flux.empty());
        given(mockInitSolarSystem.getPlanetsInSolarSystem()).willReturn(getPlanets());
        given(mockRepository.save(any())).willReturn(getGeometricWeather());
        StepVerifier.create(weatherProcessorUnderTest.movePlanetsInTheTime(0,0,25,
            Optional.empty(), false))
            .expectNextCount(25)
            .verifyComplete();
        then(this.mockRepository).should(times(25)).save(any());
        then(this.mockRepository).should(times(25)).findByDate(any());
    }

    private Mono<GeometricWeather> getGeometricWeather(){
        GeometricWeather geometricWeather = GeometricWeather.builder().id("id").build();
        return Mono.just(geometricWeather);
    }

    private List<Planet> getPlanets(){
        return List.of(
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
    }

}
