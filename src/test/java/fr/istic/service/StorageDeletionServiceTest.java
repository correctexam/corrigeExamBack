package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link StorageDeletionService}: objects must be removed from the storage
 * only after a successful commit, and a failing deletion must never break the others.
 */
class StorageDeletionServiceTest {

    private RecordingStorage storage;
    private FakeRegistry registry;
    private StorageDeletionService service;

    @BeforeEach
    void setUp() {
        storage = new RecordingStorage();
        registry = new FakeRegistry();
        service = new StorageDeletionService();
        service.fichierS3Service = storage;
        service.transactionSynchronizationRegistry = registry;
    }

    @Test
    void deletesOnlyAfterCommit() {
        registry.status = Status.STATUS_ACTIVE;

        service.deleteAfterCommit(List.of("scan/1.pdf", "template/2.pdf"));

        assertTrue(storage.deleted.isEmpty(), "nothing must be deleted before the commit");
        assertNotNull(registry.synchronization);

        registry.synchronization.afterCompletion(Status.STATUS_COMMITTED);

        assertEquals(List.of("scan/1.pdf", "template/2.pdf"), storage.deleted);
    }

    @Test
    void keepsObjectsWhenTransactionRollsBack() {
        registry.status = Status.STATUS_ACTIVE;

        service.deleteAfterCommit(List.of("scan/1.pdf"));
        registry.synchronization.afterCompletion(Status.STATUS_ROLLEDBACK);

        assertTrue(storage.deleted.isEmpty());
    }

    @Test
    void deletesImmediatelyWithoutTransaction() {
        registry.status = Status.STATUS_NO_TRANSACTION;

        service.deleteAfterCommit(List.of("cache/3.sqlite3"));

        assertNull(registry.synchronization);
        assertEquals(List.of("cache/3.sqlite3"), storage.deleted);
    }

    @Test
    void ignoresEmptyNullAndDuplicateKeys() {
        registry.status = Status.STATUS_ACTIVE;

        service.deleteAfterCommit(List.of());
        assertNull(registry.synchronization, "no synchronization for an empty list");

        service.deleteAfterCommit(Arrays.asList("scan/1.pdf", null, " ", "scan/1.pdf"));
        registry.synchronization.afterCompletion(Status.STATUS_COMMITTED);

        assertEquals(List.of("scan/1.pdf"), storage.deleted);
    }

    @Test
    void failureOnOneObjectDoesNotStopTheOthers() {
        storage.failingKeys = Set.of("template/2.pdf");

        List<String> failures = service.deleteNow(List.of("scan/1.pdf", "template/2.pdf", "cache/2.sqlite3"));

        assertEquals(List.of("template/2.pdf"), failures);
        assertEquals(List.of("scan/1.pdf", "cache/2.sqlite3"), storage.deleted);
    }

    @Test
    void failureAfterCommitIsNotPropagated() {
        registry.status = Status.STATUS_ACTIVE;
        storage.failingKeys = Set.of("scan/1.pdf");

        service.deleteAfterCommit(List.of("scan/1.pdf"));
        // must not throw: the database transaction is already committed
        registry.synchronization.afterCompletion(Status.STATUS_COMMITTED);

        assertTrue(storage.deleted.isEmpty());
    }

    /** Storage fake recording the deleted keys. */
    static class RecordingStorage extends FichierS3Service {
        final List<String> deleted = new ArrayList<>();
        Set<String> failingKeys = Set.of();

        @Override
        public void deleteObject(String name) {
            if (failingKeys.contains(name)) {
                throw new StorageException(name, "simulated failure", null);
            }
            deleted.add(name);
        }
    }

    /** Minimal transaction registry capturing the registered synchronization. */
    static class FakeRegistry implements TransactionSynchronizationRegistry {
        int status = Status.STATUS_NO_TRANSACTION;
        Synchronization synchronization;
        private final Map<Object, Object> resources = new HashMap<>();

        @Override
        public Object getTransactionKey() {
            return status == Status.STATUS_NO_TRANSACTION ? null : this;
        }

        @Override
        public void putResource(Object key, Object value) {
            resources.put(key, value);
        }

        @Override
        public Object getResource(Object key) {
            return resources.get(key);
        }

        @Override
        public void registerInterposedSynchronization(Synchronization sync) {
            this.synchronization = sync;
        }

        @Override
        public int getTransactionStatus() {
            return status;
        }

        @Override
        public void setRollbackOnly() {
            status = Status.STATUS_MARKED_ROLLBACK;
        }

        @Override
        public boolean getRollbackOnly() {
            return status == Status.STATUS_MARKED_ROLLBACK;
        }
    }
}
