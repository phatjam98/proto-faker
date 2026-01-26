package com.phatjam98.protofaker.model;

import java.util.Objects;

/** Constraints for field value generation. */
public final class Constraints {

  private final Double min;
  private final Double max;
  private final String pattern;
  private final Integer maxLength;
  private final Integer minRepeated;
  private final Integer maxRepeated;

  private Constraints(Builder builder) {
    this.min = builder.min;
    this.max = builder.max;
    this.pattern = builder.pattern;
    this.maxLength = builder.maxLength;
    this.minRepeated = builder.minRepeated;
    this.maxRepeated = builder.maxRepeated;
  }

  public Double getMin() {
    return min;
  }

  public Double getMax() {
    return max;
  }

  public String getPattern() {
    return pattern;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Integer getMinRepeated() {
    return minRepeated;
  }

  public Integer getMaxRepeated() {
    return maxRepeated;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Constraints)) {
      return false;
    }
    Constraints other = (Constraints) obj;
    return Objects.equals(min, other.min)
        && Objects.equals(max, other.max)
        && Objects.equals(pattern, other.pattern)
        && Objects.equals(maxLength, other.maxLength)
        && Objects.equals(minRepeated, other.minRepeated)
        && Objects.equals(maxRepeated, other.maxRepeated);
  }

  @Override
  public int hashCode() {
    return Objects.hash(min, max, pattern, maxLength, minRepeated, maxRepeated);
  }

  @Override
  public String toString() {
    return "Constraints{"
        + "min=" + min
        + ", max=" + max
        + ", pattern='" + pattern + '\''
        + ", maxLength=" + maxLength
        + ", minRepeated=" + minRepeated
        + ", maxRepeated=" + maxRepeated
        + '}';
  }

  /** Builder for Constraints. */
  public static final class Builder {
    private Double min;
    private Double max;
    private String pattern;
    private Integer maxLength;
    private Integer minRepeated;
    private Integer maxRepeated;

    private Builder() {}

    public Builder min(Double min) {
      this.min = min;
      return this;
    }

    public Builder max(Double max) {
      this.max = max;
      return this;
    }

    public Builder pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder maxLength(Integer maxLength) {
      this.maxLength = maxLength;
      return this;
    }

    public Builder minRepeated(Integer minRepeated) {
      this.minRepeated = minRepeated;
      return this;
    }

    public Builder maxRepeated(Integer maxRepeated) {
      this.maxRepeated = maxRepeated;
      return this;
    }

    /**
     * Build the Constraints instance with validation.
     *
     * @return The built Constraints instance
     * @throws IllegalArgumentException if constraints are invalid
     */
    public Constraints build() {
      if (min != null && max != null && min > max) {
        throw new IllegalArgumentException(
            "Invalid constraints: min (" + min + ") must be <= max (" + max + ")");
      }
      if (maxLength != null && maxLength < 0) {
        throw new IllegalArgumentException(
            "Invalid constraints: maxLength (" + maxLength + ") must be >= 0");
      }
      if (minRepeated != null && minRepeated < 0) {
        throw new IllegalArgumentException(
            "Invalid constraints: minRepeated (" + minRepeated + ") must be >= 0");
      }
      if (maxRepeated != null && maxRepeated < 0) {
        throw new IllegalArgumentException(
            "Invalid constraints: maxRepeated (" + maxRepeated + ") must be >= 0");
      }
      if (minRepeated != null && maxRepeated != null && minRepeated > maxRepeated) {
        throw new IllegalArgumentException(
            "Invalid constraints: minRepeated (" + minRepeated
                + ") must be <= maxRepeated (" + maxRepeated + ")");
      }
      return new Constraints(this);
    }
  }
}
