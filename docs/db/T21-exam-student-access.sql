-- T21 — Student portal (ADR 0003): what the students of an exam can see.
-- Target: production MySQL 8.0.12+ / MariaDB 10.3+ (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261113180000_added_exam_student_access.xml
--
-- Apply BEFORE deploying the back-end that maps Exam.studentAccess (unknown column otherwise).
-- Existing exams become GRADED (their results may already have been sent by e-mail);
-- the default then becomes SHEET for the exams created afterwards.

ALTER TABLE exam ADD COLUMN student_access VARCHAR(16) NOT NULL DEFAULT 'GRADED', ALGORITHM=INSTANT;
ALTER TABLE exam ALTER COLUMN student_access SET DEFAULT 'SHEET';

-- Check: every existing exam is GRADED
SELECT student_access, COUNT(*) FROM exam GROUP BY student_access;

-- Rollback (after redeploying a back-end without Exam.studentAccess):
-- ALTER TABLE exam DROP COLUMN student_access;
