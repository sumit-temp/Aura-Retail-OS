package strategy;

public class EmergencyPricing implements PricingStrategy {
    @Override
    public double calculateFinalPrice(double basePrice) {
        return basePrice * 0.50; // 50% discount to prevent price gouging during crisis
    }
}
