# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ProtoFaker is an actively maintained open source Java library for generating fake data for Protocol Buffer messages. It's designed for testing purposes and uses the Java Faker library to generate realistic fake data for all protobuf field types.

**Maven Central**: Available at `com.phatjam98:proto-faker:0.1.0`
**GitHub**: https://github.com/phatjam98/proto-faker
**Status**: Production-ready with automated CI/CD and releases

## Key Commands

### Build and Development
```bash
# Build the project
./gradlew assemble

# Run all tests
./gradlew test

# Run tests with verbose output
./gradlew test --info

# Run a single test class
./gradlew test --tests "com.phatjam98.protofaker.ProtoFakerSpec"

# Clean and build
./gradlew clean build
```

### Code Quality
```bash
# Run checkstyle (Google Java Format)
./gradlew checkstyleMain checkstyleTest

# Generate and verify test coverage with Jacoco
./gradlew jacocoTestReport jacocoTestCoverageVerification

# Run all quality checks
./gradlew checkstyleMain checkstyleTest jacocoTestReport jacocoTestCoverageVerification
```

### Publishing

#### Production Releases (Tag-Based Versioning)
ProtoFaker uses automated GitHub Actions workflows for releases. The process is fully automated via git tags:

```bash
# Create and push a version tag (triggers automated release)
git tag v0.2.0
git push origin v0.2.0

# GitHub Actions automatically:
# 1. Builds and tests the project
# 2. Publishes to Maven Central Portal
# 3. Creates GitHub release with changelog
# 4. Signs artifacts with GPG key 0BF66CC2B921A8AA
```

**Version Resolution**: JReleaser uses `-Pversion=X.Y.Z` parameter, where GitHub Actions extracts `X.Y.Z` from git tag `vX.Y.Z`.

#### Manual Release Commands (Local Testing)
```bash
# Build and stage artifacts locally with specific version
./gradlew publishMavenJavaPublicationToStagingRepository -Pversion=0.2.0

# Test JReleaser configuration with version
./gradlew jreleaserConfig -Pversion=0.2.0

# Test release with dry run
./gradlew jreleaserRelease --dryrun -Pversion=0.2.0

# Full release (GitHub + Maven Central) - normally done by CI/CD
./gradlew jreleaserFullRelease -Pversion=0.2.0
```

**Important**: The `maven-publish` plugin IS required for JReleaser to work. It stages artifacts in `build/staging-deploy/` which JReleaser then publishes to Maven Central Portal.

#### SNAPSHOT Publishing
SNAPSHOT versions are automatically published to Maven Central from the `main` branch:
```bash
# Automatic on every push to main
# Version format: X.Y.Z-SNAPSHOT (e.g., 0.2.0-SNAPSHOT)
```

#### Development Publishing
```bash
# Publish to local Maven repository for testing
./gradlew publishToMavenLocal
```

## Using ProtoFaker

### Maven Central Dependency
Add ProtoFaker to your project:

**Gradle:**
```gradle
dependencies {
    testImplementation 'com.phatjam98:proto-faker:0.1.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.phatjam98</groupId>
    <artifactId>proto-faker</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

### Basic Usage
```java
import com.phatjam98.protofaker.ProtoFaker;

// Generate a single fake message
MyProto fakeMessage = ProtoFaker.fake(MyProto.class);

// Generate multiple fake messages
List<MyProto> fakeMessages = ProtoFaker.fakes(MyProto.class, 10);

// Generate with a template
MyProto template = MyProto.newBuilder().setName("Fixed Name").build();
MyProto fakeWithTemplate = ProtoFaker.fake(template);
```

## Architecture

### Core Components
- **ProtoFaker<T>**: Generic class that generates fake data for any protobuf message type
- **Reflection-based approach**: Uses protobuf descriptors to introspect message structure
- **Field type mapping**: Supports all protobuf field types (primitives, enums, bytes, nested messages)

### Key Features
- Generate single fake messages: `fake()`
- Generate with template: `fake(baseProto)` 
- Generate collections: `fakes(count)` or `fakes(baseProtos)`
- Handles nested messages recursively
- Uses Java Faker for realistic data generation

### Project Structure
- Single-module Gradle project
- Main code: `src/main/java/com/phatjam98/protofaker/`
- Tests: `src/test/groovy/com/phatjam98/protofaker/`

## Testing

### Framework
- **Spock Framework**: Groovy-based testing with BDD-style specifications
- **Test files**: `*.groovy` files in `src/test/groovy/`
- **Sample protobuf**: Uses `TestOuterClass.Test` for comprehensive testing

### Dependencies
- Java 11 toolchain
- Google Protocol Buffers (protobuf-java)
- Java Faker for data generation
- Spock Framework for testing
- Logback for logging

## Code Style
- **Google Java Format**: Enforced via Checkstyle
- **Checkstyle config**: `config/checkstyle/google-java-format.xml`
- **Suppression file**: `config/checkstyle/checkstyle-suppressions.xml`

## Release Management

### JReleaser Configuration
This project uses JReleaser for automated releases to GitHub and Maven Central Portal.

**Configuration file**: `jreleaser.yml` in project root
**Key features:**
- Automated GitHub releases with conventional commit changelogs
- Maven Central Portal API publishing (replacing legacy OSSRH)
- GPG signing for artifact verification
- Multi-format artifact publishing (JAR, sources, javadoc)

### GPG Setup Status
**Current GPG Key**: RSA 4096-bit key `0BF66CC2B921A8AA` for phatjam98@gmail.com
- **Expires**: 2027-10-31
- **Status**: Configured and verified
- **Passphrase**: Not required (key configured without passphrase)

### Environment Variables Required
```bash
# GitHub token for releases
JRELEASER_GITHUB_TOKEN="<your-github-token>"

