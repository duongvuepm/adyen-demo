package org.example.adyendemo;

import com.adyen.Client;
import com.adyen.model.checkout.Amount;
import com.adyen.model.checkout.CreateCheckoutSessionRequest;
import com.adyen.model.checkout.CreateCheckoutSessionResponse;
import com.adyen.service.checkout.PaymentsApi;
import com.adyen.service.exception.ApiException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

  private final Client adyenClient;

  @Value("${app.adyen.merchantAccount}")
  private String merchantAccount;

  @PostMapping("/payments")
  public CreateCheckoutSessionResponse createPayment() throws IOException, ApiException {
    Amount amount = new Amount().currency("USD").value(1L);

    var orderId = UUID.randomUUID().toString();

    CreateCheckoutSessionRequest createCheckoutSessionRequest = new CreateCheckoutSessionRequest()
        .amount(amount)
        .merchantAccount(merchantAccount)
        .returnUrl("https://your-company.com/checkout?order=" + orderId)
        .reference(orderId)
        .countryCode("SG")
        .expiresAt(OffsetDateTime.now().plusDays(1));

    return new PaymentsApi(adyenClient).sessions(createCheckoutSessionRequest);
  }
}
