# Traveler Registration Backend

Backend API for a traveler registration application that helps collect identity details from travelers staying at the same accommodation.

## Technology Stack

- **Java 21**
- **Spring Boot 4.1.1**
- **H2 Database** (embedded, no installation required)
- **Flyway** for database migrations
- **Maven Wrapper** for builds

## Prerequisites

1. **Java 21** - [Download OpenJDK 21](https://adoptium.net/)
2. **Git** (for version control)

## Local Development Setup

### 1. Build the Project

```bash
# On Windows
.\mvnw.cmd clean install

# On Linux/Mac
./mvnw clean install
```

### 2. Run the Application

```bash
# On Windows
.\mvnw.cmd spring-boot:run

# On Linux/Mac
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Access H2 Database Console

Once the application is running, you can access the H2 console to view and query the database:

1. Open your browser at: **http://localhost:8080/h2-console**
2. Use these credentials:
   - **JDBC URL**: `jdbc:h2:file:./data/travelerdb`
   - **User Name**: `sa`
   - **Password**: _(leave empty)_
3. Click **Connect**

See [H2_DATABASE.md](H2_DATABASE.md) for more details.

### 4. Verify Setup

Check the application logs for:

- Successful database connection to H2
- Flyway migrations applied automatically
- Spring Boot started successfully

## Project Structure

```
src/
├── main/
│   ├── java/com/traveler/traveler_backend/
│   │   ├── config/          # Application configuration
│   │   ├── controller/      # REST endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── service/         # Business logic
│   │   ├── exception/       # Exception handling
│   │   └── security/        # Security configuration
│   └── resources/
│       ├── db/migration/    # Flyway SQL migrations
│       └── application.properties
└── test/                    # Test classes
```

## Running Tests

```bash
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=ClassName
```

## API Endpoints

API endpoints are versioned under `/api/v1`. Documentation will be added as endpoints are implemented.

### Health Check

- `GET /actuator/health` - Application health status (to be configured)

## Development Phases

This project follows an incremental development approach:

1. ✅ **Phase 1** - Repository and project setup
2. **Phase 2** - CRUD implementation and tests
3. **Phase 3** - Authentication and authorization
4. **Phase 4** - Invitations and guest submission
5. **Phase 5** - Transfer, acknowledgement, and deletion
6. **Phase 6** - Guest web form and mobile app
7. **Phase 7** - PDF export and sharing
8. **Phase 8** - Pre-production review

## Security Notes

- Personal data is stored **temporarily** on the server
- Data is deleted after mobile app acknowledgement
- Invitation tokens expire after 7 days
- No production secrets in source control
- HTTPS required for deployed environments

## Documentation

Additional project documentation:

- [Project Plan](ai-docs/PROJECT_PLAN.md) - Full product specification
- [Agent Guidelines](ai-docs/AGENTS.md) - Development guidelines and architecture

## Troubleshooting

### Database Reset

If you need to start fresh with an empty database:

```bash
# Stop the application
# Delete the database file
rm -rf ./data/

# Restart the application - Flyway will recreate everything
```

### Flyway Migration Errors

If migrations fail:

1. Check the `flyway_schema_history` table in H2 Console
2. To reset (DEVELOPMENT ONLY): Delete the `./data/` folder
3. Never edit applied migrations in shared environments

### Port Already in Use

If port 8080 is busy, change it in `application.properties`:

```properties
server.port=8081
```

## Contributing

Follow the development guidelines in `ai-docs/AGENTS.md` when contributing to this project.

## License

[To be defined]
