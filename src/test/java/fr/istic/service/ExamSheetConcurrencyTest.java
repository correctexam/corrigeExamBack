package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.istic.domain.ExamSheet;
import fr.istic.domain.Scan;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Sheets are created on demand by GET requests that several graders send at the same time: concurrent calls must
 * create one sheet per page range, never two.
 */
@QuarkusTest
class ExamSheetConcurrencyTest {

    private static final int CALLERS = 8;

    @Inject
    ExamSheetService examSheetService;

    private long scanId;
    private ExecutorService pool;

    @BeforeEach
    void fixture() {
        scanId = QuarkusTransaction.requiringNew().call(() -> {
            Scan scan = new Scan();
            scan.name = "scan " + UUID.randomUUID();
            scan.persist();
            return scan.id;
        });
        pool = Executors.newFixedThreadPool(CALLERS);
    }

    @AfterEach
    void stop() {
        pool.shutdownNow();
    }

    private void runTogether(Callable<?> call) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < CALLERS; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return call.call();
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get(60, TimeUnit.SECONDS);
        }
    }

    private long sheets(int pagemin, int pagemax) {
        return QuarkusTransaction.requiringNew().call(() ->
            ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId, pagemin, pagemax).count()
        );
    }

    @Test
    void concurrentLookupsOfOneSheetCreateItOnce() throws Exception {
        runTogether(() -> examSheetService.findOrCreateExamSheetByPageMinAndPageMax(Page.ofSize(20), scanId, 2, 3));

        assertEquals(1, sheets(2, 3));
    }

    @Test
    void concurrentLookupsOfAllSheetsCreateEachOnce() throws Exception {
        // 3 sheets of 2 pages
        runTogether(() -> examSheetService.findOrCreateExamSheetByName(Page.ofSize(20), scanId, 2, 6));

        assertEquals(1, sheets(0, 1));
        assertEquals(1, sheets(2, 3));
        assertEquals(1, sheets(4, 5));
    }
}
