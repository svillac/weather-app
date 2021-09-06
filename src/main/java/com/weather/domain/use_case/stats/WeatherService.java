package com.weather.domain.use_case.stats;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.weather.InitSolarSystem;
import com.weather.domain.command.ResponseWeatherDto;
import com.weather.domain.command.WeatherStatDto;
import com.weather.domain.model.planet.GeometricWeather;
import com.weather.domain.use_case.planet.WeatherProcessor;
import com.weather.domain.use_case.planet.WeatherType;
import com.weather.infraestructure.repository.GeometricWeatherRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.util.function.Tuple5;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherProcessor weatherProcessor;

    private final GeometricWeatherRepository repository;

    public Flux<GeometricWeather> getAllGeometricWeather(){
        return repository.findAll();
    }

    public ParallelFlux<ResponseWeatherDto> getWeatherOfDay(long numDays){
        return weatherProcessor.movePlanetsInTheTime(0,0,numDays,
            Optional.of(LocalDate.now()), true)
            .map(e -> ResponseWeatherDto.builder()
                        .day(numDays)
                        .weather(e.getWeather())
                        .build());
    }

    /**
     * Retorna las estadisticas de un planeta desde el un dia determinado hasta otro
     * dia en cuestion
     * @return
     */
    public Mono<WeatherStatDto> responseAnswer(final long since,final long until) {
        final Optional<LocalDate> sinceDate = Optional.of(InitSolarSystem.BEGAN_DATE_SOLAR_SYSTEM
            .plusDays(since));
        final Optional<LocalDate> untilDate = Optional.of(InitSolarSystem.BEGAN_DATE_SOLAR_SYSTEM
            .plusDays(until));


        final Flux<GeometricWeather> rainyList = repository.findByWeather(WeatherType.RAINY.toString());

        weatherProcessor.movePlanetsInTheTime(0,0,until, sinceDate, false).subscribe();
        Mono<Long> droughtCount = getCountWeather(sinceDate.get(), untilDate.get(), WeatherType.DROUGHT);
        Mono<Long> optimumCount = getCountWeather(sinceDate.get(), untilDate.get(), WeatherType.OPTIMUM);
        Mono<Long> rainyCount = getCountWeather(sinceDate.get(), untilDate.get(), WeatherType.RAINY);
        Mono<Long> wrongCount = getCountWeather(sinceDate.get(), untilDate.get(), WeatherType.WRONG);
        Mono<String> maxPerimeter = getMaxPerimeter(sinceDate.get(), untilDate.get(), rainyList);

        return Mono.zip(droughtCount, optimumCount, rainyCount, wrongCount, maxPerimeter)
            .flatMap(this::getWeatherStatDto);
    }

    private Mono<Long> getCountWeather(final LocalDate since,
                                        final LocalDate until,
                                        final WeatherType weather){
        return repository.findByWeather(weather.toString())
            .filter(e -> e.getDate().isAfter(since.minusDays(1)) && e.getDate().isBefore(until.plusDays(1)))
            .count();
    }

    private Mono<String> getMaxPerimeter(final LocalDate since,
                                         final LocalDate until,
                                         Flux<GeometricWeather> rainyList) {
        return rainyList
            .filter(e -> e.getDate().isAfter(since.minusDays(1)) && e.getDate().isBefore(until.plusDays(1)))
            .sort(Comparator.comparing(GeometricWeather::getPerimeter))
            .last()
            .map(e -> e.getDate().toString());
    }

    private Mono<WeatherStatDto> getWeatherStatDto(Tuple5<Long, Long, Long, Long, String> t) {
        final Map<String, Long> countWeather = Map.ofEntries(
            Map.entry("drought", t.getT1()),
            Map.entry("optimum", t.getT2()),
            Map.entry("rainy", t.getT3()),
            Map.entry("wrong", t.getT4()));

        return Mono.just(WeatherStatDto.builder().maxRaining(t.getT5()).countWeather(countWeather).build());
    }
}
