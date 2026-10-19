package com.eurobuddha.atomix;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.eurobuddha.atomix.swap.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Actual OTC cards, reusing OrderEditIdentityTest's unstarted activity fixture. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class OtcMarketUiTest {
    MainActivity activity;TradingContext saved;OtcDb db;OtcController controller;
    Field field(String name)throws Exception {Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);return f;}
    @Before public void setup()throws Exception {
        saved=TradingContext.active();activity=Robolectric.buildActivity(MainActivity.class).get();
        activity.setTheme(R.style.Theme_Utxo);((ExecutorService)field("io").get(activity)).shutdownNow();
        db=mock(OtcDb.class);controller=mock(OtcController.class);field("otcDb").set(activity,db);field("otc").set(activity,controller);
    }
    @After public void cleanup(){TradingContext.setActive(saved,null);}
    OtcDb.Deal deal(String currency) {
        OtcDb.Deal d=new OtcDb.Deal();d.currency=currency;d.role=OtcDb.ROLE_INSTIGATOR;d.side=OtcOffer.LP_SELLS_MINIMA;
        d.amount="2";d.price="3";d.status=OtcDb.ST_PROPOSED;d.whoseTurn=OtcDb.TURN_ME;return d;
    }
    List<String> texts(OtcDb.Deal d)throws Exception {
        when(db.allDeals()).thenReturn(Collections.singletonList(d));LinearLayout col=new LinearLayout(activity);
        Method render=MainActivity.class.getDeclaredMethod("renderOtcDeals",LinearLayout.class);render.setAccessible(true);render.invoke(activity,col);
        List<String> text=new ArrayList<>();collect(col,text);return text;
    }
    static void collect(View view,List<String> text) {
        if(view instanceof TextView)text.add(((TextView)view).getText().toString());
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)collect(group.getChildAt(i),text);}
    }
    static void noActions(List<String> text) {for(String action:new String[]{"Accept","Counter","Reject"})assertFalse(text.toString(),text.contains(action));}
    @Test public void matchingMarketRetainsAllNegotiationActions()throws Exception {
        for(TradingContext market:TradingContext.values()) {
            TradingContext.setActive(market,null);List<String> text=texts(deal(market.key));
            assertTrue(text.contains("BUY 2 "+market.coinLabel+" @ 3 USDT"));
            for(String action:new String[]{"Accept","Counter","Reject"})assertTrue(text.contains(action));
        }
        verifyNoInteractions(controller);
    }
    @Test public void foreignDealShowsItsOriginalCurrencyAndSwitchHintWithoutActions()throws Exception {
        for(TradingContext market:TradingContext.values()) {
            TradingContext.setActive(market.other(),null);List<String> text=texts(deal(market.key));
            assertTrue(text.contains("BUY 2 "+market.coinLabel+" @ 3 USDT"));
            assertTrue(text.contains("Switch to "+market.coinLabel+" to manage this deal."));noActions(text);
        }
        verifyNoInteractions(controller);
    }
    @Test public void legacyUnattributedDealsStayVisibleWithoutGuessingCurrency()throws Exception {
        for(String currency:new String[]{null,"","future-market"}) {
            List<String> text=texts(deal(currency));assertTrue(text.contains("BUY 2 Unknown currency @ 3 USDT"));
            assertTrue(text.contains("The currency for this deal is not recognised. Trading actions are disabled."));noActions(text);
        }
        verifyNoInteractions(controller);
    }
    @Test public void staleCounterActionCannotOpenAnotherMarketsEditor()throws Exception {
        TradingContext.setActive(TradingContext.MINIMA,null);
        Method counter=MainActivity.class.getDeclaredMethod("otcCounterDialog",OtcDb.Deal.class);counter.setAccessible(true);
        counter.invoke(activity,deal(TradingContext.MXUSDT.key));
        assertEquals(OtcController.MARKET_ERROR,ShadowToast.getTextOfLatestToast());assertFalse(field("modalOpen").getBoolean(activity));
        verifyNoInteractions(controller,db);
    }
}
