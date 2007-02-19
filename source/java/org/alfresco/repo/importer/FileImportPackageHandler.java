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
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterException;


/**
 * Handler for importing Repository content streams from file system
 * 
 * @author David Caruana
 */
public class FileImportPackageHandler
    implements ImportPackageHandler
{
	public final static String DEFAULT_ENCODING = "UTF-8";
	
    protected File sourceDir;
    protected File dataFile;
    protected String dataFileEncoding;

    /**
     * Construct
     * 
     * @param sourceDir
     * @param dataFile
     * @param dataFileEncoding
     */
    public FileImportPackageHandler(File sourceDir, File dataFile, String dataFileEncoding)
    {
        this.sourceDir = sourceDir;
        this.dataFile = new File(sourceDir, dataFile.getPath());
        this.dataFileEncoding = dataFileEncoding;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportPackageHandler#startImport()
     */
    public void startImport()
    {
        log("Importing from package " + dataFile.getAbsolutePath());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportPackageHandler#getDataStream()
     */
    public Reader getDataStream()
    {
        try
        {
            InputStream inputStream = new FileInputStream(dataFile);
            Reader inputReader = (dataFileEncoding == null) ? new InputStreamReader(inputStream, DEFAULT_ENCODING) : new InputStreamReader(inputStream, dataFileEncoding);
            return new BufferedReader(inputReader);
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ImporterException("Encoding " + dataFileEncoding + " is not supported");
        }
        catch(IOException e)
        {
            throw new ImporterException("Failed to read package " + dataFile.getAbsolutePath() + " due to " + e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportStreamHandler#importStream(java.lang.String)
     */
    public InputStream importStream(String content)
    {
        File fileURL = new File(content);
        if (fileURL.isAbsolute() == false)
        {
            fileURL = new File(sourceDir, content);
        }
        
        try
        {
            return new FileInputStream(fileURL);
        }
        catch(IOException e)
        {
            throw new ImporterException("Failed to read content url " + content + " from file " + fileURL.getAbsolutePath());
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportPackageHandler#endImport()
     */
    public void endImport()
    {
    }
    
    /**
     * Log Import Message
     * 
     * @param message  message to log
     */
    protected void log(String message)
    {
    }
    
}

