# Releasing ProtoFaker

This project uses JReleaser to coordinate releases to both GitHub and Maven Central Portal. The system is integrated with the `maven-publish` plugin to stage artifacts locally before publication.

## Prerequisites

### 1. Maven Central Portal Setup
- Register at https://central.sonatype.com/
- Verify namespace ownership (com.phatjam98)
- Generate User Token (NOT regular OSSRH credentials)
- Note: This project uses Maven Central Portal API, not legacy OSSRH

### 2. GitHub Personal Access Token
- Create a Personal Access Token with `repo` scope
- Go to https://github.com/settings/tokens
- Create token with full `repo` permissions for releases

### 3. GPG Signing Setup

**Current Configuration**: This project is configured with GPG key `0BF66CC2B921A8AA` (RSA 4096-bit, expires 2027-10-31).

```bash
# Verify current GPG key is available
gpg --list-secret-keys --keyid-format LONG

# Should show key 0BF66CC2B921A8AA for phatjam98@gmail.com
# If setting up a new key, generate it with:
gpg --full-gen-key

# Export public key to key servers (safe to share key ID)
gpg --keyserver keyserver.ubuntu.com --send-keys 0BF66CC2B921A8AA
gpg --keyserver keys.openpgp.org --send-keys 0BF66CC2B921A8AA
```

### 4. Environment Variables Setup

**For Local Development**: Create `.env.local` file (git-ignored) with:

```bash
# Maven Central Portal credentials (from central.sonatype.com user tokens)
export JRELEASER_MAVENCENTRAL_USERNAME="your-portal-username"
export JRELEASER_MAVENCENTRAL_PASSWORD="your-portal-token"

# GitHub token for releases  
export JRELEASER_GITHUB_TOKEN="ghp_your_github_token"

# GPG signing (using current project key 0BF66CC2B921A8AA)
# Note: This key has no passphrase, so JRELEASER_GPG_PASSPHRASE is not required
export JRELEASER_GPG_PUBLIC_KEY="$(gpg --armor --export 0BF66CC2B921A8AA)"
export JRELEASER_GPG_SECRET_KEY="$(gpg --armor --export-secret-keys 0BF66CC2B921A8AA)"
```

Then source before releases:
```bash
source .env.local
```

## Release Process

**Current Version**: `0.0.1-SNAPSHOT` (ready for first release)

### 1. Pre-Release Checklist

- [ ] All tests passing: `./gradlew test`
- [ ] Code quality checks: `./gradlew checkstyleMain checkstyleTest jacocoTestCoverageVerification`
- [ ] Documentation updated (README.md, CHANGELOG.md if exists)
- [ ] Version bumped to release version (remove `-SNAPSHOT` from build.gradle)
- [ ] Environment variables configured (source .env.local)

### 2. Prepare Release Version

```bash
# Update version in build.gradle (remove -SNAPSHOT)
# Current: version = '0.0.1-SNAPSHOT' 
# Change to: version = '0.0.1'

# Commit version change
git add build.gradle
git commit -m "chore: prepare release v0.0.1"
```

### 3. Dry Run (Highly Recommended)

**Note**: The `maven-publish` plugin automatically stages artifacts when JReleaser runs, so no manual staging is needed.

```bash
# Load environment variables
source .env.local

# Check what will be released without actually releasing
./gradlew jreleaserFullRelease --dryrun

# Review the output carefully - check:
# - Version numbers are correct (0.0.1)
# - Git tag will be created properly (v0.0.1)
# - GitHub release configuration looks good
# - Maven Central Portal artifacts are staged correctly
```

### 4. Execute Release

```bash
# This will:
# 1. Stage artifacts via maven-publish to build/staging-deploy/
# 2. Create and push git tag (v0.0.1)  
# 3. Create GitHub release with conventional commit changelog
# 4. Upload JAR artifacts to GitHub release
# 5. Publish artifacts to Maven Central Portal
./gradlew jreleaserFullRelease
```

