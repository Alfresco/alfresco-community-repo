/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
@PublicService
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
