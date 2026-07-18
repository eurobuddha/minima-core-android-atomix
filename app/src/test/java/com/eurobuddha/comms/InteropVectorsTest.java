package com.eurobuddha.comms;

import static org.junit.Assert.*;

import com.goterl.lazysodium.LazySodium;
import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;

import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

/**
 * INTEROP VECTOR GENERATOR — the byte-truth fixture for the AtomiX MDS port.
 *
 * The MDS (JavaScript) implementation must derive the SAME identities, open the SAME sealed blobs, verify the
 * SAME signatures, and derive the SAME ETH address as this native code, from the same node seed — that is what
 * makes it a real interoperating peer on the shared order books rather than a lookalike. This test emits a JSON
 * vector file that the JS side's harness consumes and asserts byte-equality against (spike 0c/0d, then the
 * phase-7 release gate). It also asserts the native side's own invariants so the fixture can't silently rot.
 *
 * Output: build/interop_vectors.json (also copied by the JS harness into the atomix-mds repo).
 */
public class InteropVectorsTest {

    /** Fixed test seeds exercising BOTH branches of the seed-bytes rule (0x → hex decode, else raw UTF-8). */
    private static final String SEED_UTF8 = "atomix-interop-vector-seed-01";
    private static final String SEED_HEX  = "0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";

    /** Fixed plaintext + message for seal/sign vectors. */
    private static final String PLAINTEXT = "{\"hash\":\"0xC0FFEE\",\"n\":1}";
    private static final String SIGN_MSG  = "atomix-sign-vector";

    /** Same rule as MainActivity/SwapService: 0x → hex bytes, else UTF-8 bytes. */
    private static byte[] seedBytes(String seed) {
        return seed.startsWith("0x") ? Hex.from(seed) : seed.getBytes(StandardCharsets.UTF_8);
    }

    @Test public void generateVectors() throws Exception {
        LazySodium ls = new LazySodiumJava(new SodiumJava());
        JSONObject out = new JSONObject();

        // ---- identities: both seeds × both legacy contexts ----
        JSONObject ids = new JSONObject();
        for (String seed : new String[]{SEED_UTF8, SEED_HEX}) {
            for (String ctx : new String[]{"usdtswap", "minimaswap"}) {
                CommsIdentity id = CommsIdentity.fromSeed(ls, seedBytes(seed), ctx);
                JSONObject j = new JSONObject();
                j.put("boxPk", Hex.to(id.boxPk));
                j.put("signPk", Hex.to(id.signPk));
                j.put("publicId", id.publicId());
                ids.put(seed + "|" + ctx, j);
            }
        }
        out.put("identities", ids);

        // ---- envelope: a REAL sealed blob (full {f,b,s} construction) the JS side must open + verify ----
        CommsIdentity sender = CommsIdentity.fromSeed(ls, seedBytes(SEED_UTF8), "usdtswap");
        CommsIdentity recip  = CommsIdentity.fromSeed(ls, seedBytes(SEED_HEX), "usdtswap");
        String blob = new LocalEcCryptoProvider(ls, sender)
                .seal(recip.publicId(), PLAINTEXT.getBytes(StandardCharsets.UTF_8));
        assertNotNull("native seal must succeed", blob);
        // native round-trip sanity so the fixture can't rot silently
        Opened opened = new LocalEcCryptoProvider(ls, recip).open(blob);
        assertNotNull(opened);
        assertEquals(PLAINTEXT, new String(opened.plaintext, StandardCharsets.UTF_8));
        assertEquals(sender.publicId(), opened.fromPublicId);
        JSONObject env = new JSONObject();
        env.put("senderPublicId", sender.publicId());
        env.put("recipientPublicId", recip.publicId());
        env.put("plaintext", PLAINTEXT);
        env.put("sealedBlobHex", blob);
        out.put("envelope", env);

        // ---- bare Ed25519 detached signature (the order-book signing primitive) ----
        byte[] msg = SIGN_MSG.getBytes(StandardCharsets.UTF_8);
        byte[] sig = new byte[com.goterl.lazysodium.interfaces.Sign.BYTES];
        assertTrue(ls.cryptoSignDetached(sig, msg, msg.length, sender.signSk));
        assertTrue(ls.cryptoSignVerifyDetached(sig, msg, msg.length, sender.signPk));
        JSONObject sj = new JSONObject();
        sj.put("message", SIGN_MSG);
        sj.put("signerPublicId", sender.publicId());
        sj.put("signPk", Hex.to(sender.signPk));
        sj.put("sigHex", Hex.to(sig));
        out.put("sign", sj);

        // ---- ETH address derivation: seedrandom output (raw 32-byte privkey) → secp256k1 address ----
        // Same call chain as EthWallet: Credentials.create(<seedrandom hex>) → .getAddress().
        String ethPriv = "0x1f2e3d4c5b6a79881f2e3d4c5b6a79881f2e3d4c5b6a79881f2e3d4c5b6a7988";
        org.web3j.crypto.Credentials creds = org.web3j.crypto.Credentials.create(ethPriv);
        JSONObject ej = new JSONObject();
        ej.put("privKey", ethPriv);
        ej.put("address", creds.getAddress());
        out.put("eth", ej);

        // ---- write the fixture ----
        File f = new File("build/interop_vectors.json");
        f.getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(f)) { w.write(out.toString(2)); }
        assertTrue(f.length() > 0);
        System.out.println("interop vectors → " + f.getAbsolutePath());
    }
}
