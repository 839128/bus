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
package org.miaixz.bus.core.center.queue;

import java.util.*;

/**
 * Linked list implementation of the {@link Deque} interface where the link pointers are tightly integrated with the
 * element. Linked deques have no capacity restrictions; they grow as necessary to support usage. They are not
 * thread-safe; in the absence of external synchronization, they do not support concurrent access by multiple threads.
 * Null elements are prohibited.
 * <p>
 * Most <b>LinkedDeque</b> operations run in constant time by assuming that the {@link Linked} parameter is associated
 * with the deque instance. Any usage that violates this assumption will result in non-deterministic behavior.
 * <p>
 * The iterators returned by this class are <em>not</em> <i>fail-fast</i>: If the deque is modified at any time after
 * the iterator is created, the iterator will be in an unknown state. Thus, in the face of concurrent modification, the
 * iterator risks arbitrary, non-deterministic behavior at an undetermined time in the future.
 *
 * @param <E> the type of elements held in this collection
 * @author Kimi Liu
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 *      http://code.google.com/p/concurrentlinkedhashmap/</a>
 * @since Java 17+
 */
public class LinkedDeque<E extends Linked<E>> extends AbstractCollection<E> implements Deque<E> {

    /**
     * Pointer to first node. Invariant: (first == null && last == null) || (first.prev == null)
     */
    E first;

    /**
     * Pointer to last node. Invariant: (first == null && last == null) || (last.next == null)
     */
    E last;

    /**
     * Links the element to the front of the deque so that it becomes the first element.
     *
     * @param e the unlinked element
     */
    void linkFirst(final E e) {
        final E f = first;
        first = e;

        if (f == null) {
            last = e;
        } else {
            f.setPrevious(e);
            e.setNext(f);
        }
    }

    /**
     * Links the element to the back of the deque so that it becomes the last element.
     *
     * @param e the unlinked element
     */
    void linkLast(final E e) {
        final E l = last;
        last = e;

        if (l == null) {
            first = e;
        } else {
            l.setNext(e);
            e.setPrevious(l);
        }
    }

    /**
     * Unlinks the non-null first element.
     */
    E unlinkFirst() {
        final E f = first;
        final E next = f.getNext();
        f.setNext(null);

        first = next;
        if (next == null) {
            last = null;
        } else {
            next.setPrevious(null);
        }
        return f;
    }

    /**
     * Unlinks the non-null last element.
     */
    E unlinkLast() {
        final E l = last;
        final E prev = l.getPrevious();
        l.setPrevious(null);
        last = prev;
        if (prev == null) {
            first = null;
        } else {
            prev.setNext(null);
        }
        return l;
    }

    /**
     * Unlinks the non-null element.
     */
    void unlink(final E e) {
        final E prev = e.getPrevious();
        final E next = e.getNext();

        if (prev == null) {
            first = next;
        } else {
            prev.setNext(next);
            e.setPrevious(null);
        }

        if (next == null) {
            last = prev;
        } else {
            next.setPrevious(prev);
            e.setNext(null);
        }
    }

    @Override
    public boolean isEmpty() {
        return (first == null);
    }

    void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Beware that, unlike in most collections, this method is <em>NOT</em> a constant-time operation.
     */
    @Override
    public int size() {
        int size = 0;
        for (E e = first; e != null; e = e.getNext()) {
            size++;
        }
        return size;
    }

    @Override
    public void clear() {
        for (E e = first; e != null;) {
            final E next = e.getNext();
            e.setPrevious(null);
            e.setNext(null);
            e = next;
        }
        first = last = null;
    }

    @Override
    public boolean contains(final Object o) {
        return (o instanceof Linked<?>) && contains((Linked<?>) o);
    }

    // A fast-path containment check
    boolean contains(final Linked<?> e) {
        return (e.getPrevious() != null) || (e.getNext() != null) || (e == first);
    }

    /**
     * Moves the element to the front of the deque so that it becomes the first element.
     *
     * @param e the linked element
     */
    public void moveToFront(final E e) {
        if (e != first) {
            unlink(e);
            linkFirst(e);
        }
    }

    /**
     * Moves the element to the back of the deque so that it becomes the last element.
     *
     * @param e the linked element
     */
    public void moveToBack(final E e) {
        if (e != last) {
            unlink(e);
            linkLast(e);
        }
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public E peekFirst() {
        return first;
    }

    @Override
    public E peekLast() {
        return last;
    }

    @Override
    public E getFirst() {
        checkNotEmpty();
        return peekFirst();
    }

    @Override
    public E getLast() {
        checkNotEmpty();
        return peekLast();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public boolean offer(final E e) {
        return offerLast(e);
    }

    @Override
    public boolean offerFirst(final E e) {
        if (contains(e)) {
            return false;
        }
        linkFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(final E e) {
        if (contains(e)) {
            return false;
        }
        linkLast(e);
        return true;
    }

    @Override
    public boolean add(final E e) {
        return offerLast(e);
    }

    @Override
    public void addFirst(final E e) {
        if (!offerFirst(e)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void addLast(final E e) {
        if (!offerLast(e)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E pollFirst() {
        return isEmpty() ? null : unlinkFirst();
    }

    @Override
    public E pollLast() {
        return isEmpty() ? null : unlinkLast();
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public boolean remove(final Object o) {
        return (o instanceof Linked<?>) && remove((E) o);
    }

    // A fast-path removal
    boolean remove(final E e) {
        if (contains(e)) {
            unlink(e);
            return true;
        }
        return false;
    }

    @Override
    public E removeFirst() {
        checkNotEmpty();
        return pollFirst();
    }

    @Override
    public boolean removeFirstOccurrence(final Object o) {
        return remove(o);
    }

    @Override
    public E removeLast() {
        checkNotEmpty();
        return pollLast();
    }

    @Override
    public boolean removeLastOccurrence(final Object o) {
        return remove(o);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean modified = false;
        for (final Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    @Override
    public void push(final E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> iterator() {
        return new AbstractLinkedIterator(first) {
            @Override
            E computeNext() {
                return cursor.getNext();
            }
        };
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new AbstractLinkedIterator(last) {
            @Override
            E computeNext() {
                return cursor.getPrevious();
            }
        };
    }

    abstract class AbstractLinkedIterator implements Iterator<E> {
        E cursor;

        /**
         * Creates an iterator that can can traverse the deque.
         *
         * @param start the initial element to begin traversal from
         */
        AbstractLinkedIterator(final E start) {
            cursor = start;
        }

        @Override
        public boolean hasNext() {
            return (cursor != null);
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final E e = cursor;
            cursor = computeNext();
            return e;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Retrieves the next element to traverse to or {@code null} if there are no more elements.
         */
        abstract E computeNext();
    }

}
