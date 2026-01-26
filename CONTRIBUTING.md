# Contributing to ProtoFaker

Thank you for your interest in contributing to ProtoFaker! This guide will help you get started with development, understand our processes, and ensure your contributions align with the project's standards.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Code Standards and Quality](#code-standards-and-quality)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Release Process](#release-process)
- [Project Structure](#project-structure)
- [Common Development Tasks](#common-development-tasks)
- [Getting Help](#getting-help)

## Getting Started

### Prerequisites

- **Java Development Kit**: OpenJDK 11 or higher
- **Git**: For version control and contribution workflow
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- **Protocol Buffers**: Understanding of protobuf concepts (optional but helpful)

### Quick Setup

1. **Fork and Clone**
   ```bash
   # Fork the repository on GitHub
   git clone https://github.com/YOUR_USERNAME/proto-faker.git
   cd proto-faker
   ```

2. **Verify Setup**
   ```bash
   # Test that everything builds correctly
   ./gradlew build
   
   # Run all tests
   ./gradlew test
   
   # Verify code quality
   ./gradlew checkstyleMain checkstyleTest
   ```

3. **Create Development Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Environment Setup

### Build System

ProtoFaker uses **Gradle** with the Wrapper for consistent builds across environments:

```bash
# Build the project
./gradlew assemble

# Clean and build from scratch
./gradlew clean build

# Run tests with verbose output
./gradlew test --info

# Generate test coverage report
./gradlew jacocoTestReport
```

### IDE Configuration

#### IntelliJ IDEA
1. Import as Gradle project
2. Set Project SDK to Java 11+
3. Enable Google Java Format plugin
4. Configure Checkstyle plugin to use `config/checkstyle/google-java-format.xml`

#### Eclipse
1. Install Gradle Buildship plugin
2. Import existing Gradle project
3. Set workspace JDK to Java 11+
4. Install Checkstyle plugin with Google Java Format rules

#### VS Code
1. Install Extension Pack for Java
2. Install Checkstyle for Java extension
3. Configure workspace settings for Java 11+

### Dependency Management

**Runtime Dependencies:**
- `protobuf-java` - Protocol Buffers Java API
- `javafaker` - Fake data generation
- `slf4j-api` + `logback-classic` - Logging

**Test Dependencies:**
- `spock-core` - BDD testing framework
- `groovy` - Groovy language for Spock tests

**Build Tools:**
- `jreleaser` - Release automation
- `jacoco` - Code coverage
- `checkstyle` - Code style enforcement

## Code Standards and Quality

### Code Style

ProtoFaker follows **Google Java Format** standards, enforced through Checkstyle:

```bash
# Check code style compliance
./gradlew checkstyleMain checkstyleTest

# Recommended: Set up pre-commit hooks
# (Install google-java-format plugin in your IDE)
```

**Key Style Guidelines:**
- 2-space indentation (not tabs)
- 100-character line length limit
- Clear, descriptive variable and method names
- Comprehensive Javadoc for public APIs
- Consistent formatting via Google Java Format

### Code Quality Requirements

**Mandatory Quality Gates:**
- ✅ All tests must pass
- ✅ Checkstyle violations = 0
- ✅ Test coverage ≥ 90% (verified by Jacoco)
- ✅ No compiler warnings
- ✅ Clear, meaningful commit messages

**Quality Check Command:**
```bash
# Run comprehensive quality checks
./gradlew clean build checkstyleMain checkstyleTest jacocoTestReport jacocoTestCoverageVerification
```

### Architecture Principles

1. **Single Responsibility**: Each class should have one clear purpose
2. **Dependency Injection**: Use constructor injection for testability
3. **Immutability**: Prefer immutable objects where possible
4. **Defensive Programming**: Validate inputs and handle edge cases
5. **Performance**: Efficient reflection usage and memory management

## Testing Guidelines

### Testing Framework

ProtoFaker uses **Spock Framework** for comprehensive BDD-style testing:

```groovy
class ProtoFakerSpec extends Specification {
    def "should generate realistic email for email field"() {
        given:
        def protoFaker = new ProtoFaker<>(TestProto)
        
        when:
        def result = protoFaker.fake()
        
        then:
        result.email.contains("@")
        result.email.contains(".")
    }
}
```

### Test Categories

**Unit Tests** (`src/test/groovy/`):
- Test individual methods and classes in isolation
- Mock external dependencies
- Fast execution (< 100ms per test)
- Cover edge cases and error conditions

**Integration Tests**:
- Test full workflows with realistic protobuf messages
- Validate field generation patterns
- Test template-based generation

**Test Coverage Requirements:**
- **Minimum**: 90% line coverage (enforced)
- **Target**: 95%+ line coverage
- **Focus**: Critical path coverage over percentage

### Writing Good Tests

```groovy
def "should generate UUID format for user_id field"() {
    given: "a ProtoFaker configured for User messages"
    def protoFaker = new ProtoFaker<>(UserProto)
    
    when: "generating fake data"
    def fakeUser = protoFaker.fake()
    
    then: "user_id should match UUID pattern"
    fakeUser.userId ==~ /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/
}
```

**Test Best Practices:**
- Use descriptive test names that explain the behavior
- Follow Given-When-Then structure
- Test one behavior per test method
- Use realistic test data
- Test both happy path and edge cases

## Pull Request Process

### Before Creating a PR

1. **Ensure Quality**
   ```bash
   ./gradlew clean build test checkstyleMain checkstyleTest jacocoTestCoverageVerification
   ```

2. **Update Documentation**
   - Update README.md if adding new features
   - Add or update Javadoc for public methods
   - Update API_REFERENCE.md if needed

3. **Follow Conventional Commits**
   ```bash
   git commit -m "feat: add support for custom field generators"
   git commit -m "fix: resolve enum handling for UNKNOWN values"
   git commit -m "docs: update API examples in README"
   ```

### PR Guidelines

**PR Title Format:**
- Use conventional commit format: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`
- Be specific and descriptive
- Examples:
  - `feat: add fluent API for repeated field count configuration`
  - `fix: handle null values in field override methods`
  - `docs: improve API documentation with more examples`

**PR Description Template:**
```markdown
## Summary
Brief description of the changes

## Changes
- List of specific changes made
- Focus on what and why, not how

## Testing
- Description of new tests added
- Manual testing performed
- Coverage impact

## Breaking Changes
- List any breaking changes
- Migration guide if applicable

## Related Issues
- Fixes #123
- Relates to #456
```

**Review Checklist:**
- [ ] Code follows Google Java Format
- [ ] All tests pass locally
- [ ] Test coverage maintained (≥90%)
- [ ] Documentation updated
- [ ] No breaking changes (unless major version)
- [ ] Conventional commit format used

### PR Review Process

1. **Automated Checks**: GitHub Actions CI must pass
2. **Peer Review**: At least one approved review required
3. **Maintainer Review**: Final approval from project maintainer
4. **Merge Strategy**: Squash and merge to maintain clean history

## Release Process

### Version Strategy

ProtoFaker follows [Semantic Versioning](https://semver.org/):

- **PATCH** (0.1.x): Bug fixes, documentation updates, internal improvements
- **MINOR** (0.x.0): New features, API additions (backward compatible)
- **MAJOR** (x.0.0): Breaking changes requiring user migration

### Release Types

**SNAPSHOT Releases** (Automated):
- Continuous deployment from `main` branch
- Available for testing integration changes
- Version format: `0.2.0-SNAPSHOT`

**Production Releases** (Manual):
- Tagged releases with full changelog
- Published to Maven Central
- GitHub releases with artifacts
- Version format: `0.2.0`

### Release Commands

```bash
# Create release (maintainers only)
# 1. Update version in gradle.properties (remove -SNAPSHOT)
# 2. Create and push tag
git tag v0.2.0
git push origin v0.2.0

# 3. GitHub Actions handles the rest:
#    - Runs tests and quality checks
#    - Creates GitHub release
#    - Publishes to Maven Central
#    - Updates documentation
```

## Project Structure

```
proto-faker/
├── src/
│   ├── main/java/com/phatjam98/protofaker/
│   │   ├── ProtoFaker.java           # Core library class for protobuf messages
│   │   ├── SchemaFaker.java          # Dynamic schema-based fake data generation
│   │   ├── AnomalyInjector.java      # Anomaly/edge case value generation
│   │   ├── FieldGenerator.java       # Custom field generation interface
│   │   └── model/
│   │       ├── FieldDefinition.java  # Schema field definition
│   │       ├── FieldType.java        # Supported field types enum
│   │       └── Constraints.java      # Value constraints for generation
│   └── test/groovy/com/phatjam98/protofaker/
│       ├── ProtoFakerSpec.groovy     # ProtoFaker test specification
│       ├── SchemaFakerSpec.groovy    # SchemaFaker test specification
│       ├── AnomalyInjectorSpec.groovy # AnomalyInjector tests
│       └── proto/                    # Test protobuf definitions
├── config/checkstyle/                # Code style configuration
├── .github/workflows/                # CI/CD workflows
├── docs/                            # Additional documentation
├── build.gradle                     # Build configuration
├── jreleaser.yml                   # Release configuration
└── README.md                       # Project documentation
```

### Key Components

**Core Classes:**
- `ProtoFaker<T>` - Main API class for generating fake protobuf data with compiled classes
- `SchemaFaker` - Dynamic schema-based generation without proto compilation
- `AnomalyInjector` - Generates anomalous values for edge case testing
- `FieldGenerator` - Functional interface for custom field generation logic

**Model Classes:**
- `FieldDefinition` - Schema field definition with name, type, constraints
- `FieldType` - Enum of supported types (STRING, INTEGER, ARRAY, OBJECT, etc.)
- `Constraints` - Value constraints (min, max, maxLength, etc.)

**Test Infrastructure:**
- Spock specifications for comprehensive testing
- Sample protobuf messages for validation
- Coverage verification and reporting

**Build System:**
- Gradle multi-plugin setup
- JReleaser integration for publishing
- Quality gates and automation

## Common Development Tasks

### Testing Your Changes in Dependent Projects

**Recommended: SNAPSHOT Publishing**

Instead of using `publishToMavenLocal`, merge your changes to `main` and use the automatically published SNAPSHOT version:

1. Create a PR with your changes
2. Once merged to `main`, CI/CD publishes `0.2.0-SNAPSHOT` to Maven Central
3. In your dependent project, add the SNAPSHOT repository:

```groovy
// build.gradle
repositories {
    mavenCentral()
    maven { url 'https://central.sonatype.com/repository/maven-snapshots/' }
}
dependencies {
    testImplementation 'com.phatjam98:proto-faker:0.2.0-SNAPSHOT'
}
```

**Why SNAPSHOT over mavenLocal?**
- No manual publishing steps
- Consistent with what other users will see
- Avoids local cache issues
- Automatically available to all collaborators

**Local Testing (Pre-PR only)**

For quick iteration before creating a PR:
```bash
./gradlew publishToMavenLocal
```

Then in your dependent project:
```groovy
repositories {
    mavenLocal()
    mavenCentral()
}
```

### Adding New Field Types

1. **Extend Field Resolution Logic**
   ```java
   // In ProtoFaker.java
   private Object generateFieldValue(Descriptors.FieldDescriptor field) {
       // Add new field type handling
   }
   ```

2. **Add Test Coverage**
   ```groovy
   // In ProtoFakerSpec.groovy
   def "should generate appropriate data for new field type"() {
       // Test new field type behavior
   }
   ```

3. **Update Documentation**
   - Add examples to README.md
   - Update API_REFERENCE.md
   - Add field type to supported list

### Adding New Field Name Patterns

1. **Update Pattern Recognition**
   ```java
   private boolean isEmailField(String fieldName) {
       return fieldName.toLowerCase().matches(".*email.*|.*mail.*");
   }
   ```

2. **Add Comprehensive Tests**
   ```groovy
   def "should recognize email patterns in field names"() {
       // Test various email field name patterns
   }
   ```

### Performance Optimization

1. **Profile First**: Use profiling tools to identify bottlenecks
2. **Measure Impact**: Benchmark before and after changes
3. **Cache Reflections**: Cache descriptor lookups for repeated use
4. **Memory Efficiency**: Minimize object creation in tight loops

### Debugging and Troubleshooting

**Enable Debug Logging:**
```java
// Add to test or main code
System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
```

**Common Issues:**
- **ClassLoader Issues**: Ensure protobuf classes are on classpath
- **Reflection Failures**: Check field naming and protobuf compilation
- **Memory Leaks**: Monitor object creation in repeated field generation

## Getting Help

### Documentation Resources

- **README.md**: Usage examples and quick start guide
- **API_REFERENCE.md**: Comprehensive API documentation
- **PROJECT_ARCHITECTURE.md**: High-level design overview
- **RELEASING.md**: Release process and automation details

### Community Support

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and community support
- **Email**: Direct contact at phatjam98@gmail.com for sensitive issues

### Development Support

- **IDE Setup Issues**: Check IDE-specific configuration guides
- **Build Problems**: Verify Java version and Gradle setup
- **Test Failures**: Review test logs and coverage reports

### Contributing Questions

**Before Starting:**
- Check existing issues and discussions
- Review recent PRs for similar changes
- Consider opening an issue for discussion

**During Development:**
- Ask questions in draft PRs
- Request feedback early and often
- Follow established patterns and conventions

**Common Questions:**
- "How do I add support for custom field types?"
- "What's the best way to test protobuf message generation?"
- "How do I handle backward compatibility?"

### Code of Conduct

ProtoFaker follows standard open source community guidelines:

- **Be Respectful**: Treat all contributors with respect
- **Be Collaborative**: Work together to improve the project
- **Be Professional**: Maintain professional communication
- **Be Inclusive**: Welcome contributors from all backgrounds

---

## Thank You

Your contributions help make ProtoFaker better for the entire Protocol Buffers community. Whether you're fixing bugs, adding features, improving documentation, or providing feedback, every contribution is valuable.

**Happy coding!** 🚀