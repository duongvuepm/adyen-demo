package org.example.adyendemo;

import com.adyen.model.notification.NotificationRequest;
import com.adyen.model.notification.NotificationRequestItem;
import com.adyen.util.HMACValidator;
import java.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adyendemo.services.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

  private final OrderService orderService;

  @Value("${app.adyen.hmacKey}")
  private String hmacKey;

  @PostMapping("/notifications")
  public ResponseEntity<String> webhooks(@RequestBody NotificationRequest notificationRequest) throws Exception {

    // fetch first (and only) NotificationRequestItem
    var notificationRequestItem = notificationRequest.getNotificationItems().stream().findFirst();

    if (notificationRequestItem.isPresent()) {

      var item = notificationRequestItem.get();

      try {
        if (getHmacValidator().validateHMAC(item, this.hmacKey)) {
          log.info("""
                  Received webhook with event: {}
                  Merchant Reference: {}
                  Alias: {}
                  PSP reference : {}
                  """
              , item.getEventCode(), item.getMerchantReference(),
              item.getAdditionalData().get("alias"), item.getPspReference());

          consumeEvent(item);

        } else {
          // invalid HMAC signature: do not send [accepted] response
          log.warn("Could not validate HMAC signature for incoming webhook message: {}", item);
          throw new RuntimeException("Invalid HMAC signature");
        }
      } catch (SignatureException e) {
        // Unexpected error during HMAC validation: do not send [accepted] response
        log.error("Error while validating HMAC Key", e);
        throw new SignatureException(e);
      }

    } else {
      // Unexpected event with no payload: do not send [accepted] response
      log.warn("Empty NotificationItem");
      throw new Exception("empty");
    }

    // Acknowledge event has been consumed
    return ResponseEntity.ok().body("[accepted]");
  }

  // process payload asynchronously
  void consumeEvent(NotificationRequestItem item) {

    log.info("Processing event: {}", item.toString());

    this.orderService.completeOrder(item.getMerchantReference(), item.getEventCode());
  }

  public HMACValidator getHmacValidator() {
    return new HMACValidator();
  }
}
