package com.eurobuddha.atomix;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eurobuddha.atomix.swap.OtcDb;
import com.eurobuddha.atomix.swap.SwapDb;

import java.util.List;

/**
 * AtomiX's half of the Dexicon handover. Dexicon calls
 * {@code startActivityForResult(new Intent("com.eurobuddha.dexicon.action.HANDOVER").setPackage("com.eurobuddha.atomix"))}
 * and only unlocks its "uninstall AtomiX" button on RESULT_OK — uninstalling deletes AtomiX's swap secrets, so
 * this screen answers OK only once nothing can be lost: no swap or OTC deal in flight in either currency, and the
 * market + OTC offer withdrawn with the withdrawals on the wire. See {@link Handover}.
 */
public class HandoverActivity extends Activity {

    private final Handler ui = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private SwapDb db;
    private OtcDb otcDb;
    private LinearLayout col;
    private boolean armed = false;   // the user pressed "Withdraw market and hand over"
    private String shown = null;     // what is on screen — rebuild only on change, so a text selection survives
    private static final int REQ_SAVE = 41, REQ_VERIFY = 42;
    // The recovery record being saved: written via the system file picker, then verified by the user re-picking it.
    private String recHash; private byte[] recBytes; private String recNote = "";

    private final Runnable tick = new Runnable() {
        @Override public void run() { render(); ui.postDelayed(this, 2_000); }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Only Dexicon may ask. getCallingPackage() is set only for startActivityForResult, which the protocol uses.
        String caller = getCallingPackage();
        if (!Handover.DEXICON.equals(caller)) { refuse("caller is not " + Handover.DEXICON); return; }
        // ...and only the real Dexicon: both apps are signed with the Minima family release key (debug builds: the
        // same machine's debug key), so a look-alike package with another key is refused.
        if (getPackageManager().checkSignatures(caller, getPackageName()) != PackageManager.SIGNATURE_MATCH) {
            refuse(caller + " is not signed with AtomiX's key"); return;
        }
        prefs = getSharedPreferences("atomix", MODE_PRIVATE);
        TradingContext.load(prefs);
        Design.load(this);
        Handover.load(prefs);
        db = new SwapDb(this);
        otcDb = new OtcDb(this);
        ScrollView scroller = new ScrollView(this);
        scroller.setBackgroundColor(Design.BG());
        col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(col);
        setContentView(scroller);
        ViewCompat.setOnApplyWindowInsetsListener(scroller, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
        setResult(RESULT_CANCELED, new Intent().putExtra("reason", "cancelled"));   // back = refusal
        // Dexicon does not store the OK: it asks again before every uninstall. Already handed over and every check
        // (all synchronous: DB, prefs, the engine's static busy flag) still passing → answer OK at once, no tap.
        // Otherwise the screen shows as usual, armed, so it finishes on its own once the checks pass.
        armed = Handover.done(prefs);
        if (armed) complete();
    }

    @Override protected void onResume() { super.onResume(); if (col != null) ui.post(tick); }
    @Override protected void onPause() { super.onPause(); ui.removeCallbacks(tick); }

    private void refuse(String reason) {
        setResult(RESULT_CANCELED, new Intent().putExtra("reason", reason));
        finish();
    }

    private List<String> swapBlockers() {
        return Handover.inFlight(db, otcDb.allDeals(), com.eurobuddha.atomix.swap.SwapEngine.initiatorStarting());
    }

    private void render() {
        // "Take back" (main screen) while we wait un-quiesces AtomiX and clears the pending-post flags — the armed
        // withdrawal is void, so the user must press it again rather than this screen answering OK unposted.
        if (armed && !Handover.active()) armed = false;
        List<String> swaps = swapBlockers();
        List<String> market = Handover.marketLive(prefs);
        if (armed && swaps.isEmpty() && market.isEmpty() && complete()) return;
        // Keep the CANCELED reason current so a back press tells Dexicon exactly what is still blocking.
        String reason = !swaps.isEmpty() ? swaps.get(0) : !market.isEmpty() ? market.get(0) : "not confirmed by the user";
        setResult(RESULT_CANCELED, new Intent().putExtra("reason", reason));
        List<SwapDb.Swap> stuck = Handover.needRecovery(db, System.currentTimeMillis());
        StringBuilder stuckSig = new StringBuilder();
        for (SwapDb.Swap s : stuck) stuckSig.append(s.hash).append(',');
        String sig = armed + "|" + swaps + "|" + market + "|" + stuckSig + "|" + recHash + "|" + recNote;
        if (sig.equals(shown)) return;
        shown = sig;

        col.removeAllViews();
        int p = Design.dp(this, 16);
        col.setPadding(p, p, p, p);
        text("Hand over to Dexicon", 20f, Design.TEXT(), true);
        text("Dexicon contains AtomiX. With both on one phone you would run two makers on one wallet, so Dexicon "
                + "will not work while AtomiX is installed.\n\n"
                + "This step withdraws your market and OTC offer and stops new swaps here. Swaps already in flight "
                + "keep settling in the background — claims, refunds and ETH withdraws are never stopped.\n\n"
                + "When every check below is ✓, Dexicon lets you uninstall AtomiX.", 14f, Design.DIM(), false);

        text("Checks", 16f, Design.TEXT(), true);
        check(swaps.isEmpty(), "No swap or OTC deal in flight (USDT⇄MxUSD and USDT⇄MINIMA)", swaps);
        check(market.isEmpty(), "Market and OTC offer withdrawn", market);

        if (!stuck.isEmpty() || !recNote.isEmpty()) {
            text("Swaps AtomiX cannot clear on its own", 16f, Design.TEXT(), true);
            text("AtomiX has tried to refund these Minima locks well past their timelock, and each lock is gone or"
                    + " was never seen (never sent, or already claimed by the counterparty). Save each one's recovery"
                    + " record — the hash, secret, contract id, lock coin id, both keys, timelock, market, role and"
                    + " amounts, all in full — then pick the saved file again so AtomiX can check it. A verified record"
                    + " stops the swap blocking the handover.",
                    13f, Design.DIM(), false);
            text(Handover.SECRET_WARNING, 13f, Design.TEXT(), true);
            if (!recNote.isEmpty()) {
                TextView n = text(recNote, 12f, Design.DIM(), false);
                n.setTypeface(Design.mono());
                n.setTextIsSelectable(true);
            }
            for (SwapDb.Swap s : stuck) {
                TextView h = text(s.hash, 12f, Design.DIM(), false);
                h.setTypeface(Design.mono());
                h.setTextIsSelectable(true);
                boolean verifying = s.hash.equals(recHash) && recBytes != null;
                TextView b = Design.pill(this, verifying ? "Pick the saved file to verify it" : "Save this swap's recovery record",
                        Design.SURFACE2(), Design.TEXT());
                Design.pressable(b);
                b.setOnClickListener(v -> { if (verifying) pickToVerify(); else saveRecord(s); });
                add(b);
            }
        }

        if (armed) {
            text("Withdrawn. Waiting for the checks above — this screen finishes on its own when they pass.", 13f, Design.DIM(), false);
        } else {
            TextView go = Design.pill(this, "Withdraw market and hand over", Design.ACCENT(), Design.ON_ACCENT());
            Design.pressable(go);
            go.setOnClickListener(v -> withdraw());
            add(go);
        }
        TextView cancel = Design.pill(this, "Cancel", Design.SURFACE2(), Design.TEXT());
        Design.pressable(cancel);
        cancel.setOnClickListener(v -> finish());   // result already holds the current blocking reason
        add(cancel);
    }

    private void saveRecord(SwapDb.Swap s) {
        try {
            recBytes = Handover.recoveryRecord(s, db).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            recHash = s.hash;
        } catch (Exception e) { recNote = "Could not build the recovery record: " + e.getMessage(); render(); return; }
        try {
            startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("application/json").putExtra(Intent.EXTRA_TITLE, "atomix-recovery-" + s.hash + ".json"), REQ_SAVE);
        } catch (Exception e) { recBytes = null; recNote = "No file picker available: " + e.getMessage(); render(); }
    }

