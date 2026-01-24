package com.phatjam98.protofaker;

import com.phatjam98.protofaker.model.Constraints;
import com.phatjam98.protofaker.model.FieldType;

/**
 * Functional interface for custom field value generation. Allows users to plug in custom generation
 * logic for specific fields.
 */
@FunctionalInterface
public interface FieldGenerator {

  /**
   * Generate a value for the given field.
   *
   * @param fieldName The name of the field being generated
   * @param type The declared type of the field
   * @param constraints The constraints defined for the field (may be null)
   * @return The generated value
   */
  Object generate(String fieldName, FieldType type, Constraints constraints);
}
