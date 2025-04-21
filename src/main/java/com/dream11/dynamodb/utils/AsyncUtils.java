package com.dream11.dynamodb.utils;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.reactivex.SingleHelper;
import java.util.concurrent.CompletableFuture;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public final class AsyncUtils {
  public static <T> Single<T> toSingle(CompletableFuture<T> future) {
    return toSingle(toVertxCompletableFuture(future));
  }

  public static <T> VertxCompletableFuture<T> toVertxCompletableFuture(
      CompletableFuture<T> future) {
    return VertxCompletableFuture.from(Vertx.currentContext(), future);
  }

  public static <T> Single<T> toSingle(VertxCompletableFuture<T> vertxFuture) {
    return SingleHelper.toSingle(
        (asyncResultHandler) -> {
          vertxFuture.toFuture().onComplete(asyncResultHandler);
        });
  }
}
