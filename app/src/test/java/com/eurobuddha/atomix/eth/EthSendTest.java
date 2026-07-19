package com.eurobuddha.atomix.eth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.math.BigInteger;

/**
 * STEP: the manual wallet Send (parity twin of the MDS build's lib/wallet.js — user decision 2026-07-19).
 * Fund-relevant: validation runs on RAW balances, the gas reserve mirrors EthTx's +20% headroom, and the
 * ERC20 calldata must be an exact transfer(address,uint256) encoding. Mirrors the MDS wallet.test.js cases
 * so BOTH apps enforce identical refusal rules.
 */
public class EthSendTest {

    private static final String TO = "0x2222222222222222222222222222222222222222";
    private static final BigInteger GP = BigInteger.valueOf(1_000_000_000L);          // 1 gwei
    private static final BigInteger ETH_1 = new BigInteger("1000000000000000000");    // 1 ETH
    private static final BigInteger USDT_50 = BigInteger.valueOf(50_000_000L);        // 50 USDT (6dp)

    @Test public void addressValidation() {
        assertTrue(EthSend.isEthAddr(TO));
        assertFalse(EthSend.isEthAddr("0x1234"));
        assertFalse(EthSend.isEthAddr("0x" + "zz".repeat(20)));
        assertFalse(EthSend.isEthAddr(null));
    }

    @Test public void gasMathMirrorsTheSerializerHeadroom() {
        assertEquals(BigInteger.valueOf(21000L * 1_200_000_000L), EthSend.gasReserveWei(GP, EthSend.GAS_ETH));
        assertEquals(new BigInteger("100000000000000000").subtract(BigInteger.valueOf(21000L * 1_200_000_000L)),
                EthSend.maxEthSendWei(new BigInteger("100000000000000000"), GP));
        assertEquals(BigInteger.ZERO, EthSend.maxEthSendWei(BigInteger.valueOf(1000), GP));
    }

    @Test public void checkSendRefusalPaths() {
        assertNull(EthSend.checkSend(true, TO, "0.5", ETH_1, USDT_50, 6, GP));
        assertNotNull(EthSend.checkSend(true, "nope", "0.5", ETH_1, USDT_50, 6, GP));      // bad address
        assertNotNull(EthSend.checkSend(true, TO, "1e2", ETH_1, USDT_50, 6, GP));          // not plain decimal
        assertNotNull(EthSend.checkSend(true, TO, "0", ETH_1, USDT_50, 6, GP));            // zero
        assertNotNull(EthSend.checkSend(true, TO, "1", ETH_1, USDT_50, 6, GP));            // amount+gas > balance
        assertNull(EthSend.checkSend(false, TO, "25", ETH_1, USDT_50, 6, GP));
        assertNotNull(EthSend.checkSend(false, TO, "50.000001", ETH_1, USDT_50, 6, GP));   // over USDT balance
        assertNotNull(EthSend.checkSend(false, TO, "25", BigInteger.ZERO, USDT_50, 6, GP)); // no gas ETH
    }

    @Test public void parseUnitsTruncatesNeverRoundsUp() {
        assertEquals(new BigInteger("250000000000000000"), EthSend.parseUnits("0.25", 18));
        assertEquals(BigInteger.valueOf(12_500_000), EthSend.parseUnits("12.5", 6));
        assertEquals(BigInteger.valueOf(1_999_999), EthSend.parseUnits("1.9999999", 6));   // 7th dp truncated
    }

    @Test public void transferCalldataIsExact() {
        String data = EthSend.buildTransferData(TO, BigInteger.valueOf(12_500_000));
        assertTrue("selector transfer(address,uint256)", data.startsWith("0xa9059cbb"));
        assertTrue("recipient encoded", data.toLowerCase().contains(TO.substring(2).toLowerCase()));
        assertTrue("12.5 USDT at 6dp encoded", data.contains(Long.toHexString(12_500_000)));
        assertEquals("2 static words after the selector", 2 + 8 + 64 * 2, data.length());
    }
}
