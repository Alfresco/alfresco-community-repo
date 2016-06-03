package org.alfresco.service.cmr.view;

import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.ContentData;


/**
 * Contract for a custom content property exporter.
 * 
 * @author David Caruana
 *
 */
public interface ExportPackageHandler
{
    /**
     * Start the Export
     */
    public void startExport();
    
    /**
     * Create a stream for accepting the package data
     * 
     * @return  the output stream
     */
    public OutputStream createDataStream();
    
    
    /**
     * Call-back for handling the export of content stream.
     * 
     * @param content content to export
     * @param contentData content descriptor
     * @return the URL to the location of the exported content
     */
    public ContentData exportContent(InputStream content, ContentData contentData);
    
    /**
     * End the Export
     */
    public void endExport();
    
}
