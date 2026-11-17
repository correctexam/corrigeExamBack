package fr.istic.service.subject;

import java.time.Duration;
import java.time.Instant;

/**
 * Edition lock of a subject (T25c, ADR 0004): one teacher writes at a time, the others read. Pure decision logic.
 *
 * <p>A lock expires {@code ttl} after its last renewal (the editor renews it while the teacher types). An expired
 * lock is free. Another teacher of the exam can take the lock over explicitly ({@code force}), after a warning in
 * the editor: teachers of a course trust each other, the lock only prevents silent overwrites.</p>
 */
public final class SubjectLocks {

    public enum Outcome {
        /** The caller holds the lock (new, renewed, taken from an expired or forced lock). */
        ACQUIRED,
        /** Another teacher holds a valid lock and the caller did not force. */
        HELD_BY_OTHER
    }

    /** A lock: holder and expiry; both null when free. */
    public record Lock(String holder, Instant expiresAt) {

        public static final Lock FREE = new Lock(null, null);

        public boolean isHeldAt(Instant now) {
            return holder != null && expiresAt != null && expiresAt.isAfter(now);
        }

        public boolean isHeldBy(String login, Instant now) {
            return isHeldAt(now) && holder.equals(login);
        }
    }

    public record Decision(Outcome outcome, Lock lock) {
    }

    private SubjectLocks() {
    }

    /** Lock requested by {@code login}: the resulting lock, unchanged when refused. */
    public static Decision acquire(Lock current, String login, boolean force, Instant now, Duration ttl) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login");
        }
        Lock c = current == null ? Lock.FREE : current;
        if (c.isHeldAt(now) && !c.holder().equals(login) && !force) {
            return new Decision(Outcome.HELD_BY_OTHER, c);
        }
        return new Decision(Outcome.ACQUIRED, new Lock(login, now.plus(ttl)));
    }

    /** Lock after a release by {@code login}: free if the caller held it, unchanged otherwise. */
    public static Lock release(Lock current, String login, Instant now) {
        Lock c = current == null ? Lock.FREE : current;
        return c.holder() != null && c.holder().equals(login) ? Lock.FREE : c;
    }

    /** A write needs the lock held by the writer and not expired. */
    public static boolean canWrite(Lock current, String login, Instant now) {
        return current != null && current.isHeldBy(login, now);
    }
}
