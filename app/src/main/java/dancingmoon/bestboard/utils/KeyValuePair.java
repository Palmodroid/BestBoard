package dancingmoon.bestboard.utils;

/**
 * Simple class to store long key and Object value pairs
 * Useful, when there are two keys, such as multiple complex parameters
 */
public class KeyValuePair
    {
    private final long key;
    private final Object value;

    public KeyValuePair( long key, Object value )
        {
        this.key = key;
        this.value = value;
        }

    public long getKey()
        {
        return key;
        }

    public Object getValue()
        {
        return value;
        }
    }
