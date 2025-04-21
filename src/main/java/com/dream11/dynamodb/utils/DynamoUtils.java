package com.dream11.dynamodb.utils;

import java.nio.ByteBuffer;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoUtils {
  public static AttributeValue getAttributeValue(Object key) {
    if (key == null) {
      return AttributeValues.nullAttributeValue();
    } else if (key instanceof String) {
      return AttributeValues.stringValue((String) key);
    } else if (key instanceof Number) {
      return AttributeValues.numberValue((Number) key);
    } else if (key instanceof ByteBuffer) {
      return AttributeValues.binaryValue(SdkBytes.fromByteBuffer((ByteBuffer) key));
    } else
      throw new IllegalArgumentException(
          String.format("Key type %s not supported", key.getClass()));
  }
}
