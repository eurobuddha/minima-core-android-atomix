package com.eurobuddha.atomix.swap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Handler;

import com.eurobuddha.atomix.eth.EthNet;
import com.eurobuddha.atomix.eth.EthRpc;
import com.eurobuddha.atomix.eth.EthWallet;
import com.eurobuddha.comms.NodeApi;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Security review 7 Nov 2026.
 *  (1) CRITICAL: a counterparty-chosen hashlock that is not exactly 32 bytes is never answered — KISS compares the
 *      bytes exactly, but the ETH counter-leg coerced it to 32 (EthHtlc.b32), so 0x00‖H let the taker withdraw my
 *      USDT with a secret my Minima claim could never match.
 *  (2) CRITICAL: the initiator never reveals its secret into a counter-leg whose refund opens within the reveal margin
 *      (the owner would race it with a refund and use the secret on the initiator's first leg). A RESPONDER collects
 *      with an already-public secret and is never blocked.
 */
public class RevealWindowTest {
    private static final String HASH = "0x" + "44".repeat(32), MY_MPK = "0xAAAA";
    private SwapEngine engine;
    private SwapDb db;
    private EthRpc rpc;
    private SwapDb.Swap sw;

    @Before public void setUp() throws Exception {
        SwapEngine.clearRetryMarkersForTest();
        EthWallet wallet = mock(EthWallet.class);
        when(wallet.address()).thenReturn("0x1111111111111111111111111111111111111111");
        db = mock(SwapDb.class);
        engine = new SwapEngine(mock(NodeApi.class), mock(MinimaHtlc.class), db, wallet, mock(Handler.class), mock(SwapEngine.Notifier.class));
        rpc = mock(EthRpc.class);
        engine.setNetwork(rpc, EthNet.MAINNET);
        engine.setMyMinimaPk(MY_MPK);
        when(db.getSecret(HASH)).thenReturn("0x" + "33".repeat(32));
        sw = new SwapDb.Swap(); sw.hash = HASH; sw.buyToken = "mxUSDT"; sw.status = SwapDb.ST_LOCKED; sw.role = "INITIATOR";
        when(db.getSwap(HASH)).thenReturn(sw);
        when(db.getRequest(HASH)).thenReturn(new String[]{"4.95", "minima"});
    }

    private static JSONObject coin(String hash, int timelock) throws Exception {
        return new JSONObject().put("tokenid", MinimaHtlc.USDT_TOKENID).put("tokenamount", "4.95")
                .put("state", new JSONObject().put("0", "0xMAKER").put("2", "[minima]").put("3", String.valueOf(timelock))
                        .put("4", MY_MPK).put("5", hash));
    }

    @Test public void nonThirtyTwoByteHashlockIsIgnored() throws Exception {
        engine.checkCanSwapCoin(coin("0x00" + HASH.substring(2), 999999), 100);
        verify(db, never()).getSecret(anyString());
        verify(db, never()).setSwapStatus(anyString(), anyString());
    }

    @Test public void initiatorDoesNotRevealIntoAClosingMinimaWindow() throws Exception {
        engine.checkCanSwapCoin(coin(HASH, 100 + SwapEngine.REVEAL_MIN_BLOCKS - 1), 100);
        verify(db, never()).setSwapStatus(eq(HASH), eq(SwapDb.ST_CLAIMING));
    }

    @Test public void initiatorClaimsWithEnoughWindow() throws Exception {
        try { engine.checkCanSwapCoin(coin(HASH, 100 + SwapEngine.CP_BLOCKS), 100); } catch (RuntimeException androidLogStub) { }
        verify(db).setSwapStatus(HASH, SwapDb.ST_CLAIMING);
    }

    @Test public void aRetryAfterMySecretWentOutIsNeverBlocked() throws Exception {
        // R2-1: my first claim carried the secret but did not mine — the secret is public now; a retry must go out
        when(db.hasEvent(HASH, SwapDb.EV_SECRET_OUT)).thenReturn(true);
        try { engine.checkCanSwapCoin(coin(HASH, 101), 100); } catch (RuntimeException androidLogStub) { }
        verify(db).setSwapStatus(HASH, SwapDb.ST_CLAIMING);
        long now = System.currentTimeMillis() / 1000;
        when(rpc.latestBlockTimestamp()).thenReturn(now);
        assertTrue(engine.revealWindowOk(HASH, now + 15));
    }

    @Test public void theFirstClaimRecordsThatTheSecretWentOut() throws Exception {
        try { engine.checkCanSwapCoin(coin(HASH, 100 + SwapEngine.CP_BLOCKS), 100); } catch (RuntimeException androidLogStub) { }
        verify(db).logEvent(HASH, SwapDb.EV_SECRET_OUT, "", "", "");
    }

    @Test public void responderCollectIsNeverBlocked() throws Exception {
        sw.role = "RESPONDER";
        try { engine.checkCanSwapCoin(coin(HASH, 101), 100); } catch (RuntimeException androidLogStub) { }
        verify(db).setSwapStatus(HASH, SwapDb.ST_CLAIMING);
    }

    @Test public void ethRevealNeedsTheMarginForTheInitiatorOnly() throws Exception {
        long now = System.currentTimeMillis() / 1000;
        when(rpc.latestBlockTimestamp()).thenReturn(now);
        assertFalse(engine.revealWindowOk(HASH, now + 15));
        assertTrue(engine.revealWindowOk(HASH, now + SwapEngine.CP_SECS - 100));
        sw.role = "RESPONDER";
        assertTrue("a responder's secret is already public", engine.revealWindowOk(HASH, now + 15));
    }
}
