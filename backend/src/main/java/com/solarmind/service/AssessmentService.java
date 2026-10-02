package com.solarmind.service;

import com.solarmind.config.SolarProperties;
import com.solarmind.dto.request.AssessmentRequest;
import com.solarmind.dto.response.*;
import com.solarmind.entity.*;
import com.solarmind.exception.*;
import com.solarmind.repository.*;
import com.solarmind.security.CurrentUser;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {
  private final AssessmentRepository assessments;
  private final UserRepository users;
  private final LocationRepository locations;
  private final RecommendationService recommendations;
  private final SolarProperties properties;

  public AssessmentService(
      AssessmentRepository a,
      UserRepository u,
      LocationRepository l,
      RecommendationService r,
      SolarProperties p) {
    assessments = a;
    users = u;
    locations = l;
    recommendations = r;
    properties = p;
  }

  private User user() {
    return users
        .findByEmail(CurrentUser.subject())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  public AssessmentResponse create(AssessmentRequest r) {
    User u = user();
    Location l =
        locations
            .findById(r.locationId())
            .orElseThrow(() -> new InvalidInputException("Location does not exist"));
    var x =
        recommendations.calculate(
            r.monthlyConsumption(), r.monthlyBill(), r.roofArea(), r.roofType(), r.budget(), l);
    Assessment a = new Assessment(u, l);
    a.setMonthlyConsumption(r.monthlyConsumption());
    a.setMonthlyBill(r.monthlyBill());
    a.setRoofArea(r.roofArea());
    a.setRoofType(r.roofType().name());
    a.setBudget(r.budget());
    a.setSelectedPanel(x.panel());
    a.setRecommendedCapacityKw(x.requiredCapacity());
    a.setPanelCount(x.panelCount());
    a.setActualCapacityKw(x.actualCapacity());
    a.setAnnualGeneration(x.annualGeneration());
    a.setAnnualSavings(x.annualSavings());
    a.setEstimatedCost(x.estimatedCost());
    a.setPaybackYears(x.paybackYears());
    a.setCo2Reduction(x.co2Kg());
    a.setEffectiveTariff(x.effectiveTariff());
    a.setRequiredRoofArea(x.requiredRoofArea());
    a.setRoofFeasible(x.roofFeasible());
    a.setWithinBudget(x.withinBudget());
    return map(assessments.save(a));
  }

  public AssessmentResponse get(Long id) {
    return assessments
        .findByIdAndUser(id, user())
        .map(this::map)
        .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));
  }

  public List<AssessmentSummary> list() {
    return assessments.findTop10ByUserOrderByCreatedAtDesc(user()).stream()
        .map(
            a ->
                new AssessmentSummary(
                    a.getId(),
                    a.getLocation().getCity() + ", " + a.getLocation().getState(),
                    a.getSelectedPanel().getBrand(),
                    a.getSelectedPanel().getModel(),
                    a.getRecommendedCapacityKw(),
                    a.getActualCapacityKw(),
                    a.getAnnualSavings(),
                    a.getEstimatedCost(),
                    a.getPaybackYears(),
                    a.isRoofFeasible(),
                    a.isWithinBudget(),
                    a.getCreatedAt()))
        .toList();
  }

  public List<Assessment> recentEntities() {
    return assessments.findTop10ByUserOrderByCreatedAtDesc(user());
  }

  public AssessmentResponse latest() {
    return assessments.findFirstByUserOrderByCreatedAtDesc(user()).map(this::map).orElse(null);
  }

  /** Returns the latest stored assessment for dashboard-only calculations. */
  public Assessment latestEntity() {
    return assessments.findFirstByUserOrderByCreatedAtDesc(user()).orElse(null);
  }

  private AssessmentResponse map(Assessment a) {
    var p = a.getSelectedPanel();
    var l = a.getLocation();
    Map<String, Object> assumptions = new LinkedHashMap<>();
    assumptions.put("generationFactor", properties.generationFactorKwhPerKwYear());
    assumptions.put("installationCostPerKw", properties.installationCostPerKw());
    assumptions.put("gridEmissionFactor", properties.gridEmissionFactor());
    assumptions.put("annualConsumptionFormula", "monthlyConsumption × 12");
    assumptions.put("tariffFormula", "monthlyBill / monthlyConsumption");
    String generationFactor = properties.generationFactorKwhPerKwYear().stripTrailingZeros().toPlainString();
    String installationCost = properties.installationCostPerKw().stripTrailingZeros().toPlainString();
    String emissionFactor = properties.gridEmissionFactor().stripTrailingZeros().toPlainString();
    assumptions.put("requiredCapacityFormula", "annualConsumption / " + generationFactor);
    assumptions.put("panelCountFormula", "ceil(requiredCapacityKw × 1000 / panelWattage)");
    assumptions.put("annualGenerationFormula", "actualCapacityKw × " + generationFactor);
    assumptions.put("annualSavingsFormula", "annualGeneration × tariff");
    assumptions.put("installationCostFormula", "actualCapacityKw × " + installationCost);
    assumptions.put("co2Formula", "annualGeneration × " + emissionFactor);
    assumptions.put("roofLayoutFactor", properties.roofLayoutFactor());
    return new AssessmentResponse(
        a.getId(),
        new LocationResponse(
            l.getId(),
            l.getRegion(),
            l.getCity(),
            l.getDistrict(),
            l.getState(),
            l.getLatitude(),
            l.getLongitude()),
        new PanelResponse(
            p.getId(),
            p.getBrand(),
            p.getModel(),
            p.getWattage(),
            p.getDailyOutput(),
            p.getMonthlyOutput(),
            p.getEfficiency(),
            p.getVmpp(),
            p.getImpp(),
            p.getDimensions(),
            p.getWeight()),
        a.getRecommendedCapacityKw(),
        a.getPanelCount(),
        a.getActualCapacityKw(),
        a.getAnnualGeneration(),
        a.getAnnualSavings(),
        a.getEstimatedCost(),
        a.getPaybackYears(),
        a.getCo2Reduction(),
        a.getCo2Reduction() == null
            ? null
            : a.getCo2Reduction().divide(java.math.BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP),
        a.getEffectiveTariff(),
        a.getRequiredRoofArea(),
        a.isRoofFeasible(),
        a.isWithinBudget(),
        assumptions,
        a.getCreatedAt());
  }
}