    private void pickToVerify() {
        try {
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*"), REQ_VERIFY);
        } catch (Exception e) { recNote = "No file picker available: " + e.getMessage(); render(); }
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if ((req != REQ_SAVE && req != REQ_VERIFY) || recBytes == null || recHash == null) return;
        android.net.Uri uri = res == RESULT_OK && data != null ? data.getData() : null;
        if (uri == null) {
            if (req == REQ_SAVE) { recBytes = null; recHash = null; }   // nothing was written — there is nothing to verify
            recNote = req == REQ_SAVE ? "Not saved — the swap still blocks the handover."
                    : "Not verified — pick the saved file to verify it. The swap still blocks the handover.";
            shown = null; render(); return;
        }
        String sha = Handover.sha256(recBytes);
        if (req == REQ_SAVE) {
            try (java.io.OutputStream os = getContentResolver().openOutputStream(uri, "w")) {
                if (os == null) throw new java.io.IOException("no output stream");
                os.write(recBytes);
                recNote = "Saved " + recBytes.length + " bytes, SHA-256 " + sha + ". Now pick that file again to verify it.";
            } catch (Exception e) {
                recBytes = null; recHash = null;
                recNote = "Could not write the file (" + e.getMessage() + ") — the swap still blocks the handover.";
            }
        } else {
            String problem;
            try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) throw new java.io.IOException("no input stream");
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) != -1) { bos.write(buf, 0, n); if (bos.size() > 1 << 20) break; }   // a record is ~2 KB
                problem = Handover.verifyRecoveryFile(bos.toByteArray(), recBytes.length, sha, recHash);
            } catch (Exception e) { problem = "the file could not be read back (" + e.getMessage() + ")"; }
            if (problem == null) {
                db.logEvent(recHash, SwapDb.EV_RECOVERY_SAVED, "", "", recBytes.length + " bytes, SHA-256 " + sha);
                recNote = "Verified: the recovery record for " + recHash + " (" + recBytes.length + " bytes, SHA-256 " + sha
                        + ") is saved. You now hold what is needed to recover this swap by hand: an unspent Minima lock"
                        + " refunds with your node's key after its timelock, the claim branch has no deadline, and the"
                        + " preimage of a withdrawn Ethereum contract is readable from that contract. It no longer blocks.";
                recBytes = null; recHash = null;
            } else {
                recNote = "Not verified: " + problem + ". Save it again — the swap still blocks the handover.";
                recBytes = null;
            }
        }
        shown = null;
        render();
    }

    private void withdraw() {
        Handover.withdraw(prefs);
        if (prefs.getBoolean(Handover.P_TOMB_PENDING, false) || prefs.getBoolean(Handover.P_OTC_PENDING, false)) {
            try {
                androidx.core.content.ContextCompat.startForegroundService(this,
                        new Intent(this, SwapService.class).setAction(SwapService.ACTION_HANDOVER));
            } catch (Exception e) { SwapLog.w("handover: service start failed: " + e.getClass().getSimpleName()); }
        }
        armed = true;
        render();
    }

    /** Answer OK — only if, under Handover.LOCK, AtomiX is still quiesced and every check still passes, so no
     *  initiator secret can be written between this re-check and the OK (see SwapEngine.prepareUnlessHandedOver). */
    private boolean complete() {
        long ts = System.currentTimeMillis();
        synchronized (Handover.LOCK) {
            if (!Handover.active() || !swapBlockers().isEmpty() || !Handover.marketLive(prefs).isEmpty()) return false;
            Handover.setDone(prefs, ts);
        }
        ui.removeCallbacks(tick);
        setResult(RESULT_OK, new Intent()
                .putExtra("handover", true)
                .putExtra("app", "com.eurobuddha.atomix")
                .putExtra("version", BuildConfig.VERSION_CODE)
                .putExtra("ts", ts));
        finish();
        return true;
    }

    private void check(boolean ok, String label, List<String> reasons) {
        text((ok ? "✓  " : "✗  ") + label, 14f, ok ? Design.POSITIVE() : Design.RED(), true);
        for (String r : reasons) {
            TextView t = text(r, 12f, Design.DIM(), false);
            t.setTypeface(Design.mono());
            t.setTextIsSelectable(true);   // identifiers are shown in full and copyable
        }
    }

    private TextView text(String s, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(bold ? Design.sansBold() : Design.sans());
        add(t);
        return t;
    }

    private void add(android.view.View v) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Design.dp(this, 10);
        col.addView(v, lp);
    }
}
