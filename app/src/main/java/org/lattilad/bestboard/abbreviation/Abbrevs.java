package org.lattilad.bestboard.abbreviation;

import java.util.HashMap;
import java.util.Map;

public class Abbrevs
    {
    private Map<Long, Abbrev> abbrevs = new HashMap<>();

    private Abbrev workingAbbrev = new Abbrev();


    public boolean add( Long id, Abbrev abbrev )
        {
        return abbrevs.put( id, abbrev ) != null;
        }

    // start?

    public Abbrev getAbbrev()
        {
        return workingAbbrev;
        }
    }
