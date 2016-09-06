package org.lattilad.bestboard.codetext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CodeText-s are a completely new feature. Code-texts are standard text, which are written by the
 * user. Bestboard translate these codes into special functions.
 * Currently two functions are implemented: abbreviation and varia-packets. Both of them should be
 * managed by CodeTextProcessor, because code-text entries are checked during typing.
 */
public class CodeTextProcessor
    {
    /* PART OF VARIA */

    /** Storage for all varia classes */
    private Map<Long, Varia> varias = new HashMap<>();

    /** Storage for code-text entries - VariaEntries are stored here */
    private EntryList variaEntries = new EntryList();


    /**
     * Stores a new varia collection. This varia collection is NOT initialized yet!
     * @param id    varia id keyword
     * @param varia collection
     * @return true if collection replaces a previous collection with the same id
     */
    public boolean addVaria(Long id, Varia varia)
        {
        return varias.put(id, varia) != null;
        }


    /**
     * Init all varia collections. VariaEntries are added to variaEntries.
     * Entries should know: CODE-TEXT and GROUP (Group contains its VARIA to activate)
     * activeVariaEntrieas are sorted!
     */
    private void initVaria()
        {
        for ( Varia varia : varias.values() )
            {
            for ( VariaGroup group : varia.getGroups() )
                {
                variaEntries.add( new VariaEntry( group.getCode(), group ));
                }
            }
        variaEntries.init( this ); // this will sort it
        codeEntries.addAll( variaEntries );
        }


    public Varia getVaria(Long id)
        {
        return varias.get( id );
        }


    /* PART OF ABBREVIATON */

    /**
     * Just stores the currently active abbreviations-list; no connection with these classes
     * IdList is defined by the button, but buttons cannot be stored, because of the clone() method
     */
    private long activeShortCutId = -1L;

    private Map<Long, EntryList> shortCuts = new HashMap<>();

    public EntryList getShortCut( Long id )
        {
        return shortCuts.get( id );
        }

    public boolean addShortCut( Long id, EntryList shortCut )
        {
        return shortCuts.put( id, shortCut) != null;
        }

    public boolean addShortCut( Long id, List<Long> shortCutList )
        {
        return shortCuts.put( id, new EntryListSet(shortCutList) ) != null;
        }

    private void initShortCut( boolean shortCutKeySet )
        {
        // initialize shortcutsets
        for ( EntryList shortCut : shortCuts.values() )
            {
            shortCut.init(this); // this will initialize shortcutsets, and sort shortcuts
            }

        // if no key is set, then all non-set shortcut should be added
        if ( !shortCutKeySet ) // no key is set at all
            {
            for ( EntryList shortCut : shortCuts.values() )
                {
                if ( !(shortCut instanceof EntryListSet) )
                    codeEntries.addAll( shortCut ); // all abbrevs should be used
                }
            }

        // if key is set, then active shortcut should start (if any)
        else if ( activeShortCutId != -1L )
            {
            codeEntries.addAll( shortCuts.get( activeShortCutId ) );
            }

        // no key is active - varia is already sorted
        else
            {
            return;
            }

        codeEntries.sort();
        }

    public boolean startShortCut( long id )
        {
        boolean overwrite = (activeShortCutId != -1);

        activeShortCutId = id;

        return overwrite;
        }

    public boolean isActiveShortCut( long id )
        {
        return id == activeShortCutId;
        }

    public void startAbbreviation( Long id )
        {
        stopAbbreviation();

        codeEntries.addAll( shortCuts.get( id ) );

        codeEntries.sort();
        activeShortCutId = id;
        }

    public void stopAbbreviation()
        {
        codeEntries.clear();
        // varia should remain intact after stop
        codeEntries.addAll( variaEntries );
        activeShortCutId = -1L;
        }


    /* COMMON PART */

    /** active code-entries */
    private EntryList codeEntries = new EntryList();

    public EntryList getCodeEntries()
        {
        return codeEntries;
        }

    /**
     * Init is called by parseMainDescriptorFile() when parsing is finished
     */
    public void init( boolean abbrevKeySet )
        {
        initVaria();
        initShortCut( abbrevKeySet );
        }

    }
