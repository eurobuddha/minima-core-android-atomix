package com.eurobuddha.atomix;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private final Runnable tick = new Runnable() {
        @Override public void run() { render(); ui.postDelayed(this, 2_000); }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Only Dexicon may ask. getCallingPackage() is set only for startActivityForResult, which the protocol uses.
        if (!Handover.DEXICON.equals(getCallingPackage())) { refuse("caller is not " + Handover.DEXICON); return; }
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
        String sig = armed + "|" + swaps + "|" + market;
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
