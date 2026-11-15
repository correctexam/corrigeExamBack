-- T21 — Student portal (ADR 0003): links sent to the students and link requests.
-- Target: production MySQL / MariaDB (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261114090000_added_student_portal.xml
-- Apply BEFORE deploying the back-end of the student portal. New tables only: no lock on existing data.
-- Rows are purged by the application after one day.

CREATE TABLE student_access_token (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(254) NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  used_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  CONSTRAINT ux_student_access_token_hash UNIQUE (token_hash),
  INDEX idx_student_access_token_expires (expires_at)
);

CREATE TABLE student_portal_request (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email_hash VARCHAR(64) NOT NULL,
  ip_hash VARCHAR(64) NOT NULL,
  challenge_hash VARCHAR(64) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ux_student_portal_request_challenge UNIQUE (challenge_hash),
  INDEX idx_student_portal_request_email (email_hash, created_at),
  INDEX idx_student_portal_request_ip (ip_hash, created_at)
);

-- Rollback:
-- DROP TABLE student_portal_request;
-- DROP TABLE student_access_token;
