package tradingbotibkr;

import com.ib.client.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IBClient implements EWrapper {
    private EClientSocket clientSocket;
    private int nextOrderId;
    private double currentPrice;
    private Set<Integer> openOrderIds;

    public IBClient() {
        EJavaSignal signal = new EJavaSignal();
        clientSocket = new EClientSocket(this, signal);
        openOrderIds = new HashSet<>();
    }

    public void connect() {
        clientSocket.eConnect("127.0.0.1", 7497, 0);
        final EReader reader = new EReader(clientSocket, new EJavaSignal());
        reader.start();
        new Thread(() -> {
            while (clientSocket.isConnected()) {
                try {
                    reader.processMsgs();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        System.out.println("Connected to IBKR");
    }

    public void requestCurrentPrice(String symbol) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("STK");
        contract.currency("USD");
        contract.exchange("SMART");

        System.out.println("Requesting current price for " + symbol);
        clientSocket.reqMktData(1, contract, "", false, false, null);
    }

    public void buyCallOption(String symbol, double strike, String expiry) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("OPT");
        contract.exchange("SMART");
        contract.currency("USD");
        contract.lastTradeDateOrContractMonth(expiry);
        contract.strike(strike);
        contract.right("C");

        Order order = new Order();
        order.action("BUY");
        order.orderType("MKT");
        order.totalQuantity(Decimal.get(1));

        clientSocket.placeOrder(nextOrderId++, contract, order);
        System.out.println("Order Placed: Buy Call Option");
        System.out.println("Symbol: " + symbol + ", Strike Price: " + strike + ", Expiry: " + expiry);
    }

    public void sellCallOption(String symbol, double strike, String expiry) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("OPT");
        contract.exchange("SMART");
        contract.currency("USD");
        contract.lastTradeDateOrContractMonth(expiry);
        contract.strike(strike);
        contract.right("C");

        Order order = new Order();
        order.action("SELL");
        order.orderType("MKT");
        order.totalQuantity(Decimal.get(1));

        clientSocket.placeOrder(nextOrderId++, contract, order);
        System.out.println("Order Placed: Sell Call Option");
        System.out.println("Symbol: " + symbol + ", Strike Price: " + strike + ", Expiry: " + expiry);
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
        if (field == 4) { // Last price
            currentPrice = price;
            System.out.println("Current Price of " + tickerId + ": " + currentPrice);
        }
    }

    @Override
    public void tickSize(int tickerId, int field, Decimal size) {

    }

    @Override
    public void tickOptionComputation(int tickerId, int field, int tickAttrib, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {

    }

    @Override
    public void nextValidId(int orderId) {
        this.nextOrderId = orderId;
        System.out.println("Next Valid Order ID: " + orderId);
    }

    @Override
    public void error(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void error(String str) {
        System.err.println(str);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {

    }


    @Override
    public void connectionClosed() {
        System.out.println("Connection closed.");
    }

    @Override
    public void connectAck() {

    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, Decimal pos, double avgCost) {

    }


    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {}

    @Override
    public void tickString(int tickerId, int tickType, String value) {}

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {}

    @Override
    public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {

    }


    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        if (!openOrderIds.contains(orderId)) {
            openOrderIds.add(orderId);
            System.out.println("Open Order: Order ID: " + orderId + ", Symbol: " + contract.symbol() + ", Action: " + order.action() + ", Quantity: " + order.totalQuantity() + ", Order Type: " + order.orderType());
        }
    }

    @Override
    public void openOrderEnd() {
        System.out.println("Open Order End");
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {}

    @Override
    public void updatePortfolio(Contract contract, Decimal position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {

    }


    @Override
    public void updateAccountTime(String timeStamp) {}

    @Override
    public void accountDownloadEnd(String accountName) {}

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {}

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {}

    @Override
    public void contractDetailsEnd(int reqId) {}

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        System.out.println("Execution Details: Req ID: " + reqId + ", Symbol: " + contract.symbol() + ", Exec ID: " + execution.execId() + ", Order ID: " + execution.orderId() + ", Shares: " + execution.shares());
    }

    @Override
    public void execDetailsEnd(int reqId) {
        System.out.println("Execution Details End: Req ID: " + reqId);
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, Decimal size) {

    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, Decimal size, boolean isSmartDepth) {

    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {}

    @Override
    public void position(String account, Contract contract, Decimal pos, double avgCost) {

    }

    @Override
    public void fundamentalData(int reqId, String data) {}

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {

    }

    @Override
    public void tickSnapshotEnd(int reqId) {

    }

    @Override
    public void historicalData(int reqId, Bar bar) {}

    @Override
    public void scannerParameters(String xml) {

    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {

    }

    @Override
    public void scannerDataEnd(int reqId) {

    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, Decimal volume, Decimal wap, int count) {

    }

    @Override
    public void currentTime(long time) {

    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {}

    @Override
    public void marketDataType(int reqId, int marketDataType) {}


    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {}

    @Override
    public void managedAccounts(String accountsList) {

    }

    @Override
    public void receiveFA(int faDataType, String xml) {

    }


    @Override
    public void positionEnd() {}

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {}

    @Override
    public void accountSummaryEnd(int reqId) {}

    @Override
    public void verifyMessageAPI(String apiData) {}

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {}

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {}

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {}

    @Override
    public void displayGroupList(int reqId, String groups) {}

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {}


    @Override
    public void positionMultiEnd(int reqId) {}

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {}

    @Override
    public void accountUpdateMultiEnd(int reqId) {}

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {}

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {}

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {}

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {}

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {}

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] descriptions) {}

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {}

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {}

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {}

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {}

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {}

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {}

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {}

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {}

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {}

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {}

    @Override
    public void rerouteMktDataReq(int reqId, int conid, String exchange) {}

    @Override
    public void rerouteMktDepthReq(int reqId, int conid, String exchange) {}

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {}

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {}

    @Override
    public void pnlSingle(int reqId, Decimal pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {

    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {}

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {}

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {}

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, Decimal size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {

    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, Decimal bidSize, Decimal askSize, TickAttribBidAsk tickAttribBidAsk) {

    }


    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {}

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {}

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {}

    @Override
    public void completedOrdersEnd() {}

    @Override
    public void replaceFAEnd(int reqId, String text) {

    }

    @Override
    public void wshMetaData(int reqId, String dataJson) {

    }

    @Override
    public void wshEventData(int reqId, String dataJson) {

    }

    @Override
    public void historicalSchedule(int reqId, String startDateTime, String endDateTime, String timeZone, List<HistoricalSession> sessions) {

    }

    @Override
    public void userInfo(int reqId, String whiteBrandingId) {

    }
}
