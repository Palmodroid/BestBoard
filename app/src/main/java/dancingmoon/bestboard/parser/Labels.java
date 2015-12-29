package dancingmoon.bestboard.parser;

import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;


/**
 * Labels of coat descriptor language
 * <p>
 * Three types of constants can be defined in coat language.
 * Char 'c', String "text" and integer numbers (long precision).
 * <p>
 * These constants can be identified by labels.
 * Labels (keyword-tokens) can be used instead of constants in descriptor file.
 * <p>
 * Some complex parameters can be identified by labels, too.
 * The type of these parameters matches to their TOKEN defined by Commands class.
 * Allowed tokens are determined by SoftBoardParser.parseLabels() method.
 * <p>
 * Constants can be defined either directly or by labels,
 * but precision check should be performed after both.
 * Therefore precision check at label level is not needed:
 * we just need two types of labels (and several others for complex types)
 * numeric (long precision, defined originally as char/integer)
 * string (defined originally as string)
 * we can read out:
 * long from numeric - it can be converted to char (after precision check)
 * string from string
 * (String cannot be null).
 * <p>
 * Common labels can be set in constructor.
 * User defined labels can be set by 'LET (label value)' command.
 * <p>
 * ((The difference between Commands and Labels class:
 * Commands - static data for interpretting language.
 * Labels - dynamic data, expanding during parse.
 * All SoftBoardParser instance can use the same Commands class,
 * while each instance should have its own Labels class.
 * This class is not thread-safe! Can be used only from one thread!))
 */
public class Labels
    {
    /** Inner class describes stored values */
    private class Data
        {
        private final long type;
        private final Object value;

        private Data (long type, Object value)
            {
            this.type = type;
            this.value = value;
            }
        }

    // Long token-code (as Key) and data (type and value) pairs are stored in this private HashMap
    private Map<Long, Data> data = new HashMap<>();


    /**
     ** ADD METHODS
     **/

    /**
     * Add a new label
     * @param key label's token-code (cannot be null)
     * @param type type of value (see commands)
     * @param value value (cannot be null)
     * @return true if label was already defined
     * @throws IllegalArgumentException if key or value is null
     */
    public boolean add( Long key, long type, Object value ) throws IllegalArgumentException
        {
        if ( key != null && value != null )
            {
            return data.put( key, new Data (type, value) ) != null;
            }
        throw new IllegalArgumentException("Null is not allowed!");
        }

    /**
     * Add label with numeric value. Type is Commands.PARAMETER_LONG
     * @param key label's token-code (cannot be null)
     * @param longValue value
     * @return true if label was already defined
     * @throws IllegalArgumentException if key is null
     */
    public boolean add( Long key, long longValue )
        {
        return add(key, Commands.PARAMETER_LONG, (Long)longValue);
        }

    /**
     * Add label with String value. Type is Commands.PARAMETER_STRING
     * @param key label's token-code (cannot be null)
     * @param stringValue value (cannot be null)
     * @return true if label was already defined
     * @throws IllegalArgumentException if key or value is null
     */
    public boolean add( Long key, String stringValue )
        {
        return add(key, Commands.PARAMETER_STRING, stringValue);
        }


    /**
     ** GET METHODS
     **/

    /**
     * Get value for key and type
     * @param key label's token-code
     * @param type label's type
     * @return value
     * @throws InvalidKeyException if label doesn't exist or type is different
     */
    public Object get( Long key, long type  ) throws InvalidKeyException
        {
        Data item = data.get( key );
        if ( item == null )
            throw new InvalidKeyException("No label!");
        if ( item.type != type )
            throw new InvalidKeyException("Invalid type!");
        return item.value;
        }

    /**
     * Get long value for given key
     * @param key label's token-code
     * @return long value
     * @throws InvalidKeyException if label doesn't exist or type is not Commands.PARAMETER_LONG
     */
    public long getLongValue( Long key  ) throws InvalidKeyException
        {
        return (long)get( key, Commands.PARAMETER_LONG );
        }

    /**
     * Get String value for given key. String value can be null
     * @param key label's token-code
     * @return String value
     * @throws InvalidKeyException if label doesn't exist or type is not Commands.PARAMETER_STRING
     */
    public String getStringValue( Long key  ) throws InvalidKeyException
        {
        return (String)get( key, Commands.PARAMETER_STRING );
        }


    /**
     ** GET METHODS FOR PREVIOUSLY SELECTED LABELS
     **/

    // Item should be seleceted before get type or value
    private Data selectedItem = null;

    /**
     * Selects label by key (independently from its type).
     * Type and value of the selected label are get by other methods.
     * @param key  label's token-code
     * @throws InvalidKeyException if label doesn't exist
     */
    public void select( Long key ) throws InvalidKeyException
        {
        selectedItem = data.get( key );
        if ( selectedItem == null )
            throw new InvalidKeyException("No label!");
        }

    /**
     * Helper method to check wether type of selected label is numeric
     * @return true if selected label is numeric
     * @throws InvalidKeyException if no label was selected
     */
    public boolean isNumericSelected() throws InvalidKeyException
        {
        return getTypeOfSelected() == Commands.PARAMETER_LONG;
        }

    /**
     * Helper method to check wether type of selected label is string
     * @return true if selected label is string
     * @throws InvalidKeyException if no label was selected
     */
    public boolean isStringSelected() throws InvalidKeyException
        {
        return getTypeOfSelected() == Commands.PARAMETER_STRING;
        }

    /**
     * Get type of selected label
     * @return type of selected label
     * @throws InvalidKeyException if no label was selected
     */
    public long getTypeOfSelected() throws InvalidKeyException
        {
        if (selectedItem == null)
            throw new InvalidKeyException("No label was selected!");
        return selectedItem.type;
        }

    /**
     * Get value of selected label. Type should be checked by getTypeOfSelected()
     * @return type of selected label
     * @throws InvalidKeyException if no label was selected
     */
    public Object getValueOfSelected() throws InvalidKeyException
        {
        if (selectedItem == null)
            throw new InvalidKeyException("No label was selected!");
        return selectedItem.value;
        }
    }
