# Changelog

All notable changes to ProtoFaker will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2024-09-07

### Initial Release - Major Milestone Achievement

ProtoFaker has successfully achieved its first production release to Maven Central, marking the completion of a comprehensive Java library publication pipeline built from scratch.

### Added

#### Core Library Features
- **ProtoFaker Generic Class**: Generate realistic fake data for any Protocol Buffer message type
- **Field-Name-Aware Generation**: Automatic contextual data generation based on field naming patterns
  - Email fields generate valid email addresses
  - Name fields generate realistic names
  - Phone fields generate proper phone number formats
  - Address fields generate street addresses
  - UUID fields generate proper UUID values
- **Fluent API Design**: Method chaining for easy field overrides and configuration
- **Template-Based Generation**: Use existing protobuf messages as templates for data generation
- **Collection Support**: Generate multiple fake messages with `fakes(count)` methods
- **Configurable Repeated Fields**: Control the count range for protobuf repeated fields
- **Comprehensive Field Type Support**: 
  - All primitive types (string, int32, int64, uint32, uint64, etc.)
  - Complex types (bytes, enum, nested messages)
  - Repeated fields with configurable count ranges
  - Recursive nested message generation

#### Technical Infrastructure
- **Java 11 Toolchain**: Modern Java development with backward compatibility
- **Protocol Buffers 3.24.4**: Latest protobuf-java integration
- **JavaFaker 1.0.2**: Realistic fake data generation engine
- **Reflection-Based Architecture**: Dynamic protobuf message introspection
- **SLF4J Logging**: Structured logging with Logback implementation

#### Testing Framework
- **Spock Framework Integration**: Groovy-based BDD testing with comprehensive specifications
- **90%+ Code Coverage**: Jacoco test coverage with verification gates
- **Google Java Format**: Enforced code style consistency via Checkstyle
- **Comprehensive Test Suite**: Full protobuf field type coverage in test scenarios

#### Build and Release Pipeline
- **Modern Gradle Build**: Multi-plugin architecture with dependency management
- **JReleaser 1.15.0**: Automated release orchestration for GitHub and Maven Central
- **Maven Central Portal**: Modern publishing via Central Portal API (not legacy OSSRH)
- **GPG Signing**: Cryptographic artifact verification with RSA 4096-bit key
- **GitHub Actions CI/CD**: Automated testing, quality checks, and release workflows
  - Continuous integration on all PRs and pushes
  - Automated SNAPSHOT publishing from main branch
  - Production release automation via git tags

#### Documentation and Developer Experience
- **Comprehensive README**: Clear usage examples and integration guides
- **Maven Central Integration**: Available via standard Maven/Gradle coordinates
- **Apache 2.0 License**: Open source with enterprise-friendly licensing
- **Professional Metadata**: Complete POM configuration with developer information

### Technical Achievements

#### Release Engineering Excellence
- **Complete CI/CD Pipeline**: From local development to production deployment
- **Multi-Environment Publishing**: Separate workflows for SNAPSHOT and release versions
- **Quality Gates**: Automated code quality verification with configurable thresholds
- **Security Best Practices**: GPG signing, secure credential management, and audit trails

#### Enterprise-Ready Architecture
- **Zero-Configuration Design**: Works out-of-the-box for any protobuf message
- **Extensible Framework**: Clear extension points for custom field generation
- **Performance Optimized**: Efficient reflection-based field resolution
- **Memory Conscious**: No persistent state between generations

#### Modern Development Practices
- **Conventional Commits**: Structured commit messages for automated changelog generation
- **Semantic Versioning**: Clear version progression with compatibility guarantees
- **Automated Quality Assurance**: Continuous code quality monitoring
- **Comprehensive Testing**: Unit, integration, and contract testing coverage

### Dependencies

#### Runtime Dependencies
- `com.google.protobuf:protobuf-java:3.24.4` - Protocol Buffers Java API
- `com.github.javafaker:javafaker:1.0.2` - Realistic fake data generation
- `org.slf4j:slf4j-api:2.0.7` - Structured logging API
- `ch.qos.logback:logback-classic:1.4.8` - Logging implementation

#### Build Dependencies
- `org.apache.groovy:groovy:4.0.15` - Groovy language support for testing
- `org.spockframework:spock-core:2.3-groovy-4.0` - BDD testing framework
- `org.jreleaser:jreleaser-gradle-plugin:1.15.0` - Release automation

### Development Environment

- **Java**: 11+ (tested with OpenJDK 11)
- **Gradle**: 8.5+ with Gradle Wrapper
- **Protocol Buffers**: 3.24.4 protoc compiler
- **Testing**: Spock Framework with Groovy 4.0

### Maven Central Coordinates

