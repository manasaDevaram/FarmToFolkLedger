package com.farmtofolk.farmtofolk_ledger.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI farmToFolkOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("FarmToFolk Ledger API")
                .version("v1")
                .description(
                    "Backend APIs for admin, verifier, and public traceability workflows."));
  }
}
