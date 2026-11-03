# PROJECT_PLAN.md

## 1. Purpose

Build a temporary traveler-registration application that helps a main
traveler collect identity details from the other travelers staying at
the same accommodation, prepare a readable PDF, and share it manually
with the accommodation owner or manager.

The application is a data-collection and document-generation tool. It
does **not** communicate directly with the police, SES.HOSPEDAJES, or
any other authority. It does not send email or WhatsApp messages
automatically.

This plan defines the initial development scope. A detailed legal review
and final validation of regulatory fields and document requirements are
deferred until before production deployment.

## 2. Product scope

### 2.1 Main traveler mobile application

The main traveler will be able to:

-   Create an account and authenticate.
-   Create and manage stays.
-   Add the minimum stay and accommodation information needed for the
    workflow and PDF.
-   Create invitation links for other travelers.
-   See the status of invitations and submitted forms.
-   Receive submitted traveler details from the backend.
-   Store stay-related personal data locally on the device using
    encrypted storage.
-   Protect the local encryption key using the operating system's secure
    storage facilities.
-   Generate a PDF for a stay.
-   Share the PDF through the device's native share sheet, including
    compatible email and messaging apps.
-   Delete local stay data when appropriate.

The application must clearly warn users that generating or sharing a PDF
does not itself submit an official report or satisfy every legal
obligation of an accommodation owner.

### 2.2 Guest web form

Invited travelers will use a web form without installing the mobile app
or creating an account.

The form will:

-   Be accessed through a unique invitation URL containing a
    cryptographically random token.
-   Accept one valid submission per invitation.
-   Expire seven days after invitation creation.
-   Collect typed identity details; document photographs will not be
    requested.
-   Validate required fields and provide understandable error messages.
-   Confirm successful submission without exposing another traveler's
    data.
-   Reject expired, invalid, already-used, or revoked invitation tokens.

The definitive field list will be configurable or easy to evolve and
will be validated during the pre-production legal review.

### 2.3 Backend API

The backend is required because guests submit data through a web form
and the main traveler's mobile device may not be online at that time.

Backend responsibilities:

-   Authenticate main travelers and authorize access to their own
    records.
-   Create and manage the minimum operational stay metadata required by
    the workflow.
-   Create, expire, and consume invitation tokens.
-   Receive guest submissions.
-   Temporarily hold personal data until the mobile application confirms
    that it has received and stored the data successfully.
-   Delete temporary personal data after that confirmation.
-   Automatically clean up expired invitations and abandoned
    submissions.
-   Provide a secure API for the mobile app and guest web form.
-   Avoid storing personal stay data permanently on the server.

The backend must not provide a direct authority-integration feature, an
email-sending feature, or a WhatsApp API integration in the initial
scope.

## 3. Confirmed product decisions

  -----------------------------------------------------------------------
  Topic                               Decision
  ----------------------------------- -----------------------------------
  Target users                        Both professional and
                                      non-professional accommodation
                                      contexts

  Regulatory field selection          Use the applicable requirements as
                                      the reference; exact fields remain
                                      subject to pre-production
                                      validation

  Guest access                        Random invitation token; one valid
                                      submission; expires seven days
                                      after creation

  Guest data entry                    Typed fields only; no
                                      identity-document photographs

  Export                              PDF

  Delivery to owner                   User manually shares the PDF using
                                      the device's native share sheet

  Email integration                   Not implemented

  WhatsApp integration                Not implemented; sharing is
                                      delegated to compatible apps on the
                                      device

  Personal stay data                  Stored locally on the main
                                      traveler's device

  Server persistence                  Minimum operational metadata plus
                                      temporary personal data required to
                                      receive and transfer submissions

  Server-side deletion                Delete temporary personal data
                                      after the mobile app confirms
                                      successful receipt and local
                                      storage

  Abandoned submissions               Seven-day expiry plus periodic
                                      safety cleanup

  Local data protection               Encrypted storage; encryption key
                                      protected by the mobile operating
                                      system

  Development database                PostgreSQL installed locally; no
                                      Docker

  Backend-first rule                  Implement and test backend CRUD
                                      before starting frontend
                                      development

  Authority integration               None

  Legal review                        Detailed review deferred until
                                      before production deployment
  -----------------------------------------------------------------------

