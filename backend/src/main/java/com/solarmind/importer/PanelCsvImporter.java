package com.solarmind.importer;

import com.solarmind.config.CsvProperties;
import com.solarmind.entity.SolarPanel;
import com.solarmind.repository.SolarPanelRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Set;
import org.slf4j.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;

@Configuration
public class PanelCsvImporter {
  private static final Logger log = LoggerFactory.getLogger(PanelCsvImporter.class);
  private static final Set<String> REQUIRED =
      Set.of(
          "Brand",
          "Panel Name/Model",
          "Wattage (W)",
          "Daily Output (kWh/day)*",
          "Monthly Output (kWh/month)*",
          "Efficiency (%)",
          "Voltage Vmpp (V)",
          "Current Impp (A)",
          "Dimensions (mm)",
          "Weight (kg)");
  private final CsvProperties properties;
  private final SolarPanelRepository repository;

  public PanelCsvImporter(CsvProperties properties, SolarPanelRepository repository) {
    this.properties = properties;
    this.repository = repository;
  }

  @Bean
  ApplicationRunner panelImporter() {
    return args -> {
      if (properties.importEnabled()) importPanels();
    };
  }

  void importPanels() throws Exception {
    int imported = 0, duplicates = 0, malformed = 0;
    Path path = Path.of(properties.panelsPath());
    requireHeaders(SimpleCsv.headers(path), REQUIRED);
    for (SimpleCsv.Row r : SimpleCsv.read(path)) {
      try {
        String brand = text(r, "Brand"), model = text(r, "Panel Name/Model");
        if (brand.isBlank() || model.isBlank())
          throw new IllegalArgumentException("brand/model is blank");
        if (repository.findByBrandAndModel(brand, model).isPresent()) {
          duplicates++;
          continue;
        }
        repository.save(
            new SolarPanel(
                brand,
                model,
                Integer.valueOf(text(r, "Wattage (W)")),
                decimal(r, "Daily Output (kWh/day)*"),
                decimal(r, "Monthly Output (kWh/month)*"),
                decimal(r, "Efficiency (%)"),
                decimal(r, "Voltage Vmpp (V)"),
                decimal(r, "Current Impp (A)"),
                text(r, "Dimensions (mm)"),
                decimal(r, "Weight (kg)")));
        imported++;
      } catch (Exception e) {
        malformed++;
        log.warn("Skipping malformed panel row {}: {}", r.line(), e.getMessage());
      }
    }
    log.info(
        "Panel import complete: imported={}, duplicates={}, malformed={}",
        imported,
        duplicates,
        malformed);
  }

  private static String text(SimpleCsv.Row r, String h) {
    return r.get(h).trim();
  }

  private static BigDecimal decimal(SimpleCsv.Row r, String h) {
    return new BigDecimal(text(r, h));
  }

  private static void requireHeaders(Set<String> actual, Set<String> required) {
    if (!actual.containsAll(required))
      throw new IllegalStateException(
          "Panel CSV headers do not match actual source headers: missing "
              + required.stream().filter(h -> !actual.contains(h)).toList());
  }
}
