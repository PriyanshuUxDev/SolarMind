package com.solarmind.config;

import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  RestClient restClient(RestClient.Builder builder, AiProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    Duration timeout = properties.timeout();
    factory.setConnectTimeout(timeout);
    factory.setReadTimeout(timeout);
    return builder.requestFactory(factory).build();
  }
}