### 5. Post-Release Tasks

```bash
# Bump to next development version
# In build.gradle: version = '0.0.2-SNAPSHOT'

# Commit and push
git add build.gradle  
git commit -m "chore: prepare next development iteration v0.0.2-SNAPSHOT"
git push origin main
```

### 6. SNAPSHOT Releases (Development)

SNAPSHOT versions are automatically handled differently by JReleaser:

**Automated SNAPSHOT Publishing** (via GitHub Actions):
- SNAPSHOT versions automatically deploy to Maven Central's snapshot repository
- No GitHub releases are created for SNAPSHOT versions
- Triggered automatically on pushes to `main` branch when version contains `-SNAPSHOT`

**Manual SNAPSHOT Publishing**:
```bash
# Load environment variables
source .env.local

# For SNAPSHOT versions, this will:
# 1. Stage artifacts to build/staging-deploy/
# 2. Deploy to Maven Central snapshot repository (https://central.sonatype.com/repository/maven-snapshots/)
# 3. Skip GitHub release creation
./gradlew jreleaserFullRelease

# Check staged artifacts locally
ls -la build/staging-deploy/com/phatjam98/proto-faker/
```

**SNAPSHOT Repository Configuration**:
- **Release versions** → `https://central.sonatype.com/api/v1/publisher` (Maven Central Portal)
- **SNAPSHOT versions** → `https://central.sonatype.com/repository/maven-snapshots/` (Maven Central Snapshots)

## Release Verification

After release, verify deployment at:

1. **GitHub Release**: https://github.com/phatjam98/proto-faker/releases
   - [ ] Release created with correct version
   - [ ] Release notes generated properly
   - [ ] JAR files attached to release

2. **Maven Central**: https://central.sonatype.com/artifact/com.phatjam98/proto-faker
   - [ ] Version published successfully
   - [ ] All required artifacts present (jar, sources, javadoc)
   - [ ] POM file looks correct

3. **Maven Central Search**: https://search.maven.org/artifact/com.phatjam98/proto-faker
   - [ ] Version appears in public search (may take 15-30 minutes)

## Conventional Commits for Better Changelogs

JReleaser generates release notes from git commit messages. Use conventional commits format:

```bash
feat: add fluent API for field overrides
fix: resolve enum generation bug for UNKNOWN values  
docs: update README with new examples
chore: update dependencies
perf: optimize repeated field generation
BREAKING CHANGE: change ProtoFaker constructor signature
```

This creates organized changelogs with sections:
- 🚀 Features (feat:)
- 🐛 Bug Fixes (fix:)  
- 📈 Performance (perf:)
- 📝 Documentation (docs:)
- 🧹 Housekeeping (chore:)

## Troubleshooting

### Common Issues

**GPG Signing Fails**
```bash
# Check GPG key exists (should show 0BF66CC2B921A8AA)
gpg --list-secret-keys --keyid-format LONG

# Test signing with project key
echo "test" | gpg --clearsign --default-key 0BF66CC2B921A8AA

# Re-export keys if needed (current project key)
export JRELEASER_GPG_PUBLIC_KEY="$(gpg --armor --export 0BF66CC2B921A8AA)"
export JRELEASER_GPG_SECRET_KEY="$(gpg --armor --export-secret-keys 0BF66CC2B921A8AA)"

# Note: Current key has no passphrase, so JRELEASER_GPG_PASSPHRASE is not needed
```

**Maven Central Portal Authentication Fails**
- Verify you're using Central Portal tokens from https://central.sonatype.com/
- NOT legacy OSSRH credentials (oss.sonatype.org) 
- Check token expiration in your Central Portal account
- Ensure JRELEASER_MAVENCENTRAL_USERNAME/PASSWORD exactly match token values

**GitHub Token Issues**
- Verify token has `repo` scope (required for releases)
- Check if organization requires SSO authorization
- Ensure token hasn't expired
- Test token: `curl -H "Authorization: token $JRELEASER_GITHUB_TOKEN" https://api.github.com/user`

