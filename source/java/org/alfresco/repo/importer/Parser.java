package org.alfresco.repo.importer;

import java.io.Reader;


/**
 * This interface represents the contract between the importer service and a 
 * parser (which is responsible for parsing the input stream and extracting
 * node descriptions).
 * 
 * The parser interacts with the passed importer to import nodes into the
 * Repository.
 *  
 * @author David Caruana
 */
public interface Parser
{
    /**
     * Parse nodes from specified input stream and import via the provided importer
     * 
     * @param viewReader Reader
     * @param importer Importer
     */
    public void parse(Reader viewReader, Importer importer);

}
