package com.eurobuddha.atomix;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

/** AtomiX's comms identity on the Trading Account is Dexicon's (DoorHost: seedrandom dexicon-doors-v1 -> "0x" + hex). */
public class AtomixAccountTest {
    @Test public void theIdentityInputIsTheSeedrandomValueAsDexiconUsesIt() throws Exception {
        String v = "0x57ED270F37D6E15831152356865F5C78913A83379870DB848ECAB80915A9584A";   // SameKeysEverywhereTest's doors seed
        assertEquals(v, AtomixAccount.identityInput(new JSONObject().put("status", true).put("response", new JSONObject().put("seedrandom", v))));
        assertEquals(v, AtomixAccount.identityInput(new JSONObject().put("response", new JSONObject().put("seedrandom", v.substring(2)))));
        assertEquals("", AtomixAccount.identityInput(new JSONObject().put("response", new JSONObject().put("seedrandom", "0x12"))));
        assertEquals("legacy vault seed unchanged", "0xAB", AtomixAccount.identityInput(new JSONObject().put("response", new JSONObject().put("seed", "0xAB"))));
        assertEquals("dexicon-doors-v1", AtomixAccount.DOORS_IDENTITY);
    }

    @Test public void onlyAChecksummedMxAddressIsAccepted() {
        org.junit.Assert.assertTrue(AtomixAccount.validMx("MxG086KWK8A5SEPVYPUJEQYH926KFUSCPHVGTM9K4UKG8FGJWNSG1DQP5TG1CUP"));
        org.junit.Assert.assertFalse("one character changed", AtomixAccount.validMx("MxG086KWK8A5SEPVYPUJEQYH926KFUSCPHVGTM9K4UKG8FGJWNSG1DQP5TG1CUQ"));
        org.junit.Assert.assertFalse("a token id is not an address", AtomixAccount.validMx("0x7D39745FBD29049BE29850B55A18BF550E4D442F930F86266E34193D89042A90"));
        org.junit.Assert.assertFalse(AtomixAccount.validMx(null));
    }
}
