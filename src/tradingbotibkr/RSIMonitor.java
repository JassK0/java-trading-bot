package tradingbotibkr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RSIMonitor {
    private IBClient client;
    private String symbol;
    private int interval;
    private double buyThreshold;
    private double sellThreshold;
    private List<Double> prices;
    private boolean hasPosition;
    private double lastStrikePrice;

    public RSIMonitor(IBClient client, String symbol, int interval, double buyThreshold, double sellThreshold) {
        this.client = client;
        this.symbol = symbol;
        this.interval = interval;
        this.buyThreshold = buyThreshold;
        this.sellThreshold = sellThreshold;
        this.prices = new ArrayList<>();
        this.hasPosition = false;
    }

    public void startMonitoring() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                client.requestCurrentPrice(symbol);
                double currentPrice = client.getCurrentPrice();
                if (currentPrice > 0) {
                    prices.add(currentPrice);
                    if (prices.size() > 14) {
                        double rsi = calculateRSI(prices);
                        if (!hasPosition && rsi < buyThreshold) {
                            lastStrikePrice = getStrikePrice(currentPrice, 4); // 4 strikes out of the money
                            client.buyCallOption(symbol, lastStrikePrice, getCurrentExpiry());
                            hasPosition = true;
                            System.out.println("Order Placed: Buy Call Option");
                            System.out.println("Symbol: " + symbol + ", Strike Price: " + lastStrikePrice + ", Expiry: " + getCurrentExpiry() + ", Current Price: " + currentPrice);
                        } else if (hasPosition && rsi > sellThreshold) {
                            client.sellCallOption(symbol, lastStrikePrice, getCurrentExpiry());
                            hasPosition = false;
                            System.out.println("Position Closed: Sell Call Option");
                            System.out.println("Symbol: " + symbol + ", Strike Price: " + lastStrikePrice + ", Expiry: " + getCurrentExpiry() + ", Current Price: " + currentPrice);
                        }
                        prices.remove(0); // Keep only the last 14 prices
                    }
                }
            }
        }, 0, interval * 60 * 1000);
    }

    // New method to test placing a buy order immediately
    public void testBuyOrder() {
        client.requestCurrentPrice(symbol);
        try {
            Thread.sleep(2000); // Wait for the price to be fetched
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double currentPrice = client.getCurrentPrice();
        if (currentPrice > 0) {
            lastStrikePrice = getStrikePrice(currentPrice, 4); // 4 strikes out of the money
            client.buyCallOption(symbol, lastStrikePrice, getCurrentExpiry());
            hasPosition = true;
            System.out.println("Test Order Placed: Buy Call Option");
            System.out.println("Symbol: " + symbol + ", Strike Price: " + lastStrikePrice + ", Expiry: " + getCurrentExpiry() + ", Current Price: " + currentPrice);
        } else {
            System.out.println("Failed to get current price for test order.");
        }
    }

    private double calculateRSI(List<Double> prices) {
        int period = 14;
        double gain = 0;
        double loss = 0;

        for (int i = 1; i < period; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }

        gain /= period;
        loss /= period;

        if (loss == 0) {
            return 100;
        }

        double rs = gain / loss;
        return 100 - (100 / (1 + rs));
    }

    private String getCurrentExpiry() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    private double getStrikePrice(double currentPrice, int strikesOut) {
        // Assuming each strike price increment is 1 point
        return Math.ceil(currentPrice) + strikesOut;
    }
}
