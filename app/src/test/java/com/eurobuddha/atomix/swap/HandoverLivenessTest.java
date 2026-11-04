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
    EthRpc rpc; SwapDb db; SwapEngine engine; long now;

    @Before public void setup() throws Exception {
        now = System.currentTimeMillis() / 1000;
        rpc = mock(EthRpc.class);
        db = mock(SwapDb.class);
        engine = new SwapEngine(mock(NodeApi.class), mock(MinimaHtlc.class), db, mock(EthWallet.class),
                mock(Handler.class), mock(SwapEngine.Notifier.class));
        engine.setNetwork(rpc, EthNet.MAINNET);
        SwapEngine.clearRetryMarkersForTest();
        Handover.load(mock(SharedPreferences.class));   // not handed over
        when(rpc.latestBlockOn(anyString())).thenReturn(new long[]{24_000_000L, now});
        when(rpc.ethCallOn(anyString(), anyString(), anyString())).thenReturn(NONE);
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
        String ev = SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0);
        assertNotNull(ev);
        assertTrue(ev, ev.contains(cid) && ev.contains(eps.get(0)) && ev.contains(eps.get(1))
                && ev.contains("block 24000000") && ev.contains("time " + now));
        // the next attempt uses the next pair, so one dead provider cannot wedge the row forever
        String next = SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 1);
        assertTrue(next, next.contains(eps.get(1)) && next.contains(eps.get(2)));
    }

    @Test public void disagreementFailureOrEarlyChainLeavesItBlocking() throws Exception {
        String cid = EthHtlc.contractId(HASH);
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString())).thenReturn(SOME);
        assertNull("second endpoint sees a contract", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0));
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString())).thenThrow(new IOException("down"));
        assertNull("read failure", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0));
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString())).thenReturn("0x12");
        assertNull("malformed reply", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now - 10, 0));
        assertNull("chain not yet past timelock + margin", SwapEngine.neverLockedEvidence(rpc, eth(), cid, now + 10, 2));
    }

    @Test public void finalizesWithEvidenceBeforeStatus() {
        SwapDb.Swap s = ethFirst(now - SwapEngine.NEVER_LOCKED_MARGIN_SECS - 60);
        engine.finalizeIfNeverLocked(eth(), s);
        InOrder o = inOrder(db);
        o.verify(db).logEvent(eq(HASH), eq(SwapDb.EV_NEVER_LOCKED), eq("ETH"), eq(""), contains("returned no contract on https://"));
        o.verify(db).setSwapStatus(HASH, SwapDb.ST_ERROR);
    }

    @Test public void notYetLateOrNotAnEthFirstInitiatorIsLeftAlone() {
        SwapDb.Swap s = ethFirst(now - SwapEngine.NEVER_LOCKED_MARGIN_SECS + 60);   // inside the margin
        engine.finalizeIfNeverLocked(eth(), s);
        s.myTimelock = now - 2 * SwapEngine.NEVER_LOCKED_MARGIN_SECS;
        s.role = "RESPONDER";
        engine.finalizeIfNeverLocked(eth(), s);
        s.role = "INITIATOR"; s.myLegIsMinima = true;
        engine.finalizeIfNeverLocked(eth(), s);
        verify(db, never()).setSwapStatus(anyString(), anyString());
        verify(db, never()).logEvent(anyString(), anyString(), any(), any(), any());
    }

    @Test public void endpointDisagreementWritesNothing() throws Exception {
        when(rpc.ethCallOn(eq(eps.get(1)), anyString(), anyString())).thenReturn(SOME);
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
