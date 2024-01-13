package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.InvalidOrderException;
import com.kopchak.worldoftoys.exception.MessageSendingException;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;
import com.kopchak.worldoftoys.model.order.payment.Currency;
import com.kopchak.worldoftoys.model.order.payment.Payment;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import com.kopchak.worldoftoys.service.EmailSenderService;
import com.kopchak.worldoftoys.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private final EmailSenderService emailSenderService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = STRIPE_API_KEY;
    }

    private static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    private static final String SUCCESSFUL_DELAYED_PAYMENT = "checkout.session.async_payment_succeeded";
    private static final String FAILED_DELAYED_PAYMENT = "checkout.session.async_payment_failed";
    private static final String PAID_SESSION_PAYMENT_STATUS = "paid";
    private static final String SESSION_ORDER_ID_METADATA_KEY = "order_id";

    @Override
    public String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId)
            throws InvalidOrderException, StripeException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty() || !orderOptional.get().getOrderStatus().equals(OrderStatus.AWAITING_PAYMENT)) {
            String errMsg = String.format("The order with id: %s does not exist or has already been paid!", orderId);
            log.error(errMsg);
            throw new InvalidOrderException(errMsg);
        }
        Customer customer = findOrCreateStripeCustomer(credentialsDto.customerEmail(), credentialsDto.customerName());
        Set<OrderDetails> orderDetails = orderOptional.get().getOrderDetails();
        SessionCreateParams sessionCreateParams = createPaymentSessionParams(customer, orderId, orderDetails);
        Session session = Session.create(sessionCreateParams);
        return session.getUrl();
    }

    @Override
    public void handlePaymentWebhook(String sigHeader, String requestBody)
            throws StripeException, MessageSendingException {
        Event event = Webhook.constructEvent(requestBody, sigHeader, WEBHOOK_SECRET_KEY);

        String eventType = event.getType();
        if (eventType.equals(CHECKOUT_SESSION_COMPLETED) || eventType.equals(SUCCESSFUL_DELAYED_PAYMENT) ||
                eventType.equals(FAILED_DELAYED_PAYMENT)) {

            Session sessionEvent = (Session) event.getDataObjectDeserializer().getObject().orElseThrow(() ->
                    new EventDataObjectDeserializationException("Event data object deserialization is impossible",
                            event.toJson()));
            SessionRetrieveParams params = SessionRetrieveParams.builder()
                    .addExpand("line_items")
                    .build();
            Session session = Session.retrieve(sessionEvent.getId(), params, null);
            Map<String, String> sessionMetadata = session.getMetadata();
            String orderId = sessionMetadata.get(SESSION_ORDER_ID_METADATA_KEY);

            buildPayment(orderId, eventType, session.getId(), sessionEvent.getPaymentStatus(), session.getAmountTotal());
        }
    }

    private Customer findOrCreateStripeCustomer(String email, String name) throws StripeException {
        CustomerSearchParams params = CustomerSearchParams.builder().setQuery("email:'" + email + "'").build();
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
    }

    private SessionCreateParams createPaymentSessionParams(Customer customer, String orderId,
                                                           Set<OrderDetails> orderDetails) {
        SessionCreateParams.Builder paramsBuilder =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setCustomer(customer.getId())
                        .setCurrency(Currency.UAH.name())
                        .putMetadata(SESSION_ORDER_ID_METADATA_KEY, orderId)
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
                              Long orderTotalAmount) throws MessageSendingException {
        Order order = orderRepository.findById(orderId).orElseThrow();

        PaymentStatus paymentStatus;
        OrderStatus orderStatus;

        if (eventType.equals(FAILED_DELAYED_PAYMENT)) {
            paymentStatus = PaymentStatus.FAILED;
            orderStatus = OrderStatus.CANCELED;
        } else {
            if (sessionPaymentStatus.equals(PAID_SESSION_PAYMENT_STATUS)) {
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
                .order(order)
                .build();
        order.getPayments().add(payment);
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        AppUser user = order.getUser();
        emailSenderService.sendEmail(user.getEmail(), user.getFirstname(), orderId, paymentStatus);
    }
}
