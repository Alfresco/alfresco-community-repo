/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