```xml
<dependency>
    <groupId>com.phatjam98</groupId>
    <artifactId>proto-faker</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

```groovy
testImplementation 'com.phatjam98:proto-faker:0.1.0'
```

### Breaking Changes

This is the initial release, so no breaking changes apply.

### Migration Guide

This is the initial public release. For users migrating from pre-release versions:

1. Update dependency coordinates to use official Maven Central artifacts
2. Remove any custom repository configurations
3. Verify field override syntax matches the fluent API documentation

### Known Issues

None reported for this release.

### Contributors

- **Travis Carter** (phatjam98@gmail.com) - Project creator and maintainer

---

## [Unreleased] - v0.2.0

### Added

#### SchemaFaker - Dynamic Schema-Based Generation
A new class for generating fake data payloads from dynamic schema definitions without requiring compiled protobuf classes. Produces `Map<String, Object>` payloads.

- **Core Methods:**
  - `fake(List<FieldDefinition> fields)` - Generate a single fake payload
  - `fakes(List<FieldDefinition> fields, int count)` - Generate multiple fake payloads
- **Configuration Methods:**
  - `withField(String fieldName, Object value)` - Override specific field values
  - `withRepeatedCount(String fieldName, int min, int max)` - Control array/repeated field sizes
  - `withGenerator(String fieldName, FieldGenerator generator)` - Custom field generation logic
  - `withAnomalyMode(double percentage)` - Enable anomaly injection (0-100%)
  - `withConstraints(boolean respectConstraints)` - Toggle constraint enforcement

#### AnomalyInjector - Edge Case Test Data Generation
Generates anomalous values outside normal constraints for testing edge cases and boundary conditions.

- **Anomaly Strategies:**
  - Integer/Long: Values 10x beyond max constraint, or well below min
  - Float/Double: Values 10 standard deviations from expected range
  - String: Empty strings, very long strings (3x max length), or strings with random bytes
  - Timestamp: Epoch zero (1970-01-01) or far future (2099)
  - Array: Empty arrays or unusually large arrays (100 items)

#### FieldGenerator Interface
Functional interface for custom per-field generation logic:
```java
@FunctionalInterface
public interface FieldGenerator {
    Object generate(String fieldName, FieldType type, Constraints constraints);
}
```

#### Model Classes
- **FieldDefinition**: Immutable schema field definition
  - Properties: `name`, `type`, `required`, `constraints`, `children`
  - Builder pattern: `FieldDefinition.builder().name("email").type(FieldType.STRING).build()`
- **FieldType**: Enum of supported types
  - Values: `STRING`, `INTEGER`, `LONG`, `FLOAT`, `DOUBLE`, `BOOLEAN`, `TIMESTAMP`, `ARRAY`, `OBJECT`, `MAP`
- **Constraints**: Value constraints for generation
  - Properties: `min`, `max`, `pattern`, `maxLength`, `minRepeated`, `maxRepeated`
  - Validation on build: min <= max, lengths >= 0

#### ProtoFaker Enhancements
- **Map Field Support**: Properly generates protobuf map fields with configurable entry counts
- **Oneof Field Handling**: Randomly selects one field per oneof group; field overrides take priority
- **Cycle Detection**: `withMaxDepth(int maxDepth)` prevents infinite recursion in self-referencing messages (default: 5)
- **Per-Field Repeated Counts**: `withRepeatedCount(String fieldName, int min, int max)` for fine-grained control
- **Input Validation**: Validates min/max constraints with descriptive error messages
- **Outer Class Name Resolution**: Handles underscore-to-CamelCase conversion for proto file names (e.g., `foo_bar.proto` → `FooBarOuterClass`)

### Changed

- Internal refactoring for better separation of concerns
- Improved error messages with field context

### Fixed

- Fixed handling of nested messages in complex proto hierarchies
- Fixed repeated field count edge cases when min equals max

### Development

#### SNAPSHOT Publishing Workflow
- Automated SNAPSHOT publishing from `main` branch to Maven Central
- SNAPSHOT repository: `https://central.sonatype.com/repository/maven-snapshots/`
- Recommended for testing integration changes before production releases

---

### Development Notes

Future releases will follow semantic versioning:
- **PATCH** (0.1.x): Bug fixes and documentation updates
- **MINOR** (0.x.0): New features maintaining backward compatibility
- **MAJOR** (x.0.0): Breaking changes requiring migration

### Release Process

- Automated SNAPSHOT releases from `main` branch
- Production releases via GitHub tags (e.g., `v0.2.0`)
- Changelog generated from conventional commit messages
- Full CI/CD pipeline with quality gates

---

**Note**: This changelog follows the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format. All dates are in ISO 8601 format (YYYY-MM-DD).