-- T25e — versions of an exam written in Typst (ADR 0005): plan and layout of each version (JSON), PDF in the object
-- storage (version/{examId}/{index}.pdf); number of versions of a print run.
-- Target: production MySQL / MariaDB (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261119090000_added_exam_version.xml
-- Apply BEFORE deploying the back-end. New table and a column with a default value on exam_edition (small table).

CREATE TABLE exam_version (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  idx INT NOT NULL,
  label VARCHAR(2) NOT NULL,
  seed BIGINT NOT NULL,
  plan LONGTEXT NOT NULL,
  layout LONGTEXT NOT NULL,
  pages INT NOT NULL,
  pdf_size BIGINT NULL,
  pdf_sha256 VARCHAR(64) NULL,
  created_at DATETIME(6) NOT NULL,
  created_by VARCHAR(50) NULL,
  PRIMARY KEY (id),
  CONSTRAINT ux_exam_version_exam_idx UNIQUE (exam_id, idx),
  CONSTRAINT fk_exam_version_exam_id FOREIGN KEY (exam_id) REFERENCES exam (id)
);

ALTER TABLE exam_edition ADD COLUMN versions INT NOT NULL DEFAULT 1;

-- Liquibase: when applied by hand, the change sets 20261119090000-1 and -2 are marked as run
-- (preconditions tableExists / columnExists) at the next migration.
