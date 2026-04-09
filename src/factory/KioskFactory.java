package factory;

import factory.components.Dispenser;
import factory.components.InventoryPolicy;
import factory.components.VerificationModule;

public interface KioskFactory {
    Dispenser createDispenser();
    VerificationModule createVerificationModule();
    InventoryPolicy createInventoryPolicy();
}
