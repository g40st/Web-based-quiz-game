package de.fhwgt.quiz.application;

import java.util.Collection;
import java.util.Map;
import java.util.TimerTask;

import de.fhwgt.quiz.error.QuizError;
import de.fhwgt.quiz.error.QuizErrorType;
import de.fhwgt.quiz.loader.CatalogLoader;
import de.fhwgt.quiz.loader.LoaderException;

/**
 * Quiz class implemented as a singleton for easy access.
 *
 * All interaction with the quiz application logic should be performed through
 * this class.
 *
 * @author Simon Westphahl
 *
 */
public class Quiz {

    private static final Quiz quiz = new Quiz();
    private static final Game game = new Game();

    private CatalogLoader loader;

    private Quiz() {}

    /**
     * Returns the quiz singleton instance.
     *
     * @return Reference to Quiz instance
     */
    public static Quiz getInstance() {
        return Quiz.quiz;
    }

    /**
     * Returns the active catalog.
     *
     * If no catalog was set the return value is <code>null</code>.
     *
     * @return Reference to catalog; <code>null</code> if no catalog set
     */
    public Catalog getCurrentCatalog() {
        return game.getCatalog();
    }

    /**
     *  Selects the given catalog for this game.
     *
     *  Setting of the catalog fails if the given player is not the superuser,
     *  loading of the catalog failed or the catalog was not found and if
     *  the game is already in progress. In all cases the error type is set
     *  and can be checked.
     *
     * @param player Reference to player object
     * @param catalogName Catalog name
     * @param error Reference to error object
     * @return Catalog instance or <code>null</code>
     */
    public Catalog changeCatalog(Player player, String catalogName,
        QuizError error) {
        // Allow only the superuser to change the catalog
        if (!player.isSuperuser()) {
            error.set(QuizErrorType.NOT_SUPERUSER);
            return null;
        }

        Catalog catalog;
        try {
            catalog = getCatalogLoader().getCatalogByName(catalogName);
        } catch (LoaderException e) {
            error.set(QuizErrorType.CATALOG_LOAD_FAILED);
            return null;
        }

        if (catalog == null) {
            error.set(QuizErrorType.CATALOG_NOT_FOUND);
            return null;
        }

        // Set catalog
        if (game.setCatalog(catalog, error)) {
            return catalog;
        } else {
            return null;
        }

    }

    /**
     * Creates a new player with the given name.
     *
     * Create fails if the game is in progress, there are too many players
     * or the requested username was already taken. In any case those cases the
     * error type is set and can be checked.
     *
     * @param name Name of the new player
     * @param error Reference to error object
     * @return Reference to new player object; <code>null</code> on error
     */
    public Player createPlayer(String name, QuizError error) {
        Player player = new Player(name);

        // Try to add player to game
        if (game.addPlayer(player, error)) {
            return player;
        } else {
            return null;
        }
    }

    /**
     * Removes the given player from the game.
     *
     * Returns <code>true</code> if the remove lead to an end of game;
     * otherwise <code>false</code>.
     *
     * Callers MUST check if there was an error. All errors returned by this
     * method are fatal. That means that the game state was reset and all
     * other players must be disconnected.
     *
     * @param player Reference to player object
     * @param error Reference to error object
     * @return <code>true</code> if remove lead to end of game;
     *          else <code>false</code>
     */
    public boolean removePlayer(Player player, QuizError error) {
        // Remove player from game
        return game.removePlayer(player, error);
    }

    /**
     * Registers the given player as done.
     *
     * If registration of this player lead to an end of game the method returns
     * <code>true</code>. In this case the end of game must be handled.
     *
     * @param player Reference to player object
     * @return <code>true</code> if game is over; else <code>false</code>
     */
    public boolean setDone(Player player) {
        // User is done
        return game.setDone(player);
    }

