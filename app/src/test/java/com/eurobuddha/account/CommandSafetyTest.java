// VENDORED VERBATIM from apks/dexicon 0.3.79 (ca0eff8821233a757ee0e531798f102e4ee2b4b5) com/eurobuddha/account/CommandSafetyTest.java by scripts/vendor-account.sh — do not edit here; change Dexicon and re-vendor.
package com.eurobuddha.account;

import static org.junit.Assert.*;

import org.junit.Test;

/** Review 23 Nov 2026 (MINOR): stock Minima Core runs every ';'-separated part of a command; AtomiX and PandaPools sent
 *  commands unchecked. One shared rule now, vendored with the account into all three apps. */
public class CommandSafetyTest {
    @Test public void oneCommandOnly() {
        assertNull(CommandSafety.failure("coins address:0x" + "AB".repeat(32)));
        assertNotNull(CommandSafety.failure("txncreate id:x;vault"));
        assertNotNull(CommandSafety.failure("balance\nvault"));
        assertNotNull(CommandSafety.failure(" "));
        assertNotNull(CommandSafety.failure("x".repeat(100_001)));
    }
}
