-- T25c — Typst subject of an exam (ADR 0004): metadata, edition lock and list of files.
-- Target: production MySQL / MariaDB (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261117090000_added_exam_subject.xml
-- Apply BEFORE deploying the back-end. New tables only: no lock on existing data.
-- The contents of the files are in the object storage under subject/{examId}/files/.

CREATE TABLE exam_subject (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  main_file VARCHAR(200) NOT NULL DEFAULT 'main.typ',
  revision INT NOT NULL DEFAULT 0,
  updated_at DATETIME(6) NOT NULL,
  updated_by VARCHAR(50) NULL,
  locked_by VARCHAR(50) NULL,
  lock_expires_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  CONSTRAINT ux_exam_subject_exam UNIQUE (exam_id),
  CONSTRAINT fk_exam_subject_exam_id FOREIGN KEY (exam_id) REFERENCES exam (id)
);

CREATE TABLE exam_subject_file (
  id BIGINT NOT NULL AUTO_INCREMENT,
  subject_id BIGINT NOT NULL,
  path VARCHAR(200) NOT NULL,
  size BIGINT NOT NULL,
  sha256 VARCHAR(64) NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ux_exam_subject_file_path UNIQUE (subject_id, path),
  CONSTRAINT fk_exam_subject_file_subject_id FOREIGN KEY (subject_id) REFERENCES exam_subject (id)
);

-- Liquibase: if the tables are created by hand, the change sets 20261117090000-1 and -2 are marked as run
-- (precondition tableExists) at the next migration.
