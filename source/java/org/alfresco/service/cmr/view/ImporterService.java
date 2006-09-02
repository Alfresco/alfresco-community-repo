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

import java.io.Reader;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;


/**
 * Importer Service.  Entry point for importing xml data sources into the Repository.
 * 
 * @author David Caruana
 *
 */
@PublicService
public interface ImporterService
{

    /**
     * Import a Repository view into the specified location
     * 
     * @param viewReader  input stream containing the xml view to parse
     * @param location  the location to import under
     * @param binding  property values used for binding property place holders in import stream
     * @param progress  progress monitor (optional)
     */
    @Auditable(parameters = {"viewReader", "location", "binding", "progress"})
    public void importView(Reader viewReader, Location location, ImporterBinding binding, ImporterProgress progress)
        throws ImporterException;

    
    /**
     * Import a Repository view into the specified location
     * 
     * This import allows for a custom content importer.
     * 
     * @param importHandler  custom content importer
     * @param location  the location to import under
     * @param binding  property values used for binding property place holders in import stream
     * @param progress  progress monitor (optional)
     */
    @Auditable(parameters = {"importHandler", "location", "binding", "progress"})
    public void importView(ImportPackageHandler importHandler, Location location, ImporterBinding binding, ImporterProgress progress)
        throws ImporterException;
    
}
