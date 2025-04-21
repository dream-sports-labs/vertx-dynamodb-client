package com.dream11.dynamodb;

import static com.dream11.dynamodb.utils.DynamoUtils.getAttributeValue;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Builder
@Data
public class ConditionalQuery {
  private Object partitionKey;

  private Object sortKeyStart;

  private Object sortKeyEnd;

  private Object beginsWith;

  private Integer limit;

  private Boolean consistentRead;

  private Boolean scanIndexForward;

  private QueryConditional getConditionalQuery() {
    if (partitionKey == null) {
      throw new IllegalArgumentException("Partition key cannot be null");
    }
    if (beginsWith != null) {
      return QueryConditional.sortBeginsWith(
          k ->
              k.partitionValue(getAttributeValue(partitionKey))
                  .sortValue(getAttributeValue(beginsWith)));
    } else if (sortKeyStart == null && sortKeyEnd == null) {
      return QueryConditional.keyEqualTo(k -> k.partitionValue(getAttributeValue(partitionKey)));
    } else if (sortKeyEnd == null) {
      return QueryConditional.sortGreaterThanOrEqualTo(
          k ->
              k.partitionValue(getAttributeValue(partitionKey))
                  .sortValue(getAttributeValue(sortKeyStart)));
    } else if (sortKeyStart == null) {
      return QueryConditional.sortLessThanOrEqualTo(
          k ->
              k.partitionValue(getAttributeValue(partitionKey))
                  .sortValue(getAttributeValue(sortKeyEnd)));
    } else {
      return QueryConditional.sortBetween(
          k ->
              k.partitionValue(getAttributeValue(partitionKey))
                  .sortValue(getAttributeValue(sortKeyStart)),
          l ->
              l.partitionValue(getAttributeValue(partitionKey))
                  .sortValue(getAttributeValue(sortKeyEnd)));
    }
  }

  public QueryEnhancedRequest getQueryEnhancedRequest(
      Map<String, AttributeValue> exclusiveStartKey, Integer limit) {
    return QueryEnhancedRequest.builder()
        .queryConditional(getConditionalQuery())
        .limit(limit)
        .exclusiveStartKey(exclusiveStartKey.isEmpty() ? null : exclusiveStartKey)
        .consistentRead(consistentRead)
        .scanIndexForward(scanIndexForward)
        .build();
  }
}
