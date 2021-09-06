package com.weather.domain.use_case.planet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.weather.InitSolarSystem;
import com.weather.domain.model.planet.GeometricWeather;
import com.weather.domain.model.planet.CartesianPoint;
import com.weather.domain.model.planet.Line;
import com.weather.infraestructure.repository.GeometricWeatherRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

@Service
@RequiredArgsConstructor
@Log4j2
public class WeatherProcessor {

    private final GeometricWeatherRepository repository;

    private final InitSolarSystem initSolarSystem;

    private static final int DEGREES = 360;

    private static final int SECONDS_IN_DAY = 86400;

    private static final long YEARS_SCHEDULE = 10;

    private static final long MOUNT_SCHEDULE = 0;

    private static final long DAYS_SCHEDULE = 0;

    private static final int PLACES_DECIMAL = 7;

    //Se ejecuta cada 15 dias
    @Scheduled(cron = "0 0 0 */15 * ?")
    void job(){
        movePlanetsInTheTime(YEARS_SCHEDULE, MOUNT_SCHEDULE, DAYS_SCHEDULE, Optional.empty(), false).subscribe();
    }


    public ParallelFlux<GeometricWeather> movePlanetsInTheTime(final @NonNull long years,
                                                        final @NonNull long mount,
                                                        final @NonNull long days,
                                                        Optional<LocalDate> since,
                                                        Boolean onlySpecificDate){

        LocalDate newDate = InitSolarSystem.BEGAN_DATE_SOLAR_SYSTEM
                .plusYears(years)
                .plusMonths(mount)
                .plusDays(days);

        LocalDate initDate = since.orElse(InitSolarSystem.BEGAN_DATE_SOLAR_SYSTEM);

        final int distanceBetweenBeganDateAndSince = (int) InitSolarSystem.BEGAN_DATE_SOLAR_SYSTEM.until( initDate, ChronoUnit.DAYS );
        final int distanceBetweenGapDates = (int) initDate.until( newDate, ChronoUnit.DAYS );
        final int start = onlySpecificDate ?   distanceBetweenGapDates : distanceBetweenBeganDateAndSince;
        final int durationGap = onlySpecificDate ? 1 : distanceBetweenGapDates;

        return Flux.range(start, durationGap)
                .parallel()
                .map(initDate::plusDays)
                .flatMap(this::findOrCalculate);

    }

    /**
     * Busca en la base de datos o calcula el dato especificado
     */
    private Mono<GeometricWeather> findOrCalculate(LocalDate e) {

        return repository.findByDate(e)
            .singleOrEmpty()
            .switchIfEmpty(this.calculusOfWeather(e));

    }

    /**
     * Calcula el estado del tiempo para una fecha nueva
     */
    private Mono<GeometricWeather> calculusOfWeather(LocalDate newDate){
        final List<CartesianPoint> listPoints = initSolarSystem.getPlanetsInSolarSystem()
            .stream()
            .parallel()
            .map(planet -> getCartesianPoints(planet, newDate))
            .collect(Collectors.toList());
        final Set<Line> listLines = getAllLines(listPoints.get(0), listPoints.get(1), listPoints.get(2));

        final GeometricWeather response = GeometricWeather.builder()
            .lines(listLines)
            .date(newDate)
            .listPoints(listPoints)
            .build();

        final GeometricWeather result = Optional.of(allLinesAreParallel(listLines))
            .filter(e -> e)
            .map(e -> planetsInLine(listLines, response))
            .orElseGet(() -> planetsInTriangle(listPoints, response));
        return repository.save(result);
    }

    /**
     * Valida que las pendientes de las rectas de las ecuaciones
     * sean paralelas
     */
    private boolean allLinesAreParallel(final Set<Line> lines){
        final long countGradients = lines.stream()
                            .map(Line::getM)
                            .distinct()
                            .count();
        return countGradients == 1;
    }


    /**
     *Se ejecuta cuando sabemos que los tres planetas forman un
     * traingulo
     */
    private GeometricWeather planetsInTriangle(List<CartesianPoint> listPoints,
        GeometricWeather response) {
        final Boolean isSunInside = isInside(listPoints.get(0),
                                             listPoints.get(1),
                                             listPoints.get(2),
                                             CartesianPoint.builder().x(0.0).y(0.0).build());
        final double trianglePerimeter = getTrianglePerimeter(listPoints.get(0),
                                                              listPoints.get(1),
                                                              listPoints.get(2));
        log.trace("The universe form a triangle");
        log.trace("Sun is inside of triangle {} ", isSunInside);
        response.setPerimeter(trianglePerimeter);
        return Optional.of(isSunInside)
                .filter(e -> e)
                .map(e -> {
                    response.setWeather(WeatherType.RAINY.toString());
                    return response;
                })
                .orElseGet(() -> {
                    response.setWeather(WeatherType.WRONG.toString());
                    return response;
                });
    }

