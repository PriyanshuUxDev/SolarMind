CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(191) NOT NULL,
    email VARCHAR(191) NOT NULL,
    password VARCHAR(191) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE locations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    region VARCHAR(191),
    city VARCHAR(191),
    district VARCHAR(191),
    state VARCHAR(191),
    latitude DOUBLE,
    longitude DOUBLE,
    CONSTRAINT pk_locations PRIMARY KEY (id),
    CONSTRAINT uk_locations_source_row UNIQUE (city, district, state, latitude, longitude)
) ENGINE=InnoDB;

CREATE INDEX idx_locations_city ON locations (city);
CREATE INDEX idx_locations_state ON locations (state);

CREATE TABLE solar_panels (
    id BIGINT NOT NULL AUTO_INCREMENT,
    brand VARCHAR(191),
    model VARCHAR(191),
    wattage INT,
    daily_output DECIMAL(19, 8),
    monthly_output DECIMAL(19, 8),
    efficiency DECIMAL(19, 8),
    vmpp DECIMAL(19, 8),
    impp DECIMAL(19, 8),
    dimensions VARCHAR(191),
    weight DECIMAL(19, 8),
    product_link VARCHAR(512),
    CONSTRAINT pk_solar_panels PRIMARY KEY (id),
    CONSTRAINT uk_panels_brand_model UNIQUE (brand, model)
) ENGINE=InnoDB;

CREATE INDEX idx_panels_brand ON solar_panels (brand);
CREATE INDEX idx_panels_wattage ON solar_panels (wattage);

CREATE TABLE assessments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    location_id BIGINT,
    selected_panel_id BIGINT,
    monthly_consumption DECIMAL(19, 8),
    monthly_bill DECIMAL(19, 8),
    roof_area DECIMAL(19, 8),
    budget DECIMAL(19, 8),
    effective_tariff DECIMAL(19, 8),
    required_roof_area DECIMAL(19, 8),
    recommended_capacity_kw DECIMAL(19, 8),
    actual_capacity_kw DECIMAL(19, 8),
    annual_generation DECIMAL(19, 8),
    annual_savings DECIMAL(19, 8),
    estimated_cost DECIMAL(19, 8),
    payback_years DECIMAL(19, 8),
    co2_reduction DECIMAL(19, 8),
    roof_type VARCHAR(191),
    panel_count INT,
    roof_feasible BOOLEAN NOT NULL,
    within_budget BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_assessments PRIMARY KEY (id),
    CONSTRAINT fk_assessments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_assessments_location FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_assessments_panel FOREIGN KEY (selected_panel_id) REFERENCES solar_panels (id)
) ENGINE=InnoDB;

CREATE INDEX idx_assessments_user_created ON assessments (user_id, created_at);
