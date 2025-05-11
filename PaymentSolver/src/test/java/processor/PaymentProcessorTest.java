package processor;

import model.Order;
import model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentProcessorTest {

    @Test
    void testSingleOrderFullPointsPayment() {
        // Order can be fully paid with points with discount applied
        Order order = new Order();
        order.setId("ORDER1");
        order.setValue(100.00);
        order.setPromotions(Arrays.asList("mZysk"));

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(15);
        points.setLimit(100.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(10);
        card.setLimit(100.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), Arrays.asList(points, card));

        assertEquals(1, result.size());
        assertEquals(85.00, result.get("PUNKTY"), 0.01);
    }

    @Test
    void testPartialPointsPayment() {
        // Order partially paid with points, rest with card, discount applied
        Order order = new Order();
        order.setId("ORDER1");
        order.setValue(50.00);
        order.setPromotions(List.of());

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(15);
        points.setLimit(15.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(10);
        card.setLimit(100.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), Arrays.asList(points, card));

        assertEquals(2, result.size());
        assertEquals(15.00, result.get("PUNKTY"), 0.01);
        assertEquals(30.00, result.get("mZysk"), 0.01);
    }

    @Test
    void testFullCardPromoPayment() {
        // Promo card is used to fully pay for the order with discount
        Order order = new Order();
        order.setId("ORDER2");
        order.setValue(200.00);
        order.setPromotions(List.of("mZysk"));

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(15);
        points.setLimit(0.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(20);
        card.setLimit(300.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(points, card));

        assertEquals(1, result.size());
        assertEquals(160.00, result.get("mZysk"), 0.01);
    }

    @Test
    void testFallbackFullCardPaymentWithoutDiscount() {
        // No valid promo or points, fallback to card payment without discount
        Order order = new Order();
        order.setId("ORDER3");
        order.setValue(120.00);
        order.setPromotions(List.of("BosBankrut"));

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(15);
        points.setLimit(0.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(0);
        card.setLimit(200.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(points, card));

        assertEquals(1, result.size());
        assertEquals(120.00, result.get("mZysk"), 0.01);
    }

    @Test
    void testInsufficientFundsNoPayment() {
        // Not enough balance in points or cards to cover the order
        Order order = new Order();
        order.setId("ORDER4");
        order.setValue(150.00);

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(10);
        points.setLimit(10.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(5);
        card.setLimit(100.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(points, card));

        assertEquals(0, result.size());
    }

    @Test
    void testNoPointsMethodPresent() {
        // No points method available, order paid with promo card
        Order order = new Order();
        order.setId("ORDER_NO_POINTS");
        order.setValue(100.00);
        order.setPromotions(List.of("mZysk"));

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(10);
        card.setLimit(200.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(card));

        assertEquals(1, result.size());
        assertEquals(90.00, result.get("mZysk"), 0.01);
    }

    @Test
    void testNotEnoughPointsForPartialPayment() {
        // Not enough points to qualify for partial discount, fallback to card
        Order order = new Order();
        order.setId("ORDER_FEW_POINTS");
        order.setValue(100.00);
        order.setPromotions(List.of());

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(15);
        points.setLimit(5.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(0);
        card.setLimit(100.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(points, card));

        assertEquals(1, result.size());
        assertEquals(100.00, result.get("mZysk"), 0.01);
    }

    @Test
    void testPromoCardNotInPaymentMethods() {
        // Promo card specified but not available in payment methods, fallback used
        Order order = new Order();
        order.setId("ORDER_PROMO_NOT_FOUND");
        order.setValue(100.00);
        order.setPromotions(List.of("BosBankrut"));

        PaymentMethod points = new PaymentMethod();
        points.setId("PUNKTY");
        points.setDiscount(10);
        points.setLimit(0.00);

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(0);
        card.setLimit(100.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(points, card));

        assertEquals(1, result.size());
        assertEquals(100.00, result.get("mZysk"), 0.01);
    }

    @Test
    void testCardPromoExistsButNotEnoughLimitAndCostNotBetter() {
        // Promo card exists but doesn't have enough limit, fallback is used
        Order order = new Order();
        order.setId("ORDER_EDGE_CASE");
        order.setValue(100.00);
        order.setPromotions(List.of("mZysk"));

        PaymentMethod card = new PaymentMethod();
        card.setId("mZysk");
        card.setDiscount(10);
        card.setLimit(50.00);

        PaymentMethod fallback = new PaymentMethod();
        fallback.setId("BosBankrut");
        fallback.setDiscount(0);
        fallback.setLimit(200.00);

        PaymentProcessor processor = new PaymentProcessor();
        Map<String, Double> result = processor.processPayments(List.of(order), List.of(card, fallback));

        assertEquals(1, result.size());
        assertEquals(100.00, result.get("BosBankrut"), 0.01);
    }
}