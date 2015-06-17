package de.fhwgt.quiz.application;

import java.util.List;

import de.fhwgt.quiz.loader.LoaderException;
import de.fhwgt.quiz.loader.QuestionLoader;


/**
 * Catalog representing a set of questions.
 *
 * @author Simon Westphahl
 *
 */
public class Catalog {

    private final String name;
    private final QuestionLoader loader;

    /**
     * Constructs a new catalog with the given name and question loader.
     *
     * @param name Name of the catalog
     * @param loader Question loader instance
     */
    public Catalog(String name, QuestionLoader loader) {
        this.name = name;
        this.loader = loader;
    }

    /**
     * Returns the name of the catalog.
     *
     * @return Catalog name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns all questions contained in the catalog.
     *
     * @return List of questions
     * @throws LoaderException If the questions could not be loaded
     */
    public List<Question> getQuestions() throws LoaderException {
        return loader.getQuestions(this);
    }
}
