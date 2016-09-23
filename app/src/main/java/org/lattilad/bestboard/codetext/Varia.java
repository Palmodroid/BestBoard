package org.lattilad.bestboard.codetext;

import java.util.*;
import org.lattilad.bestboard.*;

/**
 * Created by Beothe on 2016.08.20..
 */
public class Varia
    {
    private Map<String, VariaGroup> groups= new HashMap<>();
    private VariaGroup activeGroup = null;
    
    private long codeEntryAt = -1L;
    private int codeEntryLength;
    
    private boolean keepCode;

    
    public Varia( boolean keepCode )
        {
        this.keepCode = keepCode;
        }

    public boolean isKeepCode()
        {
        return keepCode;
        }
        
    public void setCodeEntry(long undoCounter, int length)
        {
        codeEntryAt = undoCounter;
        codeEntryLength = length;
        }
        
    public void deleteCodeEntryIfAvailable( SoftBoardListener processor )
        {
        // easier to test processCounter than keepCode
        if ( processor.checkProcessCounter( codeEntryAt ))
            {
            processor.sendDelete( -codeEntryLength );
            }
        }

    public void addGroup( VariaGroup group )
        {
        group.setVaria( this );
        groups.put( group.getCode(), group );

        if ( activeGroup == null )
            activeGroup = group;
        }

    public Collection<VariaGroup> getGroups()
        {
        return groups.values();
        }

    public void setActiveGroup( VariaGroup group )
        {
        activeGroup = group;
        }

    public String getText( int index )
        {
        return activeGroup.getText( index );
        }

    public String getTitle( int index )
        {
        return activeGroup.getTitle( index );
        }
    }