    /**
     * Starts a new game.
     *
     * Start fails if the given player is not the superuser, the game is
     * already in progress, no catalog was selected, loading of the catalog
     * failed or there were not enough players. In any of those cases the
     * error type is set and can be checked.
     *
     * @param player Reference to player object
     * @param error Reference to error object
     * @return <code>true</code> if game was started; else <code>false</code>
     */
    public boolean startGame(Player player, QuizError error) {
        // Make sure only the superuser can start a game
        if (!player.isSuperuser()) {
            error.set(QuizErrorType.NOT_SUPERUSER);
            return false;
        }

        return game.start(error);
    }

    /**
     * Returns a new question for the given player and starts the timeout.
     *
     * If there was an error, the error type is set and the return value
     * is <code>null</code>. If there are no more questions to answer,
     * the return value is <code>null</code>.
     *
     * @param player Reference to player object
     * @param timeoutTask Reference to timeout task
     * @param error Reference to error object
     * @return Question reference or <code>null</code>
     */
    public Question requestQuestion(Player player, TimerTask timeoutTask,
        QuizError error) {

        // Try to get next question
        Question question = player.getNextQuestion(error);
        if (error.isSet()) {
            return null;
        }

        // No next question available
        if (question == null) {
            return null;
        }

        // Start timeout and clocking
        player.startTimeout(question.getTimeout(), timeoutTask);
        return question;
    }

    /**
     * Answers a open question for the given player.
     *
     * Cancels the timeout task and calculates the score for the provided
     * answer. Returns the index of the correct answer.
     *
     * If there was an error the error type is set and a value of -1 returned.
     *
     * @param selection Index of the selected answer
     * @param error Reference to error object
     * @return Index of the correct answer; -1 in case of error
     */
    public long answerQuestion(Player player, long index, QuizError error) {
        // Check for running game (only verbose for error reporting)
        if (!game.isActive()) {
            error.set(QuizErrorType.GAME_NOT_ACTIVE);
            return -1;
        }

        Long correctIndex = player.answerQuestion(index, error);

        if (error.isSet()) {
            return -1;
        } else if (index == correctIndex) {
            // Signal score change if answer was correct
            game.signalPlayerChange();
        }

        return correctIndex.longValue();
    }

    /**
     * Returns the catalog loader instance.
     *
     * If no catalog loader was set, a <code>IllegalStateException</code>
     * is thrown.
     *
     * @return Catalog loader instance
     */
    private CatalogLoader getCatalogLoader() {
        if (this.loader == null) {
            throw new IllegalStateException(
                "Catalog loader must be initialized first");
        } else {
            return this.loader;
        }
    }

    /**
     * Initialize the catalog loader.
     *
     * @param loader Reference to catalog loader implementation
     */
    public void initCatalogLoader(CatalogLoader loader) {
        this.loader = loader;
    }

    /**
     * Returns a map of available catalogs.
     *
     * @return Map of catalog names and catalogs
     * @throws LoaderException If the catalogs could not be loaded
     */
    public Map<String, Catalog> getCatalogList() throws LoaderException {
        return getCatalogLoader().getCatalogs();
    }

    /**
     * Returns the catalog with the given name.
     *
     * @param catalogName Name of the catalog
     * @return Catalog instance or <code>null</code> if not found
     * @throws LoaderException If loading of the catalogs failed
     */
    public Catalog getCatalogByName(String catalogName) throws LoaderException {
        return getCatalogLoader().getCatalogByName(catalogName);
    }

    /**
     * Returns a collection of active players.
     *
     * @return Collection of players
     */
    public Collection<Player> getPlayerList() {
        return game.getPlayers();
    }

    /**
     * Waits for a change of the player list.
     *
     * This will block the calling thread until it is notified of a change.
     */
    public void waitPlayerChange() {
        game.waitPlayerChange();
    }

    /**
     * Notifies waiting threads of a change of the player list.
     *
     * This will unblock all waiting threads.
     */
    public void signalPlayerChange() {
        game.signalPlayerChange();
    }
}
