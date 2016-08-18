package org.lattilad.bestboard.abbreviation;

import org.lattilad.bestboard.buttons.ButtonAbbrev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Abbrevs
    {
    private Map<Long, Abbrev> abbrevs = new HashMap<>();

    private Abbrev workingAbbrevs = new Abbrev();

    private List<Long> startingAbbrevs = new ArrayList<>();

    public ButtonAbbrev activeButton = null;

    public boolean add( Long id, Abbrev abbrev, boolean start )
        {
        if ( start )
            {
            startingAbbrevs.add( id );
            }
        else
            {
            startingAbbrevs.remove( id ); // Previous (changed) collection could be signed as 'start'
            }
        return abbrevs.put( id, abbrev ) != null;
        }


    public void start(List<Long> idList )
        {
        stop();

        for ( Long id : idList )
            {
            workingAbbrevs.addAll( abbrevs.get( id ) );
            }

        workingAbbrevs.sort();
        }

    public void stop()
        {
        workingAbbrevs.clear();
        }

    public void init( )
        {
        start( startingAbbrevs );
        }

    public Abbrev get()
        {
        return workingAbbrevs;
        }
    }
