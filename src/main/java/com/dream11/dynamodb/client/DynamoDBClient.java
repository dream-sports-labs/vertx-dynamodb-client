package com.dream11.dynamodb.client;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class DynamoDBClient {
  private static volatile DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
  private static volatile DynamoDbAsyncClient dynamoDbAsyncClient;

  public static DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() {
    if (dynamoDbEnhancedAsyncClient == null) {
      synchronized (DynamoDbEnhancedAsyncClient.class) {
        if (dynamoDbEnhancedAsyncClient == null) {
          dynamoDbEnhancedAsyncClient = buildEnhancedAsyncClient();
        }
      }
    }
    return dynamoDbEnhancedAsyncClient;
  }

  public static DynamoDbAsyncClient getDynamoDbAsyncClient() {
    if (dynamoDbAsyncClient == null) {
      synchronized (DynamoDbAsyncClient.class) {
        if (dynamoDbAsyncClient == null) {
          dynamoDbAsyncClient = DynamoAsyncClientProvider.getAsyncClient();
        }
      }
    }
    return dynamoDbAsyncClient;
  }

  private static DynamoDbEnhancedAsyncClient buildEnhancedAsyncClient() {
    return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(getDynamoDbAsyncClient()).build();
  }
}
