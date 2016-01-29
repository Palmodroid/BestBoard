package org.lattilad.bestboard.utils;

import java.util.Iterator;
import java.util.Map;

import org.lattilad.bestboard.parser.Tokenizer;

/**
 * ExtendedMap with extended get method for HashMap.
 * The default value is returned if the map does not contain a value or
 * the value is null for the provided key.
 */
public class ExtendedMap<K, V> extends java.util.HashMap<K, V>
    {

    public ExtendedMap()
        {
        super();
        }

    public ExtendedMap(int initialCapacity)
        {
        super(initialCapacity);
        }

    public ExtendedMap(int initialCapacity, float loadFactor)
        {
        super(initialCapacity, loadFactor);
        }

    public ExtendedMap(Map t)
        {
        super(t);
        }

    /**
     * Get method extended by default object to be returned when
     * value is null or key is not found.
     * @param key key to look up
     * @param defaultValue default value to return if key is not found
     * @return value that is associated with key
     */
    public V remove(Object key, V defaultValue)
        {
        V value = remove(key);
        return ( value != null ) ? value : defaultValue;
        }

    @Override
    public String toString()
        {
        if (isEmpty())
            {
            return "{}";
            }

        StringBuilder buffer = new StringBuilder( size() * 28 );
        buffer.append('{');
        Iterator<Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry<K, V> entry = it.next();
            Object key = entry.getKey();
            if (key != this)
                {
                buffer.append(key);
                if ( key != null && key instanceof Long )
                    {
                    buffer.append('[');
                    buffer.append(Tokenizer.regenerateKeyword((long) key));
                    buffer.append(']');
                    }
                }
            else
                {
                buffer.append("(this Map)");
                }
            buffer.append('=');

            Object value = entry.getValue();
            if (value != this)
                {
                buffer.append(value);
                if ( value != null && value instanceof Long )
                    {
                    buffer.append('[');
                    buffer.append(Tokenizer.regenerateKeyword((long) value));
                    buffer.append(']');
                    }
                }
            else
                {
                buffer.append("(this Map)");
                }

            if (it.hasNext())
                {
                buffer.append(", ");
                }
            }
        buffer.append('}');
        return buffer.toString();
        }
    }
