package org.lattilad.bestboard.utils;

public class Rest
	{
	/** Resting time in ms: 1/10 sec */
	static final private long WAITING = 100L;
	
	/** Just waiting a bit */
	static public void aBit()
		{
    	try
			{
			Thread.sleep( WAITING );
			}
    	catch (InterruptedException ignored) { }
		}

	/**
	 * Just waiting a bit
	 * @param millis exactly how many milliseconds?
	 */
	static public void millis( int millis )
		{
    	try
			{
			Thread.sleep( (long)millis );
			} 
    	catch (InterruptedException ignored) { }
		}

	/**
	 * Just waiting a bit - inside a loop
	 * @param millis all time (in ms) needed by the loop
	 * @param fraction steps of the loop
	 */
	static public void fraction( int millis, int fraction )
		{
    	try
			{
			Thread.sleep( (long)(millis/fraction) );
			} 
    	catch (InterruptedException ignored) { }
		}
	}
