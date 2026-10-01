package com.solarmind.service;

import java.math.BigDecimal;
import com.solarmind.entity.Location;

public interface GenerationEstimator {
  BigDecimal estimate(BigDecimal capacityKw, Location location);
}
