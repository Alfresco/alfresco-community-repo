/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.view;

import java.io.OutputStream;

import org.alfresco.service.Auditable;


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
