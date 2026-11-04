package com.eurobuddha.atomix.swap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** 0.1.95: a "lock gone" reading logged BEFORE the newest PRESENT row is void, even when its block is higher
 *  (0.1.91/0.1.92 wrote PRESENT with the sweep's start block and GONE_SEEN with the later tip). */
public class LockEvidenceOrderTest {
    private static final String HASH = "0x" + "ab".repeat(32);

    private static SwapDb.Event ev(long id, String event, String note) {
        SwapDb.Event e = new SwapDb.Event(); e.id = id; e.event = event; e.note = note; return e;
    }

    private static long pending(List<SwapDb.Event> newestFirst) {
        SwapDb db = mock(SwapDb.class);
        when(db.getEvents(HASH)).thenReturn(newestFirst);
        return SwapEngine.pendingLockGone(db, HASH, SwapDb.EV_LOCK_SPENT);
    }

    @Test public void aReadingLoggedBeforeAPresentIsVoidEvenWithAHigherBlock() {
        List<SwapDb.Event> l = new ArrayList<>();
        l.add(ev(2, SwapDb.EV_LOCK_PRESENT, "minima block 1100"));
        l.add(ev(1, SwapDb.EV_LOCK_GONE_SEEN, SwapDb.EV_LOCK_SPENT + " minima block 1101"));
        assertEquals("legacy 0.1.92 order: the 1101 reading was voided by the later PRESENT", -1, pending(l));
    }

    @Test public void aReadingAfterThePresentStillCounts() {
        List<SwapDb.Event> l = new ArrayList<>();
        l.add(ev(3, SwapDb.EV_LOCK_GONE_SEEN, SwapDb.EV_LOCK_SPENT + " minima block 1110"));
        l.add(ev(2, SwapDb.EV_LOCK_PRESENT, "minima block 1103"));
        l.add(ev(1, SwapDb.EV_LOCK_GONE_SEEN, SwapDb.EV_LOCK_SPENT + " minima block 1100"));
        assertEquals(1110, pending(l));
    }
}
