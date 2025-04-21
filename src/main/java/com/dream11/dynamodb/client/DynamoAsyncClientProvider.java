package com.dream11.dynamodb.client;

import java.time.Duration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.TcpKeepAliveConfiguration;
import software.amazon.awssdk.retries.AdaptiveRetryStrategy;
import software.amazon.awssdk.retries.api.BackoffStrategy;
import software.amazon.awssdk.retries.api.RetryStrategy;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class DynamoAsyncClientProvider {

  public static DynamoDbAsyncClient getAsyncClient() {
    DynamoConfig dynamoConfig = DynamoConfigProvider.getConfig();
    SdkAsyncHttpClient sdkAsyncHttpClient =
        AwsCrtAsyncHttpClient.builder()
            .maxConcurrency(dynamoConfig.getMaxConcurrency())
            .connectionTimeout(Duration.ofMillis(dynamoConfig.getConnectionTimeoutMillis()))
            .connectionMaxIdleTime(Duration.ofMinutes(dynamoConfig.getConnectionIdleTimeoutMins()))
            .tcpKeepAliveConfiguration(
                TcpKeepAliveConfiguration.builder()
                    .keepAliveInterval(Duration.ofMillis(dynamoConfig.getKeepAliveIntervalMillis()))
                    .keepAliveTimeout(Duration.ofMillis(dynamoConfig.getKeepAliveTimeoutMillis()))
                    .build())
            .build();

    RetryStrategy retryStrategy =
        AdaptiveRetryStrategy.builder()
            .maxAttempts(dynamoConfig.getNumberOfRetry())
            .backoffStrategy(
                BackoffStrategy.exponentialDelayWithoutJitter(
                    Duration.ofMillis(dynamoConfig.getBackOffBaseDelayMillis()),
                    Duration.ofMillis(dynamoConfig.getBackOffMaxDelayMillis())))
            .throttlingBackoffStrategy(
                BackoffStrategy.exponentialDelayWithoutJitter(
                    Duration.ofMillis(dynamoConfig.getThrottleBaseDelayMillis()),
                    Duration.ofMillis(dynamoConfig.getThrottleMaxDelayMillis())))
            .build();

    ClientOverrideConfiguration clientOverrideConfiguration =
        ClientOverrideConfiguration.builder()
            .retryStrategy(retryStrategy)
            //
            // .apiCallAttemptTimeout(Duration.ofMillis(dynamoConfig.getApiCallAttemptTimeoutMillis()))
            //            .apiCallTimeout(Duration.ofMillis(dynamoConfig.getApiCallTimeoutMillis()))
            .build();

    return DynamoDbAsyncClient.builder()
        .overrideConfiguration(clientOverrideConfiguration)
        .httpClient(sdkAsyncHttpClient)
        .build();
  }
}
