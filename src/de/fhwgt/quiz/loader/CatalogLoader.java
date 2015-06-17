package de.fhwgt.quiz.loader;

import java.util.Map;

import de.fhwgt.quiz.application.Catalog;

/**
 * Interface for loading a map of catalogs and returning a catalog by name.
 *
 * @author Simon Westphahl
 *
 */
public interface CatalogLoader {

    /**
     * Returns a map of catalog names and catalogs.
     *
     * @return Map of catalog names and catalogs
     * @throws LoaderException If loading of the catalogs failed
     */
    public Map<String, Catalog> getCatalogs() throws LoaderException;

    /**
     * Returns the catalog with the given name.
     *
     * @param name Name of the catalog
     * @return Reference to catalog instance or <code>null</code> if not found
     * @throws LoaderException If loading of the catalogs failed
     */
    public Catalog getCatalogByName(String name) throws LoaderException;

}
