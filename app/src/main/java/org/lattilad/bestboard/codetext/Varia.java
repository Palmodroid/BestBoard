package org.lattilad.bestboard.codetext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beothe on 2016.08.20..
 */
public class Varia
    {
    private Map<String, VariaGroup> groups= new HashMap<>();
    private VariaGroup activeGroup = null;

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
    }
