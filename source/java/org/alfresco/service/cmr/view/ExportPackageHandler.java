/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
