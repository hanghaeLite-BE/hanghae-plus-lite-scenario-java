package io.hhplus.tdd.common;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {

    private static class LockRef {
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger ref = new AtomicInteger(0);
    }

    private final ConcurrentHashMap<Long, LockRef> lockRefs = new ConcurrentHashMap<>();

    public ReentrantLock acquire(Long userId) {
        LockRef lockRef = lockRefs.compute(userId, (k, v) -> {
            if (v == null) {
                v = new LockRef();
            }
            v.ref.incrementAndGet();
            return v;
        });

        return lockRef.lock;
    }

    public void release(Long userId) {
        lockRefs.compute(userId, (k, v) -> {
            boolean isAlreadyRemoved = v == null;
            if (isAlreadyRemoved) {
                return null;
            }

            boolean hasReferences = v.ref.decrementAndGet() > 0;
            if (hasReferences || v.lock.isLocked() || v.lock.hasQueuedThreads()) {
                return v;
            }

            return null;
        });
    }
}
