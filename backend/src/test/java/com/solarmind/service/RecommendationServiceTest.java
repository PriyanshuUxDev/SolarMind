package com.solarmind.service;

import static org.junit.jupiter.api.Assertions.*;

import com.solarmind.config.SolarProperties;
import com.solarmind.entity.SolarPanel;
import com.solarmind.entity.Location;
import com.solarmind.dto.request.RoofType;
import com.solarmind.repository.SolarPanelRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RecommendationServiceTest {
  private final Location location = new Location("Punjab", "Patiala", "Patiala", "Punjab", 30.3, 76.4);
  private final SolarPanelRepository panels = Mockito.mock(SolarPanelRepository.class);
  private final SolarProperties properties =
      new SolarProperties(
          new BigDecimal("1000"),
          new BigDecimal("100"),
          new BigDecimal("0.5"),
          new BigDecimal("1"),
          Map.of("FLAT", BigDecimal.ONE, "SLOPED", BigDecimal.ONE, "OTHER", BigDecimal.ONE));
  private final RecommendationService service =
      new RecommendationService(panels, properties, new ConfigGenerationEstimator(properties));

  @Test
  void workedExampleUsesSix540WPanels() {
    SolarPanel p = panel(540);
    Mockito.when(panels.findAll()).thenReturn(List.of(p));
    var r =
        service.calculate(
            new BigDecimal("250"),
            new BigDecimal("3000"),
            new BigDecimal("50"),
            RoofType.FLAT,
            new BigDecimal("10000"), location);
    assertEquals(6, r.panelCount());
    assertEquals(0, new BigDecimal("3.24").compareTo(r.actualCapacity()));
    assertTrue(r.roofFeasible());
    assertEquals(new BigDecimal("12.00000000"), r.effectiveTariff());
    assertEquals(new BigDecimal("3240.00"), r.annualGeneration());
    assertEquals(new BigDecimal("38880.00"), r.annualSavings());
    assertEquals(new BigDecimal("0.0083"), r.paybackYears());
  }

  @Test
  void roofTooSmallStillReturnsResultMarkedInfeasible() {
    Mockito.when(panels.findAll()).thenReturn(List.of(panel(540)));
    var result = service.calculate(new BigDecimal("250"), new BigDecimal("3000"), new BigDecimal("1"), RoofType.FLAT, new BigDecimal("10000"), location);
    assertFalse(result.roofFeasible());
  }

  @Test
  void noValidPanelFailsClearly() {
    Mockito.when(panels.findAll()).thenReturn(List.of(panelWithDimensions(540, "invalid")));
    var error = assertThrows(com.solarmind.exception.NoSuitablePanelException.class,
        () -> service.calculate(new BigDecimal("250"), new BigDecimal("3000"), new BigDecimal("50"), RoofType.FLAT, new BigDecimal("10000"), location));
    assertEquals("No panel has valid wattage and dimensions", error.getMessage());
  }

  @Test
  void tieBreakPrefersRoofFeasibleThenEfficiencyThenWattage() {
    SolarPanel lowEfficiency = panel(600);
    SolarPanel highEfficiency = new SolarPanel("Test Brand", "Higher efficiency", 500,
        new BigDecimal("2.16"), new BigDecimal("64.8"), new BigDecimal("21.92"),
        new BigDecimal("41.76"), new BigDecimal("12.93"), "2266 x 1133 x 35", new BigDecimal("27.5"));
    Mockito.when(panels.findAll()).thenReturn(List.of(lowEfficiency, highEfficiency));
    var result = service.calculate(new BigDecimal("250"), new BigDecimal("3000"), new BigDecimal("50"), RoofType.FLAT, new BigDecimal("10000"), location);
    assertEquals("Higher efficiency", result.panel().getModel());
  }

  @Test
  void zeroBillProducesNullPayback() {
    Mockito.when(panels.findAll()).thenReturn(List.of(panel(540)));
    var r =
        service.calculate(
            new BigDecimal("250"),
            BigDecimal.ZERO,
            new BigDecimal("50"),
            RoofType.FLAT,
            new BigDecimal("10000"), location);
    assertNull(r.paybackYears());
    assertTrue(r.annualSavings().signum() == 0);
  }

  @Test
  void overBudgetStillReturnsRecommendation() {
    Mockito.when(panels.findAll()).thenReturn(List.of(panel(540)));
    var result = service.calculate(new BigDecimal("250"), new BigDecimal("3000"), new BigDecimal("50"), RoofType.FLAT, new BigDecimal("1"), location);
    assertFalse(result.withinBudget());
    assertEquals(6, result.panelCount());
  }

  private SolarPanel panel(int wattage) {
    return panelWithDimensions(wattage, "2266 x 1133 x 35");
  }

  private SolarPanel panelWithDimensions(int wattage, String dimensions) {
    return new SolarPanel(
        "Test Brand",
        "Test Model",
        wattage,
        new BigDecimal("2.16"),
        new BigDecimal("64.8"),
        new BigDecimal("20.92"),
        new BigDecimal("41.76"),
        new BigDecimal("12.93"),
        dimensions,
        new BigDecimal("27.5"));
  }
}
