package org.zhuang.trading.api;

import com.ib.client.EClientSocket;
import com.ib.client.EReaderSignal;

public class IBActions {
    private final EWrapperImpl wrapper = new EWrapperImpl();

    public boolean isConnected() {
        return wrapper.getClient().isConnected();
    }

    public void connect() {
        final EClientSocket client = wrapper.getClient();
        final EReaderSignal signal = wrapper.getSignal();

        client.eConnect("127.0.0.1", 7497, 0);
    }
}