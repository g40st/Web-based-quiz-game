package de.fhwgt.quiz.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.xpath.XPathFactory;
import org.jdom2.xpath.XPathExpression;

import de.fhwgt.quiz.application.Catalog;
import de.fhwgt.quiz.application.Question;

public class XMLloader implements CatalogLoader {
	
	private File[] catalogDir;
    private final Map<String, Catalog> catalogs = new HashMap<String, Catalog>();
    private final String location;
	
    // Konstruktor
	public XMLloader(String location) {
        this.location = location;
    }
	

	@Override
	public Map<String, Catalog> getCatalogs() throws LoaderException {
		
		if (!catalogs.isEmpty()) {
            return catalogs;
        }

        // Construct URL for package location
        URL url = this.getClass().getClassLoader().getResource(location);
        
        
        File dir;
        try {
            // Make sure the Java package exists
            if (url != null) {
                dir = new File(url.toURI());
            } else {
                dir = new File("/");
            }
        } catch (URISyntaxException e) {
            // Try to load from the root of the classpath
            dir = new File("/");
        }
        // Add catalog files
        if (dir.exists() && dir.isDirectory()) {
            this.catalogDir = dir.listFiles(new CatalogFilter());
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
	
	// Filter class for selecting only files with a .xml extension.
	private class CatalogFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isFile() && pathname.getName().endsWith(".xml"))
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
        
        @SuppressWarnings("resource")
		@Override
        public List<Question> getQuestions(Catalog catalog)
            throws LoaderException {

            if (!questions.isEmpty()) {
                return questions;
            }
            
            System.out.println("KatalogFile: " + catalogFile);
            System.out.println("KatalogFile: " + catalogFile.getPath());
            // create the w3c DOM document from which JDOM is to be created
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // we are interested in making it namespace aware.
            factory.setNamespaceAware(true);
            DocumentBuilder dombuilder = null;
            try {
				dombuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            org.w3c.dom.Document w3cDocument = null;
			try {
				w3cDocument = dombuilder.parse(catalogFile.getPath());
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // w3cDocument is the w3c DOM object. we now build the JDOM2 object

            // the DOMBuilder uses the DefaultJDOMFactory to create the JDOM2 objects.
            DOMBuilder jdomBuilder = new DOMBuilder();

            // jdomDocument is the JDOM2 Object
            Document jdomDocument = (jdomBuilder).build(w3cDocument);

            XPathFactory xpathFactory = XPathFactory.instance();

            XPathExpression<Element> expr = xpathFactory.compile("/catalog/question/issue | /catalog/question/answer | /catalog/question/timeout", Filters.element());
            List<Element> tmpCatalog = expr.evaluate(jdomDocument);
            
            System.out.println("Laenge: " + tmpCatalog.size());
            Question question = null;
            int i = 0;
            boolean finished = false;
            for (Element catElement : tmpCatalog) {
            	if(i == 0) {
            		System.out.println("Question: " + catElement.getValue());
            		question = new Question(catElement.getValue());
            	} else if(i == 1) {
            		System.out.println("correct: " + catElement.getValue());
            		question.addAnswer(catElement.getValue());
            	} else if(i < 5) {
            		System.out.println("false: " + catElement.getValue());
            		question.addBogusAnswer(catElement.getValue());
            		
            	} else {
            		System.out.println("timeout: " + catElement.getValue());
            		question.setTimeout(Long.parseLong(catElement.getValue()));
            		i = -1;
            		finished = true;
            	}
            	i++;
            	if (finished) {
                    // Add some randomization
                    question.shuffleAnswers();
                    questions.add(question);
                    finished = false;
            	}
            }
            

       

           /* // Search the whole file for questions
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
            */
            return questions;
        }
    }
}
