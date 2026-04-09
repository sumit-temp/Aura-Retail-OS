package facade;

import factory.KioskFactory;
import memento.InventoryManager;
import command.PurchaseItemCommand;
import factory.components.Dispenser;
import state.KioskContext;
import strategy.PricingContext;

public class KioskInterface {
    private final KioskFactory factory;
    private final InventoryManager inventoryManager;
    private final Dispenser dispenser;
    private final KioskContext stateContext;
    private final PricingContext pricingContext;

    public KioskInterface(KioskFactory factory, InventoryManager inventoryManager, PricingContext pricingContext) {
        this.factory = factory;
        this.inventoryManager = inventoryManager;
        this.dispenser = factory.createDispenser();
        this.stateContext = new KioskContext();
        this.pricingContext = pricingContext;
    }
    
    public KioskContext getStateContext() { return stateContext; }

    public void restockItem(String productId, int amount) {
        inventoryManager.addItemStock(productId, amount);
        System.out.println("[Facade] Restocked " + amount + " " + productId);
    }
    
    public void simulateFailingPurchase(String userId, String productId, int amount, double basePrice) {
        purchaseInternal(userId, productId, amount, basePrice, true);
    }

    public void purchaseItem(String userId, String productId, int amount, double basePrice) {
        purchaseInternal(userId, productId, amount, basePrice, false);
    }

    private void purchaseInternal(String userId, String productId, int amount, double basePrice, boolean simulateFailure) {
        System.out.println("\n--- Initializing User Request ---");
        // Behavioral State
        stateContext.purchase();
        
        // Behavioral Strategy
        double finalPrice = pricingContext.executePricing(basePrice * amount);
        System.out.println("[Facade] Calculated price for " + amount + "x " + productId + " = $" + finalPrice);
        
        // Product constraints validation using Abstract Factory components
        boolean verified = factory.createVerificationModule().verify(userId, productId);
        boolean inStock = factory.createInventoryPolicy().canPurchase(productId, amount, inventoryManager.getStock(productId));

        if (verified && inStock) {
            // Execute Behavioral Command
            PurchaseItemCommand command = new PurchaseItemCommand(inventoryManager, dispenser, productId, amount);
            command.setSimulateHardwareFailure(simulateFailure);
            command.execute();
        } else {
            System.out.println("[Facade] Policy or verification failed. Purchase declined.");
        }
    }
}
