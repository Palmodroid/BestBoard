package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Change case of the word or the selected text
 */
public class PacketChangeCase extends Packet
    {
    private boolean lower;
    private boolean upper;
    private boolean sentence;

    public PacketChangeCase(SoftBoardData softBoardData, boolean lower, boolean upper, boolean sentence)
        {
        super( softBoardData );

        this.lower = lower;
        this.upper = upper;
        // Simplified expression
        // if none of the params is true, then sentence should be true as default
        // ti. if BOTH lower AND upper ARE false, then sentence should be always true
        this.sentence = !(lower || upper) || sentence;
        }

    /**
     * String representation of the data (title)
     * @return String representation of this packet
     */
    @Override
    public String getString()
        {
        return "CAP";
        }

    /**
     * Send data to the editor field
     *
     *                  mixed v
     * lower -> sentence -> upper ->
     */
    @Override
    public void send()
        {
        String string = softBoardData.softBoardListener.getWordOrSelected();
        int stringCase = StringUtils.checkStringCase(string, 2048);

        while (true)
            {
            // IMPORTANT !!
            // All exit cases should be included in loop !!
            if (stringCase == StringUtils.LOWER_CASE)
                stringCase = StringUtils.SENTENCE_CASE;

            else if (stringCase == StringUtils.SENTENCE_CASE)
                stringCase = StringUtils.UPPER_CASE;

            else if (stringCase == StringUtils.UPPER_CASE)
                stringCase = StringUtils.LOWER_CASE;

            // NO letters to change
            else if (stringCase == StringUtils.UNKNOWN_CASE)
                return;

            // MIXED and not yet defined cases (Title?)
            // Most important part, because everything else will belong to UPPER !!
            else
                stringCase = StringUtils.UPPER_CASE;

            // StringCase will loop through these states
            // At least one should be enabled, and loop will break;
            if (lower && stringCase == StringUtils.LOWER_CASE)
                {
                softBoardData.softBoardListener.changeLastWordOrSelected
                        (string.toLowerCase(softBoardData.locale), true);
                return;
                }

            if (upper && stringCase == StringUtils.UPPER_CASE)
                {
                softBoardData.softBoardListener.changeLastWordOrSelected
                        (string.toUpperCase(softBoardData.locale), true);
                return;
                }

            if (sentence && stringCase == StringUtils.SENTENCE_CASE)
                {
                softBoardData.softBoardListener.changeLastWordOrSelected
                        (StringUtils.toSentenceCase(string, softBoardData.locale), true);
                return;
                }
            }
        }

    @Override
    public void release()
        {
        }

    }
