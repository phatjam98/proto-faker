package com.phatjam98.protofaker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Definition of a schema field for dynamic fake data generation. */
public final class FieldDefinition {

  private final String name;
  private final FieldType type;
  private final boolean required;
  private final Constraints constraints;
  private final List<FieldDefinition> children;

  private FieldDefinition(Builder builder) {
    this.name = Objects.requireNonNull(builder.name, "name must not be null");
    this.type = Objects.requireNonNull(builder.type, "type must not be null");
    this.required = builder.required;
    this.constraints = builder.constraints;
    // Defensive copy to ensure immutability
    this.children = builder.children == null
        ? null
        : Collections.unmodifiableList(new ArrayList<>(builder.children));
  }

  public String getName() {
    return name;
  }

  public FieldType getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

  public Constraints getConstraints() {
    return constraints;
  }

  /** Child field definitions for OBJECT and ARRAY types. */
  public List<FieldDefinition> getChildren() {
    return children;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FieldDefinition)) {
      return false;
    }
    FieldDefinition other = (FieldDefinition) obj;
    return required == other.required
        && Objects.equals(name, other.name)
        && type == other.type
        && Objects.equals(constraints, other.constraints)
        && Objects.equals(children, other.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, required, constraints, children);
  }

  @Override
  public String toString() {
    return "FieldDefinition{"
        + "name='" + name + '\''
        + ", type=" + type
        + ", required=" + required
        + ", constraints=" + constraints
        + ", children=" + children
        + '}';
  }

  /** Builder for FieldDefinition. */
  public static final class Builder {
    private String name;
    private FieldType type;
    private boolean required;
    private Constraints constraints;
    private List<FieldDefinition> children;

    private Builder() {}

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder type(FieldType type) {
      this.type = type;
      return this;
    }

    public Builder required(boolean required) {
      this.required = required;
      return this;
    }

    public Builder constraints(Constraints constraints) {
      this.constraints = constraints;
      return this;
    }

    public Builder children(List<FieldDefinition> children) {
      this.children = children;
      return this;
    }

    public FieldDefinition build() {
      return new FieldDefinition(this);
    }
  }
}
