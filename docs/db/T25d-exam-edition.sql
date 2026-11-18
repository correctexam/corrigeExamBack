-- T25d — print runs of an exam written in Typst (ADR 0004): page codes CE1|edition|copy|page.
-- Target: production MySQL / MariaDB (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261118090000_added_exam_edition.xml
-- Apply BEFORE deploying the back-end. New table only: no lock on existing data.

CREATE TABLE exam_edition (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  copies INT NOT NULL,
  pages_per_copy INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  created_by VARCHAR(50) NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_exam_edition_exam_id FOREIGN KEY (exam_id) REFERENCES exam (id)
);

-- Liquibase: if the table is created by hand, the change set 20261118090000-1 is marked as run
-- (precondition tableExists) at the next migration.
