package be.stijnhooft.portal.weather.cache;

import be.stijnhooft.portal.weather.forecasts.types.Forecast;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class CacheConfiguration {

    @Value("${be.stijnhooft.portal.weather.cache.path:#{null}}")
    private String cachePath;

    @Value("${be.stijnhooft.portal.weather.cache.forecasts.max-no-of-entries:1000}")
    private int forecastsCacheMaxNumberOfEntries;

    @Value("${be.stijnhooft.portal.weather.cache.locations.max-no-of-entries:1000}")
    private int locationsCacheMaxNumberOfEntries;

    @Bean(name = "locationsCache")
    public Cache<String, LocationCacheValues> locationsCache() {
        return buildPersistenceStorageCache("locations", String.class, LocationCacheValues.class, locationsCacheMaxNumberOfEntries);
    }

    @Bean(name = "forecastsCache")
    public Cache<ForecastCacheKey, Forecast> forecastsCache() {
        return buildPersistenceStorageCache("forecasts", ForecastCacheKey.class, Forecast.class, forecastsCacheMaxNumberOfEntries);
    }

    @SuppressWarnings("rawtypes")
    private <K, V> Cache<K, V> buildPersistenceStorageCache(String cacheName, Class<K> keyType, Class<V> valueType, int maxNumberOfEntries) {
        CacheManagerBuilder cacheManagerCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();

        if (cachePath != null) {
            cacheManagerCacheManagerBuilder = cacheManagerCacheManagerBuilder
                    .with(CacheManagerBuilder.persistence(Paths.get(cachePath, cacheName).toAbsolutePath().toString()));
        }

        CacheConfigurationBuilder<K, V> configurationBuilder =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType, ResourcePoolsBuilder.heap(maxNumberOfEntries))
                .withExpiry(ExpiryPolicy.NO_EXPIRY);

        CacheManager cacheManager = cacheManagerCacheManagerBuilder
                .withCache(cacheName, configurationBuilder)
                .build();

        cacheManager.init();
        return cacheManager.getCache(cacheName, keyType, valueType);
    }


}
