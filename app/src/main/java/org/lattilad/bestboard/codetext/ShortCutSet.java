package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.utils.SimpleReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortCutSet extends EntryList
    {
    private List<Long> shortCutIds = new ArrayList<>();

    public ShortCutSet( List<Long> shortCutIds )
        {
        this.shortCutIds = shortCutIds;
        }



    private List<Entry> entries = new ArrayList<>();


    public void add( Entry entry )
        {
        entries.add( entry );
        }

    public void addAll( EntryList entryList )
        {
        if ( entryList != null )
            entries.addAll( entryList.entries );
        }

    public void clear()
        {
        entries.clear();
        }


    public void sort()
        {
        Collections.sort( entries );
        }


    public Entry lookUp(SimpleReader reader)









    }
