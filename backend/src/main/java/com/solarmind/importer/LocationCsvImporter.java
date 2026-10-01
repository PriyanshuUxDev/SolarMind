package com.solarmind.importer;

import com.solarmind.config.CsvProperties;
import com.solarmind.entity.Location;
import com.solarmind.repository.LocationRepository;
import java.nio.file.Path;
import java.util.Set;
import org.slf4j.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;

@Configuration
public class LocationCsvImporter {
  private static final Logger log = LoggerFactory.getLogger(LocationCsvImporter.class);
  private static final Set<String> REQUIRED =
      Set.of("region", "city", "district", "state", "latitude", "longitude");
  private final CsvProperties properties;
  private final LocationRepository repository;

  public LocationCsvImporter(CsvProperties properties, LocationRepository repository) {
    this.properties = properties;
    this.repository = repository;
  }

  @Bean
  ApplicationRunner locationImporter() {
    return args -> {
      if (properties.importEnabled()) importLocations();
    };
  }

  void importLocations() throws Exception {
    int imported = 0, duplicates = 0, malformed = 0;
    Path path = Path.of(properties.locationsPath());
    requireHeaders(SimpleCsv.headers(path));
    for (SimpleCsv.Row r : SimpleCsv.read(path)) {
      try {
        String region = text(r, "region"),
            city = text(r, "city"),
            district = text(r, "district"),
            state = text(r, "state");
        if (region.isBlank() || city.isBlank() || district.isBlank() || state.isBlank())
          throw new IllegalArgumentException("required text field is blank");
        Double lat = Double.valueOf(text(r, "latitude")),
            lon = Double.valueOf(text(r, "longitude"));
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180)
          throw new IllegalArgumentException("coordinate out of range");
        if (repository
            .findByCityAndDistrictAndStateAndLatitudeAndLongitude(city, district, state, lat, lon)
            .isPresent()) {
          duplicates++;
          continue;
        }
        repository.save(new Location(region, city, district, state, lat, lon));
        imported++;
      } catch (Exception e) {
        malformed++;
        log.warn("Skipping malformed location row {}: {}", r.line(), e.getMessage());
      }
    }
    log.info(
        "Location import complete: imported={}, duplicates={}, malformed={}",
        imported,
        duplicates,
        malformed);
  }

  private static String text(SimpleCsv.Row r, String h) {
    return r.get(h).trim();
  }

  private static void requireHeaders(Set<String> actual) {
    if (!actual.containsAll(REQUIRED))
      throw new IllegalStateException(
          "Location CSV must contain region, city, district, state, latitude and longitude; missing"
              + " "
              + REQUIRED.stream().filter(h -> !actual.contains(h)).toList());
  }
}
