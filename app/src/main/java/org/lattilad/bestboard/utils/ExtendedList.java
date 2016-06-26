package org.lattilad.bestboard.utils;


public class ExtendedList<V> extends java.util.ArrayList<V> implements ExtendedCopy
    {

    @Override
    public ExtendedList<V> getCopy()
        {
        ExtendedList<V> listCopy = new ExtendedList<>();

        for ( V entry : this )
            {
            listCopy.add (( entry instanceof ExtendedCopy ) ?
                         (V)((ExtendedCopy)entry).getCopy() :
                         entry );
            }

        return listCopy;
        }

	}
