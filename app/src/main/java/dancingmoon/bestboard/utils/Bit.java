package dancingmoon.bestboard.utils;

/**
 * Helper methods for bitwise operations
 */
public class Bit
    {
    public static long LONG_SIGNED_BIT = (1L << 63);

    public static long setSignedBitOn( long value )
        {
        return value | LONG_SIGNED_BIT;
        }

    public static long setSignedBitOff( long value )
        {
        return value & ~LONG_SIGNED_BIT;
        }


    }
