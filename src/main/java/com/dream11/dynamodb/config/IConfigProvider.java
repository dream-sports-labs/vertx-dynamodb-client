package com.dream11.dynamodb.config;

public interface IConfigProvider {
  /**
   * Get the configuration for the DynamoDB client.
   *
   * @return the configuration for the DynamoDB client
   */
  DynamoConfig getConfig();
}
