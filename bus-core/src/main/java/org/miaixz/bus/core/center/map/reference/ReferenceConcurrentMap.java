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
package org.miaixz.bus.core.center.map.reference;

import java.io.Serial;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ReferKit;

/**
 * 线程安全的ReferenceMap实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ReferenceConcurrentMap<K, V>
        implements ConcurrentMap<K, V>, Iterable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852269377051L;

    final ConcurrentMap<Ref<K>, Ref<V>> raw;
    private final ReferenceQueue<K> lastKeyQueue;
    private final ReferenceQueue<V> lastValueQueue;
    /**
     * 回收监听
     */
    private BiConsumer<Ref<? extends K>, Ref<? extends V>> purgeListener;

    /**
     * 构造
     *
     * @param raw {@link ConcurrentMap}实现
     */
    public ReferenceConcurrentMap(final ConcurrentMap<Ref<K>, Ref<V>> raw) {
        this.raw = raw;
        lastKeyQueue = new ReferenceQueue<>();
        lastValueQueue = new ReferenceQueue<>();
    }

    /**
     * 去包装对象
     *
     * @param <T>    对象类型
     * @param object 对象
     * @return 值
     */
    private static <T> T unwrap(final Ref<T> object) {
        return ReferKit.get(object);
    }

    /**
     * 设置对象回收清除监听
     *
     * @param purgeListener 监听函数
     */
    public void setPurgeListener(final BiConsumer<Ref<? extends K>, Ref<? extends V>> purgeListener) {
        this.purgeListener = purgeListener;
    }

    @Override
    public int size() {
        this.purgeStale();
        return this.raw.size();
    }

    @Override
    public boolean isEmpty() {
        this.purgeStale();
        return this.raw.isEmpty();
    }

    @Override
    public V get(final Object key) {
        this.purgeStale();
        return unwrap(this.raw.get(wrapKey(key)));
    }

    @Override
    public boolean containsKey(final Object key) {
        this.purgeStale();
        return this.raw.containsKey(wrapKey(key));
    }

    @Override
    public boolean containsValue(final Object value) {
        this.purgeStale();
        return this.raw.containsValue(wrapValue(value));
    }

    @Override
    public V put(final K key, final V value) {
        this.purgeStale();
        final Ref<V> vReference = this.raw.put(wrapKey(key), wrapValue(value));
        return unwrap(vReference);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        this.purgeStale();
        final Ref<V> vReference = this.raw.putIfAbsent(wrapKey(key), wrapValue(value));
        return unwrap(vReference);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public V replace(final K key, final V value) {
        this.purgeStale();
        final Ref<V> vReference = this.raw.replace(wrapKey(key), wrapValue(value));
        return unwrap(vReference);
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        this.purgeStale();
        return this.raw.replace(wrapKey(key), wrapValue(oldValue), wrapValue(newValue));
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        this.purgeStale();
        this.raw.replaceAll((rKey, rValue) -> wrapValue(function.apply(unwrap(rKey), unwrap(rValue))));
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        V result = null;
        while (null == result) {
            this.purgeStale();
            final Ref<V> vReference = this.raw.computeIfAbsent(wrapKey(key),
                    kReference -> wrapValue(mappingFunction.apply(unwrap(kReference))));

            // 如果vReference在此时被GC回收，则unwrap后为null，需要循环计算
            // 但是当用户提供的值本身为null，则直接返回之
            if (NullRef.NULL == vReference) {
                // 用户提供的值本身为null
                return null;
            }
            result = unwrap(vReference);
        }
        return result;
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V result = null;
        while (null == result) {
            this.purgeStale();
            final Ref<V> vReference = this.raw.computeIfPresent(wrapKey(key), (kReference,
                    vReference1) -> wrapValue(remappingFunction.apply(unwrap(kReference), unwrap(vReference1))));

            // 如果vReference在此时被GC回收，则unwrap后为null，需要循环计算
            // 但是当用户提供的值本身为null，则直接返回之
            if (NullRef.NULL == vReference) {
                // 用户提供的值本身为null
                return null;
            }
            result = unwrap(vReference);
        }
        return result;
    }

    @Override
    public V remove(final Object key) {
        this.purgeStale();
        return unwrap(this.raw.remove(wrapKey(key)));
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        this.purgeStale();
        return this.raw.remove(wrapKey((K) key, null), value);
    }

    @Override
    public void clear() {
        this.raw.clear();
        while (lastKeyQueue.poll() != null)
            ;
        while (lastValueQueue.poll() != null)
            ;
    }

    @Override
    public Set<K> keySet() {
        this.purgeStale();
        final Set<Ref<K>> referenceSet = this.raw.keySet();
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                final Iterator<Ref<K>> referenceIter = referenceSet.iterator();
                return new Iterator<K>() {
                    @Override
                    public boolean hasNext() {
                        return referenceIter.hasNext();
                    }

                    @Override
                    public K next() {
                        return unwrap(referenceIter.next());
                    }
                };
            }

            @Override
            public int size() {
                return referenceSet.size();
            }
        };
    }

    @Override
    public Collection<V> values() {
        this.purgeStale();
        final Collection<Ref<V>> referenceValues = this.raw.values();
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                final Iterator<Ref<V>> referenceIter = referenceValues.iterator();
                return new Iterator<V>() {
                    @Override
                    public boolean hasNext() {
                        return referenceIter.hasNext();
                    }

                    @Override
                    public V next() {
                        return unwrap(referenceIter.next());
                    }
                };
            }

            @Override
            public int size() {
                return referenceValues.size();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        this.purgeStale();
        final Set<Entry<Ref<K>, Ref<V>>> referenceEntrySet = this.raw.entrySet();
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<Ref<K>, Ref<V>>> referenceIter = referenceEntrySet.iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return referenceIter.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        final Entry<Ref<K>, Ref<V>> next = referenceIter.next();
                        return new Entry<>() {
                            @Override
                            public K getKey() {
                                return unwrap(next.getKey());
                            }

                            @Override
                            public V getValue() {
                                return unwrap(next.getValue());
                            }

                            @Override
                            public V setValue(final V value) {
                                return unwrap(next.setValue(wrapValue(value)));
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return referenceEntrySet.size();
            }
        };
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        this.purgeStale();
        this.raw.forEach((key, rValue) -> action.accept(key.get(), unwrap(rValue)));
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entrySet().iterator();
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        this.purgeStale();
        return unwrap(this.raw.compute(wrapKey(key), (kReference,
                vReference) -> wrapValue(remappingFunction.apply(unwrap(kReference), unwrap(vReference)))));
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        this.purgeStale();
        return unwrap(this.raw.merge(wrapKey(key), wrapValue(value), (vReference,
                vReference2) -> wrapValue(remappingFunction.apply(unwrap(vReference), unwrap(vReference2)))));
    }

    /**
     * 清除被回收的键和值
     */
    private void purgeStale() {
        Ref<? extends K> key;
        Ref<? extends V> value;

        // 清除无效key对应键值对
        while ((key = (Ref<? extends K>) this.lastKeyQueue.poll()) != null) {
            value = this.raw.remove(key);
            if (null != purgeListener) {
                purgeListener.accept(key, value);
            }
        }

        // 清除无效value对应的键值对
        while ((value = (Ref<? extends V>) this.lastValueQueue.poll()) != null) {
            MapKit.removeByValue(this.raw, (Ref<V>) value);
            if (null != purgeListener) {
                purgeListener.accept(null, value);
            }
        }
    }

    /**
     * 根据Reference类型构建key对应的{@link Reference}
     *
     * @param key   键
     * @param queue {@link ReferenceQueue}
     * @return {@link Reference}
     */
    abstract Ref<K> wrapKey(final K key, final ReferenceQueue<? super K> queue);

    /**
     * 根据Reference类型构建value对应的{@link Reference}
     *
     * @param value 值
     * @param queue {@link ReferenceQueue}
     * @return {@link Reference}
     */
    abstract Ref<V> wrapValue(final V value, final ReferenceQueue<? super V> queue);

    /**
     * 根据Reference类型构建key对应的{@link Reference}
     *
     * @param key 键
     * @return {@link Reference}
     */
    private Ref<K> wrapKey(final Object key) {
        return wrapKey((K) key, this.lastKeyQueue);
    }

    /**
     * 根据Reference类型构建value对应的{@link Reference}
     *
     * @param value 键
     * @return {@link Reference}
     */
    private Ref<V> wrapValue(final Object value) {
        return wrapValue((V) value, this.lastValueQueue);
    }

    private static class NullRef implements Ref {

        public static final Object NULL = new NullRef();

        @Override
        public Object get() {
            return null;
        }

    }

}
