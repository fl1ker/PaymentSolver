import com.fasterxml.jackson.databind.ObjectMapper;
import model.Order;
import model.PaymentMethod;
import processor.PaymentProcessor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Read orders
            File ordersFile = new File(args[0]);
            List<Order> orders = Arrays.asList(mapper.readValue(ordersFile, Order[].class));

            // Read payment methods
            File paymentMethodsFile = new File(args[1]);
            List<PaymentMethod> paymentMethods = Arrays.asList(mapper.readValue(paymentMethodsFile, PaymentMethod[].class));

            // Process payments
            PaymentProcessor processor = new PaymentProcessor();
            Map<String, Double> spent = processor.processPayments(orders, paymentMethods);

            // Output results
            for (Map.Entry<String, Double> entry : spent.entrySet()) {
                System.out.printf("%s %.2f%n", entry.getKey(), entry.getValue());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}