package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import entidades.Temperatura;

public class ServicioTemperaturaCiudad {

    public static List<Temperatura> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            Stream<String> lineas = Files.lines(Paths.get(nombreArchivo));
            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(textos -> new Temperatura(textos[0], LocalDate.parse(textos[1], formatoFecha),
                            Double.parseDouble(textos[2])))
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            return Collections.emptyList();
        }

    }

    public static List<String> getCiudades(List<Temperatura> datos) {
        return datos.stream()
                .map(Temperatura::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

    }

    public static List<Temperatura> filtrar(String ciudad, LocalDate desde, LocalDate hasta, List<Temperatura> datos) {
        return datos.stream()
                .filter(dato -> dato.getCiudad().equals(ciudad)
                        && !dato.getFecha().isBefore(desde) && !dato.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    public static double getPromedio(List<Double> temperaturas) {
        return temperaturas.isEmpty() ? 0 : temperaturas.stream().mapToDouble(Double::doubleValue).average().orElse(0);

    }

    public static double getMaximo(List<Double> temperaturas) {
        return temperaturas.isEmpty() ? 0 : temperaturas.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public static double getMinimo(List<Double> temperaturas) {
        return temperaturas.isEmpty() ? 0 : temperaturas.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    public static Map<String, Double> getEstadisticas(String ciudad, LocalDate desde, LocalDate hasta, List<Temperatura> datos) {
        var temperaturas = filtrar(ciudad, desde, hasta, datos)
                            .stream()
                            .map(Temperatura::getTemperatura)
                            .collect(Collectors.toList());
    
        return Map.of(
            "Promedio:", getPromedio(temperaturas),
            "Máximo:", getMaximo(temperaturas),
            "Mínimo:", getMinimo(temperaturas)
        );
    }

}
