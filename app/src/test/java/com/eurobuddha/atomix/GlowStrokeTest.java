package com.eurobuddha.atomix;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * The currency pill's stroke is what marks it as a control rather than a status badge, so its alpha
 * ramp has to hold up at BOTH ends: visible at rest (the affordance cannot depend on the glow, which
 * is off whenever the system disables animations) and not blinding at peak (ACCENT is a strong
 * orange or green and this sits in the header next to the brand).
 */
public class GlowStrokeTest {

    private static final int ORANGE = 0xFFF7931A;   // TradingContext.MINIMA.accent
    private static final int GREEN  = 0xFF26A17B;   // TradingContext.MXUSDT.accent

    private static int alphaOf(int argb) { return (argb >>> 24) & 0xFF; }
    private static int rgbOf(int argb)   { return argb & 0x00FFFFFF; }

    @Test public void theRestingStrokeIsVisibleWithoutAnyAnimation() {
        int rest = Design.strokeAlpha(ORANGE, 0f);
        assertEquals("a control that is invisible until it animates is invisible with motion off",
                0x66, alphaOf(rest));
        assertTrue(alphaOf(rest) > 0x33);
    }

    @Test public void thePeakIsFullyOpaqueButNeverOverflows() {
        assertEquals(0xFF, alphaOf(Design.strokeAlpha(ORANGE, 1f)));
        assertEquals("lift above 1 must clamp, not wrap the alpha byte",
                0xFF, alphaOf(Design.strokeAlpha(ORANGE, 4f)));
    }

    @Test public void negativeLiftClampsToRest() {
        assertEquals(0x66, alphaOf(Design.strokeAlpha(ORANGE, -1f)));
    }

    @Test public void theAccentHueIsCarriedThroughUntouched() {
        // The stroke must re-tint with the currency for free - Design.ACCENT() is a function of the
        // active TradingContext, so only the alpha may ever be rewritten.
        assertEquals(rgbOf(ORANGE), rgbOf(Design.strokeAlpha(ORANGE, 0f)));
        assertEquals(rgbOf(ORANGE), rgbOf(Design.strokeAlpha(ORANGE, 1f)));
        assertEquals(rgbOf(GREEN),  rgbOf(Design.strokeAlpha(GREEN, 0.5f)));
    }

    @Test public void theRampIsMonotonic() {
        int prev = -1;
        for (float f = 0f; f <= 1.0001f; f += 0.1f) {
            int a = alphaOf(Design.strokeAlpha(GREEN, f));
            assertTrue("alpha must not go backwards at lift " + f, a >= prev);
            prev = a;
        }
    }

    @Test public void offIsTheOnlyLevelThatDoesNotBreathe() {
        assertEquals(0f, Design.Glow.OFF.peak, 0.0001f);
        for (Design.Glow g : new Design.Glow[]{Design.Glow.SUBTLE, Design.Glow.MEDIUM, Design.Glow.STRONG}) {
            assertTrue(g.name(), g.peak > 0f);
            assertTrue("a period of 0 would divide to a zero-length animator", g.periodMs > 0);
        }
    }

    @Test public void cyclingVisitsEveryLevelAndReturns() {
        Design.Glow g = Design.Glow.OFF;
        for (int i = 0; i < Design.Glow.values().length; i++) g = g.next();
        assertEquals("long-press must cycle, not dead-end", Design.Glow.OFF, g);
    }
}
