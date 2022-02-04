package com.sample.webflux.config;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${client.response.timeout.seconds:10}")
    private Integer responseTimeoutSeconds;

    @Value("${client.connect.timeout.millis:1000}")
    private Integer connectTimeoutMillis;

    @Value("${client.dns-resolve.timeout.millis:1000}")
    private Integer dnsResolveTimeoutMillis;

    @Value("${http.metrics.enabled:true}")
    private Boolean httpMetricsEnabled;

    @Value("${http.maxIdleTime.seconds:10}")
    private Integer maxIdleTimeSeconds;

    @Value("${http.maxLifeTime.seconds:60}")
    private Integer maxLifeTimeSeconds;

    @Value("${http.pendingAcquireTimeout.seconds:1}")
    private Integer pendingAcquireTimeoutSeconds;

    @Value("${http.evictInBackground.seconds:120}")
    private Integer evictInBackgroundSeconds;

    @PostConstruct
    public void init() {
        Metrics.globalRegistry
                .config()
                .meterFilter(MeterFilter.maximumAllowableTags(reactor.netty.Metrics.HTTP_CLIENT_PREFIX, reactor.netty.Metrics.URI, 1, MeterFilter.deny()));
    }

    @Bean
    public ClientHttpConnector httpClientConnector() {
        return getClientHttpConnector("http2test", httpMetricsEnabled, HttpProtocol.H2, HttpProtocol.HTTP11);
    }

    private ClientHttpConnector getClientHttpConnector(String poolName, boolean metricsEnabled, HttpProtocol... supportedProtocols) {
        ConnectionProvider provider =
                ConnectionProvider.builder(poolName)
                        .maxIdleTime(Duration.ofSeconds(maxIdleTimeSeconds))
                        .maxLifeTime(Duration.ofSeconds(maxLifeTimeSeconds))
                        .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
                        .evictInBackground(Duration.ofSeconds(evictInBackgroundSeconds))
                        .metrics(metricsEnabled)
                        .build();
        HttpClient httpClient = HttpClient.create(provider)
                .protocol(supportedProtocols)
                .secure()
                .keepAlive(true)
                .compress(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .responseTimeout(Duration.of(responseTimeoutSeconds, ChronoUnit.SECONDS))
                .resolver(spec -> spec.queryTimeout(Duration.ofMillis(dnsResolveTimeoutMillis)).roundRobinSelection(true))
                .runOn(LoopResources.create("reactor-webclient-" + poolName))
                .metrics(metricsEnabled, s -> "disabled");
        httpClient.warmup().block();
        return new ReactorClientHttpConnector(httpClient);
    }
}
