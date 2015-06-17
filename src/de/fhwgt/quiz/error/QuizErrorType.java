package de.fhwgt.quiz.error;


/**
 * Enum of available error types used by the application logic.
 *
 * @author Simon Westphahl
 *
 */
public enum QuizErrorType implements ErrorType {

    // Login errors
    USERNAME_TAKEN(1, "Username already taken"),
    TOO_MANY_PLAYERS(2, "Too many players"),
    GAME_IN_PROGRESS(3, "Game already in progress"),

    // Errors preventing game start
    NOT_ENOUGH_PLAYERS(4, "Not enough players"),
    NO_CATALOG_SELECTED(5, "No catalog selected"),

    // Errors before game start
    CATALOG_LOAD_FAILED(6, "Could not load catalog"),
    SUPERUSER_LEFT(7, "Superuser left"),

    // Errors during game
    PLAYER_UNDERSHOT(8, "Not enough players to continue"),

    // Errors for not permitted actions
    NOT_SUPERUSER(9, "Not superuser"),
    GAME_NOT_ACTIVE(10, "Game is not active"),
    OPEN_QUESTION(11, "Open Question"),
    NO_OPEN_QUESTION(12, "No open question"),
    CATALOG_NOT_FOUND(13, "Catalog was not found");

    private final int status;
    private final String message;

    private QuizErrorType(int status, String message) {
      this.status = status;
      this.message = message;
    }

    /**
     * Get the error status code.
     *
     * @return Error status code
     */
    @Override
    public int getStatus() {
        return this.status;
    }

    /**
     * Get the error description.
     *
     * @return Error description
     */
    @Override
    public String getMessage() {
       return this.message;
    }
}