## 4. Technical baseline

Use the following baseline unless a compatibility issue requires a
documented change:

-   Java 21.
-   A stable Spring Boot release compatible with Java 21, selected
    through Spring Initializr when the project is created.
-   Maven Wrapper.
-   Spring Web.
-   Spring Data JPA.
-   Jakarta Bean Validation.
-   PostgreSQL for local development.
-   Flyway database migrations.
-   Spring Security for authentication and authorization.
-   JUnit 5 and Spring Boot testing support; Mockito where appropriate.
-   Bruno or Postman for manual API checks.
-   No Docker.

Use the Spring Initializr project generator as the starting point.
Commit the Maven Wrapper and document the exact Java and Spring Boot
versions in the repository.

## 5. Initial domain model

The exact model may evolve as the API is implemented. Keep personal data
separate from operational metadata.

### User

Represents the main traveler who owns stays.

Possible fields:

-   `id`
-   `email` or username
-   password hash or external authentication identifier
-   timestamps needed for account operations

Never store plaintext passwords.

### Stay

Represents the main traveler's stay record. The server should keep only
the operational metadata necessary to manage invitations and transfers.
Personal details associated with the stay should be kept on the mobile
device wherever feasible.

Possible server-side fields:

-   `id`
-   `ownerUserId`
-   operational status
-   creation timestamp
-   invitation/transfer coordination metadata

Accommodation and transaction fields needed for the PDF should be held
locally on the mobile device unless a specific server-side field is
demonstrably required for the invitation workflow.

### Invitation

Represents one guest invitation.

Possible fields:

-   `id`
-   `stayId`
-   token hash, not the raw token where feasible
-   creation timestamp
-   expiry timestamp
-   status (`PENDING`, `SUBMITTED`, `EXPIRED`, `REVOKED`)
-   submission timestamp

A token must be cryptographically random, unguessable, scoped to one
stay, and valid for only one successful submission.

### Traveler submission

Represents a guest's temporary personal-data submission.

Possible fields:

-   internal identifier
-   invitation identifier
-   validated traveler fields
-   submission timestamp
-   transfer/acknowledgement status
-   deletion timestamp or audit metadata that does not retain personal
    data

Do not retain identity values in logs, audit messages, metrics,
exception text, or operational metadata.

### Transfer acknowledgement

Represents the mobile app's confirmation that a submission has been
received and saved locally.

Possible fields:

-   transfer identifier
-   submission identifier or opaque reference
-   acknowledgement timestamp
-   idempotency key or acknowledgement status

Keep this record free of unnecessary personal data. After successful
acknowledgement, remove the temporary personal-data payload from the
server.

## 6. Data lifecycle and security

### 6.1 Submission flow

1.  The main traveler creates a stay in the mobile app.
2.  The app requests an invitation from the backend.
3.  The backend returns a unique guest URL containing a random token.
4.  The main traveler shares the link outside the app.
5.  The guest opens the web form and enters the requested data.
6.  The backend validates the token, validates the fields, and accepts
    one valid submission.
7.  The mobile app requests pending submissions for the relevant stay
    using an authenticated and authorized API.
8.  The mobile app validates and stores the received data in encrypted
    local storage.
9.  Only after local storage succeeds, the app sends an acknowledgement
    to the backend.
10. The backend deletes the personal-data payload for the acknowledged
    submission.
11. The mobile app can generate the PDF from its local data and share it
    through the operating system's native share sheet.

### 6.2 Failure handling

-   If the mobile app cannot receive or store a submission, it must not
    acknowledge it.
-   If acknowledgement fails after local storage succeeds, retries must
    be safe and idempotent.
-   A repeated acknowledgement must not create an error that prevents
    recovery.
-   A token must not permit multiple successful submissions.
-   Token consumption and submission creation must be handled atomically
    to prevent concurrent duplicate submissions.
-   If a guest submits successfully but the mobile app never retrieves
    the submission, a cleanup job must remove the abandoned personal
    data after the configured seven-day expiry.
-   Avoid deleting an unacknowledged submission before the main traveler
    has had the configured opportunity to retrieve it.

### 6.3 Expiration and cleanup

-   Invitation expiry is seven days from creation.
-   Expired invitations cannot accept submissions.
-   Run a scheduled cleanup process for expired invitations and
    abandoned submissions.
