package com.solarmind.service;

import com.solarmind.dto.response.*;
import com.solarmind.exception.InvalidInputException;
import com.solarmind.repository.LocationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LocationService {
  private final LocationRepository locations;

  public LocationService(LocationRepository locations) {
    this.locations = locations;
  }

  public PagedResponse<LocationResponse> search(String q, int page, int size) {
    if (q == null || q.trim().length() < 2) {
      throw new InvalidInputException("Location search query must be at least 2 characters");
    }
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 20);
    var rows = locations.search(q.trim(), PageRequest.of(safePage, safeSize));
    var content =
        rows.getContent().stream()
            .map(
                l ->
                    new LocationResponse(
                        l.getId(),
                        l.getRegion(),
                        l.getCity(),
                        l.getDistrict(),
                        l.getState(),
                        l.getLatitude(),
                        l.getLongitude()))
            .toList();
    return new PagedResponse<>(
        content, safePage, safeSize, rows.getTotalElements(), rows.getTotalPages());
  }
}
