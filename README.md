# ProtoFaker

[![Build Status](https://github.com/phatjam98/proto-faker/actions/workflows/ci.yml/badge.svg)](https://github.com/phatjam98/proto-faker/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.phatjam98/proto-faker.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.phatjam98/proto-faker)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage](https://img.shields.io/badge/coverage-90%2B%25-green.svg)](https://github.com/phatjam98/proto-faker)

**ProtoFaker** is a powerful Java library that generates realistic fake data for Protocol Buffer messages, making test data creation effortless and eliminating boilerplate code in your tests.

## ✨ Features

- 🚀 **Zero Configuration**: Generate realistic fake data for any protobuf message out-of-the-box
- 🎯 **Field-Name-Aware**: Automatically generates contextually appropriate data (emails look like emails, names like names)
- 🔧 **Fluent API**: Easy field overrides with method chaining
- 📊 **Full Field Support**: All protobuf field types including nested messages and repeated fields
- 🎲 **Customizable**: Control repeated field counts, override specific fields, and configure generation behavior
- 📈 **Test-Friendly**: Designed specifically for testing scenarios with seamless integration

## 🚀 Quick Start

### Maven
```xml
<dependency>
    <groupId>com.phatjam98</groupId>
    <artifactId>proto-faker</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

### Gradle
```groovy
testImplementation 'com.phatjam98:proto-faker:0.1.0'
```

### Gradle (Kotlin DSL)
```kotlin
testImplementation("com.phatjam98:proto-faker:0.1.0")
```

✅ **Now available on Maven Central!** Get started with the official 0.1.0 release.

## 📖 Usage

### Basic Usage

Generate a complete fake protobuf message:

```java
// Your protobuf class
var protoFaker = new ProtoFaker<>(UserProto.class);
UserProto fakeUser = protoFaker.fake();

// Results in realistic data like:
// email: "john.doe@gmail.com" 
// first_name: "John"
// phone_number: "(555) 123-4567"
// address: "123 Main St"
```

### Field-Name-Aware Generation

ProtoFaker automatically generates contextually appropriate data based on field names:

| Field Name Pattern | Generated Data |
|-------------------|----------------|
| `email`, `mail` | Valid email addresses |
| `first_name`, `firstName` | First names |
| `last_name`, `lastName` | Last names |
| `phone`, `phone_number` | Phone numbers |
| `address`, `street` | Street addresses |
| `city` | City names |
| `company`, `organization` | Company names |
| `job_title`, `position` | Job titles |
| `user_id`, `uuid` | UUIDs |
| `description`, `comment` | Lorem ipsum sentences |

### Field Overrides with Fluent API

Easily override specific fields for test scenarios:

```java
UserProto testUser = new ProtoFaker<>(UserProto.class)
    .withField("email", "test@example.com")
    .withField("first_name", "TestUser")
    .withField("age", 25)
    .fake();
```

### Template-Based Generation

Use an existing message as a template:

```java
UserProto template = UserProto.newBuilder()
    .setEmail("fixed@example.com")
    .setRole("admin")
    .build();

UserProto fakeUser = protoFaker.fake(template);
// Result: email="fixed@example.com", role="admin", other fields are generated
```

### Collections and Repeated Fields

Generate multiple fake messages:

```java
// Generate 5 fake users
List<UserProto> fakeUsers = protoFaker.fakes(5);

// Control repeated field counts
UserProto user = new ProtoFaker<>(UserProto.class)
    .withRepeatedCount(2, 4)  // 2-4 items in repeated fields
    .fake();

// Override repeated field values
UserProto user = new ProtoFaker<>(UserProto.class)
    .withField("repeated_tags", "important")
    .withRepeatedCount(1, 1)  // Exactly 1 tag
    .fake();
```

## 🎯 Advanced Examples

### E-commerce Order Testing

```java
// Generate realistic order data
OrderProto fakeOrder = new ProtoFaker<>(OrderProto.class)
    .withField("customer_email", "test.customer@example.com")
    .withField("order_status", OrderStatus.PROCESSING)
    .withRepeatedCount(2, 5)  // 2-5 items per order
    .fake();

// Template-based testing
OrderProto baseOrder = OrderProto.newBuilder()
    .setCustomerId("customer-123")
    .setOrderDate(Instant.now().toEpochMilli())
    .build();

List<OrderProto> testOrders = protoFaker.fakes(baseOrder, 10);
```

### API Response Testing

```java
// Generate realistic API response data
ApiResponseProto response = new ProtoFaker<>(ApiResponseProto.class)
    .withField("success", true)
    .withField("message", "Operation completed successfully")
    .withRepeatedCount(10, 20)  // 10-20 result items
    .fake();
```

### User Profile Testing

```java
UserProfileProto profile = new ProtoFaker<>(UserProfileProto.class)
    .withField("email", "john.doe@company.com")
    .withField("first_name", "John")
    .withField("last_name", "Doe")
    .withField("company_name", "Acme Corp")
    .withField("job_title", "Senior Developer")
    .fake();

// Results in:
// email: "john.doe@company.com"
// first_name: "John"
// last_name: "Doe"
// phone_number: "(555) 987-6543"  // Generated
// company_name: "Acme Corp"
// job_title: "Senior Developer"
// user_id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"  // Generated UUID
```

## 🛠️ Supported Field Types

ProtoFaker supports all Protocol Buffer field types:

- ✅ **Primitive Types**: `string`, `int32`, `int64`, `uint32`, `uint64`, `sint32`, `sint64`, `fixed32`, `fixed64`, `sfixed32`, `sfixed64`, `double`, `float`, `bool`
- ✅ **Complex Types**: `bytes`, `enum`, nested messages
- ✅ **Repeated Fields**: All field types in repeated context
- ✅ **Nested Messages**: Recursive generation with proper class resolution

## 🧪 Testing Integration

### JUnit 5 Example

```java
@Test
void testUserServiceWithFakeData() {
    // Arrange
    UserProto fakeUser = new ProtoFaker<>(UserProto.class)
        .withField("email", "test@example.com")
        .fake();
    
    // Act
    UserService service = new UserService();
    Result result = service.processUser(fakeUser);
    
    // Assert
    assertThat(result.isSuccess()).isTrue();
}
```

### Spock Framework Example

```groovy
def "should process user with fake data"() {
    given:
    def fakeUser = new ProtoFaker<>(UserProto)
        .withField("role", UserRole.ADMIN)
        .fake()
    
    when:
    def result = userService.processUser(fakeUser)
    
    then:
    result.success
    result.user.email.contains("@")
}
```

## 🎪 Comparison with Alternatives

| Feature | ProtoFaker | Manual Creation | Other Libraries |
|---------|------------|----------------|----------------|
| Zero Config | ✅ | ❌ | ⚠️ |
| Field-Name-Aware | ✅ | ❌ | ❌ |
| Fluent API | ✅ | ❌ | ⚠️ |
| Nested Messages | ✅ | ⚠️ | ⚠️ |
| Repeated Fields | ✅ | ⚠️ | ⚠️ |
| Template Support | ✅ | ⚠️ | ❌ |
| Test Integration | ✅ | ❌ | ⚠️ |

## 📋 Requirements

- Java 11 or higher
- Protocol Buffers 3.x
- No additional runtime dependencies (uses JavaFaker internally)

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### Development Setup
```bash
# Clone the repository
git clone https://github.com/phatjam98/proto-faker.git
cd proto-faker

# Build and test
./gradlew build

# Run tests with coverage
./gradlew test jacocoTestReport

# Run quality checks
./gradlew checkstyleMain checkstyleTest
```

### Contributing Process
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes with tests
4. Ensure all tests pass and code quality checks pass
5. Commit using [Conventional Commits](https://conventionalcommits.org/) (`git commit -m 'feat: add amazing feature'`)
6. Push to your branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Quality
- Follow Google Java Format (enforced via Checkstyle)
- Write tests for new features
- Maintain test coverage above 90%
- Use descriptive commit messages

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🎉 Why ProtoFaker?

**Before ProtoFaker:**
```java
// Tedious manual creation
UserProto user = UserProto.newBuilder()
    .setEmail("test@example.com")
    .setFirstName("Test")
    .setLastName("User")
    .setPhoneNumber("555-1234")
    .setAddress("123 Test St")
    .setCity("Test City")
    .setState("Test State")
    .setZipCode("12345")
    .setCompanyName("Test Corp")
    .setJobTitle("Tester")
    .setUserId("test-id-123")
    .build();
```

**With ProtoFaker:**
```java
// One line, realistic data
UserProto user = new ProtoFaker<>(UserProto.class).fake();

// Or with specific overrides
UserProto user = new ProtoFaker<>(UserProto.class)
    .withField("email", "test@example.com")
    .fake();
```

**Save time. Write better tests. Focus on what matters.** 🚀
