package de.fhwgt.quiz.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import de.fhwgt.quiz.application.Catalog;
import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.loader.FilesystemLoader;
import de.fhwgt.quiz.loader.LoaderException;
import de.fhwgt.quiz.loader.XMLloader;
 
@WebListener
public class QuizServletContextListener implements ServletContextListener {
 
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	ServletContext cntxt = servletContextEvent.getServletContext();
        String fullPath = cntxt.getRealPath("/WEB-INF/catalog");

        //FilesystemLoader filesystemLoader = new FilesystemLoader(fullPath);
        XMLloader xmlLoader = new XMLloader(fullPath);
        Quiz quiz = Quiz.getInstance();
        //quiz.initCatalogLoader(filesystemLoader);
        quiz.initCatalogLoader(xmlLoader);
    }
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
     
}