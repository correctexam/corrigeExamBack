-- T25e6b — version of each copy of the scan of an exam (ADR 0005), for the statistics per version.
-- Target: production MySQL / MariaDB (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261120090000_added_exam_copy_version.xml
-- Apply BEFORE deploying the back-end. New table only: no lock on existing data.

CREATE TABLE exam_copy_version (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  first_page INT NOT NULL,
  label VARCHAR(2) NOT NULL,
  edition_id BIGINT NULL,
  copy_number INT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ux_exam_copy_version_page UNIQUE (exam_id, first_page),
  CONSTRAINT fk_exam_copy_version_exam_id FOREIGN KEY (exam_id) REFERENCES exam (id)
);

-- Liquibase: if the table is created by hand, the change set 20261120090000-1 is marked as run
-- (precondition tableExists) at the next migration.
