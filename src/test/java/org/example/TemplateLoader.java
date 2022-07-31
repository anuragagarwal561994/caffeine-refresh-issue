package org.example;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import java.util.concurrent.CompletableFuture;

public class TemplateLoader {
    private final AsyncLoadingCache<String, Result> cache;

    public TemplateLoader(AsyncLoadingCache<String, Result> cache) {
        this.cache = cache;
    }

    public Result loadTemplate(String key) {
        CompletableFuture<Result> future = cache.get(key);
        return future.getNow(Result.IN_PROGRESS);
    }
}
