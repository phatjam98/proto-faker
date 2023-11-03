package com.phatjam98.protofaker;

import com.github.javafaker.Faker;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

  /**
   * Create a new ProtoFaker instance.
   */
  public ProtoFaker(Class<T> clazz) {
    this.clazz = clazz;
    this.faker = new Faker();
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
        if (field.isRepeated()) {
          // Handle repeated fields, e.g., add multiple fake values
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
        fakeData = (long) faker.number().randomNumber(2, false);
        break;
      case SINT32:
      case FIXED32:
      case INT32:
      case SFIXED32:
      case UINT32:
        fakeData = (int) faker.number().randomDigitNotZero();
        break;
      case BOOL:
        fakeData = faker.bool().bool();
        break;
      case STRING:
        // Use Java Faker or similar to generate a random string
        fakeData = faker.funnyName().name();
        break;
      case ENUM:
        List<Descriptors.EnumValueDescriptor> values = field.getEnumType().getValues();
        int randomIndex = faker.number().numberBetween(0, values.size() - 1);
        fakeData = values.get(randomIndex);
        break;
      case BYTES:
        fakeData = ByteString.copyFromUtf8(faker.shakespeare().hamletQuote());
        break;
      case MESSAGE:
        var subProtoFaker = new ProtoFaker<>((Class<T>) builder.getField(field).getClass());
        fakeData = subProtoFaker.fake();
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
}
