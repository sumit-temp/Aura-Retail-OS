package state;

public class PowerSavingMode implements KioskState {
    @Override
    public void handlePurchase(KioskContext context) {
        System.out.println("[PowerSavingMode] Waking up kiosk to handle purchase...");
        context.setState(new ActiveMode());
        context.purchase();
    }
    @Override
    public void handleMaintenance(KioskContext context) {
        System.out.println("[PowerSavingMode] Cannot enter maintenance directly from power saving. Wake up first.");
    }
}
