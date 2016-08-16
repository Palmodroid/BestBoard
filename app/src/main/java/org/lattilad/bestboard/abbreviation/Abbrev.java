package org.lattilad.bestboard.abbreviation;


import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.SimpleReader;
import org.lattilad.bestboard.utils.StringReverseReader;

import java.util.ArrayList;
import java.util.List;


public class Abbrev
    {
    private List<Entry> entries = new ArrayList<>();


    public void add( String ending, String expanded )
        {
        entries.add( new Entry( ending, expanded ));
        }


    public void addAll( Abbrev abbrev )
        {
        entries.addAll( abbrev.entries );
        }

    public boolean isEmpty()
        {
        return entries.isEmpty();
        }

    public Entry lookUp(SimpleReader reader)
        {
        int	first;
        int	last;
        int	middle = 0 ;
        int	cmp = 0;
        StringReverseReader stringReverseReader = new StringReverseReader();

        first = 0;
        last = entries.size()-1;

        StringBuilder builder = new StringBuilder();

        while ( first <= last )
            {
            // összesen v-e+1 elem, de k=ö/2 és k=(ö-1)/2 is jó (páratlannál azonos, párosnál a kisebb v. nagyobb "közepet" veszi"
            middle = first + (last - first)/2;

            reader.reset();
            stringReverseReader.setString( entries.get(middle).ending );
            cmp = Entry.compare( reader, stringReverseReader);

            builder.append("[").append(entries.get(middle).ending).append("/")
                    .append(cmp).append("/")
                    .append(first).append("-")
                    .append(middle).append("-")
                    .append(last)
                    .append("] ");

            if (cmp < 0)
                last=middle-1;

            else if (cmp > 0)
                first=middle+1;

            else
                {
                builder.append("EQ");
                Scribe.note( builder.toString() );
                return entries.get(middle);
                }

            }

        builder.append(" ** NOT FOUND");
        Scribe.note( builder.toString() );
        return null;
        }

    }

