package com.eurobuddha.atomix.swap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.eurobuddha.comms.NodeApi;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * STEP: the 0.1.17 incident's missing half — detection without healing. A node reset with a different
 * seed orphaned the persisted swap identity; the guard (0.1.17) makes that loud, this (0.1.18) makes it
 * self-correcting: setup() verifies the saved identity against the node's real keys and, when orphaned,
 * discards it and picks a fresh one from the CURRENT node. Both engines persist whatever ok() returns,
 * so the heal writes itself back; a process-static makes fg + bg converge on ONE replacement instead of
 * racing getaddress (which rotates).
 */
public class IdentityHealTest {

    private static final String OLD_PK = "0x800054A528AC4C1A85E21D88EAA27923C032C317C5542F90A3A66ED0C409CD9D";
    private static final String OLD_ADDR = "MxOLDADDR";

    private MinimaHtlc htlc;
    private JSONArray nodeKeys;
    private int getaddressCalls;

    /** Scripted NodeApi mock: newscript OK, `keys` returns nodeKeys, getaddress mints NEWPK<n>.
     *  (Mockito mock, not a subclass — the real constructor needs a Looper + MinimaAPI.) */
    private NodeApi fakeNode() {
        NodeApi node = org.mockito.Mockito.mock(NodeApi.class);
        org.mockito.Mockito.doAnswer(inv -> {
            String command = inv.getArgument(0);
            NodeApi.Cb cb = inv.getArgument(1);
            JSONObject r = new JSONObject().put("status", true);
            if (command.startsWith("keys")) r.put("response", new JSONObject().put("keys", nodeKeys));
            else if (command.startsWith("getaddress")) {
                getaddressCalls++;
                r.put("response", new JSONObject()
                        .put("miniaddress", "MxNEWADDR" + getaddressCalls)
                        .put("publickey", "0xNEWPK" + getaddressCalls));
            } else r.put("response", new JSONObject());
            cb.onResult(r);
            return null;
        }).when(node).cmd(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(NodeApi.Cb.class));
        return node;
    }

    @Before public void setUp() throws Exception {
        MinimaHtlc.resetHealForTest();
        htlc = new MinimaHtlc(fakeNode());
        nodeKeys = new JSONArray();       // default: node owns NOTHING of ours
        getaddressCalls = 0;
    }

    private String[] runSetup(String addr, String pk) {
        final String[] out = new String[3];
        htlc.setup(addr, pk, new MinimaHtlc.SetupCb() {
            @Override public void ok(String a, String p) { out[0] = a; out[1] = p; }
            @Override public void err(String m) { out[2] = m; }
        });
        return out;
    }

    @Test public void ownedIdentityIsKeptVerbatim() throws Exception {
        nodeKeys.put(new JSONObject().put("publickey", OLD_PK));
        String[] r = runSetup(OLD_ADDR, OLD_PK);
        assertEquals(OLD_ADDR, r[0]);
        assertEquals(OLD_PK, r[1]);
        assertEquals(0, getaddressCalls);
    }

    @Test public void orphanedIdentityIsReplacedFromTheCurrentNode() throws Exception {
        nodeKeys.put(new JSONObject().put("publickey", "0xSOMEOTHERKEY"));   // populated list, ours absent
        String[] r = runSetup(OLD_ADDR, OLD_PK);
        assertEquals("MxNEWADDR1", r[0]);
        assertEquals("0xNEWPK1", r[1]);
        assertEquals(1, getaddressCalls);
    }

    /** fg and bg both boot with the same dead identity: they must converge on ONE replacement. */
    @Test public void secondEngineAdoptsTheFirstHealNotAFreshPick() throws Exception {
        nodeKeys.put(new JSONObject().put("publickey", "0xSOMEOTHERKEY"));
        String[] first = runSetup(OLD_ADDR, OLD_PK);
        MinimaHtlc second = new MinimaHtlc(fakeNode());
        final String[] out = new String[2];
        second.setup(OLD_ADDR, OLD_PK, new MinimaHtlc.SetupCb() {
            @Override public void ok(String a, String p) { out[0] = a; out[1] = p; }
            @Override public void err(String m) {}
        });
        assertEquals(first[1], out[1]);                 // same healed key, no second getaddress
        assertEquals(1, getaddressCalls);
    }

    /** A busy/locked node (empty key list) must NOT destroy a good identity — trust it, guard backstops. */
    @Test public void emptyKeyListTrustsTheSavedIdentity() {
        nodeKeys = new JSONArray();
        String[] r = runSetup(OLD_ADDR, OLD_PK);
        // empty keys means "cannot verify", not "orphaned"… except an empty list is also what an
        // orphaned check sees. The distinction: loadMyKeys returns EMPTY on a busy node vs a
        // POPULATED list missing our key on a reset node. Assert the populated-miss heals (above)
        // and the empty list keeps the saved identity:
        assertEquals(OLD_ADDR, r[0]);
        assertEquals(OLD_PK, r[1]);
        assertEquals(0, getaddressCalls);
    }
}
