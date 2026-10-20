package com.eurobuddha.atomix;

import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
import com.eurobuddha.atomix.swap.*;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Actual OTC input cards, reusing the existing standalone edit fixture. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class StandaloneOtcInputTest {
    private final OrderEditIdentityTest fixture=new OrderEditIdentityTest();
    private SharedPreferences prefs;
    @Before public void setup() throws Exception {
        fixture.setup();prefs=(SharedPreferences)fixture.field("prefs").get(fixture.activity);
        prefs.edit().clear().putString("otc_sell_size","7").putString("otc_buy_size","8").commit();
        // Even an incorrect old UI handler cannot publish a transaction in this fixture.
        fixture.set("identity",null);
    }
    @After public void cleanup(){fixture.activity.finish();fixture.cleanup();}
    private Object call(String name,Class<?>[] types,Object...args) throws Exception {
        Method method=MainActivity.class.getDeclaredMethod(name,types);method.setAccessible(true);
        return method.invoke(fixture.activity,args);
    }
    private LinearLayout card() throws Exception {
        LinearLayout col=new LinearLayout(fixture.activity);
        call("renderOtcAvailability",new Class<?>[]{LinearLayout.class},col);return col;
    }
    private static List<TextView> texts(View view) {
        List<TextView> result=new ArrayList<>();if(view instanceof TextView)result.add((TextView)view);
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)result.addAll(texts(((ViewGroup)view).getChildAt(i)));
        return result;
    }
    private static List<EditText> inputs(View view) {
        List<EditText> result=new ArrayList<>();for(TextView t:texts(view))if(t instanceof EditText)result.add((EditText)t);return result;
    }
    @Test public void malformedAvailabilityCannotSaveOrDisableOneSide() throws Exception {
        for(String bad:new String[]{".","9".repeat(340)}) {
            LinearLayout card=card();List<EditText> fields=inputs(card);fields.get(0).setText(bad);fields.get(1).setText("1");
            Map<String,?> before=new HashMap<>(prefs.getAll());
            for(TextView button:texts(card))if("Go live".contentEquals(button.getText()))button.performClick();
            assertEquals(before,prefs.getAll());assertEquals(bad,fields.get(0).getText().toString());
        }
    }
    @Test public void unsavedAvailabilitySurvivesCardRefreshWithoutPublishing() throws Exception {
        LinearLayout card=card();List<EditText> fields=inputs(card);fields.get(0).setText("3.25");fields.get(1).setText("4.5");
        List<EditText> refreshed=inputs(card());assertEquals("3.25",refreshed.get(0).getText().toString());
        assertEquals("4.5",refreshed.get(1).getText().toString());
        assertEquals("7",prefs.getString("otc_sell_size",""));assertFalse(prefs.getBoolean("otc_enable",false));
    }
    @Test public void badTermsCannotPassFundsReviewWhenBalanceIsUnknown() throws Exception {
        fixture.set("minimaBal","unknown");
        for(String amount:new String[]{"NaN","Infinity","0.0000001","0","."})
            assertNotNull(call("otcCheckFunds",new Class<?>[]{String.class,String.class,String.class,String.class},
                    OtcDb.ROLE_INSTIGATOR,OtcOffer.LP_BUYS_MINIMA,amount,"1"));
        assertNull(call("otcCheckFunds",new Class<?>[]{String.class,String.class,String.class,String.class},
                OtcDb.ROLE_INSTIGATOR,OtcOffer.LP_BUYS_MINIMA,"2","3"));
    }
}
