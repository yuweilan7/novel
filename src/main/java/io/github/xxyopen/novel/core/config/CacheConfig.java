package io.github.xxyopen.novel.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.xxyopen.novel.core.constant.CacheConsts;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 缓存配置类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Configuration
public class CacheConfig {

    /**
     * Caffeine 缓存管理器
     */
    @Bean  // 将方法的返回值注入为 Bean
    @Primary  // 设置该 Bean 为默认的 CacheManager
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 创建一个空的 CaffeineCache 列表
        /**
         * CacheEnum.values() 是 Java 中的枚举类型方法，
         * 用于获取该枚举类型中所有的枚举值，返回值为一个包含所有枚举值的数组
         */
        List<CaffeineCache> caches = new ArrayList<>(CacheConsts.CacheEnum.values().length);

        // 遍历所有缓存类型
        for (var c : CacheConsts.CacheEnum.values()) {
            if (c.isLocal()) {  // 如果是本地缓存
                /**
                 * Caffeine.newBuilder(): 创建一个Caffeine实例的构造器。
                 * recordStats(): 记录缓存的统计信息，例如命中率、缓存项被回收的原因等。
                 * maximumSize(c.getMaxSize()): 设置缓存的最大容量，即缓存中能够存储的最大的缓存项的数量。
                 */
                Caffeine<Object, Object> caffeine = Caffeine.newBuilder().recordStats().maximumSize(c.getMaxSize());
                if (c.getTtl() > 0) {
                    /**
                     * expireAfterWrite() 方法用来设置写入后的存活时间。
                     * 它的参数是一个 java.time.Duration 类型的时间段，可以表示任意长度的时间间隔。
                     */
                    caffeine.expireAfterWrite(Duration.ofSeconds(c.getTtl()));
                }
                // 构造 CaffeineCache 对象，并添加到缓存列表,参数是缓存的名称和一个 Caffeine 对象
                caches.add(new CaffeineCache(c.getName(), caffeine.build()));
            }
        }

        // 设置缓存列表并返回 CacheManager
        cacheManager.setCaches(caches);
        return cacheManager;
    }


    /**
     * Redis 缓存管理器
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        /**
         * RedisCacheWriter 是一个接口，用于将 Redis 用作缓存的缓存写入器。它定义了用于访问和更新 Redis 的 API。
         * 该实现是非阻塞式写入器，可以在不进行锁定的情况下执行所有 Redis 缓存写操作
         */
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(
            connectionFactory);
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues().prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX);
        //缓存名称和缓存配置的映射表
        Map<String, RedisCacheConfiguration> cacheMap = new LinkedHashMap<>(
            CacheConsts.CacheEnum.values().length);
        // 类型推断 var 非常适合 for 循环，JDK 10 引入，JDK 11 改进
        for (var c : CacheConsts.CacheEnum.values()) {
            if (c.isRemote()) {
                if (c.getTtl() > 0) {
                    cacheMap.put(c.getName(),
                        RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                            .prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX)
                            .entryTtl(Duration.ofSeconds(c.getTtl())));
                } else {
                    cacheMap.put(c.getName(),
                        RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                            .prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX));
                }
            }
        }

        RedisCacheManager redisCacheManager = new RedisCacheManager(redisCacheWriter,
            defaultCacheConfig, cacheMap);
        /**
         * 如果在使用Spring事务管理时，同时需要对缓存进行操作，那么将缓存管理器配置为事务感知
         * 可以确保事务的一致性，即保证事务回滚时缓存操作也会被回滚。
         */
        redisCacheManager.setTransactionAware(true);
        redisCacheManager.initializeCaches();
        return redisCacheManager;
    }


}
