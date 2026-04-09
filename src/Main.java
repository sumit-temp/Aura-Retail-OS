import core.CentralRegistry;
import core.EventBus;
import core.events.EmergencyModeActivated;
import core.events.HardwareFailureEvent;
import facade.KioskInterface;
import factory.EmergencyReliefKioskFactory;
import factory.PharmacyKioskFactory;
import failure.FailureHandler;
import failure.RecalibrationHandler;
import failure.RetryHandler;
import failure.TechnicianAlertHandler;
import memento.InventoryManager;
import state.EmergencyLockdownMode;
import strategy.EmergencyPricing;
import strategy.PricingContext;
import strategy.StandardPricing;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Aura Retail OS Starting ===");
        
        // 1. Setup Singleton Registry
        CentralRegistry registry = CentralRegistry.getInstance();
        registry.setConfig("city", "Zephyrus");
        
        // 2. Setup Chain of Responsibility for Failure handling
        FailureHandler retry = new RetryHandler();
        FailureHandler recalibrate = new RecalibrationHandler();
        FailureHandler alert = new TechnicianAlertHandler();
        retry.setNextHandler(recalibrate);
        recalibrate.setNextHandler(alert);
        
        // 3. Setup Observer / EventBus for decoupled comms
        EventBus bus = EventBus.getInstance();
        
        // Register failure observer
        bus.subscribe("HARDWARE_FAILURE", event -> {
            HardwareFailureEvent failEvent = (HardwareFailureEvent) event;
            System.out.println("\n[CITY MONITORING] Received Alert: " + failEvent.getComponentName() + " failed.");
            System.out.println("[CITY MONITORING] Initiating failure recovery chain...");
            retry.handleFailure(failEvent.getErrorMessage());
        });
        
        // 4. Test Kiosk Setup via Abstract Factory and Facade
        System.out.println("\n--- Deploying Pharmacy Kiosk ---");
        InventoryManager pharmacyInventory = new InventoryManager();
        PricingContext pharmacyPricing = new PricingContext(new StandardPricing());
        KioskInterface pharmacyKiosk = new KioskInterface(new PharmacyKioskFactory(), pharmacyInventory, pharmacyPricing);
        
        System.out.println("\n-> Restocking items...");
        pharmacyKiosk.restockItem("Amoxicillin", 50);
        
        System.out.println("\n-> Normal Purchase Scenario (State: ActiveMode, Strategy: StandardPricing)");
        pharmacyKiosk.purchaseItem("User123", "Amoxicillin", 2, 10.0);

        System.out.println("\n-> Hardware Failure Scenario (Transaction Rollback + Chain of Responsibility)");
        System.out.println("Current Stock before failure: " + pharmacyInventory.getStock("Amoxicillin"));
        // Simulate hardware failure 
        pharmacyKiosk.simulateFailingPurchase("User123", "Amoxicillin", 5, 10.0);
        // Explicitly fire the event to trigger our Chain of Responsibility
        bus.publish(new HardwareFailureEvent("Pharmacy-Dispenser-Unit-1", "Motor stalled"));
        
        System.out.println("Final Stock after failed transaction: " + pharmacyInventory.getStock("Amoxicillin"));
        
        System.out.println("\n--- Deploying Emergency Relief Kiosk ---");
        InventoryManager reliefInventory = new InventoryManager();
        PricingContext reliefPricing = new PricingContext(new StandardPricing());
        KioskInterface emergencyKiosk = new KioskInterface(new EmergencyReliefKioskFactory(), reliefInventory, reliefPricing);
        emergencyKiosk.restockItem("WaterBottle", 100);

        // Emergency listener
        bus.subscribe("EMERGENCY_MODE", event -> {
            System.out.println("\n[SYSTEM] Emergency Mode Broadcasted city-wide.");
            // Change State and Strategy dynamically
            emergencyKiosk.getStateContext().setState(new EmergencyLockdownMode());
            reliefPricing.setStrategy(new EmergencyPricing());
        });
        
        bus.publish(new EmergencyModeActivated("Earthquake detected"));
        
        System.out.println("\n-> Emergency Purchase Scenario (State: EmergencyLockdownMode, Strategy: EmergencyPricing, Policy: Max 2)");
        // Try buying 5 - should be blocked by EmergencyRationInventoryPolicy limits
        emergencyKiosk.purchaseItem("User999", "WaterBottle", 5, 5.0);
        
        // Try buying 2 - valid
        emergencyKiosk.purchaseItem("User999", "WaterBottle", 2, 5.0);
        
        System.out.println("\n-> Concurrent Threading scenario on atomic operations");
        Thread t1 = new Thread(() -> pharmacyKiosk.purchaseItem("UserA", "Amoxicillin", 10, 10.0));
        Thread t2 = new Thread(() -> pharmacyKiosk.purchaseItem("UserB", "Amoxicillin", 10, 10.0));
        t1.start(); t2.start();
        
        try { t1.join(); t2.join(); } catch (InterruptedException e) {}
        System.out.println("Final Amoxicillin stock: " + pharmacyInventory.getStock("Amoxicillin"));
        System.out.println("\n=== Simulation Complete ===");
    }
}
