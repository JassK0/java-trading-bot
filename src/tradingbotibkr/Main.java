package tradingbotibkr;

public class Main {
    public static void main(String[] args) {
        IBClient client = new IBClient();
        client.connect();

        // Ensure the client is connected before making requests
        try {
            Thread.sleep(2000); // Wait for the connection to establish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RSIMonitor rsiMonitor = new RSIMonitor(client, "SPY", 1, 30, 65);
         rsiMonitor.testBuyOrder();
        //rsiMonitor.startMonitoring();
    }
}
