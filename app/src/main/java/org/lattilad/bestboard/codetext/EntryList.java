package org.lattilad.bestboard.codetext;

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
        return lookUpLongest( reader, 0, entries.size()-1, -1);
        }

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

        if (cmp < 0)
            {
            builder.append("DOWN");
            Scribe.note( builder.toString() );
            return lookUpLongest(reader, first, middle - 1 );
            }

        if (cmp > 0)
            {
            builder.append("UP");
            Scribe.note(builder.toString());
            Entry entry = lookUpLongest(reader, middle + 1, last );
            return entry != null ? entry : lookUpLongest(reader, first, middle-1, cmp-1);
            }

        // EQ found !
        // continue searching in the direction of longer codes
        builder.append("EQ - SEARCHING FOR LONGEST");
        Scribe.note(builder.toString());
        Entry longerEntry = lookUpLongest(reader, middle + 1, last );
        return longerEntry == null ? entries.get(middle) : longerEntry;
        }


    public Entry lookUpShortest( SimpleReader reader )
        {
        return lookUpLongest( reader, 0, entries.size()-1, -1);
        }

    private Entry lookUpShortest( SimpleReader reader, int first, int last )
        {
        return lookUpLongest( reader, first, last, -1);
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

        if (cmp < 0)
            {
            builder.append("DOWN");
            Scribe.note( builder.toString() );
            return lookUpShortest(reader, first, middle - 1 );
            }

        if (cmp > 0)
            {
            builder.append("UP");
            Scribe.note(builder.toString());
            Entry entry = lookUpShortest(reader, first, middle-1, cmp-1);
            return entry != null ? entry : lookUpShortest(reader, middle + 1, last );
            }

        // EQ found !
        // continue searching in the direction of longer codes
        builder.append("EQ - SEARCHING FOR LONGEST");
        Scribe.note(builder.toString());
        //?? Lehetne egyesével ??
        Entry shorterEntry = lookUpShortest(reader, first, middle-1 );
        return shorterEntry == null ? entries.get(middle) : shorterEntry;
        }



/*            if (cmp < 0)
                last=middle-1;

            else if (cmp > 0)
                first=middle+1;

            else
                {
                // EQ FOUND !
                builder.append("EQ");
                Scribe.note( builder.toString() );

                if ( lengthType == LONGEST )
                    {
                    longestFound = middle;
                    builder.setLength(0); // clear builder before further search

                    // continue searching in the direction of longer codes
                    first = middle + 1;
                    }

                else // if ( lengthType == SHORTEST ) !! everything else is: shortest
                    {
                    while ( middle > last )
                        {
                        middle--;

                        reader.reset();
                        stringReverseReader.setString( entries.get(middle).getCode() );
                        if ( ShortCutEntry.compare( reader, stringReverseReader) != 0 )
                            {
                            middle++; // middle is not eq
                            Scribe.note( " ** SHORTEST: " + entries.get(middle).getCode() );
                            return entries.get(middle);
                            }
                        }
                    }
                }
            }

        if ( longestFound > -1 )
            {
            builder.append(" ** LONGEST: ").append( entries.get(longestFound).getCode() );
            Scribe.note( builder.toString() );
            return entries.get(longestFound);
            }

        builder.append(" ** NOT FOUND");
        Scribe.note( builder.toString() );
        return null;

        }
 */



    public static final int SHORTEST = -1;
    public static final int LONGEST = 1;

    public Entry lookUp( SimpleReader reader, int lengthType )
        {
        int	first;
        int	last;
        int	middle = 0 ;
        int longestFound = -1;
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
            stringReverseReader.setString( entries.get(middle).getCode() );
            cmp = ShortCutEntry.compare( reader, stringReverseReader);

            builder.append("[").append(entries.get(middle).getCode()).append("/")
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
                // EQ FOUND !
                builder.append("EQ");
                Scribe.note( builder.toString() );

                if ( lengthType == LONGEST )
                    {
                    longestFound = middle;
                    builder.setLength(0); // clear builder before further search

                    // continue searching in the direction of longer codes
                    first = middle + 1;
                    }

                else // if ( lengthType == SHORTEST ) !! everything else is: shortest
                    {
                    while ( middle > last )
                        {
                        middle--;

                        reader.reset();
                        stringReverseReader.setString( entries.get(middle).getCode() );
                        if ( ShortCutEntry.compare( reader, stringReverseReader) != 0 )
                            {
                            middle++; // middle is not eq
                            Scribe.note( " ** SHORTEST: " + entries.get(middle).getCode() );
                            return entries.get(middle);
                            }
                        }
                    }
                }
            }

        if ( longestFound > -1 )
            {
            builder.append(" ** LONGEST: ").append( entries.get(longestFound).getCode() );
            Scribe.note( builder.toString() );
            return entries.get(longestFound);
            }

        builder.append(" ** NOT FOUND");
        Scribe.note( builder.toString() );
        return null;
        }

    }
