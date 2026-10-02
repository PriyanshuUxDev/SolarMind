package com.solarmind.service;

import com.solarmind.config.SolarProperties;
import com.solarmind.dto.response.DashboardResponse;
import com.solarmind.dto.response.DashboardResponse.AssessmentDashboard;
import com.solarmind.dto.response.DashboardResponse.BudgetFit;
import com.solarmind.dto.response.DashboardResponse.CumulativeCostPoint;
import com.solarmind.dto.response.DashboardResponse.MonthlyBillComparison;
import com.solarmind.dto.response.DashboardResponse.SelectedPanelSummary;
import com.solarmind.entity.Assessment;
import com.solarmind.entity.SolarPanel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
  private final AssessmentService assessments;
  private final SolarProperties properties;

  public DashboardService(AssessmentService assessments, SolarProperties properties) {
    this.assessments = assessments;
    this.properties = properties;
  }

  public DashboardResponse get() {
    var latest = assessments.latest();
    var recent = assessments.list();
    var entities = latest == null ? List.<Assessment>of() : assessments.recentEntities();
    if (entities.isEmpty()) {
      return new DashboardResponse(latest, recent, List.of(), null, null, null, null, null, null, List.of());
    }

    AssessmentDashboard latestDashboard = calculate(entities.get(0));
    List<AssessmentDashboard> dashboards = entities.stream().map(this::calculate).toList();
    return new DashboardResponse(
        latest,
        recent,
        latestDashboard.cumulativeCostComparison(),
        latestDashboard.monthlyBillComparison(),
        latestDashboard.solarCoveragePercent(),
        latestDashboard.budgetFit(),
        latestDashboard.roofUtilizationPercent(),
        latestDashboard.lifetimeSavings(),
        latestDashboard.selectedPanel(),
        dashboards);
  }

  private AssessmentDashboard calculate(Assessment entity) {
    BigDecimal monthlyBill = nonNegative(entity.getMonthlyBill());
    BigDecimal annualBill = monthlyBill.multiply(BigDecimal.valueOf(12));
    BigDecimal annualSavings = nonNegative(entity.getAnnualSavings());
    BigDecimal estimatedCost = nonNegative(entity.getEstimatedCost());
    BigDecimal tariffRate = nonNegative(properties.tariffEscalationRate());
    BigDecimal degradationRate = nonNegative(properties.panelDegradationRate());
    BigDecimal tariffFactor = BigDecimal.ONE.add(tariffRate);
    BigDecimal outputFactor = BigDecimal.ONE.subtract(degradationRate).max(BigDecimal.ZERO);

    List<CumulativeCostPoint> comparison = new ArrayList<>();
    comparison.add(new CumulativeCostPoint(0, money(BigDecimal.ZERO), money(estimatedCost)));
    BigDecimal withoutSolar = BigDecimal.ZERO;
    BigDecimal withSolar = estimatedCost;
    for (int year = 1; year <= properties.systemLifespanYears(); year++) {
      BigDecimal escalatedBill = annualBill.multiply(tariffFactor.pow(year));
      BigDecimal yearlySavings = annualSavings.multiply(outputFactor.pow(year - 1)).multiply(tariffFactor.pow(year - 1));
      BigDecimal remainingBill = escalatedBill.subtract(yearlySavings).max(BigDecimal.ZERO);
      withoutSolar = withoutSolar.add(escalatedBill);
      withSolar = withSolar.add(remainingBill);
      comparison.add(new CumulativeCostPoint(year, money(withoutSolar), money(withSolar)));
    }

    BigDecimal monthlyAfter = monthlyBill.subtract(annualSavings.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP)).max(BigDecimal.ZERO);
    BigDecimal coverageDenominator = nonNegative(entity.getMonthlyConsumption()).multiply(BigDecimal.valueOf(12));
    BigDecimal coverage = coverageDenominator.signum() == 0 ? null
        : nonNegative(entity.getAnnualGeneration()).divide(coverageDenominator, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    BigDecimal budget = nonNegative(entity.getBudget());
    BigDecimal difference = budget.subtract(estimatedCost).setScale(2, RoundingMode.HALF_UP);
    BigDecimal roofArea = nonNegative(entity.getRoofArea());
    BigDecimal roofUtilization = roofArea.signum() == 0 ? null
        : nonNegative(entity.getRequiredRoofArea()).divide(roofArea, 8, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    BigDecimal lifetimeSavings = BigDecimal.ZERO;
    for (int year = 1; year <= properties.systemLifespanYears(); year++) {
      lifetimeSavings = lifetimeSavings.add(annualSavings.multiply(outputFactor.pow(year - 1)).multiply(tariffFactor.pow(year - 1)));
    }

    SolarPanel panel = entity.getSelectedPanel();
    SelectedPanelSummary panelSummary = panel == null ? null
        : new SelectedPanelSummary(panel.getBrand(), panel.getModel(), panel.getWattage(), panel.getEfficiency());
    var location = entity.getLocation();
    var summary = new com.solarmind.dto.response.AssessmentSummary(
        entity.getId(),
        location == null ? "Location unavailable" : location.getCity() + ", " + location.getState(),
        panel == null ? "Panel unavailable" : panel.getBrand(),
        panel == null ? "" : panel.getModel(),
        entity.getRecommendedCapacityKw(),
        entity.getActualCapacityKw(),
        entity.getAnnualSavings(),
        entity.getEstimatedCost(),
        entity.getPaybackYears(),
        entity.isRoofFeasible(),
        entity.isWithinBudget(),
        entity.getCreatedAt());
    return new AssessmentDashboard(
        entity.getId(),
        summary,
        entity.getPanelCount(),
        entity.getAnnualGeneration(),
        comparison,
        new MonthlyBillComparison(money(monthlyBill), money(monthlyAfter)),
        coverage,
        new BudgetFit(money(budget), money(estimatedCost), money(difference), entity.isWithinBudget()),
        roofUtilization,
        money(lifetimeSavings),
        panelSummary);
  }

  private static BigDecimal nonNegative(BigDecimal value) {
    return value == null || value.signum() < 0 ? BigDecimal.ZERO : value;
  }

  private static BigDecimal money(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }
}
