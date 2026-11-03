# AGENTS.md

## Mission

Implement the traveler-registration application according to
`PROJECT_PLAN.md`. Work incrementally, keep personal data exposure
minimal, and prioritize correctness, security, testability, and
maintainability.

Do not implement the entire product in one pass. Do not begin frontend
implementation until the backend CRUD and its tests are complete and
passing.

## 1. Source of truth and scope

- Read `PROJECT_PLAN.md` before making architectural or implementation
  changes.
-   Treat confirmed decisions in that file as requirements.
-   If a requirement is ambiguous, choose the smallest safe
    implementation and document the assumption.
-   Do not silently expand scope.
-   Do not add direct integrations with the police, SES.HOSPEDAJES,
    email providers, or WhatsApp APIs.
-   Do not add identity-document photo uploads or OCR.
-   Do not claim that the generated PDF is an official submission or is
    automatically importable into an authority system.
-   Keep the full legal review and final regulatory field validation as
    a pre-production task, while keeping the data model adaptable enough
    to support the results.

## 2. Technology constraints

-   Use Java 21.
-   Use the stable Spring Boot version selected through Spring
    Initializr and compatible with Java 21.
-   Use Maven Wrapper; do not require a globally installed Maven.
-   Use Spring Web, Spring Data JPA, Jakarta Bean Validation,
    PostgreSQL, Flyway, and Spring Security as appropriate to the
    current phase.
-   Use JUnit 5 and Spring Boot testing support. Use Mockito where it
    adds value.
-   Use PostgreSQL installed locally for development.
-   Do not introduce Docker or Docker Compose.
-   Do not add new infrastructure or dependencies without a clear need
    and a short explanation.
-   Keep dependency versions compatible and managed through Spring Boot
    dependency management where possible.

## 3. Architecture and code organization

Use a clear layered architecture. A reasonable package structure is:

-   `config` --- application and security configuration.
-   `controller` --- REST endpoints and HTTP-level concerns.
-   `dto` --- request and response objects.
-   `entity` --- JPA persistence entities.
-   `repository` --- Spring Data repositories.
-   `service` --- business rules and orchestration.
-   `exception` --- domain exceptions and centralized error handling.
-   `security` --- authentication and authorization components.
-   `cleanup` --- scheduled expiry and cleanup jobs, when that phase is
    reached.

Adjust the structure if the codebase already has a consistent
convention, but avoid unnecessary architectural patterns or
abstractions.

Rules:

-   Controllers must stay thin.
-   Put business rules in services.
-   Use repositories only for persistence access.
-   Use DTOs for API requests and responses; never expose JPA entities
    directly from controllers.
-   Use constructor injection.
-   Prefer immutable DTOs where practical.
-   Use meaningful names and small methods with one clear
    responsibility.
-   Avoid generic frameworks, factories, facades, or abstractions unless
    they solve a concrete problem.
-   Use transactions at appropriate service boundaries.
-   Avoid N+1 queries and unbounded collection endpoints.
-   Use pagination when a list can reasonably grow.
-   Keep API routes consistently versioned under `/api/v1` unless the
    existing project establishes another documented convention.

## 4. Data model and privacy

-   Keep personal traveler data separate from operational metadata.
-   Store only the minimum operational stay data required by the backend
    workflow.
-   Do not store personal stay data permanently on the server.
-   Treat guest submissions as temporary payloads.
-   Store invitation tokens as hashes where feasible; do not persist or
    log raw tokens unnecessarily.
-   Give tokens sufficient cryptographic randomness and make them
    unguessable.
-   An invitation expires seven days after creation and accepts only one
    successful submission.
-   Make token consumption and submission creation atomic to prevent
    concurrent duplicate submissions.
-   Store no plaintext passwords. If password authentication is
    implemented, use Spring Security's supported password hashing
    mechanisms.
-   Do not log identity fields, document numbers, addresses, birth
    dates, raw tokens, form payloads, PDF content, passwords, or
    secrets.
-   Do not place personal data in exception messages, metric labels,
    tracing attributes, URLs, or audit events.
-   Do not add personal data to test snapshots or fixtures unless
    necessary; use clearly synthetic values.
-   Never commit credentials, signing keys, encryption keys, database
    passwords, or production secrets.
-   Document any new personal-data field and why it is necessary.

## 5. Authorization and API safety

-   Every operation on a stay, invitation, or submission must verify
    that the authenticated user is authorized to access it.
-   Do not rely on a client-provided owner ID as proof of ownership.
-   Validate all external input on the server.
-   Use Jakarta Bean Validation for request DTOs where applicable.
-   Return consistent error responses through centralized exception
    handling.
-   Do not reveal whether another user's record exists.
-   Apply appropriate rate limits or abuse protections to public
    invitation endpoints when feasible.
-   Never return another traveler's submission through a guest
    invitation endpoint.
-   Use HTTPS in deployed environments; local development may use
    localhost.
-   Keep CORS configuration restrictive and explicit when the frontend
    is introduced.
-   Do not disable security checks just to make a test pass.

## 6. Temporary submission lifecycle

Follow this lifecycle:

1.  Guest submits data through a valid invitation.
2.  Backend validates the token and payload and stores a temporary
    submission.
3.  The authenticated main traveler's mobile app retrieves pending
    submissions for an authorized stay.
4.  The mobile app saves the data successfully in encrypted local
    storage.
5.  Only then does the mobile app acknowledge receipt.
6.  The backend deletes the temporary personal-data payload after a
    valid acknowledgement.

Additional requirements:

-   If local storage fails, the client must not acknowledge receipt.
-   Retrieval and acknowledgement must be retry-safe.
-   Acknowledgement should be idempotent.
-   Do not delete a submission before successful acknowledgement, except
    under the documented expiry/cleanup policy.
