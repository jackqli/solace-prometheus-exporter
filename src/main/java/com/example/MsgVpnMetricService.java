package com.example;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import java.util.List;
import org.openapitools.client.api.MsgVpnApi;
import org.openapitools.client.model.MsgVpnsResponse;

public class MsgVpnMetricService {
  private static final Integer pageSize = 10;
  private MsgVpnApi msgVpnApi;
  private TopicEndpointMetricService topicEndpointMetricService;
  private ClientMetricService clientMetricService;
  private MultiGauge msgVpnStateGauge;
  private MultiGauge msgVpnMaxSpoolUsageGauge;
  private MultiGauge msgVpnCurrentSpoolUsageGauge;

  public MsgVpnMetricService(MsgVpnApi msgVpnApi,
      TopicEndpointMetricService topicEndpointMetricService,
      ClientMetricService clientMetricService,
      MeterRegistry meterRegistry) {
    this.msgVpnApi = msgVpnApi;
    this.topicEndpointMetricService = topicEndpointMetricService;
    this.clientMetricService = clientMetricService;
    msgVpnStateGauge = MultiGauge.builder("vpn.state").register(meterRegistry);
    msgVpnMaxSpoolUsageGauge = MultiGauge.builder("vpn.max.spool.usage").register(meterRegistry);
    msgVpnCurrentSpoolUsageGauge = MultiGauge.builder("vpn.current.spool.usage").register(meterRegistry);
  }

  public void collect() {
    MsgVpnsResponse vpns = msgVpnApi.getMsgVpns(pageSize, null, List.of(), List.of());
    extracted(vpns);

    while (vpns.getMeta().getPaging()!=null && vpns.getMeta().getPaging().getCursorQuery() !=null) {
      vpns = msgVpnApi.getMsgVpns(pageSize, vpns.getMeta().getPaging().getCursorQuery(), List.of(), List.of());
      extracted(vpns);
    }
  }

  private void extracted(MsgVpnsResponse vpns) {
    vpns.getData().stream().forEach(vpn -> topicEndpointMetricService.collect(vpn.getMsgVpnName()));
    vpns.getData().stream().forEach(vpn -> clientMetricService.collect(vpn.getMsgVpnName()));

    msgVpnStateGauge.register(vpns.getData().stream()
    .map(vpn -> MultiGauge.Row.of(Tags.of("name", vpn.getMsgVpnName()),vpn.getState().equals("up") ? 1 : 0))
        .collect(toList()));

    msgVpnMaxSpoolUsageGauge.register(vpns.getData().stream()
        .map(vpn -> MultiGauge.Row.of(Tags.of("name", vpn.getMsgVpnName()),vpn.getMaxMsgSpoolUsage()))
        .collect(toList()));

    msgVpnCurrentSpoolUsageGauge.register(vpns.getData().stream()
        .map(vpn -> MultiGauge.Row.of(Tags.of("name", vpn.getMsgVpnName()),vpn.getMsgSpoolUsage()))
        .collect(toList()));
  }
}
