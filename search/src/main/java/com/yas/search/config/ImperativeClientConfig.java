package com.yas.search.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.yas.search.repository")
@ComponentScan(basePackages = "com.yas.search.service")
@RequiredArgsConstructor
public class ImperativeClientConfig extends ElasticsearchConfiguration {

    private final ElasticsearchDataConfig elasticsearchConfig;

    @Override
    public ClientConfiguration clientConfiguration() {
        String originalUrl = elasticsearchConfig.getUrl();
        boolean useSSL = originalUrl.startsWith("https://");
        String host = originalUrl
            .replace("https://", "")
            .replace("http://", "");

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
            ClientConfiguration.builder().connectedTo(host);

        if (useSSL) {
            try {
                // Trust all certificates (disable verification for dev/self-signed certs)
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }}, null);
                builder = builder.usingSsl(sslContext);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create SSL context", e);
            }
        }

        return builder
                .withBasicAuth(elasticsearchConfig.getUsername(),
                            elasticsearchConfig.getPassword())
                .build();
    }
}
