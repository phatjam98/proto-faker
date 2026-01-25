package com.phatjam98.protofaker;

import com.phatjam98.protofaker.model.Constraints;
import com.phatjam98.protofaker.model.FieldType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates anomalous values for each field type. Used by SchemaFaker when anomaly mode is enabled.
 */
public class AnomalyInjector {

  private static final long EPOCH_ZERO = 0L;
  private static final long YEAR_2099_MILLIS = 4_070_908_800_000L;
  private static final int ANOMALOUS_ARRAY_SIZE = 100;

  /**
   * Generate an anomalous value for the given field type and constraints.
   *
   * @param type The field type
   * @param constraints The field constraints (may be null)
   * @return An anomalous value
   */
  public Object generateAnomaly(FieldType type, Constraints constraints) {
    switch (type) {
      case INTEGER:
        return generateAnomalousInteger(constraints);
      case LONG:
        return generateAnomalousLong(constraints);
      case FLOAT:
        return generateAnomalousFloat(constraints);
      case DOUBLE:
        return generateAnomalousDouble(constraints);
      case STRING:
        return generateAnomalousString(constraints);
      case BOOLEAN:
        return ThreadLocalRandom.current().nextBoolean();
      case TIMESTAMP:
        return generateAnomalousTimestamp();
      case ARRAY:
        return generateAnomalousArray();
      default:
        return null;
    }
  }

  private Integer generateAnomalousInteger(Constraints constraints) {
    if (constraints != null && constraints.getMax() != null) {
      return (int) (constraints.getMax() * 10);
    }
    if (constraints != null && constraints.getMin() != null) {
      return (int) (constraints.getMin() - Math.abs(constraints.getMin()) * 10 - 1);
    }
    return Integer.MAX_VALUE / 2;
  }

  private Long generateAnomalousLong(Constraints constraints) {
    if (constraints != null && constraints.getMax() != null) {
      return (long) (constraints.getMax() * 10);
    }
    if (constraints != null && constraints.getMin() != null) {
      return (long) (constraints.getMin() - Math.abs(constraints.getMin()) * 10 - 1);
    }
    return Long.MAX_VALUE / 2;
  }

  private Float generateAnomalousFloat(Constraints constraints) {
    if (constraints != null && constraints.getMin() != null && constraints.getMax() != null) {
      double midpoint = (constraints.getMin() + constraints.getMax()) / 2.0;
      double range = constraints.getMax() - constraints.getMin();
      double stddev = range / 2.0;
      return (float) (midpoint + 10 * stddev);
    }
    return Float.MAX_VALUE / 2;
  }

  private Double generateAnomalousDouble(Constraints constraints) {
    if (constraints != null && constraints.getMin() != null && constraints.getMax() != null) {
      double midpoint = (constraints.getMin() + constraints.getMax()) / 2.0;
      double range = constraints.getMax() - constraints.getMin();
      double stddev = range / 2.0;
      return midpoint + 10 * stddev;
    }
    return Double.MAX_VALUE / 2;
  }

  private String generateAnomalousString(Constraints constraints) {
    int strategy = ThreadLocalRandom.current().nextInt(3);
    switch (strategy) {
      case 0:
        return "";
      case 1:
        if (constraints != null && constraints.getMaxLength() != null) {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < constraints.getMaxLength() * 3; i++) {
            sb.append('x');
          }
          return sb.toString();
        }
        return "x".repeat(1000);
      case 2:
        byte[] randomBytes = new byte[50];
        ThreadLocalRandom.current().nextBytes(randomBytes);
        return new String(randomBytes, StandardCharsets.ISO_8859_1);
      default:
        return "";
    }
  }

  private Long generateAnomalousTimestamp() {
    if (ThreadLocalRandom.current().nextBoolean()) {
      return EPOCH_ZERO;
    }
    return YEAR_2099_MILLIS;
  }

  private List<Object> generateAnomalousArray() {
    if (ThreadLocalRandom.current().nextBoolean()) {
      return new ArrayList<>();
    }
    List<Object> largeArray = new ArrayList<>();
    for (int i = 0; i < ANOMALOUS_ARRAY_SIZE; i++) {
      largeArray.add("item_" + i);
    }
    return largeArray;
  }
}
