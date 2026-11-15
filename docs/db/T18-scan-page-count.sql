-- T18 — Number of pages of a scan, cached in the database
-- Target: production MySQL 8.0.12+ / MariaDB 10.3+ (quarkus.liquibase.migrate-at-start=false in %prod).
-- Liquibase equivalent: config/liquibase/changelog/20261113000000_added_scan_page_count.xml
-- (it is skipped, and marked as run, when the column already exists).
--
-- Apply BEFORE deploying the back-end that maps Scan.pageCount: Hibernate would otherwise fail
-- on every query on scan (unknown column page_count).
--
-- The column is nullable and has no default: existing scans keep NULL and get their count on the
-- first call to GET /api/scans/{id}/pagecount. No backfill is needed.
--
-- ALGORITHM=INSTANT only changes the table metadata: no copy of the table, whose LONGBLOB column
-- `content` can be large. If the server refuses it (MySQL < 8.0.12, MariaDB < 10.3), do NOT drop
-- the clause blindly: the fallback rebuilds the whole table; run it during a maintenance window.

ALTER TABLE scan ADD COLUMN page_count INT NULL, ALGORITHM=INSTANT;

-- Check
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'scan' AND COLUMN_NAME = 'page_count';

-- Rollback (after redeploying a back-end without Scan.pageCount):
-- ALTER TABLE scan DROP COLUMN page_count;
