/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.metric;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.serialize.BaseSerializer;
import org.miaixz.bus.cache.serialize.Hessian2Serializer;

import jakarta.annotation.PreDestroy;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;

/**
 * Memcached 缓存支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemcachedCache<K, V> implements CacheX<K, V> {

    private static final int _30_DAYS = 30 * 24 * 60 * 60;

    private MemcachedClient client;

    private BaseSerializer serializer;

    public MemcachedCache(String ipPorts) throws IOException {
        this(ipPorts, new Hessian2Serializer());
    }

    public MemcachedCache(String addressList, BaseSerializer serializer) throws IOException {
        client = new XMemcachedClientBuilder(addressList).build();
        this.serializer = serializer;
    }

    @Override
    public V read(K key) {
        try {
            byte[] bytes = client.get((String) key);
            return serializer.deserialize(bytes);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(K key, V value, long expire) {
        byte[] byteValue = serializer.serialize(value);
        try {
            if (expire == CacheExpire.FOREVER) {
                client.set((String) key, _30_DAYS, byteValue);
            } else {
                client.set((String) key, (int) (expire / 1000), byteValue);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<K, V> read(Collection<K> keys) {
        try {
            Map<String, byte[]> byteMap = client.get((Collection<String>) keys);
            Map<K, V> resultMap = new HashMap<>(byteMap.size());
            for (Map.Entry<String, byte[]> entry : byteMap.entrySet()) {
                String key = entry.getKey();
                Object value = serializer.deserialize(entry.getValue());

                resultMap.put((K) key, (V) value);
            }

            return resultMap;
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        for (Map.Entry<K, V> entry : keyValueMap.entrySet()) {
            this.write(entry.getKey(), entry.getValue(), expire);
        }
    }

    @Override
    public void remove(K... keys) {
        try {
            for (K key : keys) {
                client.delete((String) key);
            }
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        try {
            client.flushAll();
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void tearDown() {
        if (null != client && !client.isShutdown()) {
            try {
                client.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
