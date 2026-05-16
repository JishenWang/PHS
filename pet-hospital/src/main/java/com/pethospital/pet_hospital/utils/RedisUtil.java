package com.pethospital.pet_hospital.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis缓存工具类
 * 提供常用的Redis操作，Redis不可用时自动降级为内存缓存
 */
@Component
@SuppressWarnings("null")
@Slf4j
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 内存后备缓存（Redis不可用时使用）
    private final ConcurrentHashMap<String, Object> localCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> localCacheExpire = new ConcurrentHashMap<>();
    private final ScheduledExecutorService expireCleaner = Executors.newSingleThreadScheduledExecutor();

    public RedisUtil() {
        // 每30秒清理一次过期的内存缓存
        expireCleaner.scheduleAtFixedRate(this::cleanExpiredLocalCache, 30, 30, TimeUnit.SECONDS);
    }

    private void cleanExpiredLocalCache() {
        long now = System.currentTimeMillis();
        localCacheExpire.entrySet().removeIf(entry -> {
            if (entry.getValue() != null && entry.getValue() < now) {
                localCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    private boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置缓存（不过期）
     */
    public void set(String key, Object value) {
        try {
            if (isRedisAvailable()) {
                redisTemplate.opsForValue().set(key, value);
            } else {
                localCache.put(key, value);
                localCacheExpire.remove(key);
            }
        } catch (Exception e) {
            log.warn("Redis set 失败，降级到内存缓存: {}", key);
            localCache.put(key, value);
            localCacheExpire.remove(key);
        }
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (isRedisAvailable()) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                localCache.put(key, value);
                localCacheExpire.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
            }
        } catch (Exception e) {
            log.warn("Redis set 失败，降级到内存缓存: {}", key);
            localCache.put(key, value);
            localCacheExpire.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        }
    }

    /**
     * 设置缓存（过期时间，秒）
     */
    public void setWithExpire(String key, Object value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            if (isRedisAvailable()) {
                return redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            log.warn("Redis get 失败，降级到内存缓存: {}", key);
        }
        // 检查内存缓存是否过期
        Long expire = localCacheExpire.get(key);
        if (expire != null && expire < System.currentTimeMillis()) {
            localCache.remove(key);
            localCacheExpire.remove(key);
            return null;
        }
        return localCache.get(key);
    }

    /**
     * 获取缓存并转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) return null;
        return (T) value;
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        boolean result = false;
        try {
            if (isRedisAvailable()) {
                result = Boolean.TRUE.equals(redisTemplate.delete(key));
            }
        } catch (Exception e) {
            log.warn("Redis delete 失败: {}", key);
        }
        localCache.remove(key);
        localCacheExpire.remove(key);
        return result;
    }

    /**
     * 判断是否存在
     */
    public boolean hasKey(String key) {
        try {
            if (isRedisAvailable()) {
                return Boolean.TRUE.equals(redisTemplate.hasKey(key));
            }
        } catch (Exception e) {
            log.warn("Redis hasKey 失败: {}", key);
        }
        Long expire = localCacheExpire.get(key);
        if (expire != null && expire < System.currentTimeMillis()) {
            localCache.remove(key);
            localCacheExpire.remove(key);
            return false;
        }
        return localCache.containsKey(key);
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (isRedisAvailable()) {
                return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
            }
        } catch (Exception e) {
            log.warn("Redis expire 失败: {}", key);
        }
        localCacheExpire.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        return true;
    }

    /**
     * 获取剩余过期时间（秒）
     */
    public Long getExpire(String key) {
        try {
            if (isRedisAvailable()) {
                return redisTemplate.getExpire(key);
            }
        } catch (Exception e) {
            log.warn("Redis getExpire 失败: {}", key);
        }
        Long expire = localCacheExpire.get(key);
        if (expire == null) return -2L;
        long remain = (expire - System.currentTimeMillis()) / 1000;
        return remain > 0 ? remain : -2L;
    }

    /**
     * 递增
     */
    public Long increment(String key, long delta) {
        try {
            if (isRedisAvailable()) {
                return redisTemplate.opsForValue().increment(key, delta);
            }
        } catch (Exception e) {
            log.warn("Redis increment 失败: {}", key);
        }
        Object val = localCache.get(key);
        long newVal = (val instanceof Number) ? ((Number) val).longValue() + delta : delta;
        localCache.put(key, newVal);
        return newVal;
    }

    /**
     * 递减
     */
    public Long decrement(String key, long delta) {
        return increment(key, -delta);
    }

    /**
     * 清除所有缓存（Redis + 本地缓存）
     */
    public void clearAll() {
        try {
            if (isRedisAvailable()) {
                java.util.Set<String> keys = redisTemplate.keys("*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info("已清除 Redis 缓存 {} 条", keys.size());
                }
            }
        } catch (Exception e) {
            log.warn("清除 Redis 缓存失败: {}", e.getMessage());
        }
        localCache.clear();
        localCacheExpire.clear();
        log.info("已清除本地缓存");
    }
}
