package com.eurobuddha.atomix.swap;

import static org.junit.Assert.*;

import com.eurobuddha.atomix.TradingContext;

import org.junit.Test;

import java.util.Collections;

/** 0.1.89: a persisted buy request is re-armed only in the market whose take channel it arrived on. */
public class PendingTakeMarketTest {
    private static final String A = "0x" + "aa".repeat(32), B = "0x" + "bb".repeat(32);

    @Test public void requestsStayInTheirOwnMarket() {
        String s = SwapTake.edit("", TradingContext.MXUSDT, A, null);
        s = SwapTake.edit(s, TradingContext.MINIMA, B, null);
        assertEquals(Collections.singleton(A), SwapTake.pending(s, TradingContext.MXUSDT));
        assertEquals(Collections.singleton(B), SwapTake.pending(s, TradingContext.MINIMA));
        // Pruning one market's finished request leaves the other market's alone.
        s = SwapTake.edit(s, TradingContext.MXUSDT, null, Collections.singleton(A));
        assertTrue(SwapTake.pending(s, TradingContext.MXUSDT).isEmpty());
        assertEquals(Collections.singleton(B), SwapTake.pending(s, TradingContext.MINIMA));
    }

    @Test public void untaggedLegacyRequestsAreNeverReArmed() {
        String legacy = A + "," + B;
        for (TradingContext m : TradingContext.values()) assertTrue(SwapTake.pending(legacy, m).isEmpty());
        assertEquals(Collections.singleton(A), SwapTake.pending(SwapTake.edit(legacy, TradingContext.MINIMA, A, null), TradingContext.MINIMA));
        assertFalse(SwapTake.edit(legacy, TradingContext.MINIMA, A, null).contains(B));
    }
}
