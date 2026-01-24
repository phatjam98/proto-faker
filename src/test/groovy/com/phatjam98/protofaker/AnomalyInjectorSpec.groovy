package com.phatjam98.protofaker

import com.phatjam98.protofaker.model.Constraints
import com.phatjam98.protofaker.model.FieldType
import spock.lang.Specification

class AnomalyInjectorSpec extends Specification {

    def injector = new AnomalyInjector()

    def "generates anomalous INTEGER outside max range"() {
        given:
        def constraints = Constraints.builder().min(0.0).max(100.0).build()

        when:
        def result = injector.generateAnomaly(FieldType.INTEGER, constraints)

        then:
        result instanceof Integer
        (Integer) result > 100 || (Integer) result < 0
    }

    def "generates anomalous LONG outside max range"() {
        given:
        def constraints = Constraints.builder().min(0.0).max(1000.0).build()

        when:
        def result = injector.generateAnomaly(FieldType.LONG, constraints)

        then:
        result instanceof Long
        (Long) result > 1000L || (Long) result < 0L
    }

    def "generates anomalous FLOAT far from midpoint"() {
        given:
        def constraints = Constraints.builder().min(0.0).max(100.0).build()

        when:
        def result = injector.generateAnomaly(FieldType.FLOAT, constraints)

        then:
        result instanceof Float
        (Float) result > 100.0f || (Float) result < 0.0f
    }

    def "generates anomalous DOUBLE far from midpoint"() {
        given:
        def constraints = Constraints.builder().min(0.0).max(100.0).build()

        when:
        def result = injector.generateAnomaly(FieldType.DOUBLE, constraints)

        then:
        result instanceof Double
        (Double) result > 100.0 || (Double) result < 0.0
    }

    def "generates anomalous STRING (empty, long, or random bytes)"() {
        given:
        def constraints = Constraints.builder().maxLength(10).build()

        when:
        def results = (0..50).collect { injector.generateAnomaly(FieldType.STRING, constraints) }

        then:
        results.every { it instanceof String }
        // Should produce at least one empty string or one exceeding maxLength
        results.any { ((String) it).isEmpty() || ((String) it).length() > 10 }
    }

    def "generates anomalous TIMESTAMP at epoch or far future"() {
        when:
        def results = (0..20).collect { injector.generateAnomaly(FieldType.TIMESTAMP, null) }

        then:
        results.every { it instanceof Long }
        results.any { (Long) it == 0L || (Long) it == 4070908800000L }
    }

    def "generates anomalous ARRAY (empty or 100+ items)"() {
        when:
        def results = (0..20).collect { injector.generateAnomaly(FieldType.ARRAY, null) }

        then:
        results.every { it instanceof List }
        results.any { ((List) it).isEmpty() || ((List) it).size() >= 100 }
    }

    def "generates anomalous INTEGER without constraints"() {
        when:
        def result = injector.generateAnomaly(FieldType.INTEGER, null)

        then:
        result instanceof Integer
        (Integer) result == Integer.MAX_VALUE / 2 as int
    }

    def "generates anomalous BOOLEAN (unchanged behavior)"() {
        when:
        def result = injector.generateAnomaly(FieldType.BOOLEAN, null)

        then:
        result instanceof Boolean
    }
}
