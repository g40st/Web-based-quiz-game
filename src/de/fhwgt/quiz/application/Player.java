package de.fhwgt.quiz.application;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.fhwgt.quiz.error.QuizError;
import de.fhwgt.quiz.error.QuizErrorType;


/**
 * Player class which encapsulates data and logic associated with a player.
 *
 * @author Simon Westphahl
 *
 */
public class Player {

    private static Timer timer = new Timer();

    private final String name;

    private boolean questionActive = false;
    private boolean done = false;
    private long playerId = -1;
    private long score;
    private long start;
    private long stop;
    private int questionIndex = -1;
    private List<Question> questions;
    private TimerTask timeoutTask;

    /**
     * Constructs a new player with the given name.
     *
     * @param name Player name
     */
    protected Player(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the player.
     *
     * @return Name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the score of the player.
     *
     * @return Score value
     */
    public long getScore() {
        return this.score;
    }

    /**
     * Sets the player ID.
     *
     * @param id Player id (positive) or -1 if not set
     */
    protected void setId(long id) {
        this.playerId = id;
    }

    /**
     * Returns the player ID.
     *
     * @return Player id
     */
    public Long getId() {
        return this.playerId;
    }

    /**
     * Checks if the player is active.
     *
     * An active player has a valid id that is greater than or equal to 0;
     *
     * @return <code>true</code> if active; else <code>false</code>
     */
    public boolean isActive() {
        if (this.playerId < 0)
            return false;
        else
            return true;
    }

    /**
     * Checks if the player is the superuser.
     *
     * A superuser has a player ID of 0.
     *
     * @return <code>true</code> if superuser; else <code>false</code>
     */
    public boolean isSuperuser() {
        if (this.playerId == 0)
            return true;
        else
            return false;
    }

    /**
     * Checks if the user is done answering questions.
     *
     * @return <code>true</code> if done; else <code>false</code>
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Checks if there are questions assign to the client.
     *
     * Returns true if there are questions assign, false if there are
     * no questions. This can be used to determine if there is a
     * active game;
     *
     * @return <code>true</code> if there are questions assigned;
     *          otherwise <code>false</code>
     */
    private boolean hasQuestions() {
        if (this.questions == null)
            return false;
        else
            return true;
    }

    /**
     * Assigns a list of questions to the player.
     *
     * @param questions List of questions
     */
    protected void setQuestions(List<Question> questions) {
        this.questions = questions;
        this.questionIndex = this.questions.size();
    }

    /**
     * Returns the next question from the list of questions.
     *
     * If there was an error, the error type is set and the return value
     * is <code>null</code>. If there are no more questions to answer,
     * only the return value is <code>null</code>.
     *
     * @param error Reference to error object
     * @return Question reference or <code>null</code>
     */
    protected synchronized Question getNextQuestion(QuizError error) {
        // Make sure there are questions assigned
        if (!this.hasQuestions()) {
            error.set(QuizErrorType.GAME_NOT_ACTIVE);
            return null;
        }

        // Unanswered question
        if (questionActive) {
            error.set(QuizErrorType.OPEN_QUESTION);
            return null;
        }

        // Check if player is done
        if (this.questionIndex > 0) {
            this.questionIndex--;
            this.questionActive = true;
            return this.questions.get(this.questionIndex);
        } else {
            this.done = true;
            return null;
        }
    }

    /**
     * Starts time measurement for a new question and schedules the
     * timeout task for execution.
     *
     * @param timeout Timeout for question in milliseconds
     * @param timeoutTask Timeout task
     */
    protected void startTimeout(long timeout, TimerTask timeoutTask) {
        this.timeoutTask = timeoutTask;
        this.start = System.currentTimeMillis();
        Player.timer.schedule(this.timeoutTask, timeout);
    }

    /**
     * Cancels the timeout task.
     */
    protected void stopTimeout() {
        if (this.timeoutTask != null)
            this.timeoutTask.cancel();
    }

    /**
     * Answers a open question.
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
    protected synchronized long answerQuestion(long selection, QuizError error) {
        if (!questionActive) {
            error.set(QuizErrorType.NO_OPEN_QUESTION);
            return -1L;
        }
        this.stopTimeout();
        this.stop = System.currentTimeMillis();
        this.questionActive = false;
        Question question = questions.get(this.questionIndex);

        // Add score if answer was valid
        if (question.validateAnswer(selection))
            this.score += scoreForTimeLeft(this.stop - this.start,
                question.getTimeout());

        return question.getCorrectIndex();
    }

    /**
     * Calculates the score depending on the time left.
     *
     * @return Calculated score
     */
    private long scoreForTimeLeft(long timeTaken, long timeout) {
        // Make sure the time left is not smaller than 0
        long timeLeft = Math.max(timeout - timeTaken, 0);
        long score = (timeLeft * 1000) / timeout;
        return ((score + 5) / 10) * 10;
    }
}
