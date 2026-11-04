package com.eurobuddha.atomix.swap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.SharedPreferences;
import android.os.Handler;

import com.eurobuddha.atomix.Handover;
import com.eurobuddha.atomix.TradingContext;
import com.eurobuddha.atomix.eth.EthHtlc;
import com.eurobuddha.atomix.eth.EthNet;
import com.eurobuddha.atomix.eth.EthRpc;
import com.eurobuddha.atomix.eth.EthWallet;
import com.eurobuddha.comms.NodeApi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.List;

/** 0.1.90: rows that can never clear must not block the Dexicon handover forever — an ETH-first lock that never
 *  landed is finalized only on two independent endpoints' evidence — and a lost start callback ages out. */
public class HandoverLivenessTest {
    static final String HASH = "0x" + "5a".repeat(32);
    static final String NONE = "0x" + "0".repeat(768);                                        // zero sender = no contract
    static final String SOME = "0x" + "0".repeat(24) + "12".repeat(20) + "0".repeat(704);      // a real contract
    final List<String> eps = EthRpc.fallbacks();
    static final String WALLET = "0x" + "34".repeat(20);
    static final String LOCK_TX = "0x" + "7e".repeat(32);
    EthRpc rpc; SwapDb db; SwapEngine engine; long now;

    @Before public void setup() throws Exception {
        now = System.currentTimeMillis() / 1000;
        rpc = mock(EthRpc.class);
        db = mock(SwapDb.class);
        EthWallet w = mock(EthWallet.class);
        when(w.address()).thenReturn(WALLET);
        engine = new SwapEngine(mock(NodeApi.class), mock(MinimaHtlc.class), db, w,
                mock(Handler.class), mock(SwapEngine.Notifier.class));
        engine.setNetwork(rpc, EthNet.MAINNET);
        SwapEngine.clearRetryMarkersForTest();
        Handover.load(mock(SharedPreferences.class));   // not handed over
        when(rpc.latestBlockOn(anyString())).thenReturn(new long[]{24_000_000L, now});
        when(rpc.ethCallOn(anyString(), anyString(), anyString(), anyString())).thenReturn(NONE);
        when(rpc.callOn(anyString(), eq("eth_getTransactionCount"), any())).thenReturn("0x5");   // pending == latest
    }

    private static String ev(EthRpc rpc, EthHtlc eth, String cid, long notBefore, int pair) {
        return SwapEngine.neverLockedEvidence(rpc, eth, cid, notBefore, pair, null, WALLET);
    }

    @After public void cleanup() { engine.shutdown(); SwapEngine.clearRetryMarkersForTest(); }

    private EthHtlc eth() { return new EthHtlc(rpc, null, EthNet.MAINNET); }

    private SwapDb.Swap ethFirst(long timelock) {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.role = "INITIATOR"; s.direction = "ERC20_TO_MINIMA"; s.status = SwapDb.ST_STARTED;
        s.myLegIsMinima = false; s.myTimelock = timelock;
        when(db.getSwap(HASH)).thenReturn(s);
        return s;
    }

    @Test public void bothEndpointsShowingNoContractIsEvidenceNamingBoth() {
        String cid = EthHtlc.contractId(HASH);
        String ev = ev(rpc, eth(), cid, now - 10, 0);
        assertNotNull(ev);
        assertTrue(ev, ev.contains(cid) && ev.contains(eps.get(0)) && ev.contains(eps.get(1))
                && ev.contains("block 24000000") && ev.contains("time " + now));
        // the next attempt uses the next pair, so one dead provider cannot wedge the row forever
        String next = ev(rpc, eth(), cid, now - 10, 1);
        assertTrue(next, next.contains(eps.get(1)) && next.contains(eps.get(2)));
    }

