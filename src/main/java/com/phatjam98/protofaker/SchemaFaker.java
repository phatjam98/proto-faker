package com.phatjam98.protofaker;

import com.github.javafaker.Faker;
import com.phatjam98.protofaker.model.Constraints;
import com.phatjam98.protofaker.model.FieldDefinition;
import com.phatjam98.protofaker.model.FieldType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates fake data payloads from dynamic schema definitions. Unlike {@link ProtoFaker}, this
 * class does not require compiled protobuf classes and produces {@code Map<String, Object>}
 * payloads.
 */
public class SchemaFaker {

  private static final int DEFAULT_MIN_REPEATED = 1;
  private static final int DEFAULT_MAX_REPEATED = 5;

  private final Faker faker;
  private final AnomalyInjector anomalyInjector;
  private final Map<String, Object> fieldOverrides;
  private final Map<String, int[]> repeatedCounts;
  private final Map<String, FieldGenerator> fieldGenerators;
  private double anomalyPercentage;
  private boolean respectConstraints;

  /** Create a new SchemaFaker instance. */
  public SchemaFaker() {
    this.faker = new Faker();
    this.anomalyInjector = new AnomalyInjector();
    this.fieldOverrides = new HashMap<>();
    this.repeatedCounts = new HashMap<>();
    this.fieldGenerators = new HashMap<>();
    this.anomalyPercentage = 0.0;
    this.respectConstraints = true;
  }

  /**
   * Override the value for a specific field.
   *
   * @param fieldName Name of the field to override
   * @param value Value to use for this field
   * @return This SchemaFaker instance for method chaining
   */
  public SchemaFaker withField(String fieldName, Object value) {
    fieldOverrides.put(fieldName, value);
    return this;
  }

  /**
   * Set the repeated count range for a specific field.
   *
   * @param fieldName Name of the array/repeated field
   * @param min Minimum number of items
   * @param max Maximum number of items
   * @return This SchemaFaker instance for method chaining
   */
  public SchemaFaker withRepeatedCount(String fieldName, int min, int max) {
    if (min < 0) {
      throw new IllegalArgumentException(
          "Minimum repeated count must be non-negative for field '" + fieldName + "': min=" + min);
    }
    if (max < min) {
      throw new IllegalArgumentException(
          "Maximum repeated count must be greater than or equal to minimum for field '"
              + fieldName + "': min=" + min + ", max=" + max);
    }
    repeatedCounts.put(fieldName, new int[] {min, max});
    return this;
  }

  /**
   * Set a custom generator for a specific field.
   *
   * @param fieldName Name of the field
   * @param generator Custom generator to use
   * @return This SchemaFaker instance for method chaining
   */
  public SchemaFaker withGenerator(String fieldName, FieldGenerator generator) {
    fieldGenerators.put(fieldName, generator);
    return this;
  }

  /**
   * Enable anomaly injection mode.
   *
   * @param anomalyPercentage Percentage of generated objects that should contain anomalies (0-100)
   * @return This SchemaFaker instance for method chaining
   */
  public SchemaFaker withAnomalyMode(double anomalyPercentage) {
    if (Double.isNaN(anomalyPercentage) || Double.isInfinite(anomalyPercentage)) {
      throw new IllegalArgumentException(
          "anomalyPercentage must be a finite number between 0 and 100");
    }
    double clamped = anomalyPercentage;
    if (clamped < 0.0) {
      clamped = 0.0;
    } else if (clamped > 100.0) {
      clamped = 100.0;
    }
    this.anomalyPercentage = clamped;
    return this;
  }

  /**
   * Set whether to respect field constraints during generation.
   *
   * @param respectConstraints true to respect constraints (default), false to ignore them
   * @return This SchemaFaker instance for method chaining
   */
  public SchemaFaker withConstraints(boolean respectConstraints) {
    this.respectConstraints = respectConstraints;
    return this;
  }

  /**
   * Generate a single fake payload from the given field definitions.
   *
   * @param fields List of field definitions describing the schema
   * @return A map representing the generated fake payload
   */
  public Map<String, Object> fake(List<FieldDefinition> fields) {
    boolean injectAnomaly = shouldInjectAnomaly();
    Map<String, Object> result = new HashMap<>();

    for (FieldDefinition field : fields) {
      String fieldName = field.getName();

      // Skip non-required fields with ~30% probability, unless override/generator configured
      boolean hasOverride = fieldOverrides.containsKey(fieldName);
      boolean hasGenerator = fieldGenerators.containsKey(fieldName);
      if (!field.isRequired() && !hasOverride && !hasGenerator
          && ThreadLocalRandom.current().nextDouble() < 0.3) {
        continue;
      }

      Object value = generateFieldValue(field, injectAnomaly);
      if (value != null) {
        result.put(fieldName, value);
      }
    }

    return result;
  }

