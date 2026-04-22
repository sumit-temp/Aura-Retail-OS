package core.events;

public class LowStockEvent implements SystemEvent {
    private String productId;
    private int currentStock;
    private int threshold;

    public LowStockEvent(String productId, int currentStock, int threshold) {
        this.productId = productId;
        this.currentStock = currentStock;
        this.threshold = threshold;
    }

    @Override
    public String getEventType() {
        return "LOW_STOCK";
    }

    public String getProductId() {
        return productId;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return "LowStockEvent{" +
                "productId='" + productId + '\'' +
                ", currentStock=" + currentStock +
                ", threshold=" + threshold +
                '}';
    }
}
