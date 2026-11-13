package com.example.shopping.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.shopping.cart.CartCheckedOut;
import com.example.shopping.order.internal.FulfillmentResult;
import com.example.shopping.order.internal.OrderFulfillmentPort;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Scenario test: a checked-out cart leads to an order being created from its contents, via the
 * in-process {@code CartCheckedOut} event. The external fulfillment service is mocked.
 */
@ApplicationModuleTest
class OrderCreationTests {

    @MockitoBean
    private OrderFulfillmentPort fulfillment;

    @Autowired
    private OrderRepository orders;

    @Test
    void createsOrderOnCheckout(Scenario scenario) {
        when(fulfillment.submit(ArgumentMatchers.any())).thenReturn(FulfillmentResult.ok());

        scenario.publish(new CartCheckedOut("cart-1", Map.of("socks", 3, "t-shirt", 2), Instant.now()))
                .andWaitForStateChange(() -> orders.findByCartId("cart-1"))
                .andVerify(order -> {
                    assertThat(order).isPresent();
                    assertThat(order.get().totalUnits()).isEqualTo(5);
                });
    }
}
