package com.phatjam98.protofaker;

import com.github.javafaker.Faker;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoFaker<T extends GeneratedMessageV3> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtoFaker.class);

  private final Faker faker;

  public ProtoFaker() {
    this.faker = new Faker();
  }

  public T fake(Class<T> clazz) {
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

  private Object getFakeDataForField(Descriptors.FieldDescriptor field, T.Builder builder) {
    var type = field.getType();
    switch (type) {
      case DOUBLE:
        return faker.number().randomDouble(2, 0, 100);
      case FLOAT:
        return (float) faker.number().randomDouble(2, 0, 100);
      case SINT64:
      case INT64:
      case FIXED64:
      case UINT64:
      case SFIXED64:
        return (long) faker.number().randomNumber(2, false);
      case SINT32:
      case FIXED32:
      case INT32:
      case SFIXED32:
      case UINT32:
        return (int) faker.number().randomDigitNotZero();
      case BOOL:
        return faker.bool().bool();
      case STRING:
        // Use Java Faker or similar to generate a random string
        return faker.funnyName().name();
      case ENUM:
        List<Descriptors.EnumValueDescriptor> values = field.getEnumType().getValues();
        int randomIndex = faker.number().numberBetween(0, values.size() - 1);
        return values.get(randomIndex);
      case BYTES:
        return ByteString.copyFromUtf8(faker.shakespeare().hamletQuote());
      case MESSAGE:
        var thingy = (Class<T>) builder.getField(field).getClass();
        return fake(thingy);
      default:
        return null;
    }
  }

  private T.Builder getBuilder(Class<T> clazz) {
    T.Builder builder = null;

    try {
      Method newBuilderMethod = clazz.getDeclaredMethod("newBuilder");
      builder = (GeneratedMessageV3.Builder) newBuilderMethod.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("No newBuilder method found", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Cannot access newBuilder method", e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Error invoking newBuilder method", e);
    }

    return builder;
  }

  public T fake(T baseProto) {
    return null;
  }

  public List<T> fakes(Class<T> clazz, int count) {
    return null;
  }

  public List<T> fakes(T baseProto, int count) {
    return null;
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
