package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.StripeCheckoutException;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;
import com.kopchak.worldoftoys.model.order.payment.Currency;
import com.kopchak.worldoftoys.model.order.payment.Payment;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import com.kopchak.worldoftoys.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Value(value = "${stripe.api.key}")
    private String STRIPE_API_KEY;

    @Value(value = "${stripe.success.url}")
    private String STRIPE_SUCCESS_URL;

    @Value(value = "${stripe.webhook.secret.key}")
    private String WEBHOOK_SECRET_KEY;

    private final OrderRepository orderRepository;

    @Override
    public String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Stripe.apiKey = STRIPE_API_KEY;
        Customer customer = findOrCreateStripeCustomer(credentialsDto.customerEmail(), credentialsDto.customerName());
        Set<OrderDetails> orderDetails = order.getOrderDetails();
        SessionCreateParams sessionCreateParams = createPaymentSessionParams(customer, orderId, orderDetails);
        Session session;
        try {
            session = Session.create(sessionCreateParams);
        } catch (StripeException e) {
            throw new StripeCheckoutException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage());
        }
        return session.getUrl();
    }

    public boolean isNonExistentOrPaidOrder(String orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.isEmpty() || !order.get().getOrderStatus().equals(OrderStatus.AWAITING_PAYMENT);
    }

    public void handlePaymentWebhook(HttpServletRequest request) throws StripeException, IOException {
        Stripe.apiKey = STRIPE_API_KEY;
        String sigHeader = request.getHeader("Stripe-Signature");
        String requestBody = IOUtils.toString(request.getReader());

        Event event;
        try {
            event = Webhook.constructEvent(requestBody, sigHeader, WEBHOOK_SECRET_KEY);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e.getMessage());
        }

        String eventType = event.getType();
        if (eventType.equals("checkout.session.completed") ||
                eventType.equals("checkout.session.async_payment_succeeded") ||
                eventType.equals("checkout.session.async_payment_failed")) {

            Session sessionEvent = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
            SessionRetrieveParams params = SessionRetrieveParams.builder()
                    .addExpand("line_items")
                    .build();
            Session session = Session.retrieve(sessionEvent.getId(), params, null);
            Map<String, String> sessionMetadata = session.getMetadata();
            String orderId = sessionMetadata.get("order_id");

            buildPayment(orderId, eventType, session.getId(), sessionEvent.getPaymentStatus(), session.getAmountTotal());
        }

    }

    private Customer findOrCreateStripeCustomer(String email, String name) {
        CustomerSearchParams params = CustomerSearchParams.builder().setQuery("email:'" + email + "'").build();
        try {
            CustomerSearchResult customerSearchResult = Customer.search(params);
            if (customerSearchResult.getData().isEmpty()) {
                CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                        .setName(name)
                        .setEmail(email)
                        .build();
                return Customer.create(customerCreateParams);
            } else {
                return customerSearchResult.getData().get(0);
            }
        } catch (StripeException e) {
            throw new StripeCheckoutException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage());
        }
    }

    private SessionCreateParams createPaymentSessionParams(Customer customer, String orderId, Set<OrderDetails> orderDetails) {
        SessionCreateParams.Builder paramsBuilder =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setCustomer(customer.getId())
                        .setCurrency(Currency.UAH.name())
                        .putMetadata("order_id", orderId)
                        .setSuccessUrl(STRIPE_SUCCESS_URL);

        orderDetails.forEach(orderDetail ->
                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(orderDetail.getQuantity().longValue())
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(orderDetail.getProduct().getName())
                                                                .build()
                                                )
                                                .setCurrency(Currency.UAH.name())
                                                .setUnitAmountDecimal(orderDetail.getProduct().getPrice()
                                                        .multiply(BigDecimal.valueOf(100)))
                                                .build())
                                .build())
        );
        return paramsBuilder.build();
    }

    private void buildPayment(String orderId, String eventType, String paymentId, String sessionPaymentStatus,
                              Long orderTotalAmount) {
        Order paidOrder = orderRepository.findById(orderId).orElseThrow();

        PaymentStatus paymentStatus;
        OrderStatus orderStatus;

        if (eventType.equals("checkout.session.async_payment_failed")) {
            paymentStatus = PaymentStatus.FAILED;
            orderStatus = OrderStatus.CANCELED;
        } else {
            if (sessionPaymentStatus.equals("paid")) {
                paymentStatus = PaymentStatus.COMPLETE;
                orderStatus = OrderStatus.AWAITING_FULFILMENT;
            } else {
                paymentStatus = PaymentStatus.PENDING;
                orderStatus = OrderStatus.AWAITING_PAYMENT;
            }
        }

        Payment payment = Payment.builder()
                .id(paymentId)
                .dateTime(LocalDateTime.now())
                .status(paymentStatus)
                .price(BigDecimal.valueOf(orderTotalAmount, 2))
                .order(paidOrder)
                .build();
        paidOrder.getPayments().add(payment);
        paidOrder.setOrderStatus(orderStatus);
        orderRepository.save(paidOrder);
    }
}
