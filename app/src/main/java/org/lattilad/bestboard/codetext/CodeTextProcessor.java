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
        variaEntries.sort();
        codeEntries.addAll( variaEntries );
        }


    public Varia getVaria(Long id)
        {
        return varias.get( id );
        }


    /* PART OF ABBREVIATON */


    private Map<Long, EntryList> shortCuts = new HashMap<>();


    public boolean addShortCut( Long id, EntryList shortCut )
        {
        return shortCuts.put( id, shortCut) != null;
        }

    public boolean addShortCut( Long id, List<Long> shortCutList )
        {
        return shortCuts.put( id, new ShortCutSet(shortCutList) ) != null;
        }




    /**
     * Just stores the currently active abbreviations-list; no connection with these classes
     * IdList is defined by the button, but buttons cannot be stored, because of the clone() method
     */
    public List<Long> activeAbbrevIdList = null;

    /**
     * Storage for code-text entries
     * Abbreviations do not have a separate class, they are just stored as EntryList of AbbreviationEntries
     * All these entries (grouped by id-s) are stored here
     */
    private Map<Long, EntryList> abbreviations = new HashMap<>();

    /**
     * Stores a new abbreviation collection, which is NOT Initialised yet!
     * @param id abbreviation id keyword
     * @param abbreviation collection of abbreviation entries
     * @return true if collection replaces a previous collection with the same id
     */
    public boolean addAbbreviation(Long id, EntryList abbreviation )
        {
        return abbreviations.put( id, abbreviation) != null;
        }

    private void initAbbreviation( boolean abbrevKeySet )
        {
        if ( !abbrevKeySet ) // no key is set at all
            {
            for ( EntryList abbreviation : abbreviations.values() )
                {
                codeEntries.addAll( abbreviation ); // all abbrevs should be used
                }
            }
        else if ( activeAbbrevIdList != null ) // start key is set
            {
            for ( Long id : activeAbbrevIdList )
                {
                codeEntries.addAll( abbreviations.get( id ) ); // keys abbrevs should be used
                }
            }
        else // keys are available, but no starting key is set
            {
            return; // there are no abbreviations at all, no further sort is needed
            }
        codeEntries.sort();
        }


    /* COMMON PART */

    /** active code-entries */
    private EntryList codeEntries = new EntryList();

    /**
     * Init is called by parseMainDescriptorFile() when parsing is finished
     */
    public void init( boolean abbrevKeySet )
        {
        initVaria();
        initAbbreviation( abbrevKeySet );
        }

    public void startAbbreviation(List<Long> idList )
        {
        stopAbbreviation();

        for ( Long id : idList )
            {
            codeEntries.addAll( abbreviations.get( id ) );
            }

        codeEntries.sort();
        }

    public void stopAbbreviation()
        {
        codeEntries.clear();
        // varia should remain intact after stop
        codeEntries.addAll( variaEntries );
        }

    public EntryList getCodeEntries()
        {
        return codeEntries;
        }
    }
