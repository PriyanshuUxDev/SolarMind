package com.solarmind.dto.response;

import java.util.List;
import java.math.BigDecimal;

public record DashboardResponse(
    AssessmentResponse latest,
    List<AssessmentSummary> recentAssessments,
    List<CumulativeCostPoint> cumulativeCostComparison,
    MonthlyBillComparison monthlyBillComparison,
    BigDecimal solarCoveragePercent,
    BudgetFit budgetFit,
    BigDecimal roofUtilizationPercent,
    BigDecimal lifetimeSavings,
    SelectedPanelSummary selectedPanel,
    List<AssessmentDashboard> assessmentDashboards) {

  public record CumulativeCostPoint(
      int year, BigDecimal withoutSolar, BigDecimal withSolar) {}

  public record MonthlyBillComparison(BigDecimal before, BigDecimal after) {}

  /** Difference is budget minus estimated cost; negative means over budget. */
  public record BudgetFit(
      BigDecimal budget, BigDecimal estimatedCost, BigDecimal difference, boolean withinBudget) {}

  public record SelectedPanelSummary(
      String brand, String model, Integer wattage, BigDecimal efficiency) {}

  public record AssessmentDashboard(
      Long assessmentId,
      AssessmentSummary assessment,
      Integer panelCount,
      BigDecimal annualGeneration,
      List<CumulativeCostPoint> cumulativeCostComparison,
      MonthlyBillComparison monthlyBillComparison,
      BigDecimal solarCoveragePercent,
      BudgetFit budgetFit,
      BigDecimal roofUtilizationPercent,
      BigDecimal lifetimeSavings,
      SelectedPanelSummary selectedPanel) {}
}
