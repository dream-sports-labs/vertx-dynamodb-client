package com.dream11.dynamodb.config;

import com.typesafe.config.Optional;
import lombok.Data;
import lombok.NonNull;

@Data
public class DynamoConfig {

  static final Integer MAX_CONCURRENCY = 10;
  static final Integer READ_TIMEOUT_IN_MILLIS = 2000;
  static final Integer RETRY_COUNT = 1;

  static final Integer API_CALL_ATTEMPT_TIMEOUT_IN_MILLIS = 200;
  static final Integer API_CALL_TIMEOUT_IN_MILLIS = 100;
  static final Integer CONNECT_TIMEOUT_IN_MILLIS = 10000;
  static final Integer CONNECTION_ACQUIRE_TIMEOUT_IN_MILLIS = 100;
  static final Integer CONNECTION_IDLE_TIMEOUT_IN_MINUTES = 2;
  static final Integer KEEP_ALIVE_TIMEOUT_IN_MILLIS = 550;

  // Backpressure configs
  static final Integer BACK_OFF_BASE_DELAY_IN_MILLIS = 5;
  static final Integer BACK_OFF_MAX_DELAY_IN_MILLIS = 50;
  static final Integer THROTTLE_BASE_DELAY_IN_MILLIS = 5;
  static final Integer THROTTLE_MAX_DELAY_IN_MILLIS = 50;
  static final Integer KEEP_ALIVE_INTERVAL_IN_MILLIS = 150;

  @NonNull @Optional Integer maxConcurrency = MAX_CONCURRENCY;
  @NonNull @Optional Integer readTimeoutMillis = READ_TIMEOUT_IN_MILLIS;
  @NonNull @Optional Integer numberOfRetry = RETRY_COUNT;
  @NonNull @Optional Integer apiCallAttemptTimeoutMillis = API_CALL_ATTEMPT_TIMEOUT_IN_MILLIS;
  @NonNull @Optional Integer apiCallTimeoutMillis = API_CALL_TIMEOUT_IN_MILLIS;
  @NonNull @Optional Integer connectionTimeoutMillis = CONNECT_TIMEOUT_IN_MILLIS;
  @NonNull @Optional Integer connectionAcquireTimeoutMillis = CONNECTION_ACQUIRE_TIMEOUT_IN_MILLIS;
  @NonNull @Optional Integer connectionIdleTimeoutMins = CONNECTION_IDLE_TIMEOUT_IN_MINUTES;
  @NonNull @Optional Integer backOffBaseDelayMillis = BACK_OFF_BASE_DELAY_IN_MILLIS;
  @NonNull @Optional Integer backOffMaxDelayMillis = BACK_OFF_MAX_DELAY_IN_MILLIS;
  @NonNull @Optional Integer throttleBaseDelayMillis = THROTTLE_BASE_DELAY_IN_MILLIS;
  @NonNull @Optional Integer throttleMaxDelayMillis = THROTTLE_MAX_DELAY_IN_MILLIS;
  @NonNull @Optional Integer keepAliveIntervalMillis = KEEP_ALIVE_INTERVAL_IN_MILLIS;
  @NonNull @Optional Integer keepAliveTimeoutMillis = KEEP_ALIVE_TIMEOUT_IN_MILLIS;
}
