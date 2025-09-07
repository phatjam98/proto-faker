# Security Policy

## Supported Versions

ProtoFaker follows semantic versioning. Security updates are provided for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.x     | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

The ProtoFaker team takes security vulnerabilities seriously. We appreciate your efforts to responsibly disclose any security concerns.

### Reporting Process

**Please do NOT report security vulnerabilities through public GitHub issues.**

Instead, please report security vulnerabilities by emailing **phatjam98@gmail.com** with the following information:

- **Subject**: [SECURITY] ProtoFaker Vulnerability Report
- **Description**: Detailed description of the vulnerability
- **Impact**: Potential impact and attack scenarios
- **Reproduction**: Step-by-step instructions to reproduce the issue
- **Affected Versions**: Which versions of ProtoFaker are affected
- **Proposed Solution**: If you have suggestions for fixing the issue

### Response Timeline

- **Acknowledgment**: We will acknowledge receipt of your report within 48 hours
- **Initial Assessment**: We will provide an initial assessment within 7 days
- **Status Updates**: We will provide regular updates on our progress
- **Resolution**: We aim to resolve critical vulnerabilities within 30 days

### Disclosure Policy

- We practice **responsible disclosure**
- We will work with you to understand and resolve the issue before any public disclosure
- We will credit you in our security advisory unless you prefer to remain anonymous
- We request that you do not publicly disclose the vulnerability until we have had a chance to address it

## Artifact Verification

### GPG Signature Verification

All ProtoFaker releases are signed with GPG for integrity verification.

**Signing Key Information:**
- **Key ID**: `0BF66CC2B921A8AA`
- **Key Type**: RSA 4096-bit
- **Owner**: phatjam98@gmail.com
- **Expires**: 2027-10-31

### Verifying Release Signatures

#### 1. Import the Public Key

```bash
# Import from keyserver
gpg --keyserver keyserver.ubuntu.com --recv-keys 0BF66CC2B921A8AA

# Or import from GitHub release assets (when available)
curl -L https://github.com/phatjam98/proto-faker/releases/download/v1.x.x/public-key.asc | gpg --import
```

#### 2. Verify Key Fingerprint

```bash
gpg --fingerprint 0BF66CC2B921A8AA
```

**Expected fingerprint**: Verify the displayed fingerprint matches our published fingerprint.

#### 3. Verify Release Artifacts

```bash
# Download the signature file (.asc) alongside the JAR
gpg --verify proto-faker-1.x.x.jar.asc proto-faker-1.x.x.jar
```

**Expected output**: Should show "Good signature from 'phatjam98@gmail.com'"

### Maven Central Verification

ProtoFaker artifacts published to Maven Central include:
- **PGP Signatures**: All artifacts are signed with our GPG key
- **Checksums**: SHA-1 and MD5 checksums for integrity verification
- **Sources**: Source JAR for transparency
- **Javadoc**: Documentation JAR

## Security Best Practices for Contributors

### Development Environment

1. **Use Latest Java**: Ensure you're using a supported Java version (11+)
2. **Keep Dependencies Updated**: Regularly update dependencies to patch security vulnerabilities
3. **Scan Dependencies**: Use tools like `./gradlew dependencyCheckAnalyze` to identify vulnerable dependencies
4. **Secure Development**: Follow secure coding practices for Java development

### Credential Management

1. **Environment Variables**: Use environment variables for sensitive configuration
2. **Local Environment Files**: Use `.env.local` files (never commit to Git)
3. **Secure Storage**: Store credentials in secure password managers
4. **Access Controls**: Use least-privilege access for tokens and keys
5. **Regular Rotation**: Rotate credentials regularly

### Code Contributions

1. **Security Review**: All pull requests undergo security review
2. **Static Analysis**: Code is analyzed with Checkstyle and other tools
3. **Dependency Scanning**: Dependencies are scanned for known vulnerabilities
4. **Test Coverage**: Maintain high test coverage to catch potential issues

### Release Security

1. **Signed Releases**: All releases are GPG-signed
2. **Secure Pipeline**: Release pipeline uses secured environment variables
3. **Audit Trail**: Full audit trail for all releases through GitHub Actions
4. **Verification**: Multi-step verification process before publication

## Security Architecture

### Threat Model

ProtoFaker is a testing utility library with the following security considerations:

**In Scope:**
- Supply chain security (dependency vulnerabilities)
- Build and release pipeline security
- Artifact integrity and authenticity
- Secure development practices

**Out of Scope:**
- Runtime security (ProtoFaker is for testing only)
- Production data protection (generates fake data)
- Network security (no network operations)

### Security Controls

1. **Dependency Management**: Automated dependency vulnerability scanning
2. **Code Quality**: Enforced coding standards and static analysis
3. **Build Security**: Secured CI/CD pipeline with credential management
4. **Artifact Signing**: GPG signing of all release artifacts
5. **Access Controls**: Limited maintainer access with MFA requirements

## Acknowledgments

We would like to thank the following individuals for their responsible disclosure of security vulnerabilities:

*No security vulnerabilities have been reported to date.*

## Contact

For security-related questions or concerns:
- **Email**: phatjam98@gmail.com
- **Subject**: [SECURITY] ProtoFaker Security Inquiry

For general questions, please use GitHub Issues or Discussions.

---

**Last Updated**: 2025-09-07