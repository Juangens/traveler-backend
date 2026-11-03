-- V1__initial_schema.sql
-- Initial database schema for traveler-registration application
-- This creates the baseline schema structure
-- Compatible with H2 and PostgreSQL

-- Create users table for main travelers
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Create stays table (operational metadata only, no personal data)
CREATE TABLE stays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stays_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_stays_owner ON stays(owner_user_id);
CREATE INDEX idx_stays_status ON stays(status);

-- Create invitations table
CREATE TABLE invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stay_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    revoked_at TIMESTAMP,
    CONSTRAINT fk_invitations_stay FOREIGN KEY (stay_id) REFERENCES stays(id) ON DELETE CASCADE
);

CREATE INDEX idx_invitations_stay ON invitations(stay_id);
CREATE INDEX idx_invitations_token_hash ON invitations(token_hash);
CREATE INDEX idx_invitations_status ON invitations(status);
CREATE INDEX idx_invitations_expires_at ON invitations(expires_at);

-- Create traveler_submissions table (temporary personal data storage)
-- IMPORTANT: Personal data here must be deleted after acknowledgement
CREATE TABLE traveler_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invitation_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_submissions_invitation FOREIGN KEY (invitation_id) REFERENCES invitations(id) ON DELETE CASCADE
);

CREATE INDEX idx_submissions_invitation ON traveler_submissions(invitation_id);
CREATE INDEX idx_submissions_acknowledged ON traveler_submissions(acknowledged_at);
CREATE INDEX idx_submissions_deleted ON traveler_submissions(deleted_at);

-- Create acknowledgements table (tracks when mobile app confirms receipt)
CREATE TABLE acknowledgements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    acknowledged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acknowledgements_submission FOREIGN KEY (submission_id) REFERENCES traveler_submissions(id) ON DELETE CASCADE
);

CREATE INDEX idx_acknowledgements_submission ON acknowledgements(submission_id);
CREATE INDEX idx_acknowledgements_idempotency ON acknowledgements(idempotency_key);
