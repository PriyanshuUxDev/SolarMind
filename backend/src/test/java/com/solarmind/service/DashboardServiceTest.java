package com.solarmind.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solarmind.config.SolarProperties;
import com.solarmind.dto.response.DashboardResponse;
import com.solarmind.entity.Assessment;
import com.solarmind.entity.Location;
import com.solarmind.entity.SolarPanel;
import com.solarmind.entity.User;
import com.solarmind.repository.AssessmentRepository;
import com.solarmind.repository.LocationRepository;
import com.solarmind.repository.SolarPanelRepository;
import com.solarmind.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class DashboardServiceTest {
  private final AssessmentRepository assessmentRepository = mock(AssessmentRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final LocationRepository locationRepository = mock(LocationRepository.class);
  private final SolarPanelRepository panelRepository = mock(SolarPanelRepository.class);
  private final SolarProperties properties =
      new SolarProperties(
          BigDecimal.valueOf(1000),
          BigDecimal.valueOf(100),
          BigDecimal.ONE,
          BigDecimal.ONE,
          Map.of("FLAT", BigDecimal.ONE),
          25,
          BigDecimal.ZERO,
          BigDecimal.ZERO);
  private final RecommendationService recommendations =
      new RecommendationService(panelRepository, properties, new ConfigGenerationEstimator(properties));
  private final AssessmentService assessments =
      new AssessmentService(assessmentRepository, userRepository, locationRepository, recommendations, properties);
  private final DashboardService service = new DashboardService(assessments, properties);

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void noAssessmentReturnsEmptyDashboardData() {
    User user = user();
    authenticate(user);
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(assessmentRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
    when(assessmentRepository.findTop10ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());

    DashboardResponse result = service.get();

    assertNull(result.latest());
    assertTrue(result.cumulativeCostComparison().isEmpty());
    assertNull(result.monthlyBillComparison());
    assertNull(result.lifetimeSavings());
  }

  @Test
  void buildsSeriesWithYearZeroAndConfiguredLifespan() {
    DashboardResponse result = configured(assessment(new BigDecimal("100")));

    assertEquals(26, result.cumulativeCostComparison().size());
    assertEquals(0, result.cumulativeCostComparison().get(0).year());
    assertEquals(new BigDecimal("0.00"), result.cumulativeCostComparison().get(0).withoutSolar());
    assertEquals(new BigDecimal("300.00"), result.cumulativeCostComparison().get(0).withSolar());
  }

  @Test
  void zeroSavingsDoesNotProduceInvalidValues() {
    Assessment assessment = assessment(new BigDecimal("0"));
    DashboardResponse result = configured(assessment);

    assertEquals(new BigDecimal("0.00"), result.lifetimeSavings());
    assertEquals(new BigDecimal("100.00"), result.monthlyBillComparison().before());
    assertEquals(new BigDecimal("100.00"), result.monthlyBillComparison().after());
  }

  @Test
  void noEscalationCrossoverMatchesSimplePayback() {
    DashboardResponse result = configured(assessment(new BigDecimal("100")));

    var yearThree = result.cumulativeCostComparison().get(3);
    assertEquals(0, yearThree.withSolar().compareTo(yearThree.withoutSolar()));
  }

  private DashboardResponse configured(Assessment assessment) {
    User user = assessment.getUser();
    authenticate(user);
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(assessmentRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(assessment));
    when(assessmentRepository.findTop10ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(assessment));
    return service.get();
  }

  private User user() {
    return new User("Asha", "asha@example.com", "encoded");
  }

  private void authenticate(User user) {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));
  }

  private Assessment assessment(BigDecimal savings) {
    User user = user();
    Assessment assessment = new Assessment(user, new Location("Punjab", "Patiala", "Patiala", "Punjab", 30.3, 76.4));
    assessment.setMonthlyBill(new BigDecimal("100"));
    assessment.setMonthlyConsumption(new BigDecimal("100"));
    assessment.setRoofArea(new BigDecimal("10"));
    assessment.setRequiredRoofArea(new BigDecimal("5"));
    assessment.setBudget(new BigDecimal("500"));
    assessment.setEstimatedCost(new BigDecimal("300"));
    assessment.setAnnualSavings(savings);
    assessment.setAnnualGeneration(new BigDecimal("1200"));
    assessment.setWithinBudget(true);
    assessment.setSelectedPanel(new SolarPanel("Test", "Panel", 540, null, null, new BigDecimal("20"), null, null, "2266 x 1133", null));
    return assessment;
  }
}
