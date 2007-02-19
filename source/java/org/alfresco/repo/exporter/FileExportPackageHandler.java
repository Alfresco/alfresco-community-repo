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
package org.alfresco.repo.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.util.TempFileProvider;


/**
 * Handler for exporting Repository to file system files
 * 
 * @author David Caruana
 */
public class FileExportPackageHandler
    implements ExportPackageHandler
{
    protected MimetypeService mimetypeService = null;
    protected File contentDir;
    protected File absContentDir;
    protected File absDataFile;
    protected boolean overwrite;
    protected OutputStream absDataStream = null;

    /**
     * Constuct Handler
     * 
     * @param destDir  destination directory
     * @param dataFile  filename of data file (relative to destDir)
     * @param packageDir  directory for content (relative to destDir)  
     * @param overwrite  force overwrite of existing package directory
     * @param mimetypeService (optional) mimetype service
     */
    public FileExportPackageHandler(File destDir, File dataFile, File contentDir, boolean overwrite, MimetypeService mimetypeService)
    {
        this.contentDir = contentDir;
        this.absContentDir = new File(destDir, contentDir.getPath());
        this.absDataFile = new File(destDir, dataFile.getPath());
        this.overwrite = overwrite;
        this.mimetypeService = mimetypeService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportPackageHandler#startExport()
     */
    public void startExport()
    {
        log("Exporting to package " + absDataFile.getAbsolutePath());
        
        if (absContentDir.exists())
        {
            if (overwrite == false)
            {
                throw new ExporterException("Package content dir " + absContentDir.getAbsolutePath() + " already exists.");
            }
            log("Warning: Overwriting existing package dir " + absContentDir.getAbsolutePath());
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportPackageHandler#createDataStream()
     */
    public OutputStream createDataStream()
    {
        if (absDataFile.exists())
        {
            if (overwrite == false)
            {
                throw new ExporterException("Package data file " + absDataFile.getAbsolutePath() + " already exists.");
            }
            log("Warning: Overwriting existing package file " + absDataFile.getAbsolutePath());
            absDataFile.delete();
        }

        try
        {
            absDataFile.createNewFile();
            absDataStream = new FileOutputStream(absDataFile);
            return absDataStream;
        }
        catch(IOException e)
        {
            throw new ExporterException("Failed to create package file " + absDataFile.getAbsolutePath() + " due to " + e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportStreamHandler#exportStream(java.io.InputStream)
     */
    public ContentData exportContent(InputStream content, ContentData contentData)
    {
        // if the content stream to output is empty, then just return content descriptor as is
        if (content == null)
        {
            return contentData;
        }
        
        // Lazily create package directory
        try
        {
            absContentDir.mkdirs();
        }
        catch(SecurityException e)
        {
            throw new ExporterException("Failed to create package dir " + absContentDir.getAbsolutePath() + " due to " + e.getMessage());
        }
        
        // Create file in package directory to hold exported content
        String extension = "bin";
        if (mimetypeService != null)
        {
            String mimetype = contentData.getMimetype();
            if (mimetype != null && mimetype.length() > 0)
            {
                try
                {
                    extension = mimetypeService.getExtension(mimetype);
                }
                catch(AlfrescoRuntimeException e)
                {
                    // use default extension
                }
            }
        }
        File outputFile = TempFileProvider.createTempFile("export", "." + extension, absContentDir);
        
        try
        {
            // Copy exported content from repository to exported file
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[2048 * 10];
            int read = content.read(buffer, 0, 2048 *10);
            while (read != -1)
            {
                outputStream.write(buffer, 0, read);
                read = content.read(buffer, 0, 2048 *10);
            }
            outputStream.close();
        }
        catch(FileNotFoundException e)
        {
            throw new ExporterException("Failed to create export package file due to " + e.getMessage());
        }
        catch(IOException e)
        {
            throw new ExporterException("Failed to export content due to " + e.getMessage());
        }
        
        // return relative path to exported content file (relative to xml export file)
        File url = new File(contentDir, outputFile.getName());
        return new ContentData(url.getPath(), contentData.getMimetype(), contentData.getSize(), contentData.getEncoding());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportPackageHandler#endExport()
     */
    public void endExport()
    {
        // close Export File
        if (absDataStream != null)
        {
            try
            {
                absDataStream.close();
            }
            catch(IOException e)
            {
                throw new ExporterException("Failed to close package data file " + absDataFile + " due to" + e.getMessage());
            }
        }            
    }
    
    /**
     * Log Export Message
     * 
     * @param message  message to log
     */
    protected void log(String message)
    {
    }
}
