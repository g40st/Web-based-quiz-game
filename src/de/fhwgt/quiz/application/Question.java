package de.fhwgt.quiz.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Question encapsulates all data belonging to a question.
 *
 * @author Simon Westphahl
 *
 */
public class Question {

    private static final int ANSWER_COUNT = 4;
    private static final long DEFAULT_TIMEOUT = 10000;
    private static final int MILLISECOND_FACTOR = 1000;

    private final String question;
    private final List<String> answers = new ArrayList<String>();
    private String correctAnswer;
    private long timeout = DEFAULT_TIMEOUT;

    /**
     * Constructs a new question with the given question text.
     *
     * @param question Question text
     */
    public Question(String question) {
        this.question = question;
    }

    /**
     * Returns the question text.
     *
     * @return Question text
     */
    public String getQuestion() {
        return this.question;
    }

    /**
     * Checks if the question is complete.
     *
     * A complete question has four answers total and one correct answer.
     * The question should not be added to a catalog if it is not complete.
     *
     * @return <code>true</code> if complete; else <code>false</code>
     */
    public boolean isComplete() {
        if (correctAnswer != null && answers.size() == ANSWER_COUNT)
            return true;
        else
            return false;
    }

    /**
     * Returns the timeout for this question.
     *
     * @return Timeout in milliseconds
     */
    public long getTimeout() {
        return this.timeout;
    }

    /**
     * Adds a bogus answer for this question.
     *
     * @param answer Bogus answer
     */
    public void addBogusAnswer(String answer) {
        this.answers.add(answer);
    }

    /**
     * Adds the correct answer for this question.
     *
     * @param answer Correct answer
     */
    public void addAnswer(String answer) {
        this.answers.add(answer);
        this.correctAnswer = answer;
    }

    /**
     * Returns the index of the correct answer.
     *
     * @return Index of correct answer
     */
    public long getCorrectIndex() {
        return this.answers.indexOf(this.correctAnswer);
    }

    /**
     * Returns the list of possible answers,
     *
     * @return List of answers
     */
    public List<String> getAnswerList() {
        return this.answers;
    }

    /**
     * Sets the timeout for this question.
     *
     * @param timeout Timeout in milliseconds
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout * MILLISECOND_FACTOR;
    }

    /**
     * Checks if a given selection is correct.
     *
     * @param selection Index of the chosen answer
     * @return <code>true</code> if selection was correct;
     *          else <code>false</code>
     */
    public boolean validateAnswer(long selection) {
        boolean correct;
        try {
            correct = (correctAnswer == this.answers.get((int) selection)) ?
                       true : false;
        } catch (IndexOutOfBoundsException e) {
            correct = false;
        }
        return correct;
    }

    /**
     * Shuffles the set of answers.
     *
     * The answers should only be shuffled once when added to a catalog.
     * Otherwise the validation may fail due to a different ordering.
     */
    public void shuffleAnswers() {
        Collections.shuffle(this.answers);
    }
}
