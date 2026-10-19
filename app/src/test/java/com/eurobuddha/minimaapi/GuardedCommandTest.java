package com.eurobuddha.minimaapi;

import android.app.Application;
import android.content.Intent;
import android.os.Looper;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

/** NodeRoutingTest's synthetic registration/reply protocol with the actual paused Android main queue. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=33) @LooperMode(LooperMode.Mode.PAUSED)
public class GuardedCommandTest {
    Application app; MinimaAPI api; List<JSONObject> replies;
    @Before public void setup() {
        app=RuntimeEnvironment.getApplication();replies=new ArrayList<>();
        api=new MinimaAPI(app,null);
    }
    @After public void cleanup(){api.onDestroy();idle();}
    void idle(){Shadows.shadowOf(Looper.getMainLooper()).idle();}
    List<Intent> broadcasts(){return Shadows.shadowOf(app).getBroadcastIntents();}
    List<Intent> commands(){List<Intent> out=new ArrayList<>();for(Intent i:broadcasts())if(MinimaAPIMessages.MINIMA_API_CMD.equals(i.getAction()))out.add(i);return out;}
    void reply(Intent request,String body) {
        Intent reply=new Intent(MinimaAPIMessages.MINIMA_API_RESPONSE)
            .putExtra(MinimaAPIMessages.MINIMA_API_REGISTER_MINIMAID,request.getStringExtra(MinimaAPIMessages.MINIMA_API_REGISTER_MINIMAID))
            .putExtra(MinimaAPIMessages.MINIMA_API_RESPONSE_ID,request.getStringExtra(MinimaAPIMessages.MINIMA_API_RESPONSE_ID))
            .putExtra(MinimaAPIMessages.MINIMA_API_RESPONSE_RESULT,body);
        api.ResponseReceived(reply);idle();
    }
    void discover() {
        for(Intent request:new ArrayList<>(broadcasts()))if(MinimaAPIMessages.MINIMA_API_REGISTER.equals(request.getAction())&&MinimaAPI.PANDAMONIUM_PACKAGE.equals(request.getPackage()))reply(request,"{\"status\":true,\"enabled\":true}");
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1500));
    }
    void assertCancelled(){assertTrue(commands().isEmpty());assertEquals(1,replies.size());assertFalse(replies.get(0).has("status"));assertTrue(replies.get(0).optString("transporterror").contains("No command was sent"));}
    @Test public void revocationWhileOnMainQueueCannotBroadcast(){
        discover();AtomicBoolean allowed=new AtomicBoolean(true);
        api.Command("send amount:1",allowed::get,replies::add);allowed.set(false);idle();assertCancelled();
    }
    @Test public void revocationWhileDiscoveringCannotBroadcast(){
        AtomicBoolean allowed=new AtomicBoolean(true);api.Command("send amount:1",allowed::get,replies::add);idle();
        assertTrue(commands().isEmpty());assertTrue(replies.isEmpty());allowed.set(false);discover();assertCancelled();
    }
    @Test public void authorizedCommandIsSentOnceToTheDiscoveredNode(){
        api.Command("balance",()->true,replies::add);idle();assertTrue(commands().isEmpty());discover();
        assertEquals(1,commands().size());assertEquals(MinimaAPI.PANDAMONIUM_PACKAGE,commands().get(0).getPackage());
        assertEquals("balance",commands().get(0).getStringExtra(MinimaAPIMessages.MINIMA_API_CMD_ACTION));
    }
    @Test public void revocationAfterDispatchDoesNotDiscardARealReceipt(){
        discover();AtomicBoolean allowed=new AtomicBoolean(true);api.Command("send amount:1",allowed::get,replies::add);idle();
        Intent sent=commands().get(0);allowed.set(false);
        String body="{\"status\":true,\"response\":{\"txpowid\":\"0x"+"12".repeat(32)+"\"}}";
        reply(sent,body);reply(sent,body);assertEquals(1,replies.size());assertEquals(Boolean.TRUE,replies.get(0).opt("status"));
    }
    @Test public void nullAndThrowingGuardsCannotBroadcast(){
        discover();for(BooleanSupplier guard:Arrays.<BooleanSupplier>asList(null,()->{throw new IllegalStateException("fixture");})){
            replies.clear();api.Command("send amount:1",guard,replies::add);idle();assertCancelled();
        }
    }
    @Test public void destroyedOwnerCannotDispatchQueuedCommand(){
        discover();api.Command("send amount:1",()->true,replies::add);api.onDestroy();idle();assertTrue(commands().isEmpty());
    }
    @Test public void failedPairingWithNoRegistrationListenerDoesNotCrash(){
        api.onDestroy();app.getSharedPreferences("minima_api_prefs",0).edit().putString("myapp_uid","broken").commit();
        int count=broadcasts().size();api=new MinimaAPI(app,null);assertEquals(count,broadcasts().size());
    }
}