-   Remove temporary personal data and unnecessary invitation metadata
    when no longer needed.
-   Define retention and backup behavior before production. Do not claim
    that deletion is immediate from every backup or infrastructure layer
    unless that has been verified.

### 6.4 Security requirements

-   Use Spring Security for authentication and authorization.
-   Every stay operation must verify ownership by the authenticated
    user.
-   Use HTTPS in deployed environments.
-   Store password hashes only if local password authentication is
    implemented.
-   Store invitation tokens as hashes where feasible; never log raw
    tokens.
-   Apply server-side validation to every request.
-   Use DTOs rather than exposing persistence entities directly.
-   Avoid logging personal data, document numbers, tokens, form
    payloads, or PDF contents.
-   Keep secrets out of source control.
-   Use encrypted local mobile storage and operating-system secure key
    storage.
-   Use least privilege for database credentials.
-   Protect against brute-force token guessing with sufficient token
    entropy and reasonable rate limits.
-   Return generic error responses that do not disclose another user's
    records or reveal sensitive token details.
-   Define production encryption, backup, monitoring, and
    incident-response policies before deployment.

### 6.5 Important architectural limitation

In the initial architecture, the backend temporarily receives traveler
data and may process it in plaintext. Database encryption alone does not
prevent the server application from accessing the data. If the product
requirement changes to ensure that the server can never read guest
identity data, end-to-end encryption must be designed before
implementing the submission API.

## 7. REST API design principles

Exact routes may be refined during implementation. Keep the API
resource-oriented and versioned, for example under `/api/v1`.

Likely resources and operations:

-   Authentication: register/login or equivalent supported
    authentication flow.
-   Stays: create, list, retrieve, update, and delete the authenticated
    user's own stays.
-   Invitations: create, list/status, revoke, and inspect invitation
    status.
-   Guest submission: validate an invitation and submit one form.
-   Pending submissions: retrieve unacknowledged submissions for an
    authorized stay.
-   Acknowledgements: confirm successful local receipt and storage.
-   Health endpoint: a minimal operational health check without exposing
    sensitive configuration.

Do not implement PDF generation in the backend by default; the PDF
should be generated by the mobile app from its local copy unless a later
technical decision justifies another approach.

## 8. Development phases and acceptance criteria

### Phase 1 --- Repository and project setup

Tasks:

-   Generate the Spring Boot project with Spring Initializr.
-   Configure Java 21 and Maven Wrapper.
-   Configure PostgreSQL local connection.
-   Add Flyway and an initial migration.
-   Add environment-specific configuration and keep secrets out of Git.
-   Create a README with setup and run instructions.

Acceptance criteria:

-   The app builds using the Maven Wrapper.
-   The application starts locally and connects to PostgreSQL.
-   Database migrations run predictably.
-   No Docker is required.

### Phase 2 --- CRUD implementation and tests

Tasks:

-   Implement the first domain entities and migrations.
-   Add repositories, services, DTOs, validation, controllers, and error
    handling.
-   Implement CRUD for the agreed initial resources.
-   Add unit and integration tests.
-   Document requests and expected responses.

Acceptance criteria:

-   CRUD operations work against the local database.
-   Invalid input produces consistent validation errors.
-   Tests cover normal cases and relevant failures.
-   Controllers do not expose JPA entities directly.
-   Ownership and authorization checks are in place before personal
    records are exposed.

### Phase 3 --- Authentication and authorization

Tasks:

-   Implement the chosen authentication flow.
-   Enforce ownership checks on every stay and invitation operation.
-   Add tests for unauthorized and cross-user access.

Acceptance criteria:

-   An authenticated user cannot access another user's stay or
    submissions.
-   Secrets and credentials are not committed.
-   Authentication failures do not expose sensitive details.

### Phase 4 --- Invitations and guest submission

Tasks:

-   Generate random invitation tokens.
-   Implement the seven-day expiry rule.
-   Implement single-use submissions and atomic token consumption.
-   Validate guest input.
-   Add cleanup for expired invitations and abandoned submissions.

Acceptance criteria:

-   Invalid, expired, revoked, and already-used tokens are rejected.
-   Concurrent requests cannot create two successful submissions for one
    invitation.
-   Submitted data is not written to logs.
-   Cleanup is tested.

