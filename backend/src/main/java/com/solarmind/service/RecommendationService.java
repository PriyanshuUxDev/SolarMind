package com.solarmind.service;

import com.solarmind.config.SolarProperties;
import com.solarmind.entity.SolarPanel;
import com.solarmind.entity.Location;
import com.solarmind.dto.request.RoofType;
import com.solarmind.exception.InvalidInputException;
import com.solarmind.exception.NoSuitablePanelException;
import com.solarmind.repository.SolarPanelRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {
  private final SolarPanelRepository panels;
  private final SolarProperties properties;
  private final GenerationEstimator generation;

  public RecommendationService(
      SolarPanelRepository panels, SolarProperties properties, GenerationEstimator generation) {
    this.panels = panels;
    this.properties = properties;
    this.generation = generation;
  }

  public Result calculate(
      BigDecimal monthlyConsumption,
      BigDecimal monthlyBill,
      BigDecimal roofArea,
      RoofType roofType,
      BigDecimal budget,
      Location location) {
    BigDecimal annual = monthlyConsumption.multiply(BigDecimal.valueOf(12));
    BigDecimal tariff = monthlyBill.divide(monthlyConsumption, 8, RoundingMode.HALF_UP);
    BigDecimal required =
        annual.divide(properties.generationFactorKwhPerKwYear(), 8, RoundingMode.HALF_UP);
    BigDecimal roofFactor = properties.roofTypeUsableFactor().get(roofType.name());
    if (roofFactor == null) {
      throw new InvalidInputException("No usable-area factor configured for roof type " + roofType);
    }
    BigDecimal usable = roofArea.multiply(roofFactor);

    List<Candidate> valid =
        panels.findAll().stream().map(this::candidate).filter(Objects::nonNull).toList();
    if (valid.isEmpty()) {
      throw new NoSuitablePanelException("No panel has valid wattage and dimensions");
    }

    List<SystemOption> options =
        valid.stream().map(panel -> option(panel, required, usable, budget)).toList();
    SystemOption selected =
        options.stream()
            .sorted(
                Comparator.comparing(SystemOption::roofFeasible)
                    .reversed()
                    .thenComparing(
                        option -> option.candidate().panel().getEfficiency(),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(
                        option -> option.candidate().panel().getWattage(),
                        Comparator.reverseOrder()))
            .findFirst()
            .orElseThrow();

    BigDecimal annualGeneration = generation.estimate(selected.actualCapacity(), location);
    BigDecimal savings = annualGeneration.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
    BigDecimal payback =
        savings.signum() > 0
            ? selected.estimatedCost().divide(savings, 4, RoundingMode.HALF_UP)
            : null;
    BigDecimal co2Kg =
        annualGeneration
            .multiply(properties.gridEmissionFactor())
            .setScale(2, RoundingMode.HALF_UP);
    BigDecimal co2Tonnes = co2Kg.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);

    return new Result(
        selected.candidate().panel(),
        selected.panelCount(),
        required.setScale(2, RoundingMode.HALF_UP),
        selected.actualCapacity(),
        annualGeneration,
        savings,
        selected.estimatedCost(),
        payback,
        co2Kg,
        co2Tonnes,
        tariff,
        selected.requiredRoofArea(),
        selected.roofFeasible(),
        selected.withinBudget());
  }

  private SystemOption option(
      Candidate candidate, BigDecimal required, BigDecimal usable, BigDecimal budget) {
    int count =
        required
            .multiply(BigDecimal.valueOf(1000))
            .divide(BigDecimal.valueOf(candidate.panel().getWattage()), 0, RoundingMode.CEILING)
            .intValueExact();
    BigDecimal actual =
        BigDecimal.valueOf(count)
            .multiply(BigDecimal.valueOf(candidate.panel().getWattage()))
            .divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP);
    BigDecimal estimatedCost =
        actual.multiply(properties.installationCostPerKw()).setScale(2, RoundingMode.HALF_UP);
    BigDecimal requiredRoof =
        candidate
            .area()
            .multiply(BigDecimal.valueOf(count))
            .multiply(properties.roofLayoutFactor())
            .setScale(2, RoundingMode.HALF_UP);
    return new SystemOption(
        candidate,
        count,
        actual,
        estimatedCost,
        requiredRoof,
        requiredRoof.compareTo(usable) <= 0,
        estimatedCost.compareTo(budget) <= 0);
  }

  private Candidate candidate(SolarPanel panel) {
    try {
      if (panel.getWattage() == null || panel.getWattage() <= 0 || panel.getDimensions() == null) {
        return null;
      }
      Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(panel.getDimensions());
      List<BigDecimal> dimensions = new ArrayList<>();
      while (matcher.find() && dimensions.size() < 2) {
        dimensions.add(new BigDecimal(matcher.group(1)));
      }
      if (dimensions.size() < 2) {
        return null;
      }
      BigDecimal area =
          dimensions
              .get(0)
              .multiply(dimensions.get(1))
              .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
      return new Candidate(panel, area);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private record Candidate(SolarPanel panel, BigDecimal area) {}

  private record SystemOption(
      Candidate candidate,
      int panelCount,
      BigDecimal actualCapacity,
      BigDecimal estimatedCost,
      BigDecimal requiredRoofArea,
      boolean roofFeasible,
      boolean withinBudget) {}

  public record Result(
      SolarPanel panel,
      int panelCount,
      BigDecimal requiredCapacity,
      BigDecimal actualCapacity,
      BigDecimal annualGeneration,
      BigDecimal annualSavings,
      BigDecimal estimatedCost,
      BigDecimal paybackYears,
      BigDecimal co2Kg,
      BigDecimal co2Tonnes,
      BigDecimal effectiveTariff,
      BigDecimal requiredRoofArea,
      boolean roofFeasible,
      boolean withinBudget) {}
}
