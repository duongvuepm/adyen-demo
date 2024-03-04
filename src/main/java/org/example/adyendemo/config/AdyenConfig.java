package org.example.adyendemo.config;

import com.adyen.Client;
import com.adyen.enums.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdyenConfig {

  @Value("${app.adyen.api-key}")
  private String adyenApiKey;

  @Bean
  public Client adyenClient() {
    return new Client(adyenApiKey, Environment.TEST);
  }

}
