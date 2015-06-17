package de.fhwgt.quiz.application;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.fhwgt.quiz.error.QuizError;
import de.fhwgt.quiz.error.QuizErrorType;
import de.fhwgt.quiz.loader.LoaderException;


/**
 * The <code>Game</code> class holds game state and maintains a list of
 * active players as well as the selected catalog of questions for the game.
 * All state changing methods are synchronized to avoid race conditions.
 * Threads can wait for a change of the player list (block).
 *
 * @author Simon Westphahl
 *
 */
public class Game {

    private static final int USER_MIN = 2;
    private static final int USER_MAX = 6;

    private final Lock lock = new ReentrantLock();
    private final Condition playerChanged = lock.newCondition();

    private final AtomicReference<Catalog> catalog =
        new AtomicReference<Catalog>();

    private final Map<String, Player> players =
        new ConcurrentSkipListMap<String, Player>();

    private boolean active = false;
    private long playerIds = -1;
    private long activePlayers = 0;

    /**
     * Resets the game to its initial state.
     */
    private void reset() {
        this.playerIds = -1;
        this.players.clear();
        this.activePlayers = 0;
        this.catalog.set(null);
        this.active = false;
        this.signalPlayerChange();
    }

    /**
     * Starts a new game.
     *
     * Start fails if the game is already in progress, no catalog was selected,
     * loading of the catalog failed or there were not enough players. In
     * any of those cases the error type is set and can be checked.
     *
     * @param error Reference to error object
     * @return <code>true</code> if game was started; else <code>false</code>
     */
    protected synchronized boolean start(QuizError error) {

        // Make sure preconditions are meet
        if (isActive()) {
            error.set(QuizErrorType.GAME_IN_PROGRESS);
            return false;
        } else if (!hasCatalog()) {
            error.set(QuizErrorType.NO_CATALOG_SELECTED);
            return false;
        }

        if (this.players.size() >= USER_MIN) {
            try {
                assignQuestions();
            } catch (LoaderException e) {
                error.set(QuizErrorType.CATALOG_LOAD_FAILED);
                return false;
            }
            this.active = true;
            return true;
        } else {
            error.set(QuizErrorType.NOT_ENOUGH_PLAYERS);
            return false;
        }
    }

    /**
     * Checks if the game is currently active.
     *
     * @return <code>true</code> if game is active; else <code>false</code>
     */
    public synchronized boolean isActive() {
        return this.active;
    }

    /**
     * Selects the given catalog for this game.
     *
     * Setting of the catalog fails if the game is already in progress.
     *
     * @param catalog Reference to catalog object
     * @param error Reference to error object
     * @return <code>true</code> if set successful; else <code>false</code>
     */
    protected synchronized boolean setCatalog(Catalog catalog, QuizError error) {
        if (isActive()) {
            error.set(QuizErrorType.GAME_IN_PROGRESS);
            return false;
        }
        this.catalog.set(catalog);
        return true;
    }

    /**
     * Returns the active catalog.
     *
     * If no catalog was set the return value is <code>null</code>.
     *
     * @return Reference to catalog; <code>null</code> if no catalog set
     */
    public Catalog getCatalog() {
        return this.catalog.get();
    }

    /**
     * Checks if a catalog was set.
     *
     * @return <code>true</code> if set; else <code>false</code>
     */
    public boolean hasCatalog() {
        return (this.catalog.get() == null) ? false : true;
    }

    /**
     * Returns a collection of active players.
     *
     * @return Collection of players
     */
    public Collection<Player> getPlayers() {
        return players.values();
    }

    /**
     * Adds a new player to the game.
     *
     * Returns true if the player was added successfully or false if
     * the player could not be added.
     *
     * Add fails if the game is in progress, there are too many players
     * or the requested username was already taken. In any case those cases the
     * error type is set and can be checked.
     *
     * @param player Reference to player object
     * @param error Reference to error object
     * @return <code>true</code> if success; else <code>false</code>
     */
    public synchronized boolean addPlayer(Player player, QuizError error) {

        // Only login if no game is active
        if (isActive()) {
            error.set(QuizErrorType.GAME_IN_PROGRESS);
            return false;
        }

        // Make sure there are only MAX_PLAYER active players
        if (players.size() >= USER_MAX) {
            error.set(QuizErrorType.TOO_MANY_PLAYERS);
            return false;
        }

        // Try to login player
        if (players.containsKey(player.getName())) {
            error.set(QuizErrorType.USERNAME_TAKEN);
            return false;
        } else {
            ++activePlayers;
            player.setId(++playerIds);
            players.put(player.getName(), player);
            this.signalPlayerChange();
        }
        return true;
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
    public synchronized boolean removePlayer(Player player, QuizError error) {

        // Stop possible timer
        player.stopTimeout();

        if (players.remove(player.getName()) == null) {
            // User did not exist
            return false;
        }

        if (players.isEmpty()) {
            reset();
            return false;
        } else if (players.size() == 1 && isActive()) {
            error.set(QuizErrorType.PLAYER_UNDERSHOT);
            reset();
            return false;
        } else if (player.isSuperuser() && !isActive()) {
            error.set(QuizErrorType.SUPERUSER_LEFT);
            reset();
            return false;
        }

        // Register player as done if game is active
        return setDone(player);
    }

    /**
     * Assigns questions from the current catalog to the players.
     *
     * @throws LoaderException If there was an error loading the
     *          catalog
     */
    private void assignQuestions() throws LoaderException {
        List<Question> questions = this.catalog.get().getQuestions();
        Collections.shuffle(questions);

        for (Player player : players.values()) {
            player.setQuestions(questions);
        }
    }

    /**
     * Registers the given player as done.
     *
     * @param player Reference to player object
     * @return <code>true</code> if game is over; else <code>false</code>
     */
    public synchronized boolean setDone(Player player) {

        if (activePlayers > 0) {
            --activePlayers;
        }

        this.signalPlayerChange();
        if (activePlayers == 0) {
            return true;
        }
        return false;
    }

    /**
     * Waits for a change of the player list.
     *
     * This will block the calling thread until it is notified of a change.
     */
    public void waitPlayerChange() {
        lock.lock();
        try {
            playerChanged.await();
        } catch (InterruptedException e) {
            // Can be ignored
        } finally {
            lock.unlock();
        }
    }

    /**
     * Notifies waiting threads of a change of the player list.
     *
     * This will unblock all waiting threads.
     */
    public void signalPlayerChange() {
        lock.lock();
        playerChanged.signalAll();
        lock.unlock();
    }

}
