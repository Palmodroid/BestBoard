package org.lattilad.bestboard.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Singly linked, non deletable list
 * List can grow (implements add), but items cannot be removed.
 * List can only be iterated
 * form lastly added (first in the list)
 * to firstly added (last in the list) element.
 */
public class SinglyLinkedList<E> implements Iterable<E>
    {
    // List elements are stored in Link class.
    // Each element contains a pointer to the previous item.
    private static class Link<E>
        {
        private final E data;
        private final Link<E> next;

        private Link( E o, Link<E> n )
            {
            data = o;
            next = n;
            }
        }

    // Starting point (last element) of list
    private Link<E> start;


    public SinglyLinkedList()
        {
        this.start = null;
        }

    public SinglyLinkedList( SinglyLinkedList root )
        {
        this.start = root.start;
        }

    // These mathods are not needed
    private SinglyLinkedList( Link<E> start )
        {
        this.start = start;
        }

    public SinglyLinkedList<E> createCopy()
        {
        return new SinglyLinkedList( start );
        }
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    /**
     * Adds an item to the beginning of the list.
     * @param item item to add
     */
    public void add(E item)
        {
        start = new Link( item, start );
        }


    /**
     * Get the last item (at the beginning of the list)
     * @return the last item
     * @throws NoSuchElementException if there are no elements at all
     */
    public E getLast()
        {
        if ( start == null )
            throw new NoSuchElementException();
        return start.data;
        }


    /**
     * Returns an iterator to read the elements from the list.
     * Elements added after the creation of an iterator will be not included in the iteration.
     * @return An Iterator instance
     */
    @Override
    public Iterator<E> iterator()
        {
        return new SinglyLinkedListIterator( start );
        }

    // Iterator class
    private static class SinglyLinkedListIterator<E> implements Iterator<E>
        {
        // Starting point of list
        private Link<E> iterator;

        /**
         * Constructor
         * @param start starting point of iterator
         */
        private SinglyLinkedListIterator( Link<E> start)
            {
            iterator = start;
            }

        /**
         * Returns true if there is at least one more element, false otherwise.
         */
        @Override
        public boolean hasNext()
            {
            return iterator != null;
            }

        /**
         * Returns the next object and advances the iterator.
         * @return the next object.
         * @throws NoSuchElementException if there are no more elements.
         */
        @Override
        public E next()
            {
            if (hasNext())
                {
                E item = iterator.data;
                iterator = iterator.next;
                return item;
                }
            throw new NoSuchElementException();
            }

        /**
         * Remove is not supported.
         * Throws UnsupportedOperationException
         */
        @Override
        public void remove()
            {
            throw new UnsupportedOperationException();
            }
        }
    }
