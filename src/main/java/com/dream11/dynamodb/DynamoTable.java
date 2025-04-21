package com.dream11.dynamodb;

import static com.dream11.dynamodb.utils.DynamoUtils.getAttributeValue;

import com.dream11.dynamodb.client.DynamoDBClient;
import com.dream11.dynamodb.utils.AsyncUtils;
import com.dream11.dynamodb.utils.Constant;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.IgnoreNullsMode;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

@Getter
public class DynamoTable<T> {
  private final DynamoDbAsyncTable<T> dynamoDbAsyncTable;
  private final Class<T> type;
  private final DynamoDbEnhancedAsyncClient dynamoDbClient;

  public DynamoTable(String tableName, Class<T> type) {
    this.dynamoDbClient = DynamoDBClient.getDynamoDbEnhancedAsyncClient();
    this.dynamoDbAsyncTable = this.dynamoDbClient.table(tableName, TableSchema.fromBean(type));
    this.type = type;
  }

  private BatchGetItemEnhancedRequest buildBatchGetItemRequest(List<Key> keys) {
    ReadBatch.Builder<T> readBatchBuilder = ReadBatch.builder(this.type);
    readBatchBuilder.mappedTableResource(this.dynamoDbAsyncTable);
    for (Key key : keys) {
      readBatchBuilder.addGetItem(GetItemEnhancedRequest.builder().key(key).build());
    }
    return BatchGetItemEnhancedRequest.builder().addReadBatch(readBatchBuilder.build()).build();
  }

  public Single<List<T>> batchGetItem(List<Key> keys) {
    AtomicReference<List<Key>> unprocessedKeys = new AtomicReference<>(keys);

    return Observable.defer(
            () ->
                Observable.fromPublisher(
                    dynamoDbClient.batchGetItem(buildBatchGetItemRequest(unprocessedKeys.get()))))
        .flatMap(
            batchGetResultPage -> {
              unprocessedKeys.set(
                  batchGetResultPage.unprocessedKeysForTable(this.dynamoDbAsyncTable));
              return Observable.fromIterable(
                  batchGetResultPage.resultsForTable(this.dynamoDbAsyncTable));
            })
        .repeatUntil(() -> unprocessedKeys.get().isEmpty())
        .toList();
  }

  private BatchWriteItemEnhancedRequest buildBatchPutItemRequest(List<T> putRequests) {
    WriteBatch.Builder<T> writeBatchBuilder = WriteBatch.builder(this.type);
    for (T putItem : putRequests) {
      writeBatchBuilder.addPutItem(putItem);
    }
    return BatchWriteItemEnhancedRequest.builder().addWriteBatch(writeBatchBuilder.build()).build();
  }

  private BatchWriteItemEnhancedRequest buildBatchDeleteItemRequest(List<Key> deleteRequests) {
    WriteBatch.Builder<T> writeBatchBuilder = WriteBatch.builder(this.type);

    for (Key deleteItem : deleteRequests) {
      writeBatchBuilder.addDeleteItem(deleteItem);
    }
    return BatchWriteItemEnhancedRequest.builder().addWriteBatch(writeBatchBuilder.build()).build();
  }

  public Single<Boolean> batchPutItem(List<T> putItemRequsts) {
    AtomicReference<List<T>> putUnprocessedKeys = new AtomicReference<>(putItemRequsts);

    return Single.defer(
            () ->
                AsyncUtils.toSingle(
                    dynamoDbClient.batchWriteItem(
                        buildBatchPutItemRequest(putUnprocessedKeys.get()))))
        .flatMapCompletable(
            batchWriteResult -> {
              putUnprocessedKeys.set(
                  batchWriteResult.unprocessedPutItemsForTable(this.dynamoDbAsyncTable));
              return Completable.complete();
            })
        .repeatUntil(() -> putUnprocessedKeys.get().isEmpty())
        .andThen(Single.just(true));
  }

  public Single<Boolean> batchDeleteItem(List<Key> deleteRequests) {
    AtomicReference<List<Key>> deleteUnprocessedKeys = new AtomicReference<>(deleteRequests);

    return Single.defer(
            () ->
                AsyncUtils.toSingle(
                    dynamoDbClient.batchWriteItem(
                        buildBatchDeleteItemRequest(deleteUnprocessedKeys.get()))))
        .flatMapCompletable(
            batchWriteResult -> {
              deleteUnprocessedKeys.set(
                  batchWriteResult.unprocessedDeleteItemsForTable(this.dynamoDbAsyncTable));
              return Completable.complete();
            })
        .repeatUntil(() -> deleteUnprocessedKeys.get().isEmpty())
        .andThen(Single.just(true));
  }

  public Single<T> getItem(Key key, Boolean consistentRead) {
    return getItem(
        GetItemEnhancedRequest.builder().key(key).consistentRead(consistentRead).build());
  }

  public Single<T> getItem(Object partitionKey, Boolean consistentRead) {
    GetItemEnhancedRequest getItemEnhancedRequest =
        GetItemEnhancedRequest.builder()
            .key(k -> k.partitionValue(getAttributeValue(partitionKey)))
            .consistentRead(consistentRead)
            .build();

    return getItem(getItemEnhancedRequest);
  }

