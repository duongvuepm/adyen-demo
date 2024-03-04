package org.example.adyendemo;

import com.adyen.model.notification.NotificationRequest;
import com.adyen.model.notification.NotificationRequestItem;
import com.adyen.util.HMACValidator;
import java.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController {

  @Value("${app.adyen.hmacKey:myKey}")
  private String hmacKey;

  @PostMapping("/webhooks/notifications")
  public ResponseEntity<String> webhooks(@RequestBody String json) throws Exception {

    // from JSON string to object
    var notificationRequest = NotificationRequest.fromJson(json);

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
  }

  public HMACValidator getHmacValidator() {
    return new HMACValidator();
  }
}