**Artifacts Not Found**
```bash
# Check if maven-publish plugin staged correctly
ls -la build/staging-deploy/com/phatjam98/proto-faker/

# Expected structure:
# build/staging-deploy/com/phatjam98/proto-faker/0.0.1/
#   ├── proto-faker-0.0.1.jar
#   ├── proto-faker-0.0.1-javadoc.jar  
#   ├── proto-faker-0.0.1-sources.jar
#   └── proto-faker-0.0.1.pom

# Re-stage if needed (automatically done by jreleaserFullRelease)
./gradlew publishMavenJavaPublicationToStagingRepository
```

**JReleaser Configuration Issues**
```bash
# Check if jreleaser.yml is found and valid
./gradlew jreleaserConfig

# Test configuration with dummy values
JRELEASER_GITHUB_TOKEN=dummy \
JRELEASER_MAVENCENTRAL_USERNAME=dummy \
JRELEASER_MAVENCENTRAL_PASSWORD=dummy \
JRELEASER_GPG_PUBLIC_KEY=dummy \
JRELEASER_GPG_SECRET_KEY=dummy \
./gradlew jreleaserFullRelease --dryrun
```

### Debug Commands

```bash
# Check full configuration (includes sensitive data masking)
./gradlew jreleaserConfig

# Validate release without executing (recommended)
./gradlew jreleaserFullRelease --dryrun

# View detailed logs during release
./gradlew jreleaserFullRelease --info

# Check specific JReleaser configuration section  
./gradlew jreleaserConfig -q | grep -A 20 "project:"

# Test maven-publish integration
./gradlew publishMavenJavaPublicationToStagingRepository --info
```

### Getting Help

- JReleaser Documentation: https://jreleaser.org/guide/latest/
- JReleaser Discussions: https://github.com/jreleaser/jreleaser/discussions
- Maven Central Support: central-support@sonatype.com

## Deployment Configuration Details

**JReleaser Configuration** (`jreleaser.yml`):
- **Release Deploy**: `mavenCentral.release-deploy` handles production releases to Maven Central Portal
- **SNAPSHOT Deploy**: `nexus2.snapshot-deploy` handles SNAPSHOT versions to Maven Central snapshots repository  
- **Conditional Processing**: JReleaser automatically routes based on version (SNAPSHOT vs release)
- **GitHub Releases**: Automatically skipped for SNAPSHOT versions using `skipRelease: '{{isSnapshot}}'`

**Repository Routing**:
```yaml
# Production releases (e.g., 0.0.1)
mavenCentral.release-deploy.url: https://central.sonatype.com/api/v1/publisher

# SNAPSHOT releases (e.g., 0.0.1-SNAPSHOT)  
nexus2.snapshot-deploy.snapshotUrl: https://central.sonatype.com/repository/maven-snapshots/
```

## GitHub Actions Automation

The project includes automated CI/CD via multiple workflows:

**1. CI Workflow** (`.github/workflows/ci.yml`):
- ✅ Runs tests on every push/PR to `main` branch
- ✅ Performs code quality checks (Checkstyle, Jacoco coverage)
- ✅ Validates code without publishing

**2. SNAPSHOT Release Workflow** (`.github/workflows/snapshot.yml`):
- ✅ Automatically triggered on pushes to `main` branch
- ✅ Only processes versions containing `-SNAPSHOT`
- ✅ Publishes to Maven Central snapshots repository
- ✅ Skips GitHub release creation
- ✅ Uses JReleaser with proper SNAPSHOT routing

**3. Production Release Workflow** (`.github/workflows/release.yml`):
- ✅ Triggered on version tags (e.g., `v0.0.1`)  
- ✅ Creates GitHub releases with changelogs
- ✅ Publishes to Maven Central Portal
- ✅ Handles full release process

### Current CI/CD Workflow