  public Single<T> getItem(Object partitionKey, Object sortKey, Boolean consistentRead) {
    GetItemEnhancedRequest getItemEnhancedRequest =
        GetItemEnhancedRequest.builder()
            .key(
                k ->
                    k.partitionValue(getAttributeValue(partitionKey))
                        .sortValue(getAttributeValue(sortKey)))
            .consistentRead(consistentRead)
            .build();

    return getItem(getItemEnhancedRequest);
  }

  public Single<T> getItem(GetItemEnhancedRequest getItemEnhancedRequest) {
    return AsyncUtils.toSingle(dynamoDbAsyncTable.getItem(getItemEnhancedRequest))
        .filter(Objects::nonNull)
        .switchIfEmpty(
            Single.error(DynamoDbException.builder().message(Constant.RECORD_NOT_FOUND).build()));
  }

  public Single<TableStatus> getTableDetails() {
    return AsyncUtils.toSingle(dynamoDbAsyncTable.describeTable())
        .map(describeTableResponse -> describeTableResponse.table().tableStatus());
  }

  public Single<Boolean> insertData(T object) {
    return AsyncUtils.toSingle(dynamoDbAsyncTable.putItem(object).thenApply(x -> true));
  }

  public Single<Boolean> updateData(T updatedItem, Boolean ignoreNulls) {
    UpdateItemEnhancedRequest<T> updateItemEnhancedRequest =
        UpdateItemEnhancedRequest.builder(this.type)
            .item(updatedItem)
            .ignoreNullsMode(ignoreNulls ? IgnoreNullsMode.SCALAR_ONLY : IgnoreNullsMode.DEFAULT)
            .build();

    return AsyncUtils.toSingle(
        dynamoDbAsyncTable.updateItem(updateItemEnhancedRequest).thenApply(x -> true));
  }

  public Single<Boolean> updateData(T updatedItem) {
    return updateData(updatedItem, true);
  }

  private Single<List<T>> getItemsWithLimit(ConditionalQuery conditionalQuery, String indexName) {
    AtomicReference<Map<String, AttributeValue>> lastEvaluatedKey =
        new AtomicReference<>(new HashMap<>());
    AtomicInteger limit = new AtomicInteger(conditionalQuery.getLimit());

    return Observable.defer(
            () ->
                Objects.nonNull(indexName)
                    ? Observable.fromPublisher(
                        dynamoDbAsyncTable
                            .index(indexName)
                            .query(
                                conditionalQuery.getQueryEnhancedRequest(
                                    lastEvaluatedKey.get(), limit.get()))
                            .limit(1))
                    : Observable.fromPublisher(
                        dynamoDbAsyncTable
                            .query(
                                conditionalQuery.getQueryEnhancedRequest(
                                    lastEvaluatedKey.get(), limit.get()))
                            .limit(1)))
        .observeOn(RxHelper.scheduler(Vertx.currentContext()))
        .flatMap(
            tPage -> {
              lastEvaluatedKey.set(tPage.lastEvaluatedKey());
              limit.set(limit.get() - tPage.items().size());
              return Observable.fromIterable(tPage.items());
            })
        .repeatUntil(() -> lastEvaluatedKey.get() == null || limit.get() <= 0)
        .toList();
  }

  private Single<List<T>> getItemsWithoutLimit(
      ConditionalQuery conditionalQuery, String indexName) {
    AtomicReference<Map<String, AttributeValue>> lastEvaluatedKey =
        new AtomicReference<>(new HashMap<>());

    return Observable.defer(
            () ->
                Objects.nonNull(indexName)
                    ? Observable.fromPublisher(
                        dynamoDbAsyncTable
                            .index(indexName)
                            .query(
                                conditionalQuery.getQueryEnhancedRequest(
                                    lastEvaluatedKey.get(), null))
                            .limit(1))
                    : Observable.fromPublisher(
                        dynamoDbAsyncTable
                            .query(
                                conditionalQuery.getQueryEnhancedRequest(
                                    lastEvaluatedKey.get(), null))
                            .limit(1)))
        .observeOn(RxHelper.scheduler(Vertx.currentContext()))
        .flatMap(
            tPage -> {
              lastEvaluatedKey.set(tPage.lastEvaluatedKey());
              return Observable.fromIterable(tPage.items());
            })
        .repeatUntil(() -> lastEvaluatedKey.get() == null)
        .toList();
  }

  public Single<List<T>> getItems(ConditionalQuery conditionalQuery, String indexName) {
    return conditionalQuery.getLimit() == null
        ? getItemsWithoutLimit(conditionalQuery, indexName)
        : getItemsWithLimit(conditionalQuery, indexName);
  }

  public Single<List<T>> getItems(ConditionalQuery conditionalQuery) {
    return conditionalQuery.getLimit() == null
        ? getItemsWithoutLimit(conditionalQuery, null)
        : getItemsWithLimit(conditionalQuery, null);
  }
}
