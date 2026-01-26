package com.phatjam98.protofaker

import com.phatjam98.protofaker.model.Constraints
import com.phatjam98.protofaker.model.FieldDefinition
import com.phatjam98.protofaker.model.FieldType
import spock.lang.Specification

class SchemaFakerSpec extends Specification {

    def schemaFaker = new SchemaFaker()

    def "fake generates STRING field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("label")
                .type(FieldType.STRING)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("label")
        result.get("label") instanceof String
        !((String) result.get("label")).isEmpty()
    }

    def "fake generates INTEGER field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("count")
                .type(FieldType.INTEGER)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("count")
        result.get("count") instanceof Integer
        (Integer) result.get("count") >= 1
    }

    def "fake generates LONG field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("timestamp")
                .type(FieldType.LONG)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("timestamp")
        result.get("timestamp") instanceof Long
    }

    def "fake generates FLOAT field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("score")
                .type(FieldType.FLOAT)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("score")
        result.get("score") instanceof Float
    }

    def "fake generates DOUBLE field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("latitude")
                .type(FieldType.DOUBLE)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("latitude")
        result.get("latitude") instanceof Double
    }

    def "fake generates BOOLEAN field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("active")
                .type(FieldType.BOOLEAN)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("active")
        result.get("active") instanceof Boolean
    }

    def "fake generates TIMESTAMP field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("created_at")
                .type(FieldType.TIMESTAMP)
                .required(true)
                .build()
        ]
        // Capture time bounds before generating to avoid race condition
        def toleranceMillis = 1000L
        def beforeNow = System.currentTimeMillis()
        def thirtyDaysAgo = beforeNow - (30L * 24 * 60 * 60 * 1000) - toleranceMillis

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("created_at")
        result.get("created_at") instanceof Long
        def afterNow = System.currentTimeMillis() + toleranceMillis
        (Long) result.get("created_at") >= thirtyDaysAgo
        (Long) result.get("created_at") <= afterNow
    }

    def "fake generates multiple fields"() {
        given:
        def fields = [
            FieldDefinition.builder().name("name").type(FieldType.STRING).required(true).build(),
            FieldDefinition.builder().name("age").type(FieldType.INTEGER).required(true).build(),
            FieldDefinition.builder().name("active").type(FieldType.BOOLEAN).required(true).build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.size() == 3
        result.containsKey("name")
        result.containsKey("age")
        result.containsKey("active")
    }

    def "fakes generate multiple payloads"() {
        given:
        def fields = [
            FieldDefinition.builder().name("id").type(FieldType.STRING).required(true).build()
        ]

        when:
        def results = schemaFaker.fakes(fields, 10)

        then:
        results.size() == 10
        results.every { it.containsKey("id") }
    }

    def "fake respects min/max constraints on INTEGER"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("age")
                .type(FieldType.INTEGER)
                .required(true)
                .constraints(Constraints.builder().min(18.0).max(65.0).build())
                .build()
        ]

        when:
        def results = schemaFaker.fakes(fields, 100)

        then:
        results.every { (Integer) it.get("age") >= 18 && (Integer) it.get("age") <= 65 }
    }

    def "fake respects min/max constraints on DOUBLE"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("temperature")
                .type(FieldType.DOUBLE)
                .required(true)
                .constraints(Constraints.builder().min(-40.0).max(50.0).build())
                .build()
        ]

        when:
        def results = schemaFaker.fakes(fields, 100)

        then:
        results.every { (Double) it.get("temperature") >= -40.0 && (Double) it.get("temperature") <= 50.0 }
    }

    def "fake respects maxLength constraint on STRING"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("code")
                .type(FieldType.STRING)
                .required(true)
                .constraints(Constraints.builder().maxLength(5).build())
                .build()
        ]

        when:
        def results = schemaFaker.fakes(fields, 50)

        then:
        results.every { ((String) it.get("code")).length() <= 5 }
    }

    def "fake generates ARRAY field with default bounds"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("tags")
                .type(FieldType.ARRAY)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("tags")
        result.get("tags") instanceof List
        def tags = (List) result.get("tags")
        tags.size() >= 1
        tags.size() <= 5
    }

    def "fake generates ARRAY field with minRepeated/maxRepeated constraints"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("items")
                .type(FieldType.ARRAY)
                .required(true)
                .constraints(Constraints.builder().minRepeated(3).maxRepeated(7).build())
                .build()
        ]

        when:
        def results = schemaFaker.fakes(fields, 50)

        then:
        results.every {
            def items = (List) it.get("items")
            items.size() >= 3 && items.size() <= 7
        }
    }

    def "fake generates ARRAY of OBJECTs when children defined"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("users")
                .type(FieldType.ARRAY)
                .required(true)
                .children([
                    FieldDefinition.builder().name("name").type(FieldType.STRING).required(true).build(),
                    FieldDefinition.builder().name("age").type(FieldType.INTEGER).required(true).build()
                ])
                .constraints(Constraints.builder().minRepeated(2).maxRepeated(2).build())
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("users")
        def users = (List) result.get("users")
        users.size() == 2
        users.every { it instanceof Map }
        users.every { ((Map) it).containsKey("name") && ((Map) it).containsKey("age") }
    }

    def "fake generates OBJECT field with children"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("address")
                .type(FieldType.OBJECT)
                .required(true)
                .children([
                    FieldDefinition.builder().name("street").type(FieldType.STRING).required(true).build(),
                    FieldDefinition.builder().name("city").type(FieldType.STRING).required(true).build(),
                    FieldDefinition.builder().name("zip_code").type(FieldType.STRING).required(true).build()
                ])
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("address")
        def address = (Map) result.get("address")
        address.containsKey("street")
        address.containsKey("city")
        address.containsKey("zip_code")
    }

    def "fake generates MAP field"() {
        given:
        def fields = [
            FieldDefinition.builder()
                .name("metadata")
                .type(FieldType.MAP)
                .required(true)
                .build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        result.containsKey("metadata")
        result.get("metadata") instanceof Map
        ((Map) result.get("metadata")).size() >= 1
    }

    def "fake generates contextual strings based on field names"() {
        given:
        def fields = [
            FieldDefinition.builder().name("email").type(FieldType.STRING).required(true).build(),
            FieldDefinition.builder().name("first_name").type(FieldType.STRING).required(true).build(),
            FieldDefinition.builder().name("city").type(FieldType.STRING).required(true).build(),
            FieldDefinition.builder().name("user_id").type(FieldType.STRING).required(true).build()
        ]

        when:
        def result = schemaFaker.fake(fields)

        then:
        ((String) result.get("email")).contains("@")
        !((String) result.get("first_name")).isEmpty()
        !((String) result.get("city")).isEmpty()
        ((String) result.get("user_id")).contains("-")
    }

    def "withField overrides specific field value"() {
        given:
        def faker = new SchemaFaker().withField("status", "ACTIVE")
        def fields = [
            FieldDefinition.builder().name("status").type(FieldType.STRING).required(true).build(),
            FieldDefinition.builder().name("name").type(FieldType.STRING).required(true).build()
        ]

        when:
        def result = faker.fake(fields)

        then:
        result.get("status") == "ACTIVE"
        result.get("name") instanceof String
    }

    def "withRepeatedCount overrides array size for specific field"() {
        given:
        def faker = new SchemaFaker().withRepeatedCount("tags", 5, 5)
        def fields = [
            FieldDefinition.builder().name("tags").type(FieldType.ARRAY).required(true).build()
        ]

        when:
        def result = faker.fake(fields)

        then:
        ((List) result.get("tags")).size() == 5
    }

    def "withGenerator uses custom generator for field"() {
        given:
        def faker = new SchemaFaker()
            .withGenerator("latitude", { name, type, constraints -> 42.3601 } as FieldGenerator)
        def fields = [
            FieldDefinition.builder().name("latitude").type(FieldType.DOUBLE).required(true).build()
        ]

        when:
        def result = faker.fake(fields)

        then:
        result.get("latitude") == 42.3601
    }

    def "withAnomalyMode injects anomalies at specified percentage"() {
        given:
        def faker = new SchemaFaker().withAnomalyMode(100.0)
        def fields = [
            FieldDefinition.builder()
                .name("temperature")
                .type(FieldType.DOUBLE)
                .required(true)
                .constraints(Constraints.builder().min(0.0).max(100.0).build())
                .build()
        ]

        when:
        def results = faker.fakes(fields, 100)
        def anomalousCount = results.count {
            def temp = (Double) it.get("temperature")
            temp < 0.0 || temp > 100.0
        }

        then:
        // With 100% anomaly mode and 1/3 field chance, expect some anomalies
        anomalousCount > 0
    }

    def "withAnomalyMode at 0 produces no anomalies"() {
        given:
        def faker = new SchemaFaker().withAnomalyMode(0.0)
        def fields = [
            FieldDefinition.builder()
                .name("count")
                .type(FieldType.INTEGER)
                .required(true)
                .constraints(Constraints.builder().min(1.0).max(100.0).build())
                .build()
        ]

        when:
        def results = faker.fakes(fields, 100)

        then:
        results.every {
            def val = (Integer) it.get("count")
            val >= 1 && val <= 100
        }
    }

    def "withConstraints false ignores constraints"() {
        given:
        def faker = new SchemaFaker().withConstraints(false)
        def fields = [
            FieldDefinition.builder()
                .name("value")
                .type(FieldType.INTEGER)
                .required(true)
                .constraints(Constraints.builder().min(1.0).max(5.0).build())
                .build()
        ]

        when:
        def results = faker.fakes(fields, 100)

        then:
        // Without constraints, values should use default range (1-10000)
        results.any { (Integer) it.get("value") > 5 }
    }
}
