package org.lattilad.bestboard.utils;

/**
 * Measure of average velocity of occuring events
 */
public class TimeCounter
    {
    private final int PERIOD_LIMIT_MS = 3000; // 3 sec
    private final int EVENT_COUNT_LIMIT = 6;

    private long measuredPeriod;
    private int measuredCounts;
    private long lastTime;

    private long periodLimit;

    private int velocity;


    public TimeCounter()
        {
        clear( PERIOD_LIMIT_MS );
        }

    /**
     * Clears this counter, and sets perid limit
     * @param periodLimitMs no measurement can be than above this limit (millisecs)
     */
    public void clear( int periodLimitMs )
        {
        measuredPeriod = 0L;
        measuredCounts = 0;
        lastTime = 0L;
        periodLimit = (long)periodLimitMs * 1000000L;
        }


    /**
     * Adds eventCount events to the measurement
     * @param eventCounts number of events
     */
    public void measure( int eventCounts )
        {
        long now = System.nanoTime();

        // period between two events
        long eventPeriod = now - lastTime;

        // if period is within limits,
        // then it is added to the average
        if ( eventPeriod < periodLimit )
            {
            measuredPeriod += eventPeriod;
            measuredCounts += eventCounts;
            }

        // period of new event starts at this time
        lastTime = now;

        // no result below EVENT_COUNT_LIMIT events
        if ( measuredCounts < EVENT_COUNT_LIMIT )
            {
            velocity = 0;
            }
        else
            {
            // velocity: events / time
            // time was measured in nanos, but we need to calculate with minutes (60 sec)
            velocity = (int)(measuredCounts * 60L * 1000000000L / measuredPeriod);
            }
        }

    /**
     * Returns average velocity (events/mins).
     * Measurement starts after EVENT_COUNT_LIMIT events.
     * @return velocity
     */
    public int getVelocity()
        {
        return velocity;
        }
    }
