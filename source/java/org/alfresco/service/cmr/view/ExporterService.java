package org.alfresco.service.cmr.view;

import java.io.OutputStream;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;


/**
 * Exporter Service
 * 
 * @author David Caruana
 */
public interface ExporterService
{
    /**
     * Export a view of the Repository using the default xml view schema.
     * 
     * All repository information is exported to the single output stream.  This means that any
     * content properties are base64 encoded. 
     * 
     * @param viewWriter  the output stream to export to
     * @param parameters  export parameters
     * @param progress  exporter callback for tracking progress of export
     */
    @Auditable(parameters = {"viewWriter", "parameters", "progress"})
    public void exportView(OutputStream viewWriter, ExporterCrawlerParameters parameters, Exporter progress)
        throws ExporterException;

    /**
     * Export a view of the Repository using the default xml view schema.
     * 
     * This export supports the custom handling of content properties.
     * 
     * @param exportHandler  the custom export handler for content properties
     * @param parameters  export parameters
     * @param progress  exporter callback for tracking progress of export
     */
    @Auditable(parameters = {"exportHandler", "parameters", "progress"})
    public void exportView(ExportPackageHandler exportHandler, ExporterCrawlerParameters parameters, Exporter progress)
        throws ExporterException;

    
    /**
     * Export a view of the Repository using a custom crawler and exporter.
     * 
     * @param exporter  custom exporter
     * @param parameters  export parameters
     * @param progress  exporter callback for tracking progress of export
     */
    @Auditable(parameters = {"exporter", "parameters", "progress"})
    public void exportView(Exporter exporter, ExporterCrawlerParameters parameters, Exporter progress);
    
}
