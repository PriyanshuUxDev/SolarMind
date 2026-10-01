package com.solarmind.controller;

import com.solarmind.dto.response.*;
import com.solarmind.service.LocationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
  private final LocationService s;

  public LocationController(LocationService s) {
    this.s = s;
  }

  @GetMapping("/search")
  PagedResponse<LocationResponse> search(
      @RequestParam String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {
    return s.search(query, page, limit);
  }
}