    @Test public void disagreementFailureOrEarlyChainLeavesItBlocking() throws Exception {
        String cid = EthHtlc.contractId(HASH);
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString(), anyString())).thenReturn(SOME);
        assertNull("second endpoint sees a contract", ev(rpc, eth(), cid, now - 10, 0));
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString(), anyString())).thenThrow(new IOException("down"));
        assertNull("read failure", ev(rpc, eth(), cid, now - 10, 0));
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString(), anyString())).thenReturn("0x12");
        assertNull("malformed reply", ev(rpc, eth(), cid, now - 10, 0));
        assertNull("chain not yet past timelock + margin", ev(rpc, eth(), cid, now + 10, 2));
    }

    @Test public void finalizesWithEvidenceBeforeStatus() {
        SwapDb.Swap s = ethFirst(now - SwapEngine.NEVER_LOCKED_MARGIN_SECS - 60);
        engine.finalizeIfNeverLocked(eth(), s);
        InOrder o = inOrder(db);
        o.verify(db).logEvent(eq(HASH), eq(SwapDb.EV_NEVER_LOCKED), eq("ETH"), eq(""), contains("returned no contract on https://"));
        o.verify(db).setSwapStatus(HASH, SwapDb.ST_ERROR);
    }

    @Test public void notYetLateMinimaLegOrSettlingRowIsLeftAlone() {
        SwapDb.Swap s = ethFirst(now - SwapEngine.NEVER_LOCKED_MARGIN_SECS + 60);   // inside the margin
        engine.finalizeIfNeverLocked(eth(), s);
        s.myTimelock = now - 2 * SwapEngine.NEVER_LOCKED_MARGIN_SECS;
        s.myLegIsMinima = true;                                                    // my leg is a Minima lock
        engine.finalizeIfNeverLocked(eth(), s);
        s.myLegIsMinima = false; s.status = SwapDb.ST_CLAIMING;                    // settling, not "never locked"
        engine.finalizeIfNeverLocked(eth(), s);
        verify(db, never()).setSwapStatus(anyString(), anyString());
        verify(db, never()).logEvent(anyString(), anyString(), any(), any(), any());
    }

    /** 0.1.91 MAJOR: the responder's ETH counter-leg row is written LOCKED before newContract; if that lock never
     *  landed the row must clear on the same two-endpoint evidence, not block the handover forever. */
    @Test public void responderCounterLegThatNeverLandedIsFinalized() {
        SwapDb.Swap s = ethFirst(now - 2 * SwapEngine.NEVER_LOCKED_MARGIN_SECS);
        s.role = "RESPONDER"; s.direction = "MINIMA_TO_ERC20"; s.status = SwapDb.ST_LOCKED;
        engine.finalizeIfNeverLocked(eth(), s);
        InOrder o = inOrder(db);
        o.verify(db).logEvent(eq(HASH), eq(SwapDb.EV_NEVER_LOCKED), eq("ETH"), eq(""), contains("nothing pending"));
        o.verify(db).setSwapStatus(HASH, SwapDb.ST_ERROR);
    }

    @Test public void contractReadIsPinnedToTheBlockJustRead() throws Exception {
        assertNotNull(ev(rpc, eth(), EthHtlc.contractId(HASH), now - 10, 0));
        String pinned = "0x" + Long.toHexString(24_000_000L);
        verify(rpc).ethCallOn(eq(eps.get(0)), anyString(), anyString(), eq(pinned));
        verify(rpc).ethCallOn(eq(eps.get(1)), anyString(), anyString(), eq(pinned));
        verify(rpc, never()).ethCallOn(anyString(), anyString(), anyString(), eq("latest"));
    }

    @Test public void aPendingNonceOrUnreadableNonceRefuses() throws Exception {
        String cid = EthHtlc.contractId(HASH);
        when(rpc.callOn(eq(eps.get(1)), eq("eth_getTransactionCount"), any())).thenAnswer(i ->
                "pending".equals(((org.json.JSONArray) i.getArgument(2)).getString(1)) ? "0x6" : "0x5");
        assertNull("a tx of mine is still waiting to mine", ev(rpc, eth(), cid, now - 10, 0));
        doThrow(new IOException("down")).when(rpc).callOn(eq(eps.get(1)), eq("eth_getTransactionCount"), any());
        assertNull("nonce unreadable", ev(rpc, eth(), cid, now - 10, 0));
        assertNull("no wallet address", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 2, null, null));
    }

    @Test public void theRecordedLockTxMustBeUnknownOrReverted() throws Exception {
        String cid = EthHtlc.contractId(HASH);
        // unknown on both endpoints: fine
        String unknown = SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0, LOCK_TX, WALLET);
        assertTrue(unknown, unknown != null && unknown.contains("lock tx " + LOCK_TX + " unknown"));
        // pending on the second endpoint: refuse
        when(rpc.callOn(eq(eps.get(1)), eq("eth_getTransactionByHash"), any()))
                .thenReturn(new org.json.JSONObject().put("hash", LOCK_TX).put("blockNumber", org.json.JSONObject.NULL));
        assertNull("still pending", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0, LOCK_TX, WALLET));
        // mined and reverted: fine; mined and successful: refuse; receipt unreadable: refuse
        when(rpc.callOn(eq(eps.get(1)), eq("eth_getTransactionByHash"), any()))
                .thenReturn(new org.json.JSONObject().put("hash", LOCK_TX).put("blockNumber", "0x10"));
        when(rpc.callOn(eq(eps.get(1)), eq("eth_getTransactionReceipt"), any()))
                .thenReturn(new org.json.JSONObject().put("status", "0x0"));
        String reverted = SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0, LOCK_TX, WALLET);
        assertTrue(reverted, reverted != null && reverted.contains("reverted (status 0x0)"));
        when(rpc.callOn(eq(eps.get(1)), eq("eth_getTransactionReceipt"), any()))
                .thenReturn(new org.json.JSONObject().put("status", "0x1"));
        assertNull("it mined", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0, LOCK_TX, WALLET));
        when(rpc.callOn(eq(eps.get(1)), eq("eth_getTransactionReceipt"), any())).thenReturn(null);
        assertNull("receipt unreadable", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0, LOCK_TX, WALLET));
    }

    @Test public void theLockTxIsTakenFromTheRecordedEvents() throws Exception {
        SwapDb.Event e = new SwapDb.Event();
        e.event = SwapDb.EV_CPSENT; e.token = "ETH:0x" + "dd".repeat(20); e.note = LOCK_TX;
        when(db.getEvents(HASH)).thenReturn(java.util.Collections.singletonList(e));
        when(rpc.callOn(anyString(), eq("eth_getTransactionByHash"), any()))
                .thenReturn(new org.json.JSONObject().put("hash", LOCK_TX).put("blockNumber", org.json.JSONObject.NULL));
        SwapDb.Swap s = ethFirst(now - 2 * SwapEngine.NEVER_LOCKED_MARGIN_SECS);
        s.role = "RESPONDER"; s.status = SwapDb.ST_LOCKED;
        engine.finalizeIfNeverLocked(eth(), s);
        verify(db, never()).setSwapStatus(anyString(), anyString());   // its lock tx is still pending
    }

    @Test public void endpointDisagreementWritesNothing() throws Exception {
        when(rpc.ethCallOn(anyString(), anyString(), anyString(), anyString())).thenReturn(NONE, SOME);   // first says none, second sees it
        engine.finalizeIfNeverLocked(eth(), ethFirst(now - 2 * SwapEngine.NEVER_LOCKED_MARGIN_SECS));
        verify(db, never()).setSwapStatus(anyString(), anyString());
    }

    @Test public void lostStartCallbackAgesOut() {
        TradingContext saved = TradingContext.active();
        TradingContext.setActive(TradingContext.MXUSDT, null);
        MinimaHtlc minima = mock(MinimaHtlc.class);          // generateSecret never answers: the lost callback
        when(minima.ready()).thenReturn(true);
        when(minima.activeToken()).thenReturn(TradingContext.MXUSDT.tokenId);
        EthWallet wallet = mock(EthWallet.class);
        when(wallet.ready()).thenReturn(true);
        SwapEngine e = new SwapEngine(mock(NodeApi.class), minima, db, wallet, mock(Handler.class), mock(SwapEngine.Notifier.class));
        e.setMyMinimaPk("0x1234");
        e.setNetwork(rpc, EthNet.MAINNET);
        Order maker = new Order();
        maker.market = TradingContext.MXUSDT; maker.minimaPublicKey = "0x5678"; maker.ethAddress = "0x" + "12".repeat(20);
        final String[] err = {null};
        SwapEngine.StartCb cb = new SwapEngine.StartCb() {
            public void ok(String h) {}
            public void err(String m) { err[0] = m; }
        };
        try {
            e.startMinimaToErc20(maker, "2", "USDT", "6", true, cb);
            e.startErc20ToMinima(maker, "USDT", "6", "2", true, cb);
            assertNull(err[0]);
            assertTrue(SwapEngine.initiatorStarting());
            SwapEngine.ageStartMarkersForTest(SwapEngine.START_MARKER_TTL_MS - 60_000);
            assertTrue("still young", SwapEngine.initiatorStarting());
            SwapEngine.ageStartMarkersForTest(120_000);
            assertFalse("aged out", SwapEngine.initiatorStarting());
            e.startMinimaToErc20(maker, "2", "USDT", "6", true, cb);   // the stale "sell" marker no longer wedges selling
            assertNull(err[0], err[0]);
            assertTrue(SwapEngine.initiatorStarting());
        } finally {
            SwapEngine.ageStartMarkersForTest(SwapEngine.START_MARKER_TTL_MS + 1);
            SwapEngine.initiatorStarting();
            e.shutdown();
            TradingContext.setActive(saved, null);
        }
    }
}
