package org.lattilad.bestboard.utils;

/**
 * Trilean class with  3 states : TRUE - FALSE - IGNORED
 * http://javarevisited.blogspot.hu/2011/08/enum-in-java-example-tutorial.html
 */
public enum Trilean
    {
    TRUE
        {
        @Override
        public boolean booleanValue()
            {
            return true;
            }
        },

    FALSE
        {
        @Override
        public boolean booleanValue()
            {
            return false;
            }
        },

    IGNORED
        {
        @Override
        public boolean booleanValue()
            {
            throw new IllegalStateException("IGNORED cannot be converted to boolean.");
            }
        };

    public static Trilean valueOf(boolean value)
        {
        return value ? TRUE : FALSE;
        }

    // null could be treated as IGNORED
    public static Trilean valueOf(Boolean value)
        {
        if ( value == null )
            return IGNORED;
        else if (value)
            return TRUE;
        else
            return FALSE;
        }

    public boolean isTrue()
        {
        return this.equals( TRUE );
        }

    public boolean isFalse()
        {
        return this.equals( FALSE );
        }

    public boolean isIgnored()
        {
        return this.equals( IGNORED );
        }

    public abstract boolean booleanValue();
    }