    /**
     * Se ejecuta cuando las lineas rectas que forman los planetas estan
     * totalmente paralelas
     */
    private GeometricWeather planetsInLine(Set<Line> listLines,
        GeometricWeather response) {
        log.trace("The universe form a line");
        response.setPerimeter(0.0);
        return listLines.stream().findFirst()
                    .map(line -> line.getC() == 0)
                    .filter(e -> e)
                    .map(e -> {
                        response.setWeather(WeatherType.DROUGHT.toString());
                        return response;
                    })
                    .orElseGet(() -> {
                        response.setWeather(WeatherType.OPTIMUM.toString());
                        return response;
                    });
    }

    /**
     * Genera la ecuacion de la linea recta partiendo de dos puntos
     */
    private Line getLineEquation(final CartesianPoint p, final CartesianPoint q){

        double m =  roundAvoid((q.getY()-p.getY())/(q.getX()-p.getX()), 1);
        final double c =  roundAvoid( p.getY() - m*p.getX(), 1);
        log.trace("Equation calculated to p: {} q: {} f(x)={}x+{}", p.toString(), q.toString(), m, c);
        return Line.builder().m(m).c(c).build();
    }

    /**
     * Trae una lista de lineas con las combinaciones de todos los puntos
     */
    private Set<Line> getAllLines(final CartesianPoint p1,
                                  final CartesianPoint p2,
                                  final CartesianPoint p3){
        Set<Line> straightList = new HashSet<>();
        straightList.add(getLineEquation(p1,p2));
        straightList.add(getLineEquation(p2,p3));
        straightList.add(getLineEquation(p1,p3));
        return straightList;
    }

    /**
     * Devuelve los componentes X y Y de un planeta en el plano cartesiano
     */
    private CartesianPoint getCartesianPoints(Planet planet, LocalDate newDate){
        long days = InitSolarSystem.BEGAN_DATE_SOLAR_SYSTEM.until( newDate, ChronoUnit.DAYS );
        final Double angleXInDegree = getLocationPlanet(planet, days);

        log.trace("Planet {} with angle mov {} ", planet.getName(), angleXInDegree);
        return CartesianPoint.builder()
            .name(planet.getName())
            .x(roundAvoid(Math.cos(Math.toRadians(angleXInDegree))*planet.getRadiusInKilometers(), PLACES_DECIMAL))
            .y(roundAvoid(Math.sin(Math.toRadians(angleXInDegree))*planet.getRadiusInKilometers(), PLACES_DECIMAL))
            .build();
    }

    /**
     * Redondea un double
     */
    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    /**
     * Retorna un double que es la cantidad de grados a los cuales un planeta se encuentra del
     * eje X
     **/
    private Double getLocationPlanet(@NonNull Planet planet, @NonNull long days){
        log.trace("Get location planet {} at the date {}", planet.getName(), days);
        final Double degreesToMove = getDegreesToMove(planet, days);
        log.trace("the planet is moved {}", degreesToMove);
        return Optional.of(degreesToMove)
                .map(mov -> movePlanet(planet, mov))
                .orElseThrow();
    }

    /**
     * Controla en sentido en el cual se mueve el planeta
     */
    private Double movePlanet(final Planet planet, final Double mov) {
        return planet.getMotion() == Motion.ANTICLOCKWISE ?
                InitSolarSystem.INITIAL_DEGREE_IN_X + mov :
                (InitSolarSystem.INITIAL_DEGREE_IN_X + mov) * -1 ;
    }

    /**
     * retorna los grados que se mueve un planeta
     */
    private Double getDegreesToMove(final Planet planet, final long duration) {
        final double distanceRunner = planet.getTangentialVelocity() * (duration*SECONDS_IN_DAY);
        final BigDecimal spinDoIt = new BigDecimal(distanceRunner/planet.getCirclePerimeter());
        log.trace("planet had runner {} m in {} days and {} spins", distanceRunner, duration, spinDoIt.doubleValue());
        final int intValue = spinDoIt.intValue();
        final double percentBowMoved = spinDoIt.subtract(new BigDecimal(intValue)).doubleValue();
        return percentBowMoved*DEGREES;
    }

    /**
     * Responde la pregunta se el sol esta dentro del triangulo
     */
    static boolean isInside(CartesianPoint v1,
                            CartesianPoint v2,
                            CartesianPoint v3,
                            CartesianPoint pt)
    {
        log.trace("Calcule if the point {} is in triangle formed by p1:{},p2:{},p3:{}",
            pt.toString(), v1.toString(), v2.toString(), v3.toString());

        double d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(pt, v1, v2);
        d2 = sign(pt, v2, v3);
        d3 = sign(pt, v3, v1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    static double sign(final CartesianPoint p1, final CartesianPoint p2, final CartesianPoint p3)
    {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    /**
     * Extrae el perimetro del triangulo
     */
    private double getTrianglePerimeter(final CartesianPoint p1, final CartesianPoint p2, final CartesianPoint p3){
        final double l1 = getDistanceBetweenPoints(p1, p2);
        final double l2 = getDistanceBetweenPoints(p2, p3);
        final double l3 = getDistanceBetweenPoints(p3, p1);
        return l1+l2+l3;
    }

    private double getDistanceBetweenPoints(CartesianPoint p1, CartesianPoint p2){
        return Math.sqrt(Math.pow((p2.getX()-p1.getX()),2)+Math.sqrt(Math.pow((p2.getY()-p1.getY()),2)));
    }
}
