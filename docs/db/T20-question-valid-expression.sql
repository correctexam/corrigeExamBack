-- T20 — Grading DSL for QCM questions (ADR 0002): the valid expression may exceed 255 characters.
-- Target: production MySQL / MariaDB (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261113150000_widen_question_valid_expression.xml
--
-- Apply before using DSL longer than 255 characters (the new back-end accepts up to 1024 and would
-- otherwise fail on save). Widening a VARCHAR keeps the data. The question table is small: the
-- rebuild MySQL may do here is quick.

ALTER TABLE question MODIFY valid_expression VARCHAR(1024) NULL;

-- Check
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'question' AND COLUMN_NAME = 'valid_expression';

-- Rollback (only if no expression is longer than 255 characters):
-- ALTER TABLE question MODIFY valid_expression VARCHAR(255) NULL;
