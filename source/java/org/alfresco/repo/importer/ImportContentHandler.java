package org.alfresco.repo.importer;

import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;


/**
 * Content Handler that interacts with an Alfresco Importer
 * 
 * @author David Caruana
 */
public interface ImportContentHandler extends ContentHandler, ErrorHandler
{
    /**
     * Sets the Importer
     * 
     * @param importer Importer
     */
    public void setImporter(Importer importer);

    /**
     * Call-back for importing content streams
     * 
     * @param content  content stream identifier
     * @return  the input stream
     */
    public InputStream importStream(String content);
}
