package com.solarmind.service;

import com.solarmind.dto.response.*;
import com.solarmind.exception.*;
import com.solarmind.repository.SolarPanelRepository;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class PanelService {
  private static final Set<String> SORT_FIELDS = Set.of("brand", "model", "wattage", "efficiency");
  private final SolarPanelRepository panels;

  public PanelService(SolarPanelRepository panels) {
    this.panels = panels;
  }

  public PagedResponse<PanelResponse> list(
      int page,
      int size,
      String q,
      String brand,
      Integer minW,
      Integer maxW,
      BigDecimal minE,
      String sort,
      String direction) {
    if (sort != null && !SORT_FIELDS.contains(sort))
      throw new InvalidInputException("Invalid panel sort field");
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    String field = sort == null ? "brand" : sort;
    Sort.Order order =
        new Sort.Order(
            "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
    var p =
        panels.search(
            q, brand, minW, maxW, minE, PageRequest.of(safePage, safeSize, Sort.by(order)));
    return new PagedResponse<>(
        p.getContent().stream().map(this::map).toList(),
        safePage,
        safeSize,
        p.getTotalElements(),
        p.getTotalPages());
  }

  public PanelResponse get(Long id) {
    return panels
        .findById(id)
        .map(this::map)
        .orElseThrow(() -> new ResourceNotFoundException("Panel not found"));
  }

  public java.util.List<String> brands() {
    return panels.brands();
  }

  private PanelResponse map(com.solarmind.entity.SolarPanel p) {
    return new PanelResponse(
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
        p.getWeight());
  }
}
