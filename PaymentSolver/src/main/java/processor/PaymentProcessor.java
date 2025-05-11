package processor;

import model.Order;
import model.PaymentMethod;

import java.util.*;
import java.util.stream.Collectors;

public class PaymentProcessor {
    private static final String POINTS_METHOD = "PUNKTY";
    private static final double MIN_POINTS_PERCENTAGE = 0.1; // 10%
    private static final double PARTIAL_POINTS_DISCOUNT = 0.1; // 10%

    public Map<String, Double> processPayments(List<Order> orders, List<PaymentMethod> paymentMethods) {
        // Copy limits to track remaining funds
        Map<String, Double> remainingLimits = paymentMethods.stream()
                .collect(Collectors.toMap(PaymentMethod::getId, PaymentMethod::getLimit));
        Map<String, Double> spent = new HashMap<>();

        // Process each order
        for (Order order : orders) {
            // Find the best payment option
            PaymentOption bestOption = findBestPaymentOption(order, paymentMethods, remainingLimits);

            // Update spent amounts and limits
            if (bestOption.points > 0) {
                spent.merge(POINTS_METHOD, bestOption.points, Double::sum);
                remainingLimits.merge(POINTS_METHOD, -bestOption.points, Double::sum);
            }
            if (bestOption.cardAmount > 0) {
                spent.merge(bestOption.cardMethod, bestOption.cardAmount, Double::sum);
                remainingLimits.merge(bestOption.cardMethod, -bestOption.cardAmount, Double::sum);
            }
        }

        return spent;
    }

    private PaymentOption findBestPaymentOption(Order order, List<PaymentMethod> paymentMethods,
                                                Map<String, Double> remainingLimits) {
        double orderValue = order.getValue();
        PaymentOption bestOption = new PaymentOption();
        double bestCost = Double.MAX_VALUE; // Initialize to max to ensure any valid option is better

        // Option 1: Full payment with points
        PaymentMethod pointsMethod = paymentMethods.stream()
                .filter(pm -> pm.getId().equals(POINTS_METHOD))
                .findFirst()
                .orElse(null);
        if (pointsMethod != null && remainingLimits.getOrDefault(POINTS_METHOD, 0.0) >= orderValue) {
            double cost = orderValue * (1 - pointsMethod.getDiscount() / 100.0);
            if (cost < bestCost) {
                bestCost = cost;
                bestOption = new PaymentOption(POINTS_METHOD, cost, 0, null); // Use discounted amount
            }
        }

        // Option 2: Partial payment with points (>=10%) + card
        if (pointsMethod != null) {
            double minPoints = orderValue * MIN_POINTS_PERCENTAGE;
            double availablePoints = Math.min(remainingLimits.getOrDefault(POINTS_METHOD, 0.0), orderValue);
            if (availablePoints >= minPoints) {
                double discountedValue = orderValue * (1 - PARTIAL_POINTS_DISCOUNT);
                double cardAmount = discountedValue - availablePoints;

                // Try each card method (even if not in promotions, as it's allowed)
                for (PaymentMethod card : paymentMethods) {
                    if (card.getId().equals(POINTS_METHOD)) continue;
                    if (remainingLimits.getOrDefault(card.getId(), 0.0) >= cardAmount) {
                        double cost = discountedValue;
                        if (cost < bestCost) {
                            bestCost = cost;
                            bestOption = new PaymentOption(POINTS_METHOD, availablePoints, cardAmount, card.getId());
                        }
                    }
                }
            }
        }

        // Option 3: Full payment with card (only for promotions)
        for (String promo : order.getPromotions()) {
            PaymentMethod card = paymentMethods.stream()
                    .filter(pm -> pm.getId().equals(promo))
                    .findFirst()
                    .orElse(null);
            if (card != null && remainingLimits.getOrDefault(card.getId(), 0.0) >= orderValue) {
                double cost = orderValue * (1 - card.getDiscount() / 100.0);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestOption = new PaymentOption(null, 0, cost, card.getId()); // Use discounted amount
                }
            }
        }

        // If no option was found, fall back to full card payment with no discount
        if (bestCost == Double.MAX_VALUE) {
            for (PaymentMethod card : paymentMethods) {
                if (card.getId().equals(POINTS_METHOD)) continue;
                if (remainingLimits.getOrDefault(card.getId(), 0.0) >= orderValue) {
                    bestCost = orderValue;
                    bestOption = new PaymentOption(null, 0, orderValue, card.getId());
                    break;
                }
            }
        }

        return bestOption;
    }

    private static class PaymentOption {
        String pointsMethod;
        double points;
        double cardAmount;
        String cardMethod;

        PaymentOption() {}

        PaymentOption(String pointsMethod, double points, double cardAmount, String cardMethod) {
            this.pointsMethod = pointsMethod;
            this.points = points;
            this.cardAmount = cardAmount;
            this.cardMethod = cardMethod;
        }
    }
}