package com.solarmind.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.solarmind.config.SolarProperties;
import com.solarmind.dto.request.AssessmentRequest;
import com.solarmind.dto.request.RoofType;
import com.solarmind.entity.Assessment;
import com.solarmind.entity.Location;
import com.solarmind.entity.SolarPanel;
import com.solarmind.entity.User;
import com.solarmind.exception.ResourceNotFoundException;
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
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AssessmentServiceTest {
  private final AssessmentRepository assessments = mock(AssessmentRepository.class);
  private final UserRepository users = mock(UserRepository.class);
  private final LocationRepository locations = mock(LocationRepository.class);
  private final SolarPanelRepository panels = mock(SolarPanelRepository.class);
  private final SolarProperties properties =
      new SolarProperties(
          BigDecimal.ONE,
          BigDecimal.ONE,
          BigDecimal.ONE,
          BigDecimal.ONE,
          Map.of("FLAT", BigDecimal.ONE),
          25,
          BigDecimal.ZERO,
          BigDecimal.ZERO);
  private final RecommendationService recommendations =
      new RecommendationService(panels, properties, new ConfigGenerationEstimator(properties));
  private final AssessmentService service =
      new AssessmentService(assessments, users, locations, recommendations, properties);

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createPersistsAssessmentForAuthenticatedUser() {
    User user = new User("Asha", "asha@example.com", "encoded");
    Location location = new Location("Punjab", "Patiala", "Patiala", "Punjab", 30.3, 76.4);
    SolarPanel panel = panel();
    authenticateAs(user.getEmail());
    when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(locations.findById(7L)).thenReturn(Optional.of(location));
    when(panels.findAll()).thenReturn(List.of(panel));
    when(assessments.save(any(Assessment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.create(
        new AssessmentRequest(
            7L,
            new BigDecimal("250"),
            new BigDecimal("3000"),
            new BigDecimal("50"),
            RoofType.FLAT,
            new BigDecimal("10000")));

    ArgumentCaptor<Assessment> captor = ArgumentCaptor.forClass(Assessment.class);
    verify(assessments).save(captor.capture());
    assertSame(user, captor.getValue().getUser());
    assertSame(location, captor.getValue().getLocation());
    assertSame(panel, captor.getValue().getSelectedPanel());
  }

  @Test
  void assessmentCannotBeReadByAnotherUser() {
    User owner = new User("Asha", "asha@example.com", "encoded");
    User other = new User("Ravi", "ravi@example.com", "encoded");
    Assessment assessment =
        new Assessment(owner, new Location("Punjab", "Patiala", "Patiala", "Punjab", 30.3, 76.4));
    assessment.setSelectedPanel(panel());

    authenticateAs(owner.getEmail());
    when(users.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
    when(assessments.findByIdAndUser(12L, owner)).thenReturn(Optional.of(assessment));
    assertEquals("Patiala", service.get(12L).location().city());

    authenticateAs(other.getEmail());
    when(users.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
    when(assessments.findByIdAndUser(12L, other)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.get(12L));
    verify(assessments).findByIdAndUser(12L, other);
  }

  @Test
  void listIncludesAssessmentSummaryDetails() {
    User user = new User("Asha", "asha@example.com", "encoded");
    Location location = new Location("Punjab", "Patiala", "Patiala", "Punjab", 30.3, 76.4);
    Assessment assessment = new Assessment(user, location);
    assessment.setSelectedPanel(panel());
    assessment.setRecommendedCapacityKw(new BigDecimal("2.50"));
    assessment.setAnnualSavings(new BigDecimal("12000"));
    assessment.setEstimatedCost(new BigDecimal("150000"));
    assessment.setPaybackYears(new BigDecimal("12.5"));
    assessment.setRoofFeasible(true);
    assessment.setWithinBudget(false);
    authenticateAs(user.getEmail());
    when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(assessments.findTop10ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(assessment));

    var summary = service.list().get(0);

    assertEquals("Patiala, Punjab", summary.locationLabel());
    assertEquals("Test Brand", summary.selectedPanelBrand());
    assertEquals("Test Model", summary.selectedPanelModel());
    assertEquals(new BigDecimal("150000"), summary.estimatedCost());
    assertEquals(new BigDecimal("12.5"), summary.paybackYears());
    assertTrue(summary.roofFeasible());
    assertFalse(summary.withinBudget());
  }

  private void authenticateAs(String email) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(email, null, List.of()));
  }

  private SolarPanel panel() {
    return new SolarPanel(
        "Test Brand",
        "Test Model",
        540,
        new BigDecimal("2.16"),
        new BigDecimal("64.8"),
        new BigDecimal("20.92"),
        new BigDecimal("41.76"),
        new BigDecimal("12.93"),
        "2266 x 1133 x 35",
        new BigDecimal("27.5"));
  }
}
