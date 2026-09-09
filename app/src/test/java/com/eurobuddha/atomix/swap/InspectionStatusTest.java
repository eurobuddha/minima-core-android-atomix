package com.eurobuddha.atomix.swap;
import com.eurobuddha.atomix.eth.EthHtlc;
import org.junit.Test;
import static org.junit.Assert.*;
public class InspectionStatusTest {
    @Test public void openEthereumLegWithoutSecretDoesNotPromiseCollection() {
        String state = SwapEngine.ethClaimStatus(new EthHtlc.Contract(), false);
        assertEquals("locked; waiting for the secret", state); assertFalse(state.contains("claimable"));
    }
    @Test public void secretAvailabilityDoesNotOverrideTerminalState() {
        EthHtlc.Contract c = new EthHtlc.Contract(); c.refunded = true;
        assertEquals("refunded", SwapEngine.ethClaimStatus(c, true));
        c.refunded = false; c.withdrawn = true;
        assertEquals("withdrawn (complete)", SwapEngine.ethClaimStatus(c, true));
    }
}
