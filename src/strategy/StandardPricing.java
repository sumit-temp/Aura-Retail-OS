package strategy;

public class StandardPricing implements PricingStrategy {
    @Override
    public double calculateFinalPrice(double basePrice) {
        return basePrice;
    }
}
