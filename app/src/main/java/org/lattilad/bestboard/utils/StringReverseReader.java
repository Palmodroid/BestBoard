package org.lattilad.bestboard.utils;

/**
 * Reads characters from the end
 */
public class StringReverseReader implements SimpleReader
    {
    private String string = null;
    private int pointer = 0 ;

    public StringReverseReader() // setString should be used
        { }

    public StringReverseReader( String string )
        {
        setString( string );
        }

    public void setString( String string )
        {
        this.string = string;
        reset();
        }

    @Override
    public int read()
        {
        pointer--;
        if ( pointer < 0 )
            return -1;
        else
            return string.charAt( pointer );
        }

    @Override
    public void reset()
        {
        pointer = ( string == null) ? 0 : string.length();
        }
    }
