package factory.components;

public interface InventoryPolicy {
    boolean canPurchase(String productId, int requestedAmount, int currentStock);
}
