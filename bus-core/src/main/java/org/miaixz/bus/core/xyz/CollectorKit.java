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
package org.miaixz.bus.core.xyz;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.center.stream.EntryStream;
import org.miaixz.bus.core.center.stream.SimpleCollector;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;

/**
 * 可变的汇聚操作{@link Collector} 相关工具封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectorKit {

    /**
     * 说明已包含IDENTITY_FINISH特征 为 Characteristics.IDENTITY_FINISH 的缩写
     */
    public static final Set<Collector.Characteristics> CH_ID = Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    /**
     * 说明不包含IDENTITY_FINISH特征
     */
    public static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    /**
     * 提供任意对象的Join操作的{@link Collector}实现，对象默认调用toString方法
     *
     * @param delimiter 分隔符
     * @param <T>       对象类型
     * @return {@link Collector}
     */
    public static <T> Collector<T, ?, String> joining(final CharSequence delimiter) {
        return joining(delimiter, Object::toString);
    }

    /**
     * 提供任意对象的Join操作的{@link Collector}实现
     *
     * @param delimiter    分隔符
     * @param toStringFunc 自定义指定对象转换为字符串的方法
     * @param <T>          对象类型
     * @return {@link Collector}
     */
    public static <T> Collector<T, ?, String> joining(final CharSequence delimiter,
            final Function<T, ? extends CharSequence> toStringFunc) {
        return joining(delimiter, Normal.EMPTY, Normal.EMPTY, toStringFunc);
    }

    /**
     * 提供任意对象的Join操作的{@link Collector}实现
     *
     * @param delimiter    分隔符
     * @param prefix       前缀
     * @param suffix       后缀
     * @param toStringFunc 自定义指定对象转换为字符串的方法
     * @param <T>          对象类型
     * @return {@link Collector}
     */
    public static <T> Collector<T, ?, String> joining(final CharSequence delimiter, final CharSequence prefix,
            final CharSequence suffix, final Function<T, ? extends CharSequence> toStringFunc) {
        return new SimpleCollector<>(() -> new StringJoiner(delimiter, prefix, suffix),
                (joiner, ele) -> joiner.add(toStringFunc.apply(ele)), StringJoiner::merge, StringJoiner::toString,
                Collections.emptySet());
    }

    /**
     * 提供对null值友好的groupingBy操作的{@link Collector}实现，可指定map类型
     *
     * @param classifier 分组依据
     * @param mapFactory 提供的map
     * @param downstream 下游操作
     * @param <T>        实体类型
     * @param <K>        实体中的分组依据对应类型，也是Map中key的类型
     * @param <D>        下游操作对应返回类型，也是Map中value的类型
     * @param <A>        下游操作在进行中间操作时对应类型
     * @param <M>        最后返回结果Map类型
     * @return {@link Collector}
     */
    public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M> groupingBy(
            final Function<? super T, ? extends K> classifier, final Supplier<M> mapFactory,
            final Collector<? super T, A, D> downstream) {
        final Supplier<A> downstreamSupplier = downstream.supplier();
        final BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        final BiConsumer<Map<K, A>, T> accumulator = (m, t) -> {
            final K key = java.util.Optional.ofNullable(t).map(classifier).orElse(null);
            final A container = m.computeIfAbsent(key, k -> downstreamSupplier.get());
            if (ArrayKit.isArray(container) || Objects.nonNull(t)) {
                // 如果是数组类型，不需要判空，场景——分组后需要使用：java.util.unwrap.Collectors.counting 求null元素个数
                downstreamAccumulator.accept(container, t);
            }
        };
        final BinaryOperator<Map<K, A>> merger = mapMerger(downstream.combiner());
        final Supplier<Map<K, A>> mangledFactory = (Supplier<Map<K, A>>) mapFactory;

        if (downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return new SimpleCollector<>(mangledFactory, accumulator, merger, CH_ID);
        } else {
            final Function<A, A> downstreamFinisher = (Function<A, A>) downstream.finisher();
            final Function<Map<K, A>, M> finisher = intermediate -> {
                intermediate.replaceAll((k, v) -> downstreamFinisher.apply(v));
                final M castResult = (M) intermediate;
                return castResult;
            };
            return new SimpleCollector<>(mangledFactory, accumulator, merger, finisher, CH_NOID);
        }
    }

    /**
     * 提供对null值友好的groupingBy操作的{@link Collector}实现
     *
     * @param classifier 分组依据
     * @param downstream 下游操作
     * @param <T>        实体类型
     * @param <K>        实体中的分组依据对应类型，也是Map中key的类型
     * @param <D>        下游操作对应返回类型，也是Map中value的类型
     * @param <A>        下游操作在进行中间操作时对应类型
     * @return {@link Collector}
     */
    public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return groupingBy(classifier, HashMap::new, downstream);
    }

    /**
     * 提供对null值友好的groupingBy操作的{@link Collector}实现
     *
     * @param classifier 分组依据
     * @param <T>        实体类型
     * @param <K>        实体中的分组依据对应类型，也是Map中key的类型
     * @return {@link Collector}
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(
            final Function<? super T, ? extends K> classifier) {
        return groupingBy(classifier, Collectors.toList());
    }

    /**
     * 提供对null值友好的groupingBy操作的{@link Collector}实现， 对集合分组，然后对分组后的值集合进行映射
     *
     * @param classifier       分组依据
     * @param valueMapper      值映射方法
     * @param valueCollFactory 值集合的工厂方法
     * @param mapFactory       Map集合的工厂方法
     * @param <T>              元素类型
     * @param <K>              键类型
     * @param <R>              值类型
     * @param <C>              值集合类型
     * @param <M>              返回的Map集合类型
     * @return {@link Collector}
     */
    public static <T, K, R, C extends Collection<R>, M extends Map<K, C>> Collector<T, ?, M> groupingBy(
            final Function<? super T, ? extends K> classifier, final Function<? super T, ? extends R> valueMapper,
            final Supplier<C> valueCollFactory, final Supplier<M> mapFactory) {
        return groupingBy(classifier, mapFactory,
                Collectors.mapping(valueMapper, Collectors.toCollection(valueCollFactory)));
    }

    /**
     * 提供对null值友好的groupingBy操作的{@link Collector}实现， 对集合分组，然后对分组后的值集合进行映射
     *
     * @param classifier       分组依据
     * @param valueMapper      值映射方法
     * @param valueCollFactory 值集合的工厂方法
     * @param <T>              元素类型
     * @param <K>              键类型
     * @param <R>              值类型
     * @param <C>              值集合类型
     * @return {@link Collector}
     */
    public static <T, K, R, C extends Collection<R>> Collector<T, ?, Map<K, C>> groupingBy(
            final Function<? super T, ? extends K> classifier, final Function<? super T, ? extends R> valueMapper,
            final Supplier<C> valueCollFactory) {
        return groupingBy(classifier, valueMapper, valueCollFactory, HashMap::new);
    }

    /**
     * 提供对null值友好的groupingBy操作的{@link Collector}实现， 对集合分组，然后对分组后的值集合进行映射
     *
     * @param classifier  分组依据
     * @param valueMapper 值映射方法
     * @param <T>         元素类型
     * @param <K>         键类型
     * @param <R>         值类型
     * @return {@link Collector}
     */
    public static <T, K, R> Collector<T, ?, Map<K, List<R>>> groupingBy(
            final Function<? super T, ? extends K> classifier, final Function<? super T, ? extends R> valueMapper) {
        return groupingBy(classifier, valueMapper, ArrayList::new, HashMap::new);
    }

    /**
     * 对null友好的 toMap 操作的 {@link Collector}实现，默认使用HashMap
     *
     * @param keyMapper   指定map中的key
     * @param valueMapper 指定map中的value
     * @param <T>         实体类型
     * @param <K>         map中key的类型
     * @param <U>         map中value的类型
     * @return 对null友好的 toMap 操作的 {@link Collector}实现
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, (l, r) -> r);
    }

    /**
     * 对null友好的 toMap 操作的 {@link Collector}实现，默认使用HashMap
     *
     * @param keyMapper 指定map中的key
     * @param <T>       实体类型
     * @param <K>       map中key的类型
     * @return 对null友好的 toMap 操作的 {@link Collector}实现
     */
    public static <T, K> Collector<T, ?, Map<K, T>> toMap(final Function<? super T, ? extends K> keyMapper) {
        return toMap(keyMapper, Function.identity());
    }

    /**
     * 对null友好的 toMap 操作的 {@link Collector}实现，默认使用HashMap
     *
     * @param keyMapper     指定map中的key
     * @param valueMapper   指定map中的value
     * @param mergeFunction 合并前对value进行的操作
     * @param <T>           实体类型
     * @param <K>           map中key的类型
     * @param <U>           map中value的类型
     * @return 对null友好的 toMap 操作的 {@link Collector}实现
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper, final BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * 对null友好的 toMap 操作的 {@link Collector}实现
     *
     * @param keyMapper     指定map中的key
     * @param valueMapper   指定map中的value
     * @param mergeFunction 合并前对value进行的操作
     * @param mapSupplier   最终需要的map类型
     * @param <T>           实体类型
     * @param <K>           map中key的类型
     * @param <U>           map中value的类型
     * @param <M>           map的类型
     * @return 对null友好的 toMap 操作的 {@link Collector}实现
     */
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
            final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends U> valueMapper,
            final BinaryOperator<U> mergeFunction, final Supplier<M> mapSupplier) {
        final BiConsumer<M, T> accumulator = (map, element) -> map.put(
                Optional.ofNullable(element).map(keyMapper).orElse(null),
                Optional.ofNullable(element).map(valueMapper).orElse(null));
        return new SimpleCollector<>(mapSupplier, accumulator, mapMerger(mergeFunction), CH_ID);
    }

    /**
     * 用户合并map的BinaryOperator，传入合并前需要对value进行的操作
     *
     * @param mergeFunction 合并前需要对value进行的操作
     * @param <K>           key的类型
     * @param <V>           value的类型
     * @param <M>           map
     * @return 用户合并map的BinaryOperator
     */
    public static <K, V, M extends Map<K, V>> BinaryOperator<M> mapMerger(final BinaryOperator<V> mergeFunction) {
        return (m1, m2) -> {
            for (final Map.Entry<K, V> e : m2.entrySet()) {
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            }
            return m1;
        };
    }

    /**
     * 聚合这种数据类型:{@code Collection<Map<K,V>> => Map<K,List<V>>} 其中key相同的value，会累加到List中
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @return 聚合后的map
     */
    public static <K, V> Collector<Map<K, V>, ?, Map<K, List<V>>> reduceListMap() {
        return reduceListMap(HashMap::new);
    }

    /**
     * 聚合这种数据类型:{@code Collection<Map<K,V>> => Map<K,List<V>>} 其中key相同的value，会累加到List中
     *
     * @param mapSupplier 可自定义map的类型如concurrentHashMap等
     * @param <K>         key的类型
     * @param <V>         value的类型
     * @param <R>         返回值的类型
     * @return 聚合后的map
     */
    public static <K, V, R extends Map<K, List<V>>> Collector<Map<K, V>, ?, R> reduceListMap(
            final Supplier<R> mapSupplier) {
        return Collectors.reducing(mapSupplier.get(), value -> {
            final R result = mapSupplier.get();
            value.forEach((k, v) -> result.computeIfAbsent(k, i -> new ArrayList<>()).add(v));
            return result;
        }, (l, r) -> {
            final R resultMap = mapSupplier.get();
            resultMap.putAll(l);
            r.forEach((k, v) -> resultMap.computeIfAbsent(k, i -> new ArrayList<>()).addAll(v));
            return resultMap;
        });
    }

    /**
     * 将流转为{@link EntryStream}
     *
     * @param keyMapper 键的映射方法
     * @param <T>       输入元素类型
     * @param <K>       元素的键类型
     * @return 收集器
     */
    public static <T, K> Collector<T, List<T>, EntryStream<K, T>> toEntryStream(
            final Function<? super T, ? extends K> keyMapper) {
        return toEntryStream(keyMapper, Function.identity());
    }

    /**
     * 将流转为{@link EntryStream}
     *
     * @param keyMapper   键的映射方法
     * @param valueMapper 值的映射方法
     * @param <T>         输入元素类型
     * @param <K>         元素的键类型
     * @param <V>         元素的值类型
     * @return 收集器
     */
    public static <T, K, V> Collector<T, List<T>, EntryStream<K, V>> toEntryStream(
            final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        return transform(ArrayList::new, list -> EntryStream.of(list, keyMapper, valueMapper));
    }

    /**
     * 将流转为{@link EasyStream}
     *
     * @param <T> 输入元素类型
     * @return 收集器
     */
    public static <T> Collector<T, ?, EasyStream<T>> toEasyStream() {
        return transform(ArrayList::new, EasyStream::of);
    }

    /**
     * 收集元素，将其转为指定{@link Collection}集合后，再对该集合进行转换，并最终返回转换后的结果。 返回的收集器的效果等同于：
     * 
     * <pre>{@code
     * Collection<T> coll = Stream.of(a, b, c, d).collect(Collectors.toColl(collFactory));
     * R result = mapper.apply(coll);
     * }</pre>
     *
     * @param collFactory 中间收集输入元素的集合的创建方法
     * @param mapper      最终将元素集合映射为返回值的方法
     * @param <R>         返回值类型
     * @param <T>         输入元素类型
     * @param <C>         中间收集输入元素的集合类型
     * @return 收集器
     */
    public static <T, R, C extends Collection<T>> Collector<T, C, R> transform(final Supplier<C> collFactory,
            final Function<C, R> mapper) {
        Objects.requireNonNull(collFactory);
        Objects.requireNonNull(mapper);
        return new SimpleCollector<>(collFactory, C::add, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }, mapper, CH_NOID);
    }

    /**
     * 收集元素，将其转为{@link ArrayList}集合后，再对该集合进行转换，并最终返回转换后的结果。 返回的收集器的效果等同于：
     * 
     * <pre>{@code
     * List<T> coll = Stream.of(a, b, c, d).collect(Collectors.toList());
     * R result = mapper.apply(coll);
     * }</pre>
     *
     * @param mapper 最终将元素集合映射为返回值的方法
     * @param <R>    返回值类型
     * @param <T>    输入元素类型
     * @return 收集器
     */
    public static <T, R> Collector<T, List<T>, R> transform(final Function<List<T>, R> mapper) {
        return transform(ArrayList::new, mapper);
    }

    /**
     * 用于{@code Stream<Entry>} 转 Map 的情况
     *
     * @param <K> key类型
     * @param <V> value类型
     * @return map
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entryToMap() {
        return toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * 过滤
     *
     * @param predicate  断言
     * @param downstream 下游操作
     * @param <T>        元素类型
     * @param <A>        中间类型
     * @param <R>        结束类型
     * @return 一个用于过滤元素的 {@link java.util.stream.Collector}
     */
    public static <T, A, R> Collector<T, ?, R> filtering(final Predicate<? super T> predicate,
            final Collector<? super T, A, R> downstream) {
        final BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        return new SimpleCollector<>(downstream.supplier(),
                (r, t) -> Optional.of(t).filter(predicate).ifPresent(e -> downstreamAccumulator.accept(r, e)),
                downstream.combiner(), downstream.finisher(), downstream.characteristics());
    }

    /**
     * 将一个{@code Collection<T>}两个属性分流至两个List,并使用Pair收集。
     *
     * @param lMapper 左属性收集方法
     * @param rMapper 右属性收集方法
     * @param <T>     元素类型
     * @param <L>     左属性类型
     * @param <R>     右属性类型
     * @return {@code Pair<List<L>,List<R>>} Pair收集的两个List
     */
    public static <T, L, R> Collector<T, ?, Pair<List<L>, List<R>>> toPairList(
            final Function<? super T, ? extends L> lMapper, final Function<? super T, ? extends R> rMapper) {
        return toPair(lMapper, rMapper, Collectors.toList(), Collectors.toList());
    }

    /**
     * 将一个{@code Collection<T>}两个属性分流至两个Collection,并使用Pair收集。
     *
     * @param lMapper     左属性收集方法
     * @param rMapper     右属性收集方法
     * @param lDownstream 左属性下游操作
     * @param rDownstream 右属性下游操作
     * @param <T>         元素类型
     * @param <LU>        左属性类型
     * @param <LA>        左属性收集类型
     * @param <LR>        左属性收集最终类型
     * @param <RU>        左属性类型
     * @param <RA>        左属性收集类型
     * @param <RR>        左属性收集最终类型
     * @return {@code Pair<LR,RR>} Pair收集的结果
     */
    public static <T, LU, LA, LR, RU, RA, RR> Collector<T, ?, Pair<LR, RR>> toPair(
            final Function<? super T, ? extends LU> lMapper, final Function<? super T, ? extends RU> rMapper,
            final Collector<? super LU, LA, LR> lDownstream, final Collector<? super RU, RA, RR> rDownstream) {
        return new SimpleCollector<>(() -> Pair.of(lDownstream.supplier().get(), rDownstream.supplier().get()),

                (listPair, element) -> {
                    lDownstream.accumulator().accept(listPair.getLeft(), lMapper.apply(element));
                    rDownstream.accumulator().accept(listPair.getRight(), rMapper.apply(element));
                },

                (listPair1, listPair2) -> Pair.of(
                        lDownstream.combiner().apply(listPair1.getLeft(), listPair2.getLeft()),
                        rDownstream.combiner().apply(listPair1.getRight(), listPair2.getRight())),

                finisherPair -> {
                    final LR finisherLeftValue;
                    if (lDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherLeftValue = (LR) finisherPair.getLeft();
                    } else {
                        finisherLeftValue = lDownstream.finisher().apply(finisherPair.getLeft());
                    }

                    final RR finisherRightValue;
                    if (lDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherRightValue = (RR) finisherPair.getRight();
                    } else {
                        finisherRightValue = rDownstream.finisher().apply(finisherPair.getRight());
                    }

                    return Pair.of(finisherLeftValue, finisherRightValue);
                }, CH_NOID);
    }

    /**
     * 将一个{@code Collection<T>}三个属性分流至三个List,并使用Triple收集。
     *
     * @param lMapper 左属性收集方法
     * @param mMapper 中属性收集方法
     * @param rMapper 右属性收集方法
     * @param <T>     元素类型
     * @param <L>     左属性类型
     * @param <M>     中属性类型
     * @param <R>     右属性类型
     * @return {@code Triplet<List<L>,List<M>,List<R>>} Triple收集的三个List
     */
    public static <T, L, M, R> Collector<T, ?, Triplet<List<L>, List<M>, List<R>>> toTripletList(
            final Function<? super T, ? extends L> lMapper, final Function<? super T, ? extends M> mMapper,
            final Function<? super T, ? extends R> rMapper) {
        return toTriplet(lMapper, mMapper, rMapper, Collectors.toList(), Collectors.toList(), Collectors.toList());
    }

    /**
     * 将一个{@code Collection<T>}两个属性分流至两个Collection,并使用Pair收集。
     *
     * @param lMapper     左元素收集方法
     * @param mMapper     中元素收集方法
     * @param rMapper     右元素收集方法
     * @param lDownstream 左元素下游操作
     * @param mDownstream 中元素下游操作
     * @param rDownstream 右元素下游操作
     * @param <T>         元素类型
     * @param <LU>        左属性类型
     * @param <LA>        左属性收集类型
     * @param <LR>        左属性收集最终类型
     * @param <MU>        中属性类型
     * @param <MA>        中属性收集类型
     * @param <MR>        中属性收集最终类型
     * @param <RU>        左属性类型
     * @param <RA>        左属性收集类型
     * @param <RR>        左属性收集最终类型
     * @return {@code Triplet<LR,MR,RR>} Triple收集的结果
     */
    public static <T, LU, LA, LR, MU, MA, MR, RU, RA, RR> Collector<T, ?, Triplet<LR, MR, RR>> toTriplet(
            final Function<? super T, ? extends LU> lMapper, final Function<? super T, ? extends MU> mMapper,
            final Function<? super T, ? extends RU> rMapper, final Collector<? super LU, LA, LR> lDownstream,
            final Collector<? super MU, MA, MR> mDownstream, final Collector<? super RU, RA, RR> rDownstream) {
        return new SimpleCollector<>(
                () -> Triplet.of(lDownstream.supplier().get(), mDownstream.supplier().get(),
                        rDownstream.supplier().get()),

                (listTriple, element) -> {
                    lDownstream.accumulator().accept(listTriple.getLeft(), lMapper.apply(element));
                    mDownstream.accumulator().accept(listTriple.getMiddle(), mMapper.apply(element));
                    rDownstream.accumulator().accept(listTriple.getRight(), rMapper.apply(element));
                },

                (listTriple1, listTriple2) -> Triplet.of(
                        lDownstream.combiner().apply(listTriple1.getLeft(), listTriple2.getLeft()),
                        mDownstream.combiner().apply(listTriple1.getMiddle(), listTriple2.getMiddle()),
                        rDownstream.combiner().apply(listTriple1.getRight(), listTriple2.getRight())),

                finisherTriple -> {
                    final LR finisherLeftValue;
                    if (lDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherLeftValue = (LR) finisherTriple.getLeft();
                    } else {
                        finisherLeftValue = lDownstream.finisher().apply(finisherTriple.getLeft());
                    }

                    final MR finisherMiddleValue;
                    if (mDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherMiddleValue = (MR) finisherTriple.getMiddle();
                    } else {
                        finisherMiddleValue = mDownstream.finisher().apply(finisherTriple.getMiddle());
                    }

                    final RR finisherRightValue;
                    if (lDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherRightValue = (RR) finisherTriple.getRight();
                    } else {
                        finisherRightValue = rDownstream.finisher().apply(finisherTriple.getRight());
                    }

                    return Triplet.of(finisherLeftValue, finisherMiddleValue, finisherRightValue);
                }, CH_NOID);
    }

}
