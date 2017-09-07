package org.zhuang.trading.api;

import com.ib.client.Contract;

public class Contracts {
    public static Contract simpleFuture(String symbol, String contractMonth, String exchange) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("FUT");
        contract.currency("USD");
        contract.exchange(exchange);
        contract.lastTradeDateOrContractMonth(contractMonth);
        return contract;
    }
}
