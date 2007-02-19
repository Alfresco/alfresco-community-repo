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
