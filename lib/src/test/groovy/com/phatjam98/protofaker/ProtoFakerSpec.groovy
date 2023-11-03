package com.phatjam98.protofaker

import com.google.protobuf.ByteString
import com.phatjam98.protofaker.test.TestOuterClass
import spock.lang.Specification

class ProtoFakerSpec extends Specification {
    def "fake returns full proto"() {
        given:
        var protoFaker = new ProtoFaker<TestOuterClass.Test>(TestOuterClass.Test.class)

        when:
        TestOuterClass.Test result = protoFaker.fake() as TestOuterClass.Test

        then:
        result.getStringValue() instanceof String
        !result.getStringValue().isEmpty()
        result.getDoubleValue() instanceof Double
        result.getDoubleValue() > 0
        result.getFloatValue() instanceof Float
        result.getFloatValue() > 0
        result.getInt32Value() instanceof Integer
        result.getInt32Value() > 0
        result.getInt64Value() instanceof Long
        result.getInt64Value() > 0
        result.getBoolValue() instanceof Boolean
        result.getEnumValue() instanceof TestOuterClass.TestEnum
        result.getUint32Value() instanceof Integer
        result.getUint32Value() > 0
        result.getUint64Value() instanceof Long
        result.getUint64Value() > 0
        result.getSint32Value() instanceof Integer
        result.getSint32Value() > 0
        result.getSint64Value() instanceof Long
        result.getSint64Value() > 0
        result.getFixed32Value() instanceof Integer
        result.getFixed32Value() > 0
        result.getFixed64Value() instanceof Long
        result.getFixed64Value() > 0
        result.getSfixed32Value() instanceof Integer
        result.getSfixed32Value() > 0
        result.getSfixed64Value() instanceof Long
        result.getSfixed64Value() > 0
        result.getBytesValue() instanceof ByteString
        result.getBytesValue().size() > 0
        result.getNestedMessage() instanceof TestOuterClass.NestedMessage
        result.getNestedMessage().getStringValue() instanceof String
        !result.getNestedMessage().getStringValue().isEmpty()
    }

    def "fake with baseProto"() {
        given:
        var baseProto = TestOuterClass.Test.newBuilder().setStringValue("Provided String").build()
        var protoFaker = new ProtoFaker<TestOuterClass.Test>(TestOuterClass.Test.class)

        when:
        var result = protoFaker.fake(baseProto) as TestOuterClass.Test

        then:
        result.getStringValue() == "Provided String"
        !result.getNestedMessage().getStringValue().isEmpty()
    }

    def "fakes with int count"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)

        when:
        var result = protoFaker.fakes(5)

        then:
        result.size() == 5
        !result.get(1).getStringValue().isEmpty()
    }

    def "fakes with default and count"() {
        given:
        var baseProto = TestOuterClass.Test.newBuilder().setInt32Value(1234).build()
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)

        when:
        var result = protoFaker.fakes(baseProto, 5)

        then:
        result.size() == 5
        result.forEach(r -> r.getInt32Value() == 1234)
        result.forEach(r -> !r.getStringValue().isEmpty())
    }
}
