package org.lattilad.bestboard.abbreviation;


import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.SimpleReader;
import org.lattilad.bestboard.utils.StringReverseReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Abbreviations
    {
    public class Entry implements Comparable<Entry>
        {
        public Entry( String ending, String expanded )
            {
            this.ending = ending;
            this.expanded = expanded;
            }

        public String ending;
        public String expanded;

        // Should be similar to compare, but it doesn't stop after ending
        @Override
        public int compareTo(Entry another)
            {
            StringReverseReader thisString = new StringReverseReader( this.ending );
            StringReverseReader anotherString = new StringReverseReader( another.ending );
            int thisChar;
            int anotherChar;

            while ( (thisChar = thisString.read()) == (anotherChar = anotherString.read()) )
                {
                if ( thisChar == -1 ) // && == anotherChar; complete eq
                    return 0;
                }

            // if ( thisChar == -1 ) // && != anotherChar; ending eq
            //     return 0;

            return anotherChar-thisChar; // non eq
            }
        }

    private List<Entry> abbreviations = new ArrayList<>();

    public Abbreviations()
        {
        abbreviations.add( new Entry ("egy","1"));
        abbreviations.add( new Entry ("kettő","22"));
        abbreviations.add( new Entry ("három","333"));
        abbreviations.add( new Entry ("négy","4444"));
        abbreviations.add( new Entry ("öt","55555"));
        abbreviations.add( new Entry ("hat","666666"));
        abbreviations.add( new Entry ("hét","777777"));
        abbreviations.add( new Entry ("nyolc","88888888"));
        abbreviations.add( new Entry ("kilenc","99999999"));
        abbreviations.add( new Entry ("tiz","ttttttttt"));
        abbreviations.add( new Entry ("ttttttttten1","eeeeeeeeeee"));
        abbreviations.add( new Entry ("ttttttttten22","kkkkkkkkkkk"));
        abbreviations.add( new Entry ("abc","def"));
        abbreviations.add( new Entry ("def","ghi"));
        abbreviations.add( new Entry ("ghi","abc"));

        Collections.sort( abbreviations );

        for ( Entry entry : abbreviations )
            {
            Scribe.note( "Entry: " + entry.ending + " - " + entry.expanded) ;
            }

        }


    public Entry lookup(SimpleReader reader)
        {
        int	first;
        int	last;
        int	middle = 0 ;
        int	cmp = 0;
        StringReverseReader stringReverseReader = new StringReverseReader();

        first = 0;
        last = abbreviations.size()-1;

        StringBuilder builder = new StringBuilder();

        while ( first <= last )
            {
            // összesen v-e+1 elem, de k=ö/2 és k=(ö-1)/2 is jó (páratlannál azonos, párosnál a kisebb v. nagyobb "közepet" veszi"
            middle = first + (last - first)/2;

            reader.reset();
            stringReverseReader.setString( abbreviations.get(middle).ending );
            cmp = compare( reader, stringReverseReader);

            builder.append("[").append(abbreviations.get(middle).ending).append("/")
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
                return abbreviations.get(middle);
                }

            }

        builder.append(" ** NOT FOUND");
        Scribe.note( builder.toString() );
        return null;
        }

    private int compare( SimpleReader text, SimpleReader ending )
        {
        int textChar;
        int endingChar;

        while ( (textChar = text.read()) == (endingChar = ending.read()) )
            {
            if ( endingChar == -1 ) // && == textChar; complete eq
                return 0;
            }

        if ( endingChar == -1 ) // && != textChar; ending eq
            return 0;

        return endingChar-textChar; // non eq
        }

    }

