package com.weather.domain.use_case.planet;

public enum WeatherType {
    /**
     *Cuando todos el sol y los tres planetas esta alineados
     */
    DROUGHT,
    /**
     *No estan alineados y forman un triagulo y el sol esta dentro del triagulo.
     * entre mas el perimetro del tiengulo mas intensa la lluvia
     */
    RAINY,
    /**
     * Los planetas estan alineados pero no estan alineados por el sol
     */
    OPTIMUM,
    /**
     * Este estado no se determina en el ejercicio, cuando forman un triagulo pero el
     * sol se encuentra fuera
     */
    WRONG
}
