package org.lattilad.bestboard.codetext;

import java.util.ArrayList;
import java.util.List;

public class EntryListSet extends EntryList
    {
    private List<Long> shortCutIds = new ArrayList<>();

    public EntryListSet(List<Long> shortCutIds )
        {
        this.shortCutIds = shortCutIds;
        }

    public void init( CodeTextProcessor codeTextProcessor )
        {
        // for FIND a separate and sorted list is needed,
        // but otherwise it could be collected during runtime!!
        clear();
        for ( Long id : shortCutIds )
            {
            EntryList entryList = codeTextProcessor.getShortCut( id );
            if ( !(entryList instanceof EntryListSet) )
                {
                addAll( entryList );
                }
            }
        sort();
        }

    }
