-- T19a — Keep the manual corrections of a QCM when the autocorrection is run again
-- Target: production MySQL 8.0.12+ / MariaDB 10.3+ (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261113120000_added_student_response_manually_graded.xml
-- (it is skipped, and marked as run, when the column already exists).
--
-- Apply BEFORE deploying the back-end that maps StudentResponse.manuallyGraded: Hibernate would
-- otherwise fail on every query on student_response (unknown column manually_graded).
--
-- Existing responses get 0 (not corrected by hand): the corrections made before this version
-- cannot be told apart from the detected ones.
--
-- ALGORITHM=INSTANT only changes the table metadata (no copy of the table). If the server refuses
-- it (MySQL < 8.0.12, MariaDB < 10.3), the fallback rebuilds the table: run it during a maintenance window.

ALTER TABLE student_response ADD COLUMN manually_graded BIT(1) NOT NULL DEFAULT 0, ALGORITHM=INSTANT;

-- Check
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student_response' AND COLUMN_NAME = 'manually_graded';

-- Rollback (after redeploying a back-end without StudentResponse.manuallyGraded):
-- ALTER TABLE student_response DROP COLUMN manually_graded;
