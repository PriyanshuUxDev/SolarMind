package com.solarmind.dto.request;

/** Maximum accepted assessment inputs, kept together for API and validation documentation. */
public final class ValidationLimits {
  public static final String MAX_MONTHLY_CONSUMPTION = "100000";
  public static final String MAX_MONTHLY_BILL = "10000000";
  public static final String MAX_ROOF_AREA = "1000000";
  public static final String MAX_BUDGET = "1000000000";

  private ValidationLimits() {}
}
