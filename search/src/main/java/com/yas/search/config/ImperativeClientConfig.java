package com.yas.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.yas.search.repository")
@ComponentScan(basePackages = "com.yas.search.service")
@RequiredArgsConstructor
public class ImperativeClientConfig {

    private final ElasticsearchDataConfig elasticsearchConfig;

    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {
        String url = elasticsearchConfig.getUrl();
        HttpHost httpHost;
        try {
            httpHost = HttpHost.create(url);
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException("Invalid Elasticsearch URL: " + url, e);
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            new org.apache.hc.client5.http.auth.AuthScope(httpHost.getHostName(), httpHost.getPort()),
            new UsernamePasswordCredentials(
                elasticsearchConfig.getUsername(),
                elasticsearchConfig.getPassword().toCharArray()));

        Rest5Client rest5Client = Rest5Client.builder(httpHost)
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .addRequestInterceptorLast((request, entity, context) -> {
                        // Override vendor media type headers set by ElasticsearchTransportBase
                        // ES 8.8.1 rejects application/vnd.elasticsearch+json;compatible-with=8
                        request.removeHeaders("Content-Type");
                        request.removeHeaders("Accept");
                        request.addHeader("Content-Type", "application/json");
                        request.addHeader("Accept", "application/json");
                    }))
            .build();

        Rest5ClientTransport transport = new Rest5ClientTransport(rest5Client, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
