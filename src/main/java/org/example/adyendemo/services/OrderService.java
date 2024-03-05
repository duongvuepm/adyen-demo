package org.example.adyendemo.services;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderState;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.commercetools.api.models.order.OrderUpdateActionBuilder;
import com.commercetools.api.models.order.OrderUpdateBuilder;
import com.commercetools.api.models.order.PaymentState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final ProjectApiRoot apiRoot;

  public void completeOrder(String orderId, String paymentStatus) {

    if ("AUTHORISATION".equalsIgnoreCase(paymentStatus)) {

      Order currentOrder = apiRoot.orders().withId(orderId)
          .get()
          .executeBlocking()
          .getBody();

      OrderUpdateAction updateOrderState = OrderUpdateActionBuilder.of()
          .changeOrderStateBuilder()
          .orderState(OrderState.CONFIRMED)
          .build();

      OrderUpdateAction updatePaymentState = OrderUpdateActionBuilder.of()
          .changePaymentStateBuilder()
          .paymentState(PaymentState.PAID)
          .build();

      apiRoot.orders().withId(orderId)
          .post(
              OrderUpdateBuilder.of()
                  .version(currentOrder.getVersion())
                  .plusActions(updatePaymentState)
                  .plusActions(updateOrderState)
                  .build()
          )
          .execute();
    }
  }
}
