package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.SimpleReader;
import org.lattilad.bestboard.utils.StringReverseReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beothe on 2016.08.19..
 */
public class EntryList
    {
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

    public void init( CodeTextProcessor codeTextProcessor )
        {
        sort();

        Scribe.note("Entries after sort:");
        for (int n = 0; n < entries.size(); n++)
            {
            Scribe.note("    " + n + " - [" + entries.get(n).getCode() + "]");
            }
        }

    public Entry lookUpLongest( SimpleReader reader )
        {
        Scribe.debug(Debug.CODETEXT, "Searching for longest matching code");
        return lookUpLongest( reader, 0, entries.size()-1, -1);
        }

    // Just a helper method with no limit in maximal length
    private Entry lookUpLongest( SimpleReader reader, int first, int last )
        {
        return lookUpLongest( reader, first, last, -1);
        }

    private Entry lookUpLongest( SimpleReader reader, int first, int last, int maxLength )
        {
        if (last < first)
            {
            Scribe.note("NO MORE ENTRIES");
            return null;
            }

        // összesen v-e+1 elem, de k=ö/2 és k=(ö-1)/2 is jó (páratlannál azonos, párosnál a kisebb v. nagyobb "közepet" veszi"
        int middle = first + (last - first) / 2;

        reader.reset();
        StringReverseReader stringReverseReader = new StringReverseReader(entries.get(middle).getCode());
        int cmp = ShortCutEntry.compare(reader, stringReverseReader, maxLength);

        StringBuilder builder = new StringBuilder();
        builder.append("[").append(entries.get(middle).getCode()).append("/")
                .append(cmp).append("/")
                .append(first).append("-")
                .append(middle).append("-")
                .append(last)
                .append("] * ");


        if ( cmp <= -1 )
            {
            builder.append("NO OR PARTIAL MATCH, SEARCH IN ALFA DIRECTION.");
            Scribe.debug( Debug.CODETEXT, builder.toString() );
            return lookUpLongest(reader, first, middle - 1 );
            }

        if ( cmp == +1 )
            {
            builder.append("NO MATCH, SEARCH IN OMEGA DIRECTION.");
            Scribe.debug( Debug.CODETEXT, builder.toString() );
            return lookUpLongest(reader, middle + 1, last );
            }

        if (cmp > +1)
            {
            builder.append("PARTIAL MATCH, SEARCH LONGER IN OMEGA, SHORTER IN ALFA DIRECTION.");
            Scribe.debug( Debug.CODETEXT, builder.toString() );
            Entry longerEntry = lookUpLongest(reader, middle+1, last );
            return longerEntry != null ? longerEntry : lookUpLongest(reader, first, middle - 1, cmp-1 ); // it should be shorter !
            }

        // cmp == 0 - EQ found !
        builder.append("COMPLETE MATCH, SEARCH LONGER IN OMEGA DIRECTION.");
        Scribe.debug( Debug.CODETEXT, builder.toString() );
        Entry longerEntry = lookUpLongest(reader, middle+1, last );
        return longerEntry == null ? entries.get(middle) : longerEntry;
        }


    public Entry lookUpShortest( SimpleReader reader )
        {
        Scribe.debug(Debug.CODETEXT, "Searching for shortest matching code");
        return lookUpShortest( reader, 0, entries.size()-1, -1);
        }

    // Just a helper method with no limit in maximal length
    private Entry lookUpShortest( SimpleReader reader, int first, int last )
        {
        return lookUpShortest( reader, first, last, -1);
        }

    private Entry lookUpShortest( SimpleReader reader, int first, int last, int maxLength )
        {
        if (last < first)
            {
            Scribe.note("NO MORE ENTRIES");
            return null;
            }

        // összesen v-e+1 elem, de k=ö/2 és k=(ö-1)/2 is jó (páratlannál azonos, párosnál a kisebb v. nagyobb "közepet" veszi"
        int middle = first + (last - first) / 2;

        reader.reset();
        StringReverseReader stringReverseReader = new StringReverseReader(entries.get(middle).getCode());
        int cmp = ShortCutEntry.compare(reader, stringReverseReader, maxLength);

        StringBuilder builder = new StringBuilder();
        builder.append("[").append(entries.get(middle).getCode()).append("/")
                .append(cmp).append("/")
                .append(first).append("-")
                .append(middle).append("-")
                .append(last)
                .append("] * ");

        if ( cmp <= -1 )
            {
            builder.append("NO OR PARTIAL MATCH, SEARCH IN ALFA DIRECTION.");
            Scribe.debug( Debug.CODETEXT, builder.toString() );
            return lookUpShortest(reader, first, middle - 1 );
            }

        if ( cmp == +1 )
            {
            builder.append("NO MATCH, SEARCH IN OMEGA DIRECTION.");
            Scribe.debug( Debug.CODETEXT, builder.toString() );
            return lookUpShortest(reader, middle + 1, last );
            }

        if (cmp > +1)
            {
            builder.append("PARTIAL MATCH, SEARCH SHORTER IN ALFA LONGER IN OMEGA DIRECTION.");
            Scribe.debug( Debug.CODETEXT, builder.toString() );
            Entry shorterEntry = lookUpShortest(reader, first, middle-1, cmp-1 );  // It should be shorter!
            return shorterEntry != null ? shorterEntry : lookUpShortest(reader, middle + 1, last );
            }

        // cmp == 0 - EQ found !
        builder.append("COMPLETE MATCH, SEARCH SHORTER IN ALFA DIRECTION.");
        Scribe.debug( Debug.CODETEXT, builder.toString() );
        Entry shorterEntry = lookUpShortest(reader, first, middle-1, cmp-1 );  // It should be shorter!
        return shorterEntry == null ? entries.get(middle) : shorterEntry;

        // !! Consider stepwise check if looking up shorter entry in alfa direction,
        // because similar endings should mean a badly organized list
        // stepwise check could be quicker in most circumstances ??
        // almafa/fa/a is badly organized;
        // za,wa...ca,ba,aa can be searched better with halving
        // but in most cases there will be no similar endings!
        }

    }
