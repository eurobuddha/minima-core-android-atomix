package com.eurobuddha.atomix.swap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.SharedPreferences;
import android.os.Handler;

import com.eurobuddha.atomix.Handover;
import com.eurobuddha.atomix.eth.EthHtlc;
import com.eurobuddha.atomix.eth.EthNet;
import com.eurobuddha.atomix.eth.EthRpc;
import com.eurobuddha.atomix.eth.EthWallet;
import com.eurobuddha.comms.NodeApi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

/** 8 Nov 2026: a maker's USDT lock sat unmined at 0.6 gwei through a 50x base-fee spike, then mined after its deadline
 *  and reverted. The engine step that fixes this, driven for real: re-price while the lock can still be used, cancel once
 *  its own deadline has passed by CHAIN time, never early. */
public class LockRepriceCancelTest {
    static final String HASH = "07d6e2f9692d155a5b9cf30da4a7293afd1f46321caffca95321763530d74d40";
    EthRpc rpc; SwapDb db; EthHtlc eth; SwapEngine engine; long now;

    @Before public void setup() throws Exception {
        now = System.currentTimeMillis() / 1000;
        rpc = mock(EthRpc.class); db = mock(SwapDb.class); eth = mock(EthHtlc.class);
        EthWallet w = mock(EthWallet.class);
        when(w.address()).thenReturn("0x" + "34".repeat(20));
        when(w.ethBalanceWei(any())).thenReturn(new BigInteger("1000000000000000000"));   // 1 ETH: gas is affordable
        engine = new SwapEngine(mock(NodeApi.class), mock(MinimaHtlc.class), db, w, mock(Handler.class), mock(SwapEngine.Notifier.class));
        engine.setNetwork(rpc, EthNet.MAINNET);
        SwapEngine.clearRetryMarkersForTest();
        Handover.load(mock(SharedPreferences.class));
        when(rpc.callStr(eq("eth_gasPrice"), any())).thenReturn("0x3b9aca00");
        when(rpc.baseFeePerGasOrZero()).thenReturn(BigInteger.valueOf(1_000_000_000L));
        when(eth.cancelLock(HASH)).thenReturn("0x" + "cc".repeat(32));
        when(eth.repriceLock(HASH)).thenReturn("0x" + "dd".repeat(32));
    }
    @After public void cleanup() { engine.shutdown(); SwapEngine.clearRetryMarkersForTest(); }

    private SwapDb.Swap makerLock(long timelock) {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.role = "RESPONDER"; s.direction = "MINIMA_TO_ERC20"; s.status = SwapDb.ST_LOCKED;
        s.myLegIsMinima = false; s.myTimelock = timelock;
        return s;
    }

    @Test public void anUnminedLockPastItsChainDeadlineIsCancelledNotRepriced() throws Exception {
        when(rpc.latestBlockTimestamp()).thenReturn(now);
        engine.repriceMyEthLock(eth, makerLock(now - 1));
        verify(eth).cancelLock(HASH);
        verify(eth, never()).repriceLock(anyString());
        verify(db).logEvent(HASH, SwapDb.EV_LOCK_CANCELLED, "ETH", "", "0x" + "cc".repeat(32));
    }

    @Test public void aLockStillInItsWindowIsRepricedNeverCancelled() throws Exception {
        when(rpc.latestBlockTimestamp()).thenReturn(now);
        engine.repriceMyEthLock(eth, makerLock(now + SwapEngine.REVEAL_MIN_SECS + 600));
        verify(eth, never()).cancelLock(anyString());
        verify(eth).repriceLock(HASH);
    }

    @Test public void aFastPhoneClockCannotCancelALockTheChainStillHonours() throws Exception {
        when(rpc.latestBlockTimestamp()).thenReturn(now - 120);   // the chain is 2 min behind the device
        engine.repriceMyEthLock(eth, makerLock(now - 60));         // past by the phone, not by the chain
        verify(eth, never()).cancelLock(anyString());
    }

    @Test public void anUnreadableChainClockCancelsNothing() throws Exception {
        when(rpc.latestBlockTimestamp()).thenThrow(new java.io.IOException("rpc down"));
        engine.repriceMyEthLock(eth, makerLock(now - 3600));
        verify(eth, never()).cancelLock(anyString());
    }

    @Test public void aMinimaLegOrATerminalRowIsLeftAlone() throws Exception {
        when(rpc.latestBlockTimestamp()).thenReturn(now);
        SwapDb.Swap minima = makerLock(now - 1); minima.myLegIsMinima = true;
        engine.repriceMyEthLock(eth, minima);
        SwapDb.Swap done = makerLock(now - 1); done.status = SwapDb.ST_COMPLETE;
        engine.repriceMyEthLock(eth, done);
        verifyNoInteractions(eth);
    }

    /** The USB-only live-test switch: re-pricing is held off (so the cancel is what runs); the cancel itself is unchanged. */
    @Test public void stuckLockSwitchHoldsRepricingButNotTheCancel() throws Exception {
        java.io.File sw = java.io.File.createTempFile("stuck-lock-test", "");
        try {
            com.eurobuddha.atomix.eth.EthTx.stuckLockSwitch = sw;
            when(rpc.latestBlockTimestamp()).thenReturn(now);
            engine.repriceMyEthLock(eth, makerLock(now + SwapEngine.REVEAL_MIN_SECS + 600));
            verify(eth, never()).repriceLock(anyString());
            SwapEngine.clearRetryMarkersForTest();
            engine.repriceMyEthLock(eth, makerLock(now - 1));
            verify(eth).cancelLock(HASH);
            sw.delete();                                          // switch off: re-pricing is back
            SwapEngine.clearRetryMarkersForTest();
            engine.repriceMyEthLock(eth, makerLock(now + SwapEngine.REVEAL_MIN_SECS + 600));
            verify(eth).repriceLock(HASH);
        } finally { com.eurobuddha.atomix.eth.EthTx.stuckLockSwitch = null; sw.delete(); }
    }
}
