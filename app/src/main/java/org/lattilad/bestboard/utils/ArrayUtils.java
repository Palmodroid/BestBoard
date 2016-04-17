package org.lattilad.bestboard.utils;

/**
 * Useful utilities for arrays
 */
public class ArrayUtils
    {
    /**
     * Simple utility to check whether array contains item.
     * As generics don't support primitives, it is an extra method for long.
     * ((http://stackoverflow.com/questions/2721546/why-dont-java-generics-support-primitive-types ;
     * http://stackoverflow.com/a/12635769 a nice algorithm with generics ;
     * http://stackoverflow.com/questions/2250031/null-check-in-an-enhanced-for-loop))
     */
    public static boolean contains(final long[] array, final long item)
        {
        for (final long i : array)
            {
            // Scribe.debug( Tokenizer.regenerateKeyword(i) );
            if (  i == item)
                {
                // Scribe.debug( " * ITEM WAS FOUND! " + Tokenizer.regenerateKeyword(item) );
                return true;
                }
            }
        return false;
        }

    /**
     * Method was developed to distinguish between on and off signed bit in the array
     * @param array to check (cannot be null!)
     * @param item to look for
     * @return 0 if item cannot be found in the array, 1 if item can be found in the array,
     * -1 if item can be found, but array's item signed bit is ON
     */
    public static int containsWithoutSignedBit(final long[] array, final long item)
        {
        for (final long i : array)
            {
            // Scribe.debug( Tokenizer.regenerateKeyword(i) );
            if ( Bit.setSignedBitOff( i ) == item)
                {
                // Scribe.debug( " * ITEM WAS FOUND! " + Tokenizer.regenerateKeyword(item) );
                return i < 0 ? -1 : +1;
                }
            }
        return 0;
        }


    public static long[] concat(long[]... arrays)
        {
        int resultLength = 0;

        for (long[] array : arrays)
            resultLength += array.length;

        long[] result = new long[resultLength];

        int resultPosition = 0;
        for (long[] array : arrays)
            {
            System.arraycopy(array, 0, result, resultPosition, array.length);
            resultPosition += array.length;
            }
        return result;
        }

    }
