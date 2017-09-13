package org.zhuang.trading.api;

import com.google.common.eventbus.EventBus;
import com.ib.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EWrapperImpl implements EWrapper {
    private final static Logger logger = LoggerFactory.getLogger(EWrapperImpl.class);

    @Autowired
    private EventBus marketDataEventBus;

    private EReaderSignal readerSignal;
    private EClientSocket clientSocket;
    protected int currentOrderId = -1;

    public EWrapperImpl() {
        readerSignal = new EJavaSignal();
        clientSocket = new EClientSocket(this, readerSignal);
    }

    public EClientSocket getClient() {
        return clientSocket;
    }

    public EReaderSignal getSignal() {
        return readerSignal;
    }

    public int getCurrentOrderId() {
        return currentOrderId;
    }

    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        logger.info(String.format("%12s[%4d] %12s: %12f%16s:%2d",
                "Tick Price",
                tickerId,
                TickType.getField(field),
                price,
                "CanAutoExecute",
                canAutoExecute));

        switch (TickType.getField(field)) {
            case "bidPrice":
                marketDataEventBus.post(MarketDataEvent.bidPriceEvent(price));
                break;
            case "askPrice":
                marketDataEventBus.post(MarketDataEvent.askPriceEvent(price));
                break;
        }
    }

    public void tickSize(int tickerId, int field, int size) {
        logger.info(String.format("%12s[%4d] %12s: %12d",
                "Tick Size",
                tickerId,
                TickType.getField(field),
                size));
    }

    public void tickOptionComputation(int tickerId, int field,
                                      double impliedVol, double delta, double optPrice,
                                      double pvDividend, double gamma, double vega, double theta,
                                      double undPrice) {
        logger.info(String.format("TickOptionComputation. TickerId: %d, " +
                        "field: %d, ImpliedVolatility: %f, Delta: %f, " +
                        "OptionPrice: %f, pvDividend: %f, Gamma: %f, " +
                        "Vega: %f, Theta: %f, UnderlyingPrice: %f",
                tickerId,
                field,
                impliedVol,
                delta,
                optPrice,
                pvDividend,
                gamma,
                vega,
                theta,
                undPrice));
    }

    public void tickGeneric(int tickerId, int tickType, double value) {
        logger.info(String.format("Tick Generic. Ticker Id: %d, Field: %s, Value: %f",
                tickerId,
                TickType.getField(tickType),
                value));
    }

    public void tickString(int tickerId, int tickType, String value) {
        logger.info(String.format("Tick string. Ticker Id: %d, Type: %d, Value: %s",
                tickerId,
                tickType,
                value));
    }

    public void tickEFP(int tickerId, int tickType, double basisPoints,
                        String formattedBasisPoints, double impliedFuture, int holdDays,
                        String futureLastTradeDate, double dividendImpact,
                        double dividendsToLastTradeDate) {
        logger.info(String.format("TickEFP. %d, Type: %d, BasisPoints: %f, FormattedBasisPoints: %s, " +
                        "ImpliedFuture: %f, HoldDays: %d, FutureLastTradeDate: %s, DividendImpact: %f, " +
                        "DividendsToLastTradeDate: %f",
                tickerId,
                tickType,
                basisPoints,
                formattedBasisPoints,
                impliedFuture,
                holdDays,
                futureLastTradeDate,
                dividendImpact,
                dividendsToLastTradeDate));
    }

    public void orderStatus(int orderId, String status, double filled,
                            double remaining, double avgFillPrice, int permId, int parentId,
                            double lastFillPrice, int clientId, String whyHeld) {
        logger.info(String.format("OrderStatus. Id: %d, Status: %s, Filled: %f, Remaining: %f, " +
                        "AvgFillPrice: %f, PermId: %d, ParentId: %d, LastFillPrice: %f, ClientId: %d, WhyHeld: %s",
                orderId,
                status,
                filled,
                remaining,
                avgFillPrice,
                permId,
                parentId,
                lastFillPrice,
                clientId,
                whyHeld));
    }

    public void openOrder(int orderId, Contract contract, Order order,
                          OrderState orderState) {
        logger.info(String.format("OpenOrder. ID: %d, %s, %s @ %s: %s, %s %d, %s",
                orderId,
                contract.symbol(),
                contract.secType(),
                contract.exchange(),
                order.action(),
                order.orderType(),
                order.totalQuantity(),
                orderState.status()));
    }

    public void openOrderEnd() {
        logger.info("OpenOrderEnd");
    }

    public void updateAccountValue(String key, String value, String currency,
                                   String accountName) {
        logger.info("UpdateAccountValue. Key: " + key + ", Value: " + value +
                ", Currency: " + currency + ", AccountName: " + accountName);
    }

    public void updatePortfolio(Contract contract, double position,
                                double marketPrice, double marketValue, double averageCost,
                                double unrealizedPNL, double realizedPNL, String accountName) {
        logger.info("UpdatePortfolio. " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange()
                + ": Position: " + position + ", MarketPrice: " + marketPrice + ", MarketValue: " + marketValue +
                ", AverageCost: " + averageCost + ", UnrealisedPNL: " + unrealizedPNL + ", RealisedPNL: " +
                realizedPNL + ", AccountName: " + accountName);
    }

    public void updateAccountTime(String timeStamp) {
        logger.info("UpdateAccountTime. Time: " + timeStamp + "\n");
    }

    public void accountDownloadEnd(String accountName) {
        logger.info("Account download finished: " + accountName + "\n");
    }

    public void nextValidId(int orderId) {
        logger.info(String.format("Next Valid Id: [%d]", orderId));
        currentOrderId = orderId;

        marketDataEventBus.post(MarketDataEvent.nextOrderIdEvent(orderId));
    }

    public void contractDetails(int reqId, ContractDetails contractDetails) {
        logger.info("ContractDetails. ReqId: [" + reqId + "] - [" + contractDetails.contract().symbol() + "], [" +
                contractDetails.contract().secType() + "], ConId: [" + contractDetails.contract().conid() +
                "] @ [" + contractDetails.contract().exchange() + "]");
    }

    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        logger.info("bondContractDetails");
    }

    public void contractDetailsEnd(int reqId) {
        logger.info("ContractDetailsEnd. " + reqId + "\n");
    }

    public void execDetails(int reqId, Contract contract, Execution execution) {
        logger.info(String.format("ExecDetails. %d - [%s], [%s], [%s], [%d], [%d], [%f]",
                reqId,
                contract.symbol(),
                contract.secType(),
                contract.currency(),
                execution.execId(),
                execution.orderId(),
                execution.shares()));
    }

    public void execDetailsEnd(int reqId) {
        logger.info("ExecDetailsEnd. " + reqId + "\n");
    }

    public void updateMktDepth(int tickerId, int position, int operation,
                               int side, double price, int size) {
        logger.info("UpdateMarketDepth. " + tickerId + " - Position: " + position + ", Operation: " +
                operation + ", Side: " + side + ", Price: " + price + ", Size: " + size + "");
    }

    public void updateMktDepthL2(int tickerId, int position,
                                 String marketMaker, int operation, int side, double price, int size) {
        logger.info("updateMktDepthL2");
    }

    public void updateNewsBulletin(int msgId, int msgType, String message,
                                   String origExchange) {
        logger.info("News Bulletins. " + msgId + " - Type: " + msgType + ", Message: " + message +
                ", Exchange of Origin: " + origExchange + "\n");
    }

    public void managedAccounts(String accountsList) {
        logger.info(String.format("Account list: %s", accountsList));
    }

    public void receiveFA(int faDataType, String xml) {
        logger.info("Receing FA: " + faDataType + " - " + xml);
    }

    public void historicalData(int reqId, String date, double open,
                               double high, double low, double close, int volume, int count,
                               double WAP, boolean hasGaps) {
        logger.info("HistoricalData. " + reqId + " - Date: " + date + ", Open: " + open + ", High: " +
                high + ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " +
                count + ", WAP: " + WAP + ", HasGaps: " + hasGaps);
    }

    public void scannerParameters(String xml) {
        logger.info("ScannerParameters. " + xml + "\n");
    }

    public void scannerData(int reqId, int rank,
                            ContractDetails contractDetails, String distance, String benchmark,
                            String projection, String legsStr) {
        logger.info("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " +
                contractDetails.contract().symbol() + ", SecType: " + contractDetails.contract().secType() +
                ", Currency: " + contractDetails.contract().currency() + ", Distance: " + distance +
                ", Benchmark: " + benchmark + ", Projection: " + projection + ", Legs String: " + legsStr);
    }

    public void scannerDataEnd(int reqId) {
        logger.info("ScannerDataEnd. " + reqId);
    }

    public void realtimeBar(int reqId, long time, double open, double high,
                            double low, double close, long volume, double wap, int count) {
        logger.info("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " +
                high + ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " +
                count + ", WAP: " + wap);
    }

    public void currentTime(long time) {
        logger.info("currentTime");
    }

    public void fundamentalData(int reqId, String data) {
        logger.info("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
    }

    public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
        logger.info("deltaNeutralValidation");
    }

    public void tickSnapshotEnd(int reqId) {
        logger.info("TickSnapshotEnd: " + reqId);
    }

    public void marketDataType(int reqId, int marketDataType) {
        logger.info("MarketDataType. [" + reqId + "], Type: [" + marketDataType + "]\n");
    }

    public void commissionReport(CommissionReport commissionReport) {
        logger.info(String.format("CommissionReport. [%d] - [%f] [%s] RPNL [%f]",
                commissionReport.m_execId,
                commissionReport.m_commission,
                commissionReport.m_currency,
                commissionReport.m_realizedPNL));
    }

    public void position(String account, Contract contract, double pos,
                         double avgCost) {
        logger.info("Position. " + account + " - Symbol: " + contract.symbol() + ", SecType: " +
                contract.secType() + ", Currency: " + contract.currency() + ", Position: " +
                pos + ", Avg cost: " + avgCost);
    }

    public void positionEnd() {
        logger.info("PositionEnd \n");
    }

    public void accountSummary(int reqId, String account, String tag,
                               String value, String currency) {
        logger.info("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag +
                ", Value: " + value + ", Currency: " + currency);
    }

    public void accountSummaryEnd(int reqId) {
        logger.info("AccountSummaryEnd. Req Id: " + reqId + "\n");
    }

    public void verifyMessageAPI(String apiData) {
        logger.info("verifyMessageAPI");
    }

    public void verifyCompleted(boolean isSuccessful, String errorText) {
        logger.info("verifyCompleted");
    }

    public void verifyAndAuthMessageAPI(String apiData, String xyzChallange) {
        logger.info("verifyAndAuthMessageAPI");
    }

    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        logger.info("verifyAndAuthCompleted");
    }

    public void displayGroupList(int reqId, String groups) {
        logger.info("Display Group List. ReqId: " + reqId + ", Groups: " + groups + "\n");
    }

    public void displayGroupUpdated(int reqId, String contractInfo) {
        logger.info("Display Group Updated. ReqId: " + reqId + ", Contract info: " + contractInfo + "\n");
    }

    public void error(Exception e) {
        logger.info("Exception in EWrapper: " + e.getMessage());
    }

    public void error(String str) {
        logger.info("Error STR");
    }

    public void error(int id, int errorCode, String errorMsg) {
        logger.error(String.format("ID: %d, Code: %4d, Message: %s",
                id, errorCode, errorMsg));
    }

    public void connectionClosed() {
        logger.info("Connection closed");
    }

    public void connectAck() {
        if (clientSocket.isAsyncEConnect()) {
            logger.info("Acknowledging connection");
            clientSocket.startAPI();
        }
    }

    public void positionMulti(int reqId, String account, String modelCode,
                              Contract contract, double pos, double avgCost) {
        logger.info("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " +
                modelCode + ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: " +
                contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
    }

    public void positionMultiEnd(int reqId) {
        logger.info("Position Multi End. Request: " + reqId + "\n");
    }

    public void accountUpdateMulti(int reqId, String account, String modelCode,
                                   String key, String value, String currency) {
        logger.info("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " +
                modelCode + ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
    }

    public void accountUpdateMultiEnd(int reqId) {
        logger.info("Account Update Multi End. Request: " + reqId + "\n");
    }

    public void securityDefinitionOptionalParameter(int reqId, String exchange,
                                                    int underlyingConId, String tradingClass, String multiplier,
                                                    Set<String> expirations, Set<Double> strikes) {
        logger.info("Security Definition Optional Parameter. Request: " + reqId + ", Trading Class: " +
                tradingClass + ", Multiplier: " + multiplier + " \n");
    }

    public void securityDefinitionOptionalParameterEnd(int reqId) {
    }

    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        for (SoftDollarTier tier : tiers) {
            logger.info("tier: " + tier + ", ");
        }
    }
}
