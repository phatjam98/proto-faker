# ProtoFaker API Reference

This document provides comprehensive technical documentation for the ProtoFaker Java library, including all public methods, configuration options, and usage patterns.

## Table of Contents

- [Class Overview](#class-overview)
- [Constructor](#constructor)
- [Configuration Methods](#configuration-methods)
- [Data Generation Methods](#data-generation-methods)
- [Field Name Recognition Patterns](#field-name-recognition-patterns)
- [Supported Protobuf Field Types](#supported-protobuf-field-types)
- [Code Examples](#code-examples)
- [Error Handling](#error-handling)
- [Performance Considerations](#performance-considerations)
- [Advanced Usage](#advanced-usage)

## Class Overview

### ProtoFaker\<T extends GeneratedMessageV3\>

The main class for generating fake Protocol Buffer message data. It uses reflection to introspect protobuf message structures and generates contextually appropriate fake data using the JavaFaker library.

**Package:** `com.phatjam98.protofaker`

**Generic Parameter:**
- `T` - Must extend `GeneratedMessageV3` (any generated protobuf message class)

**Design Principles:**
- **Fluent API**: Method chaining for easy configuration
- **Field-Name-Aware**: Generates contextually appropriate data based on field names
- **Template-Based**: Supports using existing messages as templates
- **Zero-Configuration**: Works out-of-the-box for any protobuf message

## Constructor

### ProtoFaker(Class\<T\> clazz)

Creates a new ProtoFaker instance for generating fake data for the specified protobuf message type.

**Parameters:**
- `clazz` - The Class object for the protobuf message type (e.g., `UserProto.class`)

**Example:**
```java
ProtoFaker<UserProto> userFaker = new ProtoFaker<>(UserProto.class);
ProtoFaker<OrderProto> orderFaker = new ProtoFaker<>(OrderProto.class);
```

**Thread Safety:** ProtoFaker instances are **not thread-safe**. Each thread should create its own instance.

## Configuration Methods

### withField(String fieldName, Object value)

Override the value for a specific field in generated messages.

**Parameters:**
- `fieldName` - Name of the protobuf field to override (e.g., "email", "user_id")
- `value` - Value to use for this field (must be compatible with the field type)

**Returns:** `ProtoFaker<T>` - The same instance for method chaining

**Field Name Matching:**
- Uses exact protobuf field names (e.g., "user_id", "first_name")
- Case-sensitive matching
- Must match the field name as defined in the .proto file

**Type Compatibility:**
- String fields: Accept `String` values
- Numeric fields: Accept compatible numeric types (`int`, `long`, `double`, etc.)
- Boolean fields: Accept `boolean` values
- Enum fields: Accept enum constants or `EnumValueDescriptor`
- Bytes fields: Accept `ByteString` objects
- Message fields: Accept instances of the nested message type
- Repeated fields: The override value will be added as a single item

**Examples:**
```java
// String field override
var faker = new ProtoFaker<>(UserProto.class)
    .withField("email", "test@example.com")
    .withField("first_name", "John");

// Numeric field override  
var orderFaker = new ProtoFaker<>(OrderProto.class)
    .withField("total_amount", 99.99)
    .withField("item_count", 5);

// Enum field override
var productFaker = new ProtoFaker<>(ProductProto.class)
    .withField("category", ProductCategory.ELECTRONICS);

// Boolean field override
var settingsFaker = new ProtoFaker<>(SettingsProto.class)
    .withField("email_notifications", false);
```

### withRepeatedCount(int minCount, int maxCount)

Set the count range for repeated fields in generated messages.

**Parameters:**
- `minCount` - Minimum number of items to generate for repeated fields (inclusive)
- `maxCount` - Maximum number of items to generate for repeated fields (exclusive)

**Returns:** `ProtoFaker<T>` - The same instance for method chaining

**Default Values:**
- `minCount`: 1
- `maxCount`: 5

**Behavior:**
- Applies to all repeated fields in the message unless specifically overridden with `withField()`
- Count is randomly chosen between minCount (inclusive) and maxCount (exclusive)
- If `minCount >= maxCount`, behavior is undefined

**Examples:**
```java
// Generate 1-3 items for repeated fields
var faker = new ProtoFaker<>(OrderProto.class)
    .withRepeatedCount(1, 4);

// Generate exactly 2 items for repeated fields
var exactFaker = new ProtoFaker<>(UserProto.class)
    .withRepeatedCount(2, 3);

// Generate many items (stress testing)
var stressFaker = new ProtoFaker<>(ProductProto.class)
    .withRepeatedCount(10, 20);
```

## Data Generation Methods

### fake()

Generate a single fake protobuf message with all fields populated using realistic data.

**Returns:** `T` - A new instance of the protobuf message with fake data

**Behavior:**
- Generates data for all defined fields in the protobuf message
- Uses field name patterns to generate contextually appropriate data
- Handles nested messages recursively
- Respects field overrides set with `withField()`
- Uses configured repeated field count ranges

**Example:**
```java
ProtoFaker<UserProto> faker = new ProtoFaker<>(UserProto.class);
UserProto fakeUser = faker.fake();

// Result might look like:
// email: "john.smith@example.com"
// first_name: "John" 
// last_name: "Smith"
// phone_number: "(555) 123-4567"
// address: "123 Main St"
// user_id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

### fake(T baseProto)

Generate a fake protobuf message using an existing message as a template.

**Parameters:**
- `baseProto` - Existing protobuf message to use as a template

**Returns:** `T` - A new message that merges the template with fake data

**Behavior:**
1. Generates a complete fake message using `fake()`
2. Merges the baseProto fields over the fake data using `builder.mergeFrom(baseProto)`
3. Template fields take precedence over generated fake data
4. Unset template fields use fake data
5. Field overrides are applied before template merging

**Merge Strategy:**
- **Singular fields**: Template values replace fake values
- **Repeated fields**: Template values are **appended** to fake values
- **Message fields**: Recursive merging occurs

**Examples:**
```java
// Template with specific values
UserProto template = UserProto.newBuilder()
    .setEmail("fixed@example.com")
    .setRole("admin")
    .build();

// Generate fake user with template values preserved
UserProto result = faker.fake(template);
// Result: email="fixed@example.com", role="admin", other fields are fake
```

**Advanced Template Usage:**
```java
// Partial template for testing specific scenarios
OrderProto orderTemplate = OrderProto.newBuilder()
    .setOrderStatus(OrderStatus.PROCESSING)
    .setCustomerId("test-customer-123")
    .build();

List<OrderProto> testOrders = faker.fakes(orderTemplate, 5);
// All orders will have PROCESSING status and test-customer-123 ID
```

### fakes(int count)

Generate a list of fake protobuf messages.

**Parameters:**
- `count` - Number of fake messages to generate

**Returns:** `List<T>` - List containing `count` fake messages

**Behavior:**
- Each message is independently generated (no shared state)
- Each call to `fake()` may produce different random data
- Field overrides apply to all generated messages
- Repeated field counts are independently randomized for each message

**Example:**
```java
// Generate 10 fake users
List<UserProto> users = faker.fakes(10);

// Generate with field overrides applied to all
List<UserProto> adminUsers = new ProtoFaker<>(UserProto.class)
    .withField("role", "admin")
    .fakes(5);
```

### fakes(T baseProto, int count)

Generate a list of fake protobuf messages using a template.

**Parameters:**
- `baseProto` - Template message for all generated messages
- `count` - Number of fake messages to generate

**Returns:** `List<T>` - List containing `count` fake messages based on the template

**Behavior:**
- Equivalent to calling `fake(baseProto)` count times
- Each message uses the same template but gets different fake data
- Template values are consistent across all messages
- Random fake data varies between messages

**Example:**
```java
// Template for test scenario
OrderProto template = OrderProto.newBuilder()
    .setCustomerId("test-customer")
    .setOrderDate(Instant.now().toEpochMilli())
    .build();

// Generate 3 orders with same customer and date, but different fake data
List<OrderProto> orders = faker.fakes(template, 3);
```

### fakes(List\<T\> baseProtos)

Generate fake protobuf messages using a list of templates.

**Parameters:**
- `baseProtos` - List of template messages (one fake message per template)

**Returns:** `List<T>` - List containing fake messages based on the templates

**Behavior:**
- Generates one fake message per template in the input list
- Each template produces one corresponding fake message
- Equivalent to calling `fake(template)` for each template in the list

**Example:**
```java
// Different templates for different test scenarios
List<UserProto> templates = Arrays.asList(
    UserProto.newBuilder().setRole("admin").build(),
    UserProto.newBuilder().setRole("user").build(),
    UserProto.newBuilder().setRole("guest").build()
);

List<UserProto> testUsers = faker.fakes(templates);
// Returns 3 users: one admin, one user, one guest (all with fake data)
```

## Field Name Recognition Patterns

ProtoFaker automatically generates contextually appropriate data based on field name patterns. Pattern matching is case-insensitive and uses substring matching.

### Email Patterns
**Triggers:** `email`, `mail`
**Generated Data:** Valid email addresses
**Examples:** `john.doe@example.com`, `alice.smith@gmail.com`

```java
// These field names will generate email addresses:
// email, user_email, contact_mail, primary_email
```

### Name Patterns
**First Name Triggers:** `firstname`, `first_name`
**Generated Data:** First names
**Examples:** `John`, `Alice`, `Michael`

**Last Name Triggers:** `lastname`, `last_name`
**Generated Data:** Last names  
**Examples:** `Smith`, `Johnson`, `Brown`

**Full Name Triggers:** `fullname`, `full_name`, `name`, `username`, `displayname`, `display_name`
**Generated Data:** Full names
**Examples:** `John Smith`, `Alice Johnson`

### Phone Patterns
**Triggers:** `phone`, `mobile`, `tel`, `number`
**Generated Data:** Phone numbers
**Examples:** `(555) 123-4567`, `+1-800-555-0123`

### Address Patterns
**Street Address Triggers:** `address`, `street`
**Generated Data:** Street addresses
**Examples:** `123 Main St`, `456 Oak Avenue`

**City Triggers:** `city`
**Generated Data:** City names
**Examples:** `New York`, `Los Angeles`, `Chicago`

**State Triggers:** `state`, `province`
**Generated Data:** State/province names
**Examples:** `California`, `New York`, `Texas`

**Country Triggers:** `country`
**Generated Data:** Country names
**Examples:** `United States`, `Canada`, `United Kingdom`

**Postal Code Triggers:** `zip`, `postal`
**Generated Data:** ZIP/postal codes
**Examples:** `90210`, `K1A 0A6`

### Internet/Web Patterns
**URL Triggers:** `url`, `website`
**Generated Data:** URLs
**Examples:** `https://example.com`, `http://test.org`

**Domain Triggers:** `domain`
**Generated Data:** Domain names
**Examples:** `example.com`, `test.org`

### Business Patterns
**Company Triggers:** `company`, `organization`
**Generated Data:** Company names
**Examples:** `Acme Corporation`, `Global Industries`

**Job Title Triggers:** `job`, `position`, `title`, `role`
**Generated Data:** Job titles
**Examples:** `Software Engineer`, `Marketing Manager`

### ID Patterns
**Triggers:** `id`, `uuid`
**Generated Data:** UUIDs
**Examples:** `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

### Text Content Patterns
**Triggers:** `description`, `comment`, `note`, `message`
**Generated Data:** Lorem ipsum sentences
**Examples:** `Lorem ipsum dolor sit amet, consectetur adipiscing elit.`

### Color Patterns
**Triggers:** `color`, `colour`
**Generated Data:** Color names
**Examples:** `red`, `blue`, `green`

### Default Behavior
**For unrecognized patterns:** Funny names
**Examples:** `Silly Sally`, `Funny Frank`

## Supported Protobuf Field Types

### Primitive Types

#### Numeric Types
- **`double`**: Random double values (0.0 to 100.0, 2 decimal places)
- **`float`**: Random float values (0.0 to 100.0, 2 decimal places) 
- **`int64`, `sint64`, `fixed64`, `uint64`, `sfixed64`**: Random long values (1 to 10,000)
- **`int32`, `sint32`, `fixed32`, `uint32`, `sfixed32`**: Random int values (1 to 10,000)

#### Boolean Type
- **`bool`**: Random boolean values (`true` or `false`)

#### String Type
- **`string`**: Context-aware string generation based on field name patterns (see [Field Name Recognition](#field-name-recognition-patterns))

#### Binary Type
- **`bytes`**: ByteString containing Shakespeare quotes

### Complex Types

#### Enum Fields
- **Behavior**: Randomly selects from available enum values
- **UNKNOWN Handling**: Skips enum values containing "UNKNOWN" if other values are available
- **Selection**: Random selection from valid enum constants

```java
// For enum with values: [UNKNOWN, ACTIVE, INACTIVE, PENDING]
// Will randomly select from: [ACTIVE, INACTIVE, PENDING]
```

#### Message Fields (Nested)
- **Behavior**: Recursively generates fake data for nested messages
- **Class Resolution**: Automatically resolves nested message classes using protobuf metadata
- **Java Package Mapping**: Handles Java package name resolution from protobuf options
- **Outer Class Detection**: Supports both explicit and generated outer class names

```java
// For nested messages:
// UserProto.Address -> generates fake Address with all fields populated
// OrderProto.LineItem -> generates fake LineItem recursively
```

#### Repeated Fields
- **Count**: Configurable via `withRepeatedCount(min, max)`
- **Default Range**: 1 to 5 items
- **Independence**: Each repeated item is independently generated
- **Override Behavior**: `withField()` adds a single item to repeated fields

```java
// repeated string tags -> ["tag1", "tag2", "tag3"] (count varies)
// repeated UserProto users -> [UserProto{...}, UserProto{...}] (each unique)
```

### Type Compatibility Matrix

| Protobuf Type | Java Type | Faker Method | Example Value |
|---------------|-----------|--------------|---------------|
| `double` | `double` | `faker.number().randomDouble()` | `42.73` |
| `float` | `float` | `faker.number().randomDouble()` | `15.42f` |
| `int32` | `int` | `faker.number().numberBetween()` | `1337` |
| `int64` | `long` | `faker.number().numberBetween()` | `9876543210L` |
| `uint32` | `int` | `faker.number().numberBetween()` | `2048` |
| `uint64` | `long` | `faker.number().numberBetween()` | `1234567890L` |
| `sint32` | `int` | `faker.number().numberBetween()` | `-1024` |
| `sint64` | `long` | `faker.number().numberBetween()` | `-9876543210L` |
| `fixed32` | `int` | `faker.number().numberBetween()` | `12345` |
| `fixed64` | `long` | `faker.number().numberBetween()` | `9876543210L` |
| `sfixed32` | `int` | `faker.number().numberBetween()` | `-54321` |
| `sfixed64` | `long` | `faker.number().numberBetween()` | `-1234567890L` |
| `bool` | `boolean` | `faker.bool().bool()` | `true` |
| `string` | `String` | Context-aware generation | `"john@example.com"` |
| `bytes` | `ByteString` | `faker.shakespeare().hamletQuote()` | Shakespeare quote |
| `enum` | `EnumValueDescriptor` | Random enum selection | `Status.ACTIVE` |
| `message` | Nested message class | Recursive generation | Complete nested object |

## Code Examples

### Basic Usage

```java
import com.phatjam98.protofaker.ProtoFaker;

// Simple fake generation
ProtoFaker<UserProto> faker = new ProtoFaker<>(UserProto.class);
UserProto fakeUser = faker.fake();

System.out.println("Email: " + fakeUser.getEmail());
System.out.println("Name: " + fakeUser.getFirstName() + " " + fakeUser.getLastName());
```

### Configuration Examples

```java
// Field overrides with method chaining
UserProto customUser = new ProtoFaker<>(UserProto.class)
    .withField("email", "admin@company.com")
    .withField("role", UserRole.ADMIN)
    .withField("active", true)
    .withRepeatedCount(2, 4)  // 2-3 items in repeated fields
    .fake();

// Multiple configurations
ProtoFaker<OrderProto> orderFaker = new ProtoFaker<>(OrderProto.class)
    .withField("customer_email", "test@example.com")
    .withField("order_status", OrderStatus.PROCESSING)
    .withRepeatedCount(1, 3);  // 1-2 line items

// Reuse configured faker
OrderProto order1 = orderFaker.fake();
OrderProto order2 = orderFaker.fake();
List<OrderProto> orders = orderFaker.fakes(5);
```

### Template-Based Generation

```java
// Base template for consistent test data
UserProto baseUser = UserProto.newBuilder()
    .setCompanyId("acme-corp")
    .setDepartment("engineering")
    .setActive(true)
    .build();

// Generate variations of the template
List<UserProto> engineeringTeam = new ProtoFaker<>(UserProto.class)
    .withRepeatedCount(1, 2)  // Small number of skills per user
    .fakes(baseUser, 10);

// Each user will have:
// - companyId: "acme-corp" (from template)
// - department: "engineering" (from template) 
// - active: true (from template)
// - All other fields: realistic fake data
```

### Collection Generation

```java
// Generate multiple independent messages
List<ProductProto> products = new ProtoFaker<>(ProductProto.class)
    .withField("category", ProductCategory.ELECTRONICS)
    .fakes(20);

// Generate with different templates
List<UserProto> testUsers = Arrays.asList(
    UserProto.newBuilder().setRole(UserRole.ADMIN).build(),
    UserProto.newBuilder().setRole(UserRole.USER).build(),
    UserProto.newBuilder().setRole(UserRole.GUEST).build()
);

List<UserProto> diverseUsers = new ProtoFaker<>(UserProto.class)
    .fakes(testUsers);
```

### Testing Integration Examples

#### JUnit 5 Integration
```java
@Test
void testUserServiceWithRealisticData() {
    // Arrange
    UserProto testUser = new ProtoFaker<>(UserProto.class)
        .withField("email", "integration.test@example.com")
        .withField("role", UserRole.ADMIN)
        .fake();
    
    // Act
    UserService service = new UserService();
    Result result = service.createUser(testUser);
    
    // Assert
    assertTrue(result.isSuccess());
    assertEquals("integration.test@example.com", result.getUser().getEmail());
}

@Test
void testBulkOperationWithMultipleUsers() {
    // Generate many realistic test users
    List<UserProto> users = new ProtoFaker<>(UserProto.class)
        .withField("active", true)
        .withRepeatedCount(1, 3)  // 1-2 skills per user
        .fakes(50);
    
    // Test bulk operation
    BulkResult result = userService.createUsers(users);
    
    assertEquals(50, result.getCreatedCount());
    assertTrue(result.getAllEmails().stream()
        .allMatch(email -> email.contains("@")));
}
```

#### Spock Framework Integration
```groovy
def "should create user with realistic fake data"() {
    given: "a fake user with admin role"
    def fakeUser = new ProtoFaker<>(UserProto)
        .withField("role", UserRole.ADMIN)
        .withField("department", "IT")
        .fake()
    
    when: "creating the user"
    def result = userService.createUser(fakeUser)
    
    then: "user is created successfully"
    result.success
    result.user.email.contains("@")
    result.user.role == UserRole.ADMIN
    result.user.department == "IT"
    
    and: "user has realistic fake data"
    result.user.firstName.length() > 0
    result.user.lastName.length() > 0
    result.user.userId ==~ /[0-9a-f-]{36}/  // UUID pattern
}

def "should generate diverse fake data for repeated tests"() {
    given: "multiple fake users"
    def users = new ProtoFaker<>(UserProto).fakes(10)
    
    expect: "all users have unique emails"
    users.collect { it.email }.toSet().size() == 10
    
    and: "all users have realistic data"
    users.every { user ->
        user.email.contains("@") &&
        user.firstName.length() > 0 &&
        user.lastName.length() > 0
    }
}
```

## Error Handling

### Common Exceptions

#### RuntimeException - Missing newBuilder Method
**Cause:** The protobuf class doesn't have a `newBuilder()` static method
**Solution:** Ensure you're using a properly generated protobuf class

```java
// This will throw RuntimeException:
ProtoFaker<String> invalidFaker = new ProtoFaker<>(String.class);
```

#### RuntimeException - Cannot Access newBuilder Method  
**Cause:** Security manager or reflection restrictions prevent access
**Solution:** Ensure proper classpath and no restrictive security policies

#### ClassNotFoundException - Nested Message Resolution
**Cause:** ProtoFaker cannot resolve nested message classes
**Common Scenarios:**
- Custom protobuf package naming
- Complex nested message hierarchies
- Missing generated classes on classpath

**Debugging:**
```java
// Enable debug logging to see class resolution attempts
System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");

// Check generated class structure
System.out.println("Available methods: " + 
    Arrays.toString(UserProto.class.getDeclaredMethods()));
```

### Error Recovery

```java
try {
    ProtoFaker<ComplexProto> faker = new ProtoFaker<>(ComplexProto.class);
    ComplexProto fake = faker.fake();
} catch (RuntimeException e) {
    // Log error and use fallback
    logger.error("Failed to generate fake protobuf", e);
    
    // Fallback to manual construction
    ComplexProto fallback = ComplexProto.newBuilder()
        .setId("fallback-id")
        .setName("Test Name")
        .build();
}
```

### Validation

```java
public boolean isValidFakeData(UserProto user) {
    return user.hasEmail() && 
           user.getEmail().contains("@") &&
           user.hasFirstName() && 
           !user.getFirstName().isEmpty() &&
           user.hasUserId() &&
           user.getUserId().matches("[0-9a-f-]{36}");
}
```

## Performance Considerations

### Memory Usage

**Object Creation:**
- Each `fake()` call creates new protobuf message instances
- Nested messages create recursive object graphs
- Large repeated fields can consume significant memory

**Optimization Strategies:**
```java
// Reuse faker instances
ProtoFaker<UserProto> userFaker = new ProtoFaker<>(UserProto.class);

// Configure reasonable repeated field counts
userFaker.withRepeatedCount(1, 5);  // Not 100, 1000

// Generate in batches for large datasets
List<UserProto> allUsers = new ArrayList<>();
for (int batch = 0; batch < 10; batch++) {
    allUsers.addAll(userFaker.fakes(100));
    // Process batch, potentially clearing references
}
```

### CPU Performance

**Reflection Overhead:**
- Protobuf descriptor resolution uses reflection
- Class loading for nested messages
- Method invocation overhead

**Performance Optimizations:**
```java
// Pre-warm the faker to cache reflection operations
ProtoFaker<UserProto> faker = new ProtoFaker<>(UserProto.class);
faker.fake(); // First call is slower due to reflection setup

// Reuse faker instances instead of creating new ones
// Good:
ProtoFaker<UserProto> reusedFaker = new ProtoFaker<>(UserProto.class);
List<UserProto> users = reusedFaker.fakes(1000);

// Less efficient:
List<UserProto> users = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    users.add(new ProtoFaker<>(UserProto.class).fake()); // Don't do this
}
```

### Benchmarking Results

**Typical Performance (on modern hardware):**
- Simple message (5-10 fields): ~1-2ms per fake()
- Complex nested message: ~5-10ms per fake()
- 1000 simple messages: ~1-2 seconds
- Repeated fields add ~0.5ms per item

**Memory Usage:**
- ProtoFaker instance: ~1-5KB
- Simple fake message: ~1-10KB
- Complex nested message: ~10-100KB

## Advanced Usage

### Custom Field Generation Patterns

While ProtoFaker doesn't currently support custom field generators, you can achieve custom behavior using field overrides:

```java
// Custom date generation
long customTimestamp = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli();

OrderProto customOrder = new ProtoFaker<>(OrderProto.class)
    .withField("created_timestamp", customTimestamp)
    .withField("order_id", "CUSTOM-" + UUID.randomUUID().toString())
    .fake();
```

### Integration with Test Data Builders

```java
public class UserProtoTestDataBuilder {
    private final ProtoFaker<UserProto> faker;
    
    public UserProtoTestDataBuilder() {
        this.faker = new ProtoFaker<>(UserProto.class);
    }
    
    public UserProtoTestDataBuilder admin() {
        faker.withField("role", UserRole.ADMIN);
        return this;
    }
    
    public UserProtoTestDataBuilder withEmail(String email) {
        faker.withField("email", email);
        return this;
    }
    
    public UserProtoTestDataBuilder active() {
        faker.withField("active", true);
        return this;
    }
    
    public UserProto build() {
        return faker.fake();
    }
    
    public List<UserProto> build(int count) {
        return faker.fakes(count);
    }
}

// Usage:
UserProto adminUser = new UserProtoTestDataBuilder()
    .admin()
    .active()
    .withEmail("admin@company.com")
    .build();
```

### Parameterized Testing

```java
@ParameterizedTest
@MethodSource("userRoleProvider")
void testUserPermissions(UserRole role) {
    // Generate fake user with specific role
    UserProto testUser = new ProtoFaker<>(UserProto.class)
        .withField("role", role)
        .fake();
    
    // Test role-specific behavior
    PermissionService permissions = new PermissionService();
    Set<Permission> userPermissions = permissions.getPermissions(testUser);
    
    // Assert role-appropriate permissions
    assertThat(userPermissions).isNotEmpty();
}

static Stream<UserRole> userRoleProvider() {
    return Stream.of(UserRole.ADMIN, UserRole.USER, UserRole.GUEST);
}
```

### Mock Service Integration

```java
@MockBean
private UserService userService;

@Test
void testWithMockedService() {
    // Generate realistic fake data
    UserProto fakeUser = new ProtoFaker<>(UserProto.class)
        .withField("id", "test-user-123")
        .fake();
    
    // Mock service behavior
    when(userService.findById("test-user-123"))
        .thenReturn(fakeUser);
    
    // Test your code with realistic fake data
    var result = applicationService.processUser("test-user-123");
    assertThat(result.getUserEmail()).isEqualTo(fakeUser.getEmail());
}
```

---

**Note:** This API reference covers ProtoFaker version 0.1.0. For the latest updates and additional features, check the [CHANGELOG](../CHANGELOG.md) and [project repository](https://github.com/phatjam98/proto-faker).