# Maven Central Portal credentials
JRELEASER_MAVENCENTRAL_USERNAME="<your-portal-username>"
JRELEASER_MAVENCENTRAL_PASSWORD="<your-portal-token>"

# GPG signing (key ID: 0BF66CC2B921A8AA)
JRELEASER_GPG_PASSPHRASE="<your-gpg-passphrase>"
JRELEASER_GPG_PUBLIC_KEY="$(gpg --armor --export 0BF66CC2B921A8AA)"
JRELEASER_GPG_SECRET_KEY="$(gpg --armor --export-secret-keys 0BF66CC2B921A8AA)"
```

**Security Note**: Never commit actual credential values. Use `.env.local` for local development.

### Current Release Status
- ✅ **v0.1.0 Successfully Released**: First production release published to Maven Central
- ✅ JReleaser fully operational with tag-based versioning
- ✅ Maven-publish plugin configured for artifact staging
- ✅ GPG key 0BF66CC2B921A8AA configured and tested
- ✅ GitHub token configured and validated
- ✅ Maven Central Portal credentials configured and working
- ✅ Complete CI/CD pipeline operational (ci.yml, snapshot.yml, release.yml)
- ✅ SNAPSHOT and release workflows both tested and working
- ✅ Library available for consumption via Maven Central

### GitHub Actions CI/CD
Project includes automated CI/CD workflows:
- **ci.yml**: Runs tests and quality checks on PRs and pushes
- **snapshot.yml**: Automatically publishes SNAPSHOT releases from main branch
- **release.yml**: Handles production releases when version tags are created

Required GitHub Secrets:
- `MAVEN_USERNAME`: Maven Central token username
- `MAVEN_PASSWORD`: Maven Central token password
- `GPG_PRIVATE_KEY`: Base64-encoded GPG private key
- `GPG_PASSPHRASE`: GPG key passphrase

### Version Management & Release Process

#### Creating New Releases
1. **Update version in build.gradle.kts** if needed (for SNAPSHOT development)
2. **Ensure all tests pass**: `./gradlew test`
3. **Verify quality checks**: `./gradlew checkstyleMain checkstyleTest jacocoTestReport`
4. **Create and push version tag**:
   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```
5. **Monitor GitHub Actions** for automated release process
6. **Verify release** on Maven Central and GitHub Releases

#### Version Numbering Strategy
- **Production releases**: Semantic versioning (v0.1.0, v0.2.0, etc.)
- **SNAPSHOT releases**: Automatic from main branch (0.2.0-SNAPSHOT)
- **Version extraction**: GitHub Actions extracts version from tag (v0.1.0 → 0.1.0)

### Lessons Learned from v0.1.0 Release

#### What Works Well
- **Tag-based versioning**: Simple and reliable release process
- **JReleaser + Maven Central Portal**: Modern alternative to legacy OSSRH
- **GPG signing**: Seamless with configured key 0BF66CC2B921A8AA
- **GitHub Actions workflows**: Fully automated CI/CD pipeline
- **Version parameter resolution**: `-Pversion=X.Y.Z` works consistently

#### Key Technical Details
- **Maven Central Portal**: Uses `mavenCentral` deployer, not legacy OSSRH
- **Artifact staging**: `maven-publish` plugin required for JReleaser
- **SNAPSHOT routing**: Automatically publishes to Maven Central SNAPSHOT repository
- **Release routing**: Production releases go to Maven Central staging → release
- **GPG key management**: RSA 4096-bit key without passphrase for automation

#### Development Best Practices
- Always make sure we are on a clean branch off of main before we make changes
- NEVER commit to main or push directly to main
- When changes are considered for merge, create a PR for review
- Test releases locally with `--dryrun` flag before pushing tags
- Monitor GitHub Actions workflows for any failures
- Verify artifacts are correctly signed and published to Maven Central