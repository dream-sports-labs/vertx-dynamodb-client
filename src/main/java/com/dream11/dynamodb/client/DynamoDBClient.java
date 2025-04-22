package com.dream11.dynamodb.client;

import com.dream11.dynamodb.config.DynamoConfigProvider;
import com.dream11.dynamodb.config.IConfigProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class DynamoDBClient {
  private static volatile DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
  private static volatile DynamoDbAsyncClient dynamoDbAsyncClient;

  public static DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient(
      IConfigProvider configProvider) {
    if (dynamoDbEnhancedAsyncClient == null) {
      synchronized (DynamoDbEnhancedAsyncClient.class) {
        if (dynamoDbEnhancedAsyncClient == null) {
          dynamoDbEnhancedAsyncClient = buildEnhancedAsyncClient(configProvider);
        }
      }
    }
    return dynamoDbEnhancedAsyncClient;
  }

  public static DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() {
    return getDynamoDbEnhancedAsyncClient(new DynamoConfigProvider());
  }

  public static DynamoDbAsyncClient getDynamoDbAsyncClient(IConfigProvider configProvider) {
    if (dynamoDbAsyncClient == null) {
      synchronized (DynamoDbAsyncClient.class) {
        if (dynamoDbAsyncClient == null) {
          dynamoDbAsyncClient = DynamoAsyncClientProvider.getAsyncClient(configProvider);
        }
      }
    }
    return dynamoDbAsyncClient;
  }

  public static DynamoDbAsyncClient getDynamoDbAsyncClient() {
    return getDynamoDbAsyncClient(new DynamoConfigProvider());
  }

  private static DynamoDbEnhancedAsyncClient buildEnhancedAsyncClient(
      IConfigProvider configProvider) {
    return DynamoDbEnhancedAsyncClient.builder()
        .dynamoDbClient(getDynamoDbAsyncClient(configProvider))
        .build();
  }

  private static DynamoDbEnhancedAsyncClient buildEnhancedAsyncClient() {
    return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(getDynamoDbAsyncClient()).build();
  }
}
