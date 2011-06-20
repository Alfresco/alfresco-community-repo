/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
