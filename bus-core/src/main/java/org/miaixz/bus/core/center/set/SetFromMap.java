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
package org.miaixz.bus.core.center.set;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 基于Map的Set实现
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetFromMap<E> extends AbstractSet<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852280238812L;

    private final Map<E, Boolean> m; // The backing map
    private transient Set<E> s; // Its keySet

    /**
     * 构造
     *
     * @param map Map
     */
    public SetFromMap(final Map<E, Boolean> map) {
        m = map;
        s = map.keySet();
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return m.containsKey(o);
    }

    @Override
    public boolean remove(final Object o) {
        return m.remove(o) != null;
    }

    @Override
    public boolean add(final E e) {
        return m.put(e, Boolean.TRUE) == null;
    }

    @Override
    public Iterator<E> iterator() {
        return s.iterator();
    }

    @Override
    public Object[] toArray() {
        return s.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return super.toArray(a);
    }

    @Override
    public String toString() {
        return s.toString();
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || s.equals(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return s.containsAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return s.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return s.retainAll(c);
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        s.forEach(action);
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return s.removeIf(filter);
    }

    @Override
    public Spliterator<E> spliterator() {
        return s.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return s.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return s.parallelStream();
    }

    private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        s = m.keySet();
    }

}
