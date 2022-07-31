package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.testing.FakeTicker;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestAsync {
    private static final String KEY = "test";

    private final FakeTicker fakeTicker = new FakeTicker();

    private final HttpResultLoader resultLoader = Mockito.mock(HttpResultLoader.class);

    private final TemplateCacheLoader templateCacheLoader = new TemplateCacheLoader(resultLoader);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AsyncLoadingCache<String, Result> cache = Caffeine.newBuilder()
        .refreshAfterWrite(Duration.ofDays(1))
        .executor(executorService)
        .ticker(fakeTicker::read)
        .buildAsync(templateCacheLoader);

    private final TemplateLoader templateLoader = new TemplateLoader(cache);

    @After
    public void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void testSuccessRefreshButNotExpire() {
        when(resultLoader.getResult(KEY)).thenReturn(Result.SUCCESS);
        loopWhileInProgress(templateLoader);
        verify(resultLoader, times(1)).getResult(KEY);
        assertEquals(Result.SUCCESS, templateLoader.loadTemplate(KEY));
        Duration beforeExpiry = Duration.ofMinutes(5);
        fakeTicker.advance(beforeExpiry);
        assertSame(Result.SUCCESS, templateLoader.loadTemplate(KEY));
        when(resultLoader.getResult(KEY)).thenReturn(Result.API_FAILURE);
        Duration afterRefreshTime = Duration.ofDays(2);
        fakeTicker.advance(afterRefreshTime);
        loopWhileStatus(templateLoader, Result.SUCCESS);
        assertSame(Result.API_FAILURE, templateLoader.loadTemplate(KEY));
    }

    private void loopWhileInProgress(TemplateLoader templateLoader) {
        loopWhileStatus(templateLoader, Result.IN_PROGRESS);
    }

    private void loopWhileStatus(TemplateLoader templateLoader, Result currentResult) {
        Result newResult;
        do {
            newResult = templateLoader.loadTemplate(KEY);
        } while (newResult == currentResult);
    }
}
