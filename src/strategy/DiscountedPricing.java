package strategy;

public class DiscountedPricing implements PricingStrategy {
    private double discountPercentage;

    public DiscountedPricing(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double calculateFinalPrice(double basePrice) {
        return basePrice - (basePrice * discountPercentage / 100);
    }
}
