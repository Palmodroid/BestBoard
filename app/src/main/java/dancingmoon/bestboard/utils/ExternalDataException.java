package dancingmoon.bestboard.utils;


public class ExternalDataException extends Exception
    {
    private static final long serialVersionUID = -1708939124718783386L;

    /**
     * Constructs a new {@code Exception} that includes the current stack trace.
     */
    public ExternalDataException()
        {
        }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public ExternalDataException(String detailMessage)
        {
        super(detailMessage);
        }

    /**
     * Constructs a new {@code Exception} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable the cause of this exception.
     */
    public ExternalDataException(String detailMessage, Throwable throwable)
        {
        super(detailMessage, throwable);
        }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public ExternalDataException(Throwable throwable)
        {
        super(throwable);
        }
    }