-   Clean up expired invitations and abandoned submissions after the
    configured seven-day period.
-   Make cleanup safe to rerun.
-   Ensure that cleanup removes personal payloads and not merely a
    status flag.
-   Do not claim that database deletion also removes every backup copy
    unless the backup policy supports that claim.
-   Keep acknowledgement and cleanup metadata free of unnecessary
    personal information.

## 7. Database and migrations

-   Use Flyway for schema changes.
-   Do not rely on Hibernate schema auto-update for shared or
    production-like environments.
-   Every schema change must have a migration.
-   Do not edit a migration that may already have been applied to a
    persistent environment; create a new migration instead.
-   Keep entity mappings consistent with migrations.
-   Use appropriate database constraints for unique invitation token
    hashes, required fields, and relationships.
-   Define indexes for common lookups such as invitation status, expiry,
    stay ID, and pending submissions where justified.
-   Use transactions for operations that must succeed or fail together.
-   Use optimistic locking or equivalent safeguards where concurrency
    could cause duplicate submissions or conflicting state transitions.
-   Use synthetic development data only.

## 8. API design

-   Use explicit request and response DTOs.
-   Use appropriate HTTP status codes.
-   Use `201 Created` for successful resource creation where
    appropriate, `400` for validation errors, `401` for unauthenticated
    requests, `403` for forbidden operations, `404` for inaccessible or
    missing resources where appropriate, and `409` for state conflicts
    such as reused invitation tokens.
-   Keep error responses stable and free of personal data.
-   Do not return raw internal exception details to clients.
-   Document endpoints and representative request/response examples as
    they are implemented.
-   Do not implement endpoints for out-of-scope authority, email, or
    WhatsApp integrations.

## 9. Testing requirements

For every feature:

-   Add or update automated tests in the same change.
-   Test the happy path and relevant failure paths.
-   Test validation and authorization, not just controller success.
-   Test transactional and state-transition behavior.
-   Use integration tests for persistence and API behavior where
    appropriate.
-   Use deterministic clocks or injectable time abstractions for expiry
    tests where practical.
-   Test invitation expiry, single-use behavior, and concurrent
    submission protection.
-   Test that failed local-storage workflows do not result in
    acknowledgement, at the API-contract level where client tests exist.
-   Test acknowledgement idempotency and server-side payload deletion.
-   Test scheduled cleanup and ensure it can run repeatedly.
-   Test that API errors and application logs do not contain sensitive
    values.
-   Do not weaken or remove a test to hide a defect; fix the
    implementation or explain why the expected behavior must change.

Before finishing a phase, run the relevant Maven tests and report the
command and outcome. If tests cannot be run, state that explicitly and
explain why.

## 10. Code quality

-   Follow standard Java naming conventions.
-   Use clear, descriptive class and method names.
-   Keep methods focused and avoid duplicated business rules.
-   Avoid unnecessary comments that restate the code.
-   Add comments for non-obvious security or lifecycle decisions.
-   Do not use `System.out.println` for application logging.
-   Do not catch exceptions unless the code can handle them meaningfully
    or translate them safely.
-   Do not suppress compiler warnings without justification.
-   Do not add Lombok solely for convenience unless the project
    explicitly adopts it.
-   Avoid adding a mapping library or other dependency for a small
    number of DTO mappings.
-   Keep formatting consistent with the project formatter.

## 11. Configuration and local development

-   Provide safe defaults for local development without embedding real
    secrets.
-   Use environment variables or an untracked local configuration file
    for credentials.
-   Include an `.env.example` only if the project uses that pattern;
    never commit the real `.env`.
-   Document how to install/configure PostgreSQL locally, create the
    database and user, apply migrations, run the application with the
    Maven Wrapper, and execute tests.
-   Do not require Docker.
-   Ensure application startup failures clearly identify missing
    configuration without printing secrets.
-   Keep development, test, and production configuration separate.

## 12. Frontend sequencing

Do not implement the frontend until all of the following are true:

-   Initial backend CRUD is implemented.
-   DTO validation and centralized error handling are in place.
-   Relevant unit and integration tests pass.
-   Database migrations are committed.
-   API endpoints and example requests are documented.

When frontend work is authorized:

-   Guest web form requires no guest account.
-   Mobile app stores personal stay data in encrypted local storage.
-   The encryption key is protected by operating-system secure storage.
-   The client acknowledges receipt only after successful local
    persistence.
-   Generate the PDF locally and use the native share sheet.
-   Do not add automatic email or WhatsApp sending.

## 13. Working procedure for coding agents

For each task:

1.  Inspect the relevant files and current project structure before
    editing.
2.  State the smallest implementation plan if the task affects multiple
    files or layers.
3.  Implement only the requested phase or feature.
4.  Add migrations, validation, authorization, and tests as required.
5.  Run relevant tests and checks.
6.  Review the diff for accidental scope expansion, secrets, personal
    data in logs, and unrelated formatting changes.
7.  Summarize files changed, behavior implemented, tests run, and any
    remaining limitations.

Do not:

-   Rewrite large parts of the project without need.
-   Add future-phase features "for convenience."
-   Replace the selected stack without asking.
-   Invent regulatory requirements or claim legal compliance without
    validation.
-   Mark a task complete when tests have not been run.
-   Start the frontend while backend CRUD is still incomplete.

## 14. Definition of done

A task is done only when:

-   Its acceptance criteria are met.
-   Relevant automated tests are added or updated and pass.
-   Validation, authorization, and failure paths have been considered.
-   No sensitive data or secrets are exposed.
-   Documentation is updated where behavior or setup changes.
-   No out-of-scope integration has been introduced.
-   Any unverified legal, deployment, or backup assumptions are clearly
    recorded.
