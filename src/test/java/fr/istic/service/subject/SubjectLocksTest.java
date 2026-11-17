package fr.istic.service.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import fr.istic.service.subject.SubjectLocks.Lock;
import fr.istic.service.subject.SubjectLocks.Outcome;

/** Unit tests of {@link SubjectLocks}: single-author edition lock of a subject (T25c). */
class SubjectLocksTest {

    private static final Instant NOW = Instant.parse("2026-11-17T10:00:00Z");
    private static final Duration TTL = Duration.ofMinutes(15);

    @Test
    void freeLockIsAcquired() {
        SubjectLocks.Decision d = SubjectLocks.acquire(Lock.FREE, "alice", false, NOW, TTL);
        assertEquals(Outcome.ACQUIRED, d.outcome());
        assertEquals(new Lock("alice", NOW.plus(TTL)), d.lock());
        assertTrue(SubjectLocks.canWrite(d.lock(), "alice", NOW));
        assertFalse(SubjectLocks.canWrite(d.lock(), "bob", NOW));
    }

    @Test
    void holderRenewsItsLock() {
        Lock held = new Lock("alice", NOW.plusSeconds(10));
        SubjectLocks.Decision d = SubjectLocks.acquire(held, "alice", false, NOW, TTL);
        assertEquals(Outcome.ACQUIRED, d.outcome());
        assertEquals(NOW.plus(TTL), d.lock().expiresAt());
    }

    @Test
    void validLockOfAnotherTeacherIsRefusedUnlessForced() {
        Lock held = new Lock("alice", NOW.plusSeconds(60));
        SubjectLocks.Decision refused = SubjectLocks.acquire(held, "bob", false, NOW, TTL);
        assertEquals(Outcome.HELD_BY_OTHER, refused.outcome());
        assertEquals(held, refused.lock());

        SubjectLocks.Decision forced = SubjectLocks.acquire(held, "bob", true, NOW, TTL);
        assertEquals(Outcome.ACQUIRED, forced.outcome());
        assertEquals("bob", forced.lock().holder());
        assertFalse(SubjectLocks.canWrite(forced.lock(), "alice", NOW));
    }

    @Test
    void expiredLockIsFree() {
        Lock expired = new Lock("alice", NOW);
        assertFalse(expired.isHeldAt(NOW));
        assertFalse(SubjectLocks.canWrite(expired, "alice", NOW));
        assertEquals(Outcome.ACQUIRED, SubjectLocks.acquire(expired, "bob", false, NOW, TTL).outcome());
    }

    @Test
    void onlyTheHolderReleases() {
        Lock held = new Lock("alice", NOW.plus(TTL));
        assertEquals(held, SubjectLocks.release(held, "bob", NOW));
        assertEquals(Lock.FREE, SubjectLocks.release(held, "alice", NOW));
        assertEquals(Lock.FREE, SubjectLocks.release(null, "alice", NOW));
    }

    @Test
    void blankLoginIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> SubjectLocks.acquire(Lock.FREE, " ", false, NOW, TTL));
        assertFalse(SubjectLocks.canWrite(null, "alice", NOW));
    }
}
