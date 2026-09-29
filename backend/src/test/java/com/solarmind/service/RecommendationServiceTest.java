package com.solarmind.service;

import com.solarmind.config.SolarProperties;
import com.solarmind.entity.SolarPanel;
import com.solarmind.repository.SolarPanelRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {
  private final SolarPanelRepository panels=Mockito.mock(SolarPanelRepository.class);
  private final SolarProperties properties=new SolarProperties(new BigDecimal("1000"),new BigDecimal("100"),new BigDecimal("0.5"),new BigDecimal("1"),Map.of("FLAT",BigDecimal.ONE,"SLOPED",BigDecimal.ONE,"OTHER",BigDecimal.ONE));
  private final RecommendationService service=new RecommendationService(panels,properties,new ConfigGenerationEstimator(properties));

  @Test void workedExampleUsesSix540WPanels(){SolarPanel p=panel(540);Mockito.when(panels.findAll()).thenReturn(List.of(p)); var r=service.calculate(new BigDecimal("250"),new BigDecimal("3000"),new BigDecimal("50"),"FLAT",new BigDecimal("10000")); assertEquals(6,r.panelCount()); assertEquals(0,new BigDecimal("3.24").compareTo(r.actualCapacity())); assertTrue(r.roofFeasible());}
  @Test void zeroBillProducesNullPayback(){Mockito.when(panels.findAll()).thenReturn(List.of(panel(540))); var r=service.calculate(new BigDecimal("250"),BigDecimal.ZERO,new BigDecimal("50"),"FLAT",new BigDecimal("10000")); assertNull(r.paybackYears()); assertTrue(r.annualSavings().signum()==0);}
  private SolarPanel panel(int wattage){return new SolarPanel("Test Brand","Test Model",wattage,new BigDecimal("2.16"),new BigDecimal("64.8"),new BigDecimal("20.92"),new BigDecimal("41.76"),new BigDecimal("12.93"),"2266 x 1133 x 35",new BigDecimal("27.5"),null);}
}
