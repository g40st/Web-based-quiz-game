package de.fhwgt.quiz.error;


/**
 * Error class for passing errors up the call chain to connected clients.
 *
 * @author Simon Westphahl
 *
 */
public class QuizError {

    private ErrorType error;

    /**
     * Sets an error.
     *
     * @param error Error type to set
     */
    public void set(ErrorType error) {
        this.error = error;
    }

    /**
     * Checks if an error was set.
     *
     * @return <code>true</code> if an error was set;
     *          otherwise <code>false</code>
     */
    public boolean isSet() {
        return this.error == null ? false : true;
    }

    /**
     * Get the set error type.
     *
     * @return Set object that implements the ErrorType interface;
     *          <code>null</code> if no error was set
     */
    public ErrorType getType() {
        return this.error;
    }

    /**
     * Returns the error code of the set error type.
     *
     * This should only be called if there is an error. Make sure to check with
     * <code>hasError</code> prior to calling this method.
     *
     * @see ErrorType Possible status codes
     * @return Status code of the error type
     */
    public int getStatus() {
        return this.error.getStatus();
    }

    /**
     * Returns the description of the set error type.
     *
     * This should only be called if there is an error. Make sure to check with
     * <code>hasError</code> prior to calling this method.
     *
     * @return Error description
     */
    public String getDescription() {
        return this.error.getMessage();
    }

}
