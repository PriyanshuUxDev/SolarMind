package com.solarmind.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "locations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_locations_source_row",
            columnNames = {"city", "district", "state", "latitude", "longitude"}),
    indexes = {
      @Index(name = "idx_locations_city", columnList = "city"),
      @Index(name = "idx_locations_state", columnList = "state")
    })
public class Location {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 191)
  private String region;

  @Column(length = 191)
  private String city;

  @Column(length = 191)
  private String district;

  @Column(length = 191)
  private String state;

  private Double latitude, longitude;

  protected Location() {}

  public Location(
      String region,
      String city,
      String district,
      String state,
      Double latitude,
      Double longitude) {
    this.region = region;
    this.city = city;
    this.district = district;
    this.state = state;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Long getId() {
    return id;
  }

  public String getRegion() {
    return region;
  }

  public String getCity() {
    return city;
  }

  public String getDistrict() {
    return district;
  }

  public String getState() {
    return state;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }
}
