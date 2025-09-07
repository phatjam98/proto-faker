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

    def "fake generates repeated fields"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)

        when:
        var result = protoFaker.fake()

        then:
        result.getRepeatedStringValueCount() >= 1
        result.getRepeatedStringValueCount() <= 5
        result.getRepeatedStringValueList().forEach(s -> !s.isEmpty())
        
        result.getRepeatedInt32ValueCount() >= 1
        result.getRepeatedInt32ValueCount() <= 5
        result.getRepeatedInt32ValueList().forEach(i -> i > 0)
        
        result.getRepeatedNestedMessageCount() >= 1
        result.getRepeatedNestedMessageCount() <= 5
        result.getRepeatedNestedMessageList().forEach(m -> {
            assert m.getStringValue() instanceof String
            assert !m.getStringValue().isEmpty()
        })
    }

    def "fake with baseProto preserves repeated fields"() {
        given:
        var baseProto = TestOuterClass.Test.newBuilder()
                .addRepeatedStringValue("Fixed String 1")
                .addRepeatedStringValue("Fixed String 2")
                .setStringValue("Base String")
                .build()
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)

        when:
        var result = protoFaker.fake(baseProto)

        then:
        result.getStringValue() == "Base String"
        result.getRepeatedStringValueCount() >= 2  // Should include our fixed values plus generated ones
        result.getRepeatedStringValueList().contains("Fixed String 1")
        result.getRepeatedStringValueList().contains("Fixed String 2")
    }

    def "fake handles all protobuf field types"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)

        when:
        var result = protoFaker.fake()

        then:
        // Test all numeric types
        result.getInt32Value() != 0
        result.getInt64Value() != 0L
        result.getUint32Value() != 0
        result.getUint64Value() != 0L
        result.getFixed32Value() != 0
        result.getFixed64Value() != 0L
        result.getSfixed32Value() != 0
        result.getSfixed64Value() != 0L
        result.getSint32Value() != 0
        result.getSint64Value() != 0L
        result.getDoubleValue() != 0.0d
        result.getFloatValue() != 0.0f
        
        // Test string and bytes
        !result.getStringValue().isEmpty()
        result.getBytesValue().size() > 0
        
        // Test boolean
        result.getBoolValue() != null
        
        // Test enum
        result.getEnumValue() != TestOuterClass.TestEnum.UNKNOWN_TEST_ENUM
        
        // Test nested message
        result.getNestedMessage() != null
        !result.getNestedMessage().getStringValue().isEmpty()
    }

    def "fake generates contextual string data based on field names"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.UserProfile.class)

        when:
        var result = protoFaker.fake()

        then:
        // Email should look like an email
        result.getEmail().contains("@")
        
        // Names should look like names (not empty, reasonable length)
        !result.getFirstName().isEmpty()
        result.getFirstName().split(" ").length <= 2  // First name shouldn't be multiple words
        !result.getLastName().isEmpty()
        result.getLastName().split(" ").length <= 2   // Last name shouldn't be multiple words
        
        // Phone should look like a phone number (contains digits or dashes)
        result.getPhoneNumber().matches(".*\\d.*")
        
        // Address fields should be reasonable
        !result.getStreetAddress().isEmpty()
        !result.getCity().isEmpty()
        !result.getState().isEmpty()
        !result.getZipCode().isEmpty()
        
        // Company and job should not be empty
        !result.getCompanyName().isEmpty()
        !result.getJobTitle().isEmpty()
        
        // User ID should look like a UUID
        result.getUserId().contains("-")
        result.getUserId().length() > 30  // UUIDs are long
        
        // Description should be a sentence
        result.getDescription().endsWith(".")
        result.getDescription().split(" ").length >= 3  // Should be multiple words
    }

    def "fluent API allows field overrides"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.UserProfile.class)
                .withField("email", "custom@test.com")
                .withField("first_name", "TestFirstName")
                .withField("last_name", "TestLastName")

        when:
        var result = protoFaker.fake()

        then:
        result.getEmail() == "custom@test.com"
        result.getFirstName() == "TestFirstName"
        result.getLastName() == "TestLastName"
        // Other fields should still be generated
        !result.getCity().isEmpty()
        !result.getCompanyName().isEmpty()
    }

    def "fluent API allows repeated count configuration"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)
                .withRepeatedCount(2, 3)  // Between 2 and 3 items

        when:
        var result = protoFaker.fake()

        then:
        result.getRepeatedStringValueCount() >= 2
        result.getRepeatedStringValueCount() <= 3
        result.getRepeatedInt32ValueCount() >= 2
        result.getRepeatedInt32ValueCount() <= 3
        result.getRepeatedNestedMessageCount() >= 2
        result.getRepeatedNestedMessageCount() <= 3
    }

    def "fluent API can combine field overrides and repeated count"() {
        given:
        var protoFaker = new ProtoFaker<>(TestOuterClass.Test.class)
                .withField("string_value", "OverriddenString")
                .withRepeatedCount(1, 1)  // Exactly 1 item
                .withField("repeated_string_value", "OverriddenRepeatedString")

        when:
        var result = protoFaker.fake()

        then:
        result.getStringValue() == "OverriddenString"
        result.getRepeatedStringValueCount() == 1
        result.getRepeatedStringValueList().contains("OverriddenRepeatedString")
        // Other fields should still be generated
        result.getInt32Value() > 0
    }
}
