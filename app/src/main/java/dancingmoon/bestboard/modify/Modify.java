package dancingmoon.bestboard.modify;

import dancingmoon.bestboard.SoftBoardData;

public abstract class Modify
    {
    /**
     * Ignores spaces between cursor and character if true
     */
    protected boolean ignoreSpace;

    /**
     * SoftBoardListener is needed to communicate with Service's StoredText class.
     */
    protected SoftBoardData.SoftBoardListener softBoardListener;

    /**
     * Constructor gets the communication channel with Service and StoredText, as SoftBoardListener
     * @param softBoardListener listener to communicate with the service
     * @param ignoreSpace spaces are ignored between the text and the cursor
     */
    protected Modify( SoftBoardData.SoftBoardListener softBoardListener, boolean ignoreSpace )
        {
        this.softBoardListener = softBoardListener;
        this.ignoreSpace = ignoreSpace;
        }

    public abstract void change();
    public abstract void changeBack();
    }
