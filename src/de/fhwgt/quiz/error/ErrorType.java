package de.fhwgt.quiz.error;


/**
 * Common interface for all available error types.
 *
 * @author Simon Westphahl
 *
 */
public interface ErrorType {

    /**
     * Get the error status code.
     *
     * @return Error status code
     */
    public int getStatus();

    /**
     * Get the error description.
     *
     * @return Error description
     */
    public String getMessage();
}
