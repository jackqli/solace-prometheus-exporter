package com.example;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import java.util.List;
import org.openapitools.client.api.ClientApi;
import org.openapitools.client.model.MsgVpnClientsResponse;

public class ClientMetricService {
  private static final Integer pageSize = 10;

  private ClientApi clientApi;
  private MultiGauge clientsCountGauge;

  public ClientMetricService(ClientApi clientApi,
      MeterRegistry meterRegistry) {
    this.clientApi = clientApi;
    clientsCountGauge = MultiGauge.builder("clients.count").register(meterRegistry);
  }

  public void collect(String msgVpnName) {
    int clientsCount = 0;
    MsgVpnClientsResponse msgVpnClients = clientApi
        .getMsgVpnClients(msgVpnName, pageSize, null, List.of(), List.of());
    clientsCount += msgVpnClients.getData().size();
    while (msgVpnClients.getMeta().getPaging()!=null
        && msgVpnClients.getMeta().getPaging().getCursorQuery() !=null) {
      msgVpnClients = clientApi.getMsgVpnClients(msgVpnName, pageSize, msgVpnClients.getMeta().getPaging().getCursorQuery(), List.of(), List.of());
      clientsCount += msgVpnClients.getData().size();
    }
    clientsCountGauge.register(List.of(MultiGauge.Row.of(Tags.of("msg.vpn.name", msgVpnName), clientsCount)));
  }
}
