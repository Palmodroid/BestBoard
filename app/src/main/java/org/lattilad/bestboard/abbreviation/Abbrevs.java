package org.lattilad.bestboard.abbreviation;

import java.util.HashMap;
import java.util.Map;

public class Abbrevs
    {
    private Map<Long, Abbrev> abbrevs = new HashMap<>();

    public Abbrev addAbbrev( Long id )
        {
        Abbrev abbrev = abbrevs.get( id );
        if ( abbrev == null )
            {
            abbrev = new Abbrev();
            abbrevs.put( id, abbrev );
            }
        return abbrev;
        }

    public Abbrev getAbbrev( Long id )
        {
        return abbrevs.get( id );
        }

    public boolean checkEmptiness( Long id )
        {
        // id cannot be null!!
        if ( abbrevs.get( id ).isEmpty() )
            {
            abbrevs.remove( id );
            return true;
            }
        return false;
        }
    }
