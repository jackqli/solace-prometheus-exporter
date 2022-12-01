package com.example;

import io.micrometer.core.instrument.MeterRegistry;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ClientApi;
import org.openapitools.client.api.MsgVpnApi;
import org.openapitools.client.api.TopicEndpointApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

  @Bean
  public ApiClient apiClient(@Value("${broker.basepath}") String basePath,
      @Value("${broker.username}") String userName,
      @Value("${broker.password}") String password) {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(basePath);
    apiClient.setUsername(userName);
    apiClient.setPassword(password);
    return apiClient;
  }

  @Bean
  public MsgVpnApi msgVpnApi(ApiClient apiClient) {
    return new MsgVpnApi(apiClient);
  }

  @Bean
  public TopicEndpointApi topicEndpointApi(ApiClient apiClient) {
    return new TopicEndpointApi(apiClient);
  }

  @Bean
  public ClientApi clientApi(ApiClient apiClient) {
    return new ClientApi(apiClient);
  }

  @Bean
  public MsgVpnMetricService msgVpnMetricService(MsgVpnApi msgVpnApi,
      TopicEndpointMetricService topicEndpointMetricService,
      ClientMetricService clientMetricService,
      MeterRegistry meterRegistry) {
    return new MsgVpnMetricService(msgVpnApi, topicEndpointMetricService, clientMetricService, meterRegistry);
  }

  @Bean
  public SolaceMetricService solaceMetricService(MeterRegistry meterRegistry,
      MsgVpnMetricService msgVpnMetricService) {
    return new SolaceMetricService(meterRegistry, msgVpnMetricService);
  }

  @Bean
  public TopicEndpointMetricService topicEndpointMetricService(TopicEndpointApi topicEndpointApi,
      MeterRegistry meterRegistry) {
    return new TopicEndpointMetricService(topicEndpointApi, meterRegistry);
  }

  @Bean
  public ClientMetricService clientMetricService(ClientApi clientApi,
      MeterRegistry meterRegistry) {
    return new ClientMetricService(clientApi, meterRegistry);
  }
}
