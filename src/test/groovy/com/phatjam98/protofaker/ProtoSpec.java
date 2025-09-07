package com.phatjam98.protofaker;

/**
 * ProtoSpec is used to generate fake data for protobuf messages. This is useful for testing
 * purposes. It utilizes Java Faker to generate fake data. It is not meant to be used in production
 * code.
 *
 * @param <T> Some generated Protobuf Class
 */
public interface ProtoSpec<T> {

  Class<T> getKlass();

}
