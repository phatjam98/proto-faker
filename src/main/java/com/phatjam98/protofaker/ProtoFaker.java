package com.phatjam98.protofaker;

import com.github.javafaker.Faker;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final Class<T> clazz;
  private final Faker faker;
  private final Map<String, Object> fieldOverrides;
  private int minRepeatedCount = 1;
  private int maxRepeatedCount = 5;

  /**
   * Create a new ProtoFaker instance.
   *
   * @param clazz Protobuf Class to generate fake data for
   */
  public ProtoFaker(Class<T> clazz) {
    this.clazz = clazz;
    this.faker = new Faker();
    this.fieldOverrides = new HashMap<>();
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
   * Set the count range for repeated fields.
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
   * Create a new fake protobuf message for the given Protobuf Class. This will generate fake data
   * for all fields in the protobuf message.
   *
   * @return Fake protobuf message
   */
  public T fake() {
    Descriptors.Descriptor descriptor = getDescriptor(clazz);
    T.Builder builder = getBuilder(clazz);

    if (descriptor != null) {
      List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

      for (Descriptors.FieldDescriptor field : fields) {
        // Check if field has an override
        if (fieldOverrides.containsKey(field.getName())) {
          Object overrideValue = fieldOverrides.get(field.getName());
          if (field.isRepeated()) {
            // For repeated fields, the override should be a single value that we add once
            builder.addRepeatedField(field, overrideValue);
          } else {
            builder.setField(field, overrideValue);
          }
        } else if (field.isRepeated()) {
          // Generate multiple items for repeated fields using configured count range
          int count = faker.number().numberBetween(minRepeatedCount, maxRepeatedCount);
          for (int i = 0; i < count; i++) {
            Object fakeData = getFakeDataForField(field, builder);
            if (fakeData != null) {
              builder.addRepeatedField(field, fakeData);
            }
          }
        } else {
          Object fakeData = getFakeDataForField(field, builder);
          if (fakeData != null) {
            builder.setField(field, fakeData);
          }
        }
      }
    }

    return (T) builder.build();
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

  private Object getFakeDataForField(Descriptors.FieldDescriptor field, T.Builder builder) {
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
        // Skip the first value if it's UNKNOWN (index 0) and we have other values
        int startIndex = (values.size() > 1 && values.get(0).getName().contains("UNKNOWN")) ? 1 : 0;
        int randomIndex = faker.number().numberBetween(startIndex, values.size() - 1);
        fakeData = values.get(randomIndex);
        break;
      case BYTES:
        fakeData = ByteString.copyFromUtf8(faker.shakespeare().hamletQuote());
        break;
      case MESSAGE:
        // Get the message class from the field descriptor
        String messageClassName = field.getMessageType().getFullName();
        try {
          // Convert protobuf package name to Java package name
          String javaPackage = field.getFile().getOptions().getJavaPackage();
          if (javaPackage.isEmpty()) {
            javaPackage = field.getFile().getPackage();
          }
          String outerClassName = field.getFile().getOptions().getJavaOuterClassname();
          if (outerClassName.isEmpty()) {
            // Generate outer class name from file name
            String fileName = field.getFile().getName();
            outerClassName = fileName.substring(fileName.lastIndexOf("/") + 1, 
                fileName.lastIndexOf(".proto"));
            outerClassName = Character.toUpperCase(outerClassName.charAt(0)) 
                + outerClassName.substring(1) + "OuterClass";
          }
          
          String fullClassName = javaPackage + "." + outerClassName + "$" 
              + field.getMessageType().getName();
          Class<?> messageClass = Class.forName(fullClassName);
          
          @SuppressWarnings("rawtypes")
          var subProtoFaker = new ProtoFaker(messageClass);
          fakeData = subProtoFaker.fake();
        } catch (ClassNotFoundException e) {
          LOGGER.error("Could not find message class for field {}", field.getName(), e);
          fakeData = null;
        }
        break;
      default:
        fakeData = null;
    }

    return fakeData;
  }

  private T.Builder getBuilder(Class<T> clazz) {
    T.Builder builder = null;

    try {
      Method newBuilderMethod = clazz.getDeclaredMethod("newBuilder");
      builder = (T.Builder) newBuilderMethod.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("No newBuilder method found", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Cannot access newBuilder method", e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Error invoking newBuilder method", e);
    }

    return builder;
  }

  private Descriptors.Descriptor getDescriptor(
      Class<T> clazz) {
    Method getDescriptor = null;
    Descriptors.Descriptor descriptor = null;

    try {
      getDescriptor = clazz.getMethod("getDescriptor");
    } catch (NoSuchMethodException e) {
      LOGGER.error("The method getDescriptor was not found on class {}", clazz.getName(), e);
    }

    if (getDescriptor != null) {
      try {
        descriptor = (Descriptors.Descriptor) getDescriptor.invoke(null);
      } catch (IllegalAccessException | InvocationTargetException e) {
        LOGGER.error("Error occurred trying to invoke getDescriptor method", e);
      }
    }

    return descriptor;
  }

  /**
   * Generate contextual string data based on field name patterns.
   */
  private String generateContextualString(String fieldName) {
    String lowerFieldName = fieldName.toLowerCase();
    
    // Email patterns
    if (lowerFieldName.contains("email") || lowerFieldName.contains("mail")) {
      return faker.internet().emailAddress();
    }
    
    // Name patterns
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
    
    // Phone patterns
    if (lowerFieldName.contains("phone") || lowerFieldName.contains("mobile") 
        || lowerFieldName.contains("tel") || lowerFieldName.contains("number")) {
      return faker.phoneNumber().phoneNumber();
    }
    
    // Address patterns
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
    
    // Internet/Web patterns
    if (lowerFieldName.contains("url") || lowerFieldName.contains("website")) {
      return faker.internet().url();
    }
    if (lowerFieldName.contains("domain")) {
      return faker.internet().domainName();
    }
    
    // Company/Business patterns
    if (lowerFieldName.contains("company") || lowerFieldName.contains("organization")) {
      return faker.company().name();
    }
    if (lowerFieldName.contains("job") || lowerFieldName.contains("position") 
        || lowerFieldName.contains("title") || lowerFieldName.contains("role")) {
      return faker.job().title();
    }
    
    // ID patterns
    if (lowerFieldName.contains("id") || lowerFieldName.contains("uuid")) {
      return faker.internet().uuid();
    }
    
    // Text content patterns
    if (lowerFieldName.contains("description") || lowerFieldName.contains("comment") 
        || lowerFieldName.contains("note") || lowerFieldName.contains("message")) {
      return faker.lorem().sentence();
    }
    
    // Color patterns
    if (lowerFieldName.contains("color") || lowerFieldName.contains("colour")) {
      return faker.color().name();
    }
    
    // Default to funny names (original behavior)
    return faker.funnyName().name();
  }
}