  /**
   * Generate multiple fake payloads from the given field definitions.
   *
   * @param fields List of field definitions describing the schema
   * @param count Number of payloads to generate
   * @return A list of maps representing the generated fake payloads
   */
  public List<Map<String, Object>> fakes(List<FieldDefinition> fields, int count) {
    List<Map<String, Object>> results = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      results.add(fake(fields));
    }
    return results;
  }

  private boolean shouldInjectAnomaly() {
    if (anomalyPercentage <= 0) {
      return false;
    }
    return ThreadLocalRandom.current().nextDouble(100) < anomalyPercentage;
  }

  private Object generateFieldValue(FieldDefinition field, boolean injectAnomaly) {
    String fieldName = field.getName();

    // Check for field override
    if (fieldOverrides.containsKey(fieldName)) {
      return fieldOverrides.get(fieldName);
    }

    // Check for custom generator
    if (fieldGenerators.containsKey(fieldName)) {
      return fieldGenerators.get(fieldName)
          .generate(fieldName, field.getType(), field.getConstraints());
    }

    // Handle anomaly injection for a random field in this object
    if (injectAnomaly && shouldAnomalizeField()) {
      Object anomaly = anomalyInjector.generateAnomaly(field.getType(), field.getConstraints());
      if (anomaly != null) {
        return anomaly;
      }
    }

    return generateNormalValue(field);
  }

  private boolean shouldAnomalizeField() {
    // Roughly 1 in 3 chance per field when anomaly mode is triggered
    return ThreadLocalRandom.current().nextInt(3) == 0;
  }

  private Object generateNormalValue(FieldDefinition field) {
    FieldType type = field.getType();
    Constraints constraints = field.getConstraints();

    switch (type) {
      case STRING:
        return generateString(field.getName(), constraints);
      case INTEGER:
        return generateInteger(constraints);
      case LONG:
        return generateLong(constraints);
      case FLOAT:
        return generateFloat(constraints);
      case DOUBLE:
        return generateDouble(constraints);
      case BOOLEAN:
        return faker.bool().bool();
      case TIMESTAMP:
        return generateTimestamp();
      case ARRAY:
        return generateArray(field);
      case OBJECT:
        return generateObject(field);
      case MAP:
        return generateMap(field);
      default:
        return null;
    }
  }

  private String generateString(String fieldName, Constraints constraints) {
    String value = generateContextualString(fieldName);

    if (respectConstraints && constraints != null) {
      if (constraints.getMaxLength() != null && value.length() > constraints.getMaxLength()) {
        value = value.substring(0, constraints.getMaxLength());
      }
    }

    return value;
  }

  private Integer generateInteger(Constraints constraints) {
    int min = 1;
    int max = 10000;

    if (respectConstraints && constraints != null) {
      if (constraints.getMin() != null) {
        min = constraints.getMin().intValue();
      }
      if (constraints.getMax() != null) {
        max = constraints.getMax().intValue();
      }
    }

    if (min == max) {
      return min;
    }
    int upperBound = max < Integer.MAX_VALUE ? max + 1 : max;
    return faker.number().numberBetween(min, upperBound);
  }

  private Long generateLong(Constraints constraints) {
    long min = 1L;
    long max = 10000L;

    if (respectConstraints && constraints != null) {
      if (constraints.getMin() != null) {
        min = constraints.getMin().longValue();
      }
      if (constraints.getMax() != null) {
        max = constraints.getMax().longValue();
      }
    }

    if (min == max) {
      return min;
    }
    long upperBound = max < Long.MAX_VALUE ? max + 1 : max;
    return faker.number().numberBetween(min, upperBound);
  }

  private Float generateFloat(Constraints constraints) {
    double min = 0.0;
    double max = 100.0;

    if (respectConstraints && constraints != null) {
      if (constraints.getMin() != null) {
        min = constraints.getMin();
      }
      if (constraints.getMax() != null) {
        max = constraints.getMax();
      }
    }

    if (min == max) {
      return (float) min;
    }
    return (float) ThreadLocalRandom.current().nextDouble(min, max);
  }

  private Double generateDouble(Constraints constraints) {
    double min = 0.0;
    double max = 100.0;

    if (respectConstraints && constraints != null) {
      if (constraints.getMin() != null) {
        min = constraints.getMin();
      }
      if (constraints.getMax() != null) {
        max = constraints.getMax();
      }
    }

    if (min == max) {
      return min;
    }
    return ThreadLocalRandom.current().nextDouble(min, max);
  }

  private Long generateTimestamp() {
    // Generate a timestamp within the last 30 days
    long now = System.currentTimeMillis();
    long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
    return ThreadLocalRandom.current().nextLong(thirtyDaysAgo, now);
  }

  private List<Object> generateArray(FieldDefinition field) {
    int min = DEFAULT_MIN_REPEATED;
    int max = DEFAULT_MAX_REPEATED;

    if (repeatedCounts.containsKey(field.getName())) {
      int[] counts = repeatedCounts.get(field.getName());
      min = counts[0];
      max = counts[1];
    } else if (respectConstraints && field.getConstraints() != null) {
      if (field.getConstraints().getMinRepeated() != null) {
        min = field.getConstraints().getMinRepeated();
      }
      if (field.getConstraints().getMaxRepeated() != null) {
        max = field.getConstraints().getMaxRepeated();
      }
    }

    // Normalize range to avoid invalid arguments
    if (min < 0) {
      min = 0;
    }
    if (max < min) {
      max = min;
    }

    int count;
    if (min == max) {
      count = min;
    } else {
      int upperBound = max < Integer.MAX_VALUE ? max + 1 : max;
      count = faker.number().numberBetween(min, upperBound);
    }
    List<Object> array = new ArrayList<>();

    if (field.getChildren() != null && !field.getChildren().isEmpty()) {
      // Array of objects
      for (int i = 0; i < count; i++) {
        Map<String, Object> item = new HashMap<>();
        for (FieldDefinition child : field.getChildren()) {
          Object value = generateNormalValue(child);
          if (value != null) {
            item.put(child.getName(), value);
          }
        }
        array.add(item);
      }
    } else {
      // Array of primitives (default to strings)
      for (int i = 0; i < count; i++) {
        array.add(faker.lorem().word());
      }
    }

    return array;
  }

  private Map<String, Object> generateObject(FieldDefinition field) {
    Map<String, Object> obj = new HashMap<>();

    if (field.getChildren() != null) {
      for (FieldDefinition child : field.getChildren()) {
        Object value = generateNormalValue(child);
        if (value != null) {
          obj.put(child.getName(), value);
        }
      }
    }

    return obj;
  }

  private Map<String, Object> generateMap(FieldDefinition field) {
    int min = DEFAULT_MIN_REPEATED;
    int max = DEFAULT_MAX_REPEATED;

    if (repeatedCounts.containsKey(field.getName())) {
      int[] counts = repeatedCounts.get(field.getName());
      min = counts[0];
      max = counts[1];
    } else if (respectConstraints && field.getConstraints() != null) {
      if (field.getConstraints().getMinRepeated() != null) {
        min = field.getConstraints().getMinRepeated();
      }
      if (field.getConstraints().getMaxRepeated() != null) {
        max = field.getConstraints().getMaxRepeated();
      }
    }

    int count;
    if (min == max) {
      count = min;
    } else {
      int upperBound = max < Integer.MAX_VALUE ? max + 1 : max;
      count = faker.number().numberBetween(min, upperBound);
    }
    Map<String, Object> map = new HashMap<>();

    for (int i = 0; i < count; i++) {
      String key = faker.lorem().word() + "_" + i;
      if (field.getChildren() != null && !field.getChildren().isEmpty()) {
        FieldDefinition valueType = field.getChildren().get(0);
        map.put(key, generateNormalValue(valueType));
      } else {
        map.put(key, faker.lorem().word());
      }
    }

    return map;
  }

  /**
   * Generate contextual string data based on field name patterns. Reuses the same heuristic as
   * ProtoFaker for intelligent string generation.
   */
  private String generateContextualString(String fieldName) {
    String lowerFieldName = fieldName.toLowerCase();

    if (lowerFieldName.contains("email") || lowerFieldName.contains("mail")) {
      return faker.internet().emailAddress();
    }
    if (lowerFieldName.contains("firstname") || lowerFieldName.contains("first_name")) {
      return faker.name().firstName();
    }
    if (lowerFieldName.contains("lastname") || lowerFieldName.contains("last_name")) {
      return faker.name().lastName();
    }
    if (lowerFieldName.contains("fullname") || lowerFieldName.contains("full_name")
        || lowerFieldName.equals("name") || lowerFieldName.contains("username")
        || lowerFieldName.contains("displayname") || lowerFieldName.contains("display_name")) {
      return faker.name().fullName();
    }
    if (lowerFieldName.contains("phone") || lowerFieldName.contains("mobile")
        || lowerFieldName.contains("tel")) {
      return faker.phoneNumber().phoneNumber();
    }
    if (lowerFieldName.contains("address") || lowerFieldName.contains("street")) {
      return faker.address().streetAddress();
    }
    if (lowerFieldName.contains("city")) {
      return faker.address().city();
    }
    if (lowerFieldName.contains("state") || lowerFieldName.contains("province")) {
      return faker.address().state();
    }
    if (lowerFieldName.contains("country")) {
      return faker.address().country();
    }
    if (lowerFieldName.contains("zip") || lowerFieldName.contains("postal")) {
      return faker.address().zipCode();
    }
    if (lowerFieldName.contains("url") || lowerFieldName.contains("website")) {
      return faker.internet().url();
    }
    if (lowerFieldName.contains("domain")) {
      return faker.internet().domainName();
    }
    if (lowerFieldName.contains("company") || lowerFieldName.contains("organization")) {
      return faker.company().name();
    }
    if (lowerFieldName.contains("job") || lowerFieldName.contains("position")
        || lowerFieldName.contains("title") || lowerFieldName.contains("role")) {
      return faker.job().title();
    }
    if (lowerFieldName.contains("id") || lowerFieldName.contains("uuid")) {
      return faker.internet().uuid();
    }
    if (lowerFieldName.contains("description") || lowerFieldName.contains("comment")
        || lowerFieldName.contains("note") || lowerFieldName.contains("message")) {
      return faker.lorem().sentence();
    }
    if (lowerFieldName.contains("color") || lowerFieldName.contains("colour")) {
      return faker.color().name();
    }

    return faker.lorem().word();
  }
}
