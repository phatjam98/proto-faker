# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ProtoFaker is a Java library for generating fake data for Protocol Buffer messages. It's designed for testing purposes and uses the Java Faker library to generate realistic fake data for all protobuf field types.

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

#### JReleaser + Maven Publish Workflow
```bash
# Build and stage artifacts locally (required for JReleaser)
./gradlew publishMavenJavaPublicationToStagingRepository

# Test JReleaser configuration
./gradlew jreleaserConfig

# Test release with dry run
./gradlew jreleaserRelease --dryrun

# Full release (GitHub + Maven Central)
./gradlew jreleaserFullRelease
```

**Important**: The `maven-publish` plugin IS required for JReleaser to work. It stages artifacts in `build/staging-deploy/` which JReleaser then publishes to Maven Central.

#### Development Publishing
```bash
# Publish to local Maven repository for testing
./gradlew publishToMavenLocal
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
- ✅ JReleaser fully integrated and tested
- ✅ Maven-publish plugin configured for artifact staging
- ✅ GPG key 0BF66CC2B921A8AA configured and tested
- ✅ GitHub token configured and validated
- ✅ Maven Central Portal credentials configured
- ✅ Complete release workflow tested with dry-run
- ✅ Ready for SNAPSHOT releases

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