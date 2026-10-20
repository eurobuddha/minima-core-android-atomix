package com.eurobuddha.atomix;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
import com.eurobuddha.atomix.swap.*;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Reuses the existing standalone edit fixture and exercises real native dialog buttons. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class StandaloneMarketInputTest {
    private final OrderEditIdentityTest fixture=new OrderEditIdentityTest();
    private MockedStatic<PriceOracle> oracle;
    private SharedPreferences prefs;
    @Before public void setup() throws Exception {
        fixture.setup();oracle=mockStatic(PriceOracle.class);
        prefs=(SharedPreferences)fixture.field("prefs").get(fixture.activity);
        prefs.edit().clear().putString("order_config",Order.toConfigJson(fixture.savedOrder(false))).commit();
    }
    @After public void cleanup() {
        fixture.activity.finish();AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();
        if(dialog!=null)dialog.dismiss();oracle.close();fixture.cleanup();
    }
    private Object call(String name,Class<?>[] types,Object...args) throws Exception {
        Method method=MainActivity.class.getDeclaredMethod(name,types);method.setAccessible(true);
        return method.invoke(fixture.activity,args);
    }
    private AlertDialog open(String method) throws Exception {
        call(method,new Class<?>[0]);return ShadowAlertDialog.getLatestAlertDialog();
    }
    private static List<EditText> fields(View view) {
        List<EditText> result=new ArrayList<>();if(view instanceof EditText)result.add((EditText)view);
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)result.addAll(fields(((ViewGroup)view).getChildAt(i)));
        return result;
    }
    private static EditText hint(AlertDialog dialog,String hint) {
        for(EditText e:fields(dialog.getWindow().getDecorView()))if(hint.contentEquals(e.getHint()==null?"":e.getHint()))return e;
        throw new AssertionError("Missing field: "+hint);
    }
    @Test public void invalidSaveKeepsDialogAndStoredMarketThenValidCorrectionSaves() throws Exception {
        Map<String,?> before=new HashMap<>(prefs.getAll());AlertDialog dialog=open("editOrderDialog");
        hint(dialog,"reprice ≥ %").setText("9".repeat(340));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertTrue(dialog.isShowing());assertEquals(before,prefs.getAll());verifyNoInteractions(fixture.engine);
        hint(dialog,"reprice ≥ %").setText("1.5");fixture.activity.finish();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());assertEquals("1.5",prefs.getString(PriceOracle.P_REPRICE,""));
        verify(fixture.engine).setMyOrder(org.mockito.ArgumentMatchers.any());
    }
    @Test public void overflowingAutofillDoesNotReplaceHandEditedRows() throws Exception {
        AlertDialog dialog=open("editOrderDialog");List<EditText> all=fields(dialog.getWindow().getDecorView());
        // Seven generator fields precede the first visible ask price and amount.
        EditText price=all.get(7),amount=all.get(8);price.setText("2");amount.setText("3");
        hint(dialog,"mid").setText("9".repeat(340));hint(dialog,"step %").setText("1");hint(dialog,"ask size").setText("4");
        assertEquals("2",price.getText().toString());assertEquals("3",amount.getText().toString());
    }
    @Test public void invalidSlippageKeepsDialogAndPreferenceThenCorrectionSaves() throws Exception {
        prefs.edit().putFloat("buy_slippage_pct",4.2f).commit();AlertDialog dialog=open("customSlippageDialog");
        hint(dialog,"%").setText(".");dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertTrue(dialog.isShowing());assertEquals(4.2f,prefs.getFloat("buy_slippage_pct",0),0);
        hint(dialog,"%").setText("2.5");fixture.activity.finish();dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());assertEquals(2.5f,prefs.getFloat("buy_slippage_pct",0),0);
    }
    @Test public void quotesRejectInvalidOrSubgrainAmountsAndKeepOrdinaryGrain() throws Exception {
        for(String method:new String[]{"computeUsdt","computeMinima"}) {
            for(String raw:new String[]{".","1e3","0.0000001","9".repeat(340)})
                assertNull(call(method,new Class<?>[]{String.class,double.class},raw,1.0));
            assertNull(call(method,new Class<?>[]{String.class,double.class},"1",Double.POSITIVE_INFINITY));
            assertEquals("1.234567",call(method,new Class<?>[]{String.class,double.class},"1.2345678",1.0));
        }
    }
}
