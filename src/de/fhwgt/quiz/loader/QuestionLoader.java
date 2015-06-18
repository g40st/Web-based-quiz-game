package de.fhwgt.quiz.loader;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.fhwgt.quiz.application.Catalog;
import de.fhwgt.quiz.application.Question;

/**
 * Interface for loading a list if questions for a given catalog.
 *
 * @author Simon Westphahl
 *
 */
public interface QuestionLoader {

    /**
     * Returns a list of questions for the given catalog.
     *
     * @param catalog Catalog with questions
     * @return List of questions
     * @throws LoaderException If loading of the catalog failed
     */
    public List<Question> getQuestions(Catalog catalog) throws LoaderException;

}
