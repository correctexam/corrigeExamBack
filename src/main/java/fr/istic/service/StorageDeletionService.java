package fr.istic.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the relational database and the object storage consistent when entities
 * referencing stored files are deleted.
 *
 * <p>Objects are removed from the storage only <b>after the surrounding transaction has
 * committed</b>: if the database deletion is rolled back, the files are kept. A deletion
 * failure after commit cannot be rolled back anymore; it is logged at ERROR level with
 * the object key (orphan object) so that it can be cleaned up later.</p>
 *
 * <p>When no transaction is active, objects are deleted immediately.</p>
 */
@ApplicationScoped
public class StorageDeletionService {

    // package-private and not final so that unit tests can capture the orphan reports
    Logger log = LoggerFactory.getLogger(StorageDeletionService.class);

    @Inject
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Inject
    FichierS3Service fichierS3Service;

    /**
     * Schedules the deletion of the given objects once the current transaction commits.
     * Nothing is deleted if the transaction rolls back.
     *
     * @param objectKeys keys of the objects to delete (null or empty keys are ignored)
     */
    public void deleteAfterCommit(Collection<String> objectKeys) {
        final List<String> keys = sanitize(objectKeys);
        if (keys.isEmpty()) {
            return;
        }
        if (!isTransactionActive()) {
            log.debug("No active transaction, deleting {} storage object(s) immediately", keys.size());
            deleteNow(keys);
            return;
        }
        transactionSynchronizationRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // nothing to do before the commit
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    deleteNow(keys);
                } else {
                    log.info("Transaction not committed (status {}), keeping {} storage object(s): {}", status,
                            keys.size(), keys);
                }
            }
        });
    }

    /**
     * Deletes the given objects right away, on a best-effort basis: a failure on one object
     * does not prevent the deletion of the others and is never propagated.
     *
     * @return the keys that could not be deleted (orphan objects)
     */
    public List<String> deleteNow(Collection<String> objectKeys) {
        final List<String> failures = new ArrayList<>();
        for (String key : sanitize(objectKeys)) {
            try {
                fichierS3Service.deleteObject(key);
                log.debug("Storage object deleted: {}", key);
            } catch (Exception e) {
                failures.add(key);
                log.error("Could not delete storage object {}: it is now orphaned and must be cleaned up", key, e);
            }
        }
        return failures;
    }

    private boolean isTransactionActive() {
        try {
            return transactionSynchronizationRegistry.getTransactionStatus() == Status.STATUS_ACTIVE;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private static List<String> sanitize(Collection<String> objectKeys) {
        final List<String> keys = new ArrayList<>();
        if (objectKeys == null) {
            return keys;
        }
        for (String key : objectKeys) {
            if (key != null && !key.isBlank() && !keys.contains(key)) {
                keys.add(key);
            }
        }
        return keys;
    }
}
