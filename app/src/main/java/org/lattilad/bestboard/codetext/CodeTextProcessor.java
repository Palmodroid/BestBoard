package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.buttons.ButtonAbbrev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeTextProcessor
    {
    private Map<Long, EntryList> abbreviations = new HashMap<>();

    private EntryList activeAbbreviations = new EntryList();

    public ButtonAbbrev activeButton = null;

    public boolean addAbbreviation(Long id, EntryList abbreviation )
        {
        return abbreviations.put( id, abbreviation) != null;
        }

    public void startAbbreviation(List<Long> idList )
        {
        stopAbbreviation();

        for ( Long id : idList )
            {
            activeAbbreviations.addAll( abbreviations.get( id ) );
            }

        activeAbbreviations.sort();
        }

    public void stopAbbreviation()
        {
        activeAbbreviations.clear();
        }

    public void initAbbreviations( )
        {
        startAbbreviation(startingAbbreviations);
        }

    public EntryList getAbbreviation()
        {
        return activeAbbreviations;
        }
    }

/*

Map ID - VARIA

    Class VARIA

        Map CODE - GROUP


        LEGEND: TEXT/TITLE

        ArrayList<LEGEND>

        G


 */