```yaml
# Triggered on push to master, PRs, and GitHub releases
on:
  push:
    branches: [ master ]
  pull_request: 
    branches: [ master ]
  release:
    types: [created]
```

**Build Steps**: 
- Java 11 setup with Temurin distribution
- Gradle caching for faster builds
- Full test suite with coverage verification
- Checkstyle validation

### Upgrading to JReleaser Automation

**Option 1: Manual GitHub Release + JReleaser CI**

1. Create GitHub release manually (triggering `release.created` event)
2. CI picks up release and runs JReleaser to publish to Maven Central

**Option 2: Fully Automated Release Workflow**

Create `.github/workflows/release.yml`:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

permissions:
  contents: write
  
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
          
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: 11
          distribution: temurin
          
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          
      - name: Run Tests
        run: ./gradlew test checkstyleMain checkstyleTest
        
      - name: Release with JReleaser
        env:
          JRELEASER_GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          JRELEASER_MAVENCENTRAL_USERNAME: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          JRELEASER_MAVENCENTRAL_PASSWORD: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
          JRELEASER_GPG_PUBLIC_KEY: ${{ secrets.GPG_PUBLIC_KEY }}
          JRELEASER_GPG_SECRET_KEY: ${{ secrets.GPG_SECRET_KEY }}
        run: ./gradlew jreleaserFullRelease
```

### Required GitHub Secrets

Configure these in repository Settings > Secrets and variables > Actions:

```bash
# Maven Central Portal credentials
MAVEN_CENTRAL_USERNAME    # Your central.sonatype.com token username
MAVEN_CENTRAL_PASSWORD    # Your central.sonatype.com token password

# GPG signing (base64 encoded)
GPG_PUBLIC_KEY           # $(gpg --armor --export 0BF66CC2B921A8AA | base64 -w0)
GPG_SECRET_KEY           # $(gpg --armor --export-secret-keys 0BF66CC2B921A8AA | base64 -w0)

# Note: GITHUB_TOKEN is automatically provided, GPG_PASSPHRASE not needed for current key
```

### Migration from Legacy OSSRH

The current `.github/workflows/cicd.yml` uses outdated OSSRH secrets:

**Current (Legacy)**:
- `OSSRH_USERNAME` / `OSSRH_TOKEN` → **Deprecated**
- `GPG_SECRET_KEYS` / `GPG_PASSPHRASE` → **Different format**

**Updated for JReleaser**:
- `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` → **Central Portal tokens**
- `GPG_PUBLIC_KEY` / `GPG_SECRET_KEY` → **Armor-exported format**

### Automated Release Workflow

1. **Developer commits**: Use conventional commit messages
2. **Bump version**: Remove `-SNAPSHOT` in `build.gradle` 
3. **Create git tag**: `git tag v0.0.1 && git push origin v0.0.1`
4. **GitHub Actions**: Automatically triggered by tag push
5. **JReleaser**: Creates release + publishes to Maven Central
6. **Post-release**: Manually bump to next SNAPSHOT version

---

## Current Project State Summary

✅ **Ready for Release**:
- JReleaser 1.15.0 integrated with `maven-publish` plugin
- GPG key `0BF66CC2B921A8AA` configured and verified (expires 2027-10-31)
- Version `0.0.1-SNAPSHOT` ready for first release
- Configuration tested with dry-run

⏳ **Pending Setup**:
- Maven Central Portal credentials (need real tokens)
- GitHub Actions secrets configuration (for automation)
- Decision on manual vs. automated release workflow

🔧 **Next Steps**:
1. Set up Maven Central Portal account and generate tokens
2. Run first manual release: `./gradlew jreleaserFullRelease --dryrun`
3. Execute first release: `./gradlew jreleaserFullRelease`
4. Configure GitHub Actions for automated releases (optional)

**Key Integration**: The `maven-publish` plugin is **required** for JReleaser - it stages artifacts to `build/staging-deploy/` which JReleaser then publishes to Maven Central Portal. This integration is already configured and working.