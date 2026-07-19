package com.eurobuddha.atomix.eth;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * Manual wallet send (ETH value transfer + ERC20 USDT transfer) — the parity twin of the MDS build's
 * lib/wallet.js (user decision 2026-07-19: Send lives in BOTH apps). Every check runs on RAW integer
 * balances — the UI's rounded display strings must never gate a fund movement. Broadcasts go through
 * {@link EthTx#send} so a manual send shares the SAME per-address nonce serializer as the swap engine's
 * HTLC operations and can never nonce-clash a settlement.
 */
public final class EthSend {

    /** Fixed intrinsic gas of a plain value transfer. */
    public static final BigInteger GAS_ETH = BigInteger.valueOf(21_000);
    /** Same safety limit the engine uses for its ERC20 approve; unused gas refunds. */
    public static final BigInteger GAS_ERC20 = BigInteger.valueOf(100_000);

    private EthSend() {}

    public static boolean isEthAddr(String a) {
        return a != null && a.trim().matches("0x[0-9a-fA-F]{40}");
    }

    public static boolean validDec(String s) {
        return s != null && s.trim().matches("[0-9]+(\\.[0-9]+)?");
    }

    /** Decimal string → raw integer at {@code decimals}, truncating extra precision (never rounds UP a spend). */
    public static BigInteger parseUnits(String s, int decimals) {
        return new BigDecimal(s.trim()).movePointRight(decimals).toBigInteger();
    }

    /** The wei to RESERVE for one send's gas, mirroring EthTx's +20% headroom (the F5 base-fee floor can raise
     *  the actual price further — the review shows "≈"). */
    public static BigInteger gasReserveWei(BigInteger gasPriceWei, BigInteger gasLimit) {
        return gasLimit.multiply(gasPriceWei.multiply(BigInteger.valueOf(12)).divide(BigInteger.TEN));
    }

    /** Max spendable ETH after reserving gas for the send itself (floors at zero). */
    public static BigInteger maxEthSendWei(BigInteger balanceWei, BigInteger gasPriceWei) {
        BigInteger m = balanceWei.subtract(gasReserveWei(gasPriceWei, GAS_ETH));
        return m.signum() > 0 ? m : BigInteger.ZERO;
    }

    /**
     * Validate a send BEFORE building it. {@code eth} selects ETH vs USDT; balances are RAW (wei / 6dp units).
     * @return null when OK, else a plain-words refusal.
     */
    public static String checkSend(boolean eth, String to, String amountStr,
                                   BigInteger ethWei, BigInteger usdtRaw, int usdtDecimals, BigInteger gasPriceWei) {
        if (!isEthAddr(to)) return "Enter a valid Ethereum address (0x + 40 hex characters).";
        if (!validDec(amountStr)) return "Enter a plain decimal amount.";
        if (eth) {
            BigInteger wei = parseUnits(amountStr, 18);
            if (wei.signum() <= 0) return "Enter an amount above zero.";
            if (wei.add(gasReserveWei(gasPriceWei, GAS_ETH)).compareTo(ethWei) > 0)
                return "Not enough ETH for that amount plus network gas — try Max.";
            return null;
        }
        BigInteger raw = parseUnits(amountStr, usdtDecimals);
        if (raw.signum() <= 0) return "Enter an amount above zero.";
        if (raw.compareTo(usdtRaw) > 0) return "That is more USDT than this wallet holds.";
        if (gasReserveWei(gasPriceWei, GAS_ERC20).compareTo(ethWei) > 0)
            return "Sending USDT needs a little ETH for gas — fund the wallet with ETH first.";
        return null;
    }

    /** ERC20 transfer(to, amount) calldata — split out so the encoding is unit-testable without a network. */
    public static String buildTransferData(String to, BigInteger raw) {
        Function fn = new Function("transfer",
                Arrays.asList(new Address(to.trim()), new Uint256(raw)),
                Collections.singletonList(new org.web3j.abi.TypeReference<Bool>() {}));
        return FunctionEncoder.encode(fn);
    }

    /** Broadcast a plain ETH transfer. Returns the tx hash. Call off the main thread. */
    public static String sendEth(EthRpc rpc, Credentials creds, long chainId, String to, BigInteger wei) throws Exception {
        return EthTx.send(rpc, creds, chainId, to.trim(), "", wei, GAS_ETH);
    }

    /** Broadcast an ERC20 transfer. Returns the tx hash. Call off the main thread. */
    public static String sendErc20(EthRpc rpc, Credentials creds, long chainId, String token, String to, BigInteger raw) throws Exception {
        return EthTx.send(rpc, creds, chainId, token, buildTransferData(to, raw), BigInteger.ZERO, GAS_ERC20);
    }
}
