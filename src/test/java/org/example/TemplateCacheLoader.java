package org.example;

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.checkerframework.checker.nullness.qual.Nullable;

class TemplateCacheLoader implements CacheLoader<String, Result> {
    private final HttpResultLoader httpResultLoader;

    TemplateCacheLoader(HttpResultLoader httpResultLoader) {
        this.httpResultLoader = httpResultLoader;
    }

    @Override
    public @Nullable Result load(String key) {
        return httpResultLoader.getResult(key);
    }
}
