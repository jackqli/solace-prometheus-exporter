package com.example;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import java.util.List;
import org.openapitools.client.api.TopicEndpointApi;
import org.openapitools.client.model.MsgVpnTopicEndpointsResponse;

public class TopicEndpointMetricService {
  private static final Integer pageSize = 10;

  private TopicEndpointApi topicEndpointApi;
  private MultiGauge topicEndpointMaxSpoolUsageGauge;
  private MultiGauge topicEndpointCurrentSpoolUsageGauge;
  private MultiGauge topicEndpointTxMsgRateGauge;
  private MultiGauge topicEndpointRxMsgRateGauge;

  public TopicEndpointMetricService(TopicEndpointApi topicEndpointApi,
      MeterRegistry meterRegistry) {
    this.topicEndpointApi = topicEndpointApi;
    topicEndpointMaxSpoolUsageGauge = MultiGauge.builder("topic.endpoint.max.spool.usage").register(meterRegistry);
    topicEndpointCurrentSpoolUsageGauge = MultiGauge.builder("topic.endpoint.current.spool.usage").register(meterRegistry);
    topicEndpointTxMsgRateGauge = MultiGauge.builder("topic.endpoint.tx_msg.rate").register(meterRegistry);
    topicEndpointRxMsgRateGauge = MultiGauge.builder("topic.endpoint.rx_msg.rate").register(meterRegistry);
  }

  public void collect(String msgVpnName) {
    MsgVpnTopicEndpointsResponse msgVpnTopicEndpoints = topicEndpointApi
        .getMsgVpnTopicEndpoints(msgVpnName, pageSize, null, List.of(), List.of());
    extracted(msgVpnTopicEndpoints);

    while (msgVpnTopicEndpoints.getMeta().getPaging()!=null
        && msgVpnTopicEndpoints.getMeta().getPaging().getCursorQuery() !=null) {
      msgVpnTopicEndpoints = topicEndpointApi.getMsgVpnTopicEndpoints(msgVpnName, pageSize, msgVpnTopicEndpoints.getMeta().getPaging().getCursorQuery(), List.of(), List.of());
      extracted(msgVpnTopicEndpoints);
    }
  }

  private void extracted(MsgVpnTopicEndpointsResponse msgVpnTopicEndpoints) {
    topicEndpointMaxSpoolUsageGauge.register(msgVpnTopicEndpoints.getData().stream()
        .map(topicEndpoint -> MultiGauge.Row.of(
            Tags.of("msg.vpn.name", topicEndpoint.getMsgVpnName(),
                "topic.endpoint.name", topicEndpoint.getTopicEndpointName()),
            topicEndpoint.getMaxSpoolUsage()))
        .collect(toList()));

    topicEndpointCurrentSpoolUsageGauge.register(msgVpnTopicEndpoints.getData().stream()
        .map(topicEndpoint -> MultiGauge.Row.of(
            Tags.of("msg.vpn.name", topicEndpoint.getMsgVpnName(),
                "topic.endpoint.name", topicEndpoint.getTopicEndpointName()),
            topicEndpoint.getMsgSpoolUsage()))
        .collect(toList()));

    topicEndpointTxMsgRateGauge.register(msgVpnTopicEndpoints.getData().stream()
        .map(topicEndpoint -> MultiGauge.Row.of(
            Tags.of("msg.vpn.name", topicEndpoint.getMsgVpnName(),
                "topic.endpoint.name", topicEndpoint.getTopicEndpointName()),
            topicEndpoint.getTxMsgRate()))
        .collect(toList()));

    topicEndpointRxMsgRateGauge.register(msgVpnTopicEndpoints.getData().stream()
        .map(topicEndpoint -> MultiGauge.Row.of(
            Tags.of("msg.vpn.name", topicEndpoint.getMsgVpnName(),
                "topic.endpoint.name", topicEndpoint.getTopicEndpointName()),
            topicEndpoint.getRxMsgRate()))
        .collect(toList()));
  }
}
