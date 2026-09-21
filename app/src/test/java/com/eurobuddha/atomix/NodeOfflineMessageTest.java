package com.eurobuddha.atomix;
import com.eurobuddha.comms.NodeApi;
import org.junit.Test;
import static org.junit.Assert.*;

/** Silence and refusal are different faults with opposite remedies. The app must never answer one with
 *  the other — live 2026-09-21, a wedged node was reported as "enable AtomiX in Minima Core → Apps". */
public class NodeOfflineMessageTest {

    @Test public void onlyTheNodesOwnVerdictAsksTheUserToEnableTheApp() {
        String m = NodeApi.offlineMessage(NodeApi.Offline.REFUSED, false);
        assertTrue(m.contains("Enable AtomiX in Minima Core"));
    }

    @Test public void silenceAfterAGoodReplyBlamesTheNodeNotThePermission() {
        String m = NodeApi.offlineMessage(NodeApi.Offline.UNREACHABLE, true);
        assertTrue("a proven pairing must not be questioned", m.contains("It is enabled"));
        assertTrue(m.contains("restarting"));
        assertFalse(m.contains("Enable AtomiX in Minima Core"));
    }

    @Test public void silenceWithNoReplyEverStaysHonestlyAmbiguous() {
        String m = NodeApi.offlineMessage(NodeApi.Offline.UNREACHABLE, false);
        assertTrue(m.contains("installed and running"));
        assertTrue(m.contains("enabled in Minima Core"));
    }
}
