package com.eurobuddha.comms;

import android.content.SharedPreferences;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WriteSafetyTest {
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;
    private final Map<String,String> persisted = new HashMap<>();
    @Before public void setup() {
        prefs = mock(SharedPreferences.class); edit = mock(SharedPreferences.Editor.class);
        when(prefs.edit()).thenReturn(edit);
        when(prefs.getString(anyString(), anyString())).thenAnswer(i -> persisted.getOrDefault(i.getArgument(0), i.getArgument(1)));
        when(edit.putString(anyString(), anyString())).thenAnswer(i -> { persisted.put(i.getArgument(0), i.getArgument(1)); return edit; });
        when(edit.remove(anyString())).thenAnswer(i -> { persisted.remove(i.getArgument(0)); return edit; });
        when(edit.commit()).thenReturn(true);
    }
    @Test public void timeoutNeverAuthorizesASecondWrite() {
        assertTrue(WriteSafety.begin(prefs, "a"));
        WriteSafety.uncertain("a");
        assertTrue(WriteSafety.interrupted(prefs));
        assertFalse(WriteSafety.begin(prefs, "b"));
        assertEquals("a", persisted.get("pending"));
    }
    @Test public void completeLateReplyAllowsRecoveryButCannotClearNewerWrite() {
        assertTrue(WriteSafety.begin(prefs, "a")); WriteSafety.uncertain("a");
        WriteSafety.returned(prefs, "a", true); assertFalse(WriteSafety.interrupted(prefs));
        assertTrue(WriteSafety.begin(prefs, "b"));
        WriteSafety.returned(prefs, "a", true); assertEquals("b", persisted.get("pending"));
        assertFalse(WriteSafety.acknowledge(prefs));
        WriteSafety.returned(prefs, "b", true);
    }
    @Test public void unknownReplyRetainsPauseUntilExplicitAcknowledgement() {
        assertTrue(WriteSafety.begin(prefs, "a"));
        WriteSafety.returned(prefs, "a", false);
        assertTrue(WriteSafety.interrupted(prefs));
        assertFalse(WriteSafety.begin(prefs, "b"));
        assertTrue(WriteSafety.acknowledge(prefs));
        assertTrue(WriteSafety.begin(prefs, "b")); WriteSafety.returned(prefs, "b", true);
    }
    @Test public void restartedProcessHonorsPersistedMarker() {
        persisted.put("pending", "previous-process");
        assertFalse(WriteSafety.begin(prefs, "new-process"));
    }
    @Test public void failedPersistenceNeverAuthorizesWrite() {
        when(edit.commit()).thenReturn(false);
        assertFalse(WriteSafety.begin(prefs, "a"));
    }
    @Test public void allFundCommandsAreClassifiedAndReadsRemainAvailable() {
        for (String s : new String[]{"sign publickey:0xaa", "txnsign id:x", "txnpost id:x", "send amount:1", "consolidate", "tokencreate"}) assertTrue(s, WriteSafety.writesFunds(s));
        for (String s : new String[]{"coins", "balance", "txncheck id:x", "keys", "txnbasics id:x"}) assertFalse(s, WriteSafety.writesFunds(s));
    }
    @Test public void incompleteRepliesAreNotAcknowledgements() throws Exception {
        assertFalse(WriteSafety.completeReply(null));
        assertFalse(WriteSafety.completeReply(new JSONObject().put("response", "unknown")));
        assertFalse(WriteSafety.completeReply(new JSONObject().put("status", "true")));
        assertTrue(WriteSafety.completeReply(new JSONObject().put("status", false)));
    }
}
