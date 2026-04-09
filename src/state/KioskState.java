package state;

public interface KioskState {
    void handlePurchase(KioskContext context);
    void handleMaintenance(KioskContext context);
}
