package de.fhwgt.quiz.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fhwgt.quiz.application.Catalog;
import de.fhwgt.quiz.application.Question;

public class FilesystemLoader implements CatalogLoader {

    /**
    * RegEx to capture the question block.
    * <p>
    * Captures three groups:
    * <p>
    *  1. Group: Contains the question<br>
    *  2. Group (optional): Timeout<br>
    *  3. Group: Answer block (all possible answers)<br>
    */
    private static final String QUESTION_BLOCK_REGEX =
        "(.+)\n(?:TIMEOUT: ([0-9]+)\n)??((?:[+-] .+\n){4}?)";
    /**
     * RegEx captures the individual answers in the captured answer block
     * from the more general expression above.
     * <p>
     * There are two capture groups:
     * <p>
     *  1. Group: +/-, which states if the answer is true or false<br>
     *  2. Group: Contains the answer<br>
     */
    private static final String ANSWER_REGEX = "([+-]) (.+)\n";

    private final Pattern blockPattern = Pattern.compile(QUESTION_BLOCK_REGEX);
    private final Pattern questionPattern = Pattern.compile(ANSWER_REGEX);

    private File[] catalogDir;
    private final Map<String, Catalog> catalogs = new HashMap<String, Catalog>();
    private final String location;

    public FilesystemLoader(String location) {
        this.location = location;
    }

    @Override
    public Map<String, Catalog> getCatalogs() throws LoaderException {

        if (!catalogs.isEmpty()) {
            return catalogs;
        }

        File dir;
        // Make sure the Java package exists
        dir = new File(this.location);
        
        // Add catalog files
        if (dir.exists() && dir.isDirectory()) {
            this.catalogDir = dir.listFiles(new CatalogFilter());
            System.out.println(catalogDir);
            for (File f : catalogDir) {
                catalogs.put(f.getName(), new Catalog(f.getName(), new QuestionFileLoader(f)));
                // Namen der Kataloge ausgeben
                System.out.println(f.getName());
            }
        }

        return catalogs;
    }

    @Override
    public Catalog getCatalogByName(String name) throws LoaderException {
        if (catalogs.isEmpty()) {
            getCatalogs();
        }

        return this.catalogs.get(name);
    }

    /**
     * Filter class for selecting only files with a .cat extension.
     *
     * @author Simon Westphahl
     *
     */
    private class CatalogFilter implements FileFilter {

        /**
         * Accepts only files with a .cat extension.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.getName().endsWith(".cat"))
                return true;
            else
                return false;
        }

    }

    private class QuestionFileLoader implements QuestionLoader {

        private final File catalogFile;
        private final List <Question> questions = new ArrayList<Question>();

        public QuestionFileLoader(File file) {
            catalogFile = file;
        }
        @Override
        public List<Question> getQuestions(Catalog catalog)
            throws LoaderException {

            if (!questions.isEmpty()) {
                return questions;
            }

            Scanner scanner;
            try {
                scanner = new Scanner(catalogFile, "UTF-8");
            } catch (FileNotFoundException e) {
                throw new LoaderException();
            }

            // Search the whole file for questions
            for (String questionBlock = scanner.findWithinHorizon(blockPattern, 0);
                 questionBlock != null;
                 questionBlock = scanner.findWithinHorizon(blockPattern, 0)) {

                MatchResult m = scanner.match();
                Question question = new Question(m.group(1));

                // The 2nd group is optional
                if (m.group(2) != null) {
                    question.setTimeout(
                        new Integer(m.group(2)));
                }

                // Match the answers
                Matcher am = questionPattern.matcher(m.group(3));
                while (am.find()) {
                    if (am.group(1).equals("+")) {
                        question.addAnswer(am.group(2));
                    } else {
                        question.addBogusAnswer(am.group(2));
                    }
                }

                // Make sure the question is complete
                if (question.isComplete())
                    // Add some randomization
                    question.shuffleAnswers();
                    questions.add(question);
            }
            return questions;
        }

    }
}
