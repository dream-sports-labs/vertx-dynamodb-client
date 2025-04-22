## Installation & Usage

* Add to `pom.xml`

  ```xml
    <dependency>
      <groupId>com.dream11</groupId>
      <artifactId>vertx-dynamodb-client</artifactId>
      <version>1.0.0</version>
    </dependency>
  ```

* Create `resources/config/dynamo/dynamo-default` to override the default values

  Sample configuration file

  ```hocon
    maxConcurrency=1000
    readTimeoutMillis=200
    numberOfRetry=1
    apiCallAttemptTimeoutMillis=200
    apiCallTimeoutMillis=100
    connectionAcquireTimeoutMillis=100
    connectionTimeoutMillis=100
    connectionIdleTimeoutMins=2
    backOffBaseDelayMillis=5
    backOffMaxDelayMillis=50
    throttleBaseDelayMillis=5
    throttleMaxDelayMillis=50
    keepAliveIntervalMillis=150
    keepAliveTimeoutMillis=500
  ```

* Sample code to use the library
  ```java
    public class DynamoDAO {

      private final DynamoTable<OrderHistory> orderHistory;
  
      private DynamoTable<Orders> orders;
  
      private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
  
      private final DynamoTable<OrderHistory> orderHistory1;
  
      public Single<TableStatus> ordersHealthCheck() {
        return orders.getTableDetails();
      }
  
      public DynamoDAO() {
        this.dynamoDbEnhancedAsyncClient = DynamoDBClient.getDynamoDbEnhancedAsyncClient();
        this.orders = new DynamoTable<>("orders", Orders.class);
        this.orderHistory = new DynamoTable<>("order_history", OrderHistory.class);
        this.orderHistory1 = new DynamoTable<>("order_history1", OrderHistory.class);
      }
  
      public Single<Boolean> insertNewOrder(String status, Integer roundId, Long userId, String randomUUID) {
        Orders order = Orders.builder()
            .id(randomUUID)
            .orderStatus(OrderStatus.valueOf(status))
            .roundId(roundId)
            .userId(userId)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
        return orders.insertData(order);
      }
  
      public Single<Orders> getOrderById(String orderId) {
        return orders.getItem(orderId, true);
      }

      public Single<List<Orders>> fetchData(Long userId) {
        return orders.getItems(
          ConditionalQuery.builder().consistentRead(true)
              .limit(8)
              .partitionKey(userId)
              .build());
      }

      public Single<Boolean> updateOrder(String orderId, Long userId) {
        return orders.updateData(
            Orders.builder()
                .id(orderId)
                .userId(userId)
                .orderStatus(OrderStatus.valueOf("completed"))
                .updatedAt(System.currentTimeMillis())
                .build(), true);
      }
  }

* Sample code to use a custom config provider for dynamoDbClient

 ```java
public class CustomConfigProvider implements IConfigProvider {
  @Override
  public DynamoConfig getConfig() {
    // custom config creation logic
  }
}
```

* You can then pass this custom config provider while creating the DynamoDBClient

```java
public DynamoDAO() {
      this.dynamoDbEnhancedAsyncClient = DynamoDBClient.getDynamoDbEnhancedAsyncClient(new CustomConfigProvider());
      this.orders = new DynamoTable<>("orders", Orders.class);
      this.orderHistory = new DynamoTable<>("order_history", OrderHistory.class);
      this.orderHistory1 = new DynamoTable<>("order_history1", OrderHistory.class);
    }
```