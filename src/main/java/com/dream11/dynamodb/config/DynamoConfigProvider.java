package com.dream11.dynamodb.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;

public class DynamoConfigProvider implements IConfigProvider {

  @Override
  public DynamoConfig getConfig() {
    String environment =
        java.util.Optional.ofNullable(System.getProperty("app.environment")).orElse("default");
    String configFile = "config/dynamo/dynamo-" + environment + ".conf";
    Config config =
        ConfigFactory.load(configFile)
            .withFallback(ConfigFactory.load("config/dynamo/dynamo-default.conf"));

    return ConfigBeanFactory.create(config, DynamoConfig.class);
  }
}