### Phase 5 --- Transfer, acknowledgement, and deletion

Tasks:

-   Add an authorized endpoint for retrieving pending submissions.
-   Define the acknowledgement contract.
-   Make retrieval and acknowledgement retries safe.
-   Delete the server's personal-data payload only after successful
    acknowledgement.
-   Test failure and retry scenarios.

Acceptance criteria:

-   Failed local storage never leads to acknowledgement.
-   Successful acknowledgement triggers server-side personal-data
    deletion.
-   Duplicate acknowledgements are handled safely.
-   Abandoned payloads are removed by cleanup.

### Phase 6 --- Guest web form and mobile app

Start this phase only after the backend CRUD and the relevant API tests
are implemented and passing.

Tasks:

-   Build the guest web form.
-   Build the mobile app using Angular with Ionic and Capacitor, unless
    a later decision changes the frontend stack.
-   Add encrypted local storage and OS-protected key storage.
-   Implement invitation management and pending-submission retrieval.
-   Implement acknowledgement only after local persistence succeeds.

Acceptance criteria:

-   Guests can submit using a browser without an account.
-   The main traveler can receive and store responses on the device.
-   Personal stay data is not permanently stored in the backend.
-   Local data is encrypted at rest.

### Phase 7 --- PDF export and sharing

Tasks:

-   Define the PDF layout.
-   Generate the PDF locally from the stay and traveler records.
-   Use the native mobile share sheet.
-   Clearly identify missing or incomplete information.

Acceptance criteria:

-   The PDF is legible and includes the locally available information
    for the stay.
-   The user can share the file with compatible apps.
-   No email or WhatsApp API integration exists.
-   The UI does not imply that the PDF has been submitted to an
    authority.

### Phase 8 --- Pre-production review

Tasks:

-   Review the exact regulatory fields for professional and
    non-professional accommodation contexts.
-   Verify applicable signature, document, retention, privacy, and
    security requirements.
-   Confirm whether the PDF is merely a handover document or must meet
    additional formal requirements.
-   Review backups, deletion, encryption, access control, deployment
    configuration, and monitoring.
-   Update this plan and the implementation where required.

Acceptance criteria:

-   The legal and security review is documented before production.
-   The final field list and PDF claims have been validated.
-   Data lifecycle and retention behavior are documented.
-   No direct authority integration has been added without an explicit
    scope change.

## 9. Testing strategy

At minimum, add tests for:

-   CRUD success and validation failures.
-   Missing or invalid authentication.
-   Cross-user access attempts.
-   Invitation token generation and expiry.
-   Single-use token behavior.
-   Concurrent submissions against the same token.
-   Pending submission retrieval.
-   Acknowledgement success, failure, and retries.
-   Personal-data deletion after acknowledgement.
-   Cleanup of expired or abandoned submissions.
-   Logging and error responses that must not expose personal data.

Use automated tests for repeatable behavior and Bruno or Postman for
manual API exploration. Do not rely solely on manual testing.

## 10. Out of scope for the initial release

-   Direct integration with Police systems or SES.HOSPEDAJES.
-   Automatic submission to any authority.
-   Email delivery from the backend.
-   WhatsApp API integration.
-   Document-image upload or OCR.
-   Server-side permanent storage of personal stay records.
-   Cloud synchronization of the main traveler's personal stay data.
-   Production deployment before the pre-production legal and security
    review.

## 11. Open items to resolve during implementation

These items do not prevent creating the project skeleton, but must be
resolved before the affected feature is considered complete:

1.  Final regulatory field list for both professional and
    non-professional use cases.
2.  Whether the PDF is a convenience handover document or must meet
    additional formal/signature requirements.
3.  Authentication details for the main traveler.
4.  Exact operational stay metadata required on the server.
5.  The duration of local mobile retention and the local deletion user
    experience.
6.  Backup retention and deletion guarantees for the production
    environment.
7.  Whether end-to-end encryption is required; the initial design
    permits the backend to process temporary submissions.
8.  Final production hosting, secret management, monitoring, and
    deployment configuration.

## 12. Delivery principle

Build incrementally. Finish and test each backend phase before moving
on. Do not generate the entire application in one step, do not start
frontend work before the backend CRUD is tested, and do not add
out-of-scope integrations without an explicit decision.
