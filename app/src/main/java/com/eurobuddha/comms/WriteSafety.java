package com.eurobuddha.comms;

import android.content.SharedPreferences;
import org.json.JSONObject;

/** PandaPools NodeApi's persisted interrupted-write protocol, shared by all AtomiX SDK instances. */
final class WriteSafety {
    private static String active = "";
    private WriteSafety() {}

    static boolean writesFunds(String command) {
        String verb = command == null ? "" : command.trim().split("\\s+", 2)[0];
        return verb.equals("sign") || verb.equals("txnsign") || verb.equals("txnpost") || verb.equals("send")
                || verb.equals("consolidate") || verb.equals("tokencreate");
    }
    static boolean completeReply(JSONObject reply) {
        if (reply == null || Boolean.TRUE.equals(reply.opt("pending"))) return false;
        Object response = reply.opt("response");
        // PandaPools NodeApi.isTooLong: the node may replace a write result with an over-limit stub.
        if (Boolean.FALSE.equals(reply.opt("status")) && response instanceof String) {
            String text = ((String) response).toLowerCase(java.util.Locale.ROOT);
            if (text.contains("too long") || text.contains("max(256000)")) return false;
        }
        return reply.opt("status") instanceof Boolean || Boolean.FALSE.equals(reply.opt("enabled"));
    }
    static synchronized boolean begin(SharedPreferences prefs, String id) {
        if (!prefs.getString("pending", "").isEmpty()) return false;
        if (!prefs.edit().putString("pending", id).commit()) return false;
        active = id;
        return true;
    }
    static synchronized void uncertain(String id) { if (id.equals(active)) active = ""; }
    static synchronized void returned(SharedPreferences prefs, String id, boolean complete) {
        if (id.equals(active)) active = "";
        if (complete && id.equals(prefs.getString("pending", ""))) prefs.edit().remove("pending").commit();
    }
    static synchronized void callbackFailed(SharedPreferences prefs, String id) {
        if (prefs.getString("pending", "").isEmpty()) prefs.edit().putString("pending", id).commit();
    }
    static synchronized boolean interrupted(SharedPreferences prefs) {
        return active.isEmpty() && !prefs.getString("pending", "").isEmpty();
    }
    static synchronized boolean acknowledge(SharedPreferences prefs) {
        return active.isEmpty() && prefs.edit().remove("pending").commit();
    }
}
