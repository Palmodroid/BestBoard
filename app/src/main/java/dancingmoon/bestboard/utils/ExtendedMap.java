package dancingmoon.bestboard.utils;

import java.util.Map;

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

    }
