package com.phatjam98.protofaker;

import com.github.javafaker.Faker;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProtoFaker is used to generate fake data for protobuf messages. This is useful for testing
 * purposes. It utilizes Java Faker to generate fake data. It is not meant to be used in production
 * code.
 *
 * @param <T> Some generated Protobuf Class
 */
public class ProtoFaker<T extends GeneratedMessageV3> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtoFaker.class);
  private static final int DEFAULT_MAX_DEPTH = 5;

  private final Class<T> clazz;
  private final Faker faker;
  private final Map<String, Object> fieldOverrides;
  private final Map<String, int[]> perFieldRepeatedCounts;
  private int minRepeatedCount = 1;
  private int maxRepeatedCount = 5;
  private int maxDepth = DEFAULT_MAX_DEPTH;

  /**
   * Create a new ProtoFaker instance.
   *
   * @param clazz Protobuf Class to generate fake data for
   */
  public ProtoFaker(Class<T> clazz) {
    this.clazz = clazz;
    this.faker = new Faker();
    this.fieldOverrides = new HashMap<>();
    this.perFieldRepeatedCounts = new HashMap<>();
  }

  /**
   * Override the value for a specific field.
   *
   * @param fieldName Name of the field to override
   * @param value Value to use for this field
   * @return This ProtoFaker instance for method chaining
   */
  public ProtoFaker<T> withField(String fieldName, Object value) {
    fieldOverrides.put(fieldName, value);
    return this;
  }

  /**
   * Set the global count range for repeated fields.
   *
   * @param minCount Minimum number of items in repeated fields
   * @param maxCount Maximum number of items in repeated fields
   * @return This ProtoFaker instance for method chaining
   */
  public ProtoFaker<T> withRepeatedCount(int minCount, int maxCount) {
    this.minRepeatedCount = minCount;
    this.maxRepeatedCount = maxCount;
    return this;
  }

  /**
   * Set the count range for a specific repeated field.
   *
   * @param fieldName Name of the repeated field
   * @param minCount Minimum number of items
   * @param maxCount Maximum number of items
   * @return This ProtoFaker instance for method chaining
   */
  public ProtoFaker<T> withRepeatedCount(String fieldName, int minCount, int maxCount) {
    perFieldRepeatedCounts.put(fieldName, new int[] {minCount, maxCount});
    return this;
  }

  /**
   * Set the maximum recursion depth for self-referencing messages.
   *
   * @param maxDepth Maximum depth (default: 5)
   * @return This ProtoFaker instance for method chaining
   */
  public ProtoFaker<T> withMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  /**
   * Create a new fake protobuf message for the given Protobuf Class. This will generate fake data
   * for all fields in the protobuf message.
   *
   * @return Fake protobuf message
   */
  public T fake() {
    return fakeWithDepthTracking(clazz, new HashSet<>(), 0);
  }

  /**
   * Create a new fake protobuf message using a base protobuf message as a template. This will
   * merge the populated fields from the base protobuf message with fake data for all other fields.
   *
   * @param baseProto Base protobuf message to use as a template
   * @return Fake protobuf message
   */
  public T fake(T baseProto) {
    var fakeProto = fake();
    var builder = fakeProto.toBuilder();
    builder.mergeFrom(baseProto);

    return (T) builder.build();
  }

  /**
   * Create a list of fake protobuf messages.
   *
   * @param count Number of fake protobuf messages to create
   * @return List of fake protobuf messages
   */
  public List<T> fakes(int count) {
    List<T> fakeProtos = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      fakeProtos.add(fake());
    }

    return fakeProtos;
  }

  /**
   * Create a list of fake protobuf messages using a base protobuf message as a template.
   *
   * @param baseProto Base protobuf message to use as a template
   * @param count Number of fake protobuf messages to create
   * @return List of fake protobuf messages
   */
  public List<T> fakes(T baseProto, int count) {
    List<T> fakeProtos = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      fakeProtos.add(fake(baseProto));
    }

    return fakeProtos;
  }

  /**
   * Create a list of fake protobuf messages using a list of base protobuf messages as a template.
   *
   * @param baseProtos List of base protobuf messages to use as a template
   * @return List of fake protobuf messages
   */
  public List<T> fakes(List<T> baseProtos) {
    List<T> fakeProtos = new ArrayList<>();

    for (T baseProto : baseProtos) {
      fakeProtos.add(fake(baseProto));
    }

    return fakeProtos;
  }

  @SuppressWarnings("unchecked")
  private <M extends GeneratedMessageV3> M fakeWithDepthTracking(
      Class<M> messageClass, Set<String> ancestorTypes, int depth) {
    Descriptors.Descriptor descriptor = getDescriptorForClass(messageClass);
    GeneratedMessageV3.Builder<?> builder = getBuilderForClass(messageClass);

    if (descriptor == null || builder == null) {
      return null;
    }

    String typeName = descriptor.getFullName();

    // Cycle detection: if we've seen this type in our ancestor chain, stop
    if (ancestorTypes.contains(typeName) || depth >= maxDepth) {
      return (M) builder.build();
    }

    Set<String> currentAncestors = new HashSet<>(ancestorTypes);
    currentAncestors.add(typeName);

    // Collect oneof fields that have been set
    Set<Descriptors.FieldDescriptor> oneofSelectedFields = new HashSet<>();
    Map<Descriptors.OneofDescriptor, Descriptors.FieldDescriptor> oneofSelections = new HashMap<>();

    // Pre-select one field for each oneof group
    for (Descriptors.OneofDescriptor oneof : descriptor.getOneofs()) {
      List<Descriptors.FieldDescriptor> oneofFields = oneof.getFields();
      if (!oneofFields.isEmpty()) {
        int randomIndex = faker.number().numberBetween(0, oneofFields.size());
        Descriptors.FieldDescriptor selected = oneofFields.get(randomIndex);
        oneofSelections.put(oneof, selected);
        oneofSelectedFields.add(selected);
      }
    }

    List<Descriptors.FieldDescriptor> fields = descriptor.getFields();
    for (Descriptors.FieldDescriptor field : fields) {
      // Skip oneof fields that weren't selected
      if (field.getContainingOneof() != null && !oneofSelectedFields.contains(field)) {
        continue;
      }

      if (fieldOverrides.containsKey(field.getName())) {
        Object overrideValue = fieldOverrides.get(field.getName());
        if (field.isRepeated()) {
          builder.addRepeatedField(field, overrideValue);
        } else {
          builder.setField(field, overrideValue);
        }
      } else if (field.isMapField()) {
        populateMapField(field, builder, currentAncestors, depth);
      } else if (field.isRepeated()) {
        int min = minRepeatedCount;
        int max = maxRepeatedCount;
        if (perFieldRepeatedCounts.containsKey(field.getName())) {
          int[] counts = perFieldRepeatedCounts.get(field.getName());
          min = counts[0];
          max = counts[1];
        }
        int count = faker.number().numberBetween(min, max);
        for (int i = 0; i < count; i++) {
          Object fakeData = getFakeDataForField(field, currentAncestors, depth);
          if (fakeData != null) {
            builder.addRepeatedField(field, fakeData);
          }
        }
      } else {
        Object fakeData = getFakeDataForField(field, currentAncestors, depth);
        if (fakeData != null) {
          builder.setField(field, fakeData);
        }
      }
    }

    return (M) builder.build();
  }

  private void populateMapField(
      Descriptors.FieldDescriptor field,
      GeneratedMessageV3.Builder<?> builder,
      Set<String> ancestorTypes,
      int depth) {
    Descriptors.Descriptor entryDescriptor = field.getMessageType();
    Descriptors.FieldDescriptor keyField = entryDescriptor.findFieldByName("key");
    Descriptors.FieldDescriptor valueField = entryDescriptor.findFieldByName("value");

    int count = faker.number().numberBetween(1, 5);
    for (int i = 0; i < count; i++) {
      Message.Builder entryBuilder = builder.newBuilderForField(field);

      Object key = getFakeDataForField(keyField, ancestorTypes, depth);
      if (key != null) {
        entryBuilder.setField(keyField, key);
      }

      Object value = getFakeDataForField(valueField, ancestorTypes, depth);
      if (value != null) {
        entryBuilder.setField(valueField, value);
      }

      builder.addRepeatedField(field, entryBuilder.build());
    }
  }

  private Object getFakeDataForField(
      Descriptors.FieldDescriptor field, Set<String> ancestorTypes, int depth) {
    var type = field.getType();
    Object fakeData;

    switch (type) {
      case DOUBLE:
        fakeData = faker.number().randomDouble(2, 0, 100);
        break;
      case FLOAT:
        fakeData = (float) faker.number().randomDouble(2, 0, 100);
        break;
      case SINT64:
      case INT64:
      case FIXED64:
      case UINT64:
      case SFIXED64:
        fakeData = faker.number().numberBetween(1L, 10000L);
        break;
      case SINT32:
      case FIXED32:
      case INT32:
      case SFIXED32:
      case UINT32:
        fakeData = faker.number().numberBetween(1, 10000);
        break;
      case BOOL:
        fakeData = faker.bool().bool();
        break;
      case STRING:
        fakeData = generateContextualString(field.getName());
        break;
      case ENUM:
        List<Descriptors.EnumValueDescriptor> values = field.getEnumType().getValues();
        int startIndex =
            (values.size() > 1 && values.get(0).getName().contains("UNKNOWN")) ? 1 : 0;
        int randomIndex = faker.number().numberBetween(startIndex, values.size() - 1);
        fakeData = values.get(randomIndex);
        break;
      case BYTES:
        fakeData = ByteString.copyFromUtf8(faker.shakespeare().hamletQuote());
        break;
      case MESSAGE:
        fakeData = generateNestedMessage(field, ancestorTypes, depth);
        break;
      default:
        fakeData = null;
    }

    return fakeData;
  }

  @SuppressWarnings("unchecked")
  private Object generateNestedMessage(
      Descriptors.FieldDescriptor field, Set<String> ancestorTypes, int depth) {
    String javaPackage = field.getFile().getOptions().getJavaPackage();
    if (javaPackage.isEmpty()) {
      javaPackage = field.getFile().getPackage();
    }
    String outerClassName = field.getFile().getOptions().getJavaOuterClassname();
    if (outerClassName.isEmpty()) {
      String fileName = field.getFile().getName();
      outerClassName =
          fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".proto"));
      outerClassName =
          Character.toUpperCase(outerClassName.charAt(0)) + outerClassName.substring(1)
              + "OuterClass";
    }

    String fullClassName =
        javaPackage + "." + outerClassName + "$" + field.getMessageType().getName();
    try {
      Class<?> messageClass = Class.forName(fullClassName);
      return fakeWithDepthTracking(
          (Class<GeneratedMessageV3>) messageClass, ancestorTypes, depth + 1);
    } catch (ClassNotFoundException e) {
      LOGGER.error("Could not find message class for field {}", field.getName(), e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private <M extends GeneratedMessageV3> GeneratedMessageV3.Builder<?> getBuilderForClass(
      Class<M> clazz) {
    try {
      Method newBuilderMethod = clazz.getDeclaredMethod("newBuilder");
      return (GeneratedMessageV3.Builder<?>) newBuilderMethod.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("Error getting builder for class {}", clazz.getName(), e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private <M extends GeneratedMessageV3> Descriptors.Descriptor getDescriptorForClass(
      Class<M> clazz) {
    try {
      Method getDescriptor = clazz.getMethod("getDescriptor");
      return (Descriptors.Descriptor) getDescriptor.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("Error getting descriptor for class {}", clazz.getName(), e);
      return null;
    }
  }

  /** Generate contextual string data based on field name patterns. */
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
        || lowerFieldName.contains("tel") || lowerFieldName.contains("number")) {
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

    return faker.funnyName().name();
  }
}
