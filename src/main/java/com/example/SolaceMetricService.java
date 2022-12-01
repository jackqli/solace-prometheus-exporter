package com.example;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class SolaceMetricService {
  private MsgVpnMetricService msgVpnMetricService;
  private boolean brokerUp;

  public SolaceMetricService(MeterRegistry meterRegistry,
      MsgVpnMetricService msgVpnMetricService) {
    this.msgVpnMetricService = msgVpnMetricService;
    Gauge.builder("broker.up", () -> this.brokerUp ? 1 : 0)
        .description("if the broker is up")
        .register(meterRegistry);
  }

  @Scheduled(fixedDelayString = "${exporter.scheduled.interval}")
  public void collect() {
    try {
      log.info("queryStatus");
      this.msgVpnMetricService.collect();
      this.brokerUp = true;
    }
    catch (Exception e) {
      log.error("Error query status", e);
      this.brokerUp = false;
    }
  }
}
