package org.example.adyendemo.config;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.defaultconfig.ApiRootBuilder;
import com.commercetools.api.defaultconfig.ServiceRegion;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CommercetoolsConfig {

  @Value("${app.commercetools.clientId}")
  private String clientId;

  @Value("${app.commercetools.clientSecret}")
  private String clientSecret;

  @Value("${app.commercetools.projectKey}")
  private String projectKey;

  @Bean
  public ProjectApiRoot createApiClient() {
    return ApiRootBuilder.of()
        .defaultClient(
            ClientCredentials.of().withClientId(clientId).withClientSecret(clientSecret).build(),
            ServiceRegion.GCP_AUSTRALIA_SOUTHEAST1)
        .build(projectKey);
  }
}
