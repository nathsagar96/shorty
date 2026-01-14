# Shorty - URL Shortener Service

A modern, feature-rich URL shortening service built with Spring Boot and PostgreSQL.

## 🚀 Features

### Core Functionality

- **URL Shortening**: Convert long URLs into short, manageable links
- **Custom Aliases**: Create memorable short URLs with custom aliases
- **URL Expiration**: Set expiration times for short URLs (optional)
- **Click Tracking**: Monitor how many times each short URL has been accessed
- **Redirect Management**: Handle redirects with proper HTTP status codes

### Advanced Features

- **Automatic Cleanup**: Scheduled cleanup of expired URLs
- **Collision Detection**: Secure short code generation with retry mechanism
- **Transaction Support**: ACID-compliant operations for data integrity
- **OpenAPI Documentation**: Built-in API documentation with Swagger UI
- **Health Monitoring**: Spring Actuator endpoints for system monitoring
- **User Isolation**: Each user can only access their own URLs

## 📦 Technologies

- **Backend**: Spring Boot 4.0 with Java 25
- **Database**: PostgreSQL 18 with JPA/Hibernate
- **API Documentation**: SpringDoc OpenAPI 3.0
- **Build Tool**: Maven with Spotless code formatting
- **Code Generation**: MapStruct for DTO mapping
- **Containerization**: Docker Compose for PostgreSQL and Keycloak
- **Authentication**: OAuth2/OIDC with Keycloak

## 🚀 Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.9+
- Docker (for PostgreSQL and Keycloak)
- PostgreSQL 18 (or use Docker)

### Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/nathsagar96/shorty.git
   cd shorty
   ```

2. **Start services with Docker Compose**:

   ```bash
   docker-compose up -d
   ```

3. **Build and run the application**:

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**:
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Actuator: `http://localhost:8080/actuator`
   - Keycloak Admin Console: `http://localhost:9090/admin` (admin/admin)

## 📖 API Documentation

The application provides comprehensive OpenAPI documentation:

### Endpoints

| Method   | Path                       | Description              |
|----------|----------------------------|--------------------------|
| `POST`   | `/api/v1/urls`             | Create a new short URL   |
| `GET`    | `/api/v1/urls/{shortCode}` | Get URL details          |
| `DELETE` | `/api/v1/urls/{shortCode}` | Delete a short URL       |
| `GET`    | `/{shortCode}`             | Redirect to original URL |

### Authentication

All API endpoints except public redirects (`/{shortCode}`) require authentication via Bearer Token.

To obtain a token from Keycloak:

```bash
curl -X POST http://localhost:9090/realms/shorty/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&password=password&grant_type=password&client_id=shorty-app"
```

### Example Requests

**Create Short URL**:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "originalUrl": "https://example.com/very/long/url",
    "customAlias": "mycustom",
    "expiresInDays": 30
  }'
```

**Get URL Details**:

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8080/api/v1/urls/mycustom
```

**Redirect**:

```bash
curl -L http://localhost:8080/mycustom
```

## 🔍 Key Features

### Short Code Generation

- **Base62 Encoding**: Uses alphanumeric characters (a-z, A-Z, 0-9)
- **Secure Random**: Cryptographically secure random number generation
- **Collision Handling**: Automatic retry mechanism (3 attempts by default)
- **Validation**: Custom alias validation with regex pattern `^[a-zA-Z0-9]+$`

### URL Management

- **Expiration**: Optional expiration with automatic cleanup
- **Click Tracking**: Incremental counter for each redirect
- **Atomic Operations**: Transactional database operations
- **Concurrency Control**: Optimistic locking for high traffic
- **User Isolation**: Each user can only access their own URLs

### Error Handling

- **Custom Exceptions**: Specific exceptions for different error scenarios
- **Global Exception Handler**: Consistent error responses
- **Problem Details**: RFC 7807 compliant error responses

## 🛡️ Security

- **OAuth2/OIDC Authentication**: Integrated with Keycloak for user authentication
- **JWT Authorization**: Access tokens validated via Keycloak's JWK set
- **Role-Based Access Control**: API endpoints secured with method-level security
- **Input Validation**: Comprehensive validation for all API inputs
- **Secure Random**: Cryptographically secure short code generation
- **SQL Injection Protection**: JPA parameterized queries
- **User Isolation**: Each user can only access their own URLs

## 📊 Monitoring

- **Spring Actuator**: Health, metrics, and info endpoints
- **Prometheus Integration**: Metrics export for monitoring
- **Structured Logging**: JSON logging with log levels

## 🔧 Customization

### Short Code Configuration

```yaml
app:
  shortcode:
    length: 7  # Length of generated short codes (3-10)
```

### Retry Configuration

```yaml
app:
  retry:
    attempts: 3  # Maximum attempts for unique code generation
```

### Cleanup Schedule

```yaml
app:
  cleanup:
    cron: "0 0 0 * * ?"  # Daily at midnight
```

### Default Expiration

```yaml
app:
  url-expiration:
    default-hours: 8760  # 1 year in hours
```

## 🧪 Testing

The application includes comprehensive test coverage:

- **Unit Tests**: Service layer and utility classes
- **Mock Testing**: Mock MVC tests for controllers

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Create a new Pull Request

## 📞 Support

For issues, questions, or feature requests, please open an issue on GitHub.

---

**Shorty** - Your reliable URL shortening service! 🚀
