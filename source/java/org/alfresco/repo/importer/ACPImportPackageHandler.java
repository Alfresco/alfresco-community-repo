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
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;


/**
 * Handler for importing Repository content from zip package file
 * 
 * @author David Caruana
 */
public class ACPImportPackageHandler
    implements ImportPackageHandler
{
	public final static String DEFAULT_ENCODING = "UTF-8";
	
    protected File file;
    protected ZipFile zipFile;
    protected String dataFileEncoding;
    

    /**
     * Constuct Handler
     * 
     * @param sourceDir  source directory
     * @param packageDir  relative directory within source to place exported content  
     */
    public ACPImportPackageHandler(File zipFile, String dataFileEncoding)
    {
        this.file = zipFile;
        this.dataFileEncoding = dataFileEncoding;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportPackageHandler#startImport()
     */
    public void startImport()
    {
        log("Importing from zip file " + file.getAbsolutePath());
        try
        {
            // NOTE: This encoding allows us to workaround bug...
            //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
            zipFile = new ZipFile(file, "UTF-8");
        }
        catch(IOException e)
        {
            throw new ImporterException("Failed to read zip file due to " + e.getMessage(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportPackageHandler#getDataStream()
     */
    public Reader getDataStream()
    {
        try
        {
            // find xml meta-data file
            ZipArchiveEntry xmlMetaDataEntry = null;
            
            // TODO: First, locate xml meta-data file by name
            
            // Scan the zip entries one by one (the slow approach)
            Enumeration entries = zipFile.getEntries();
            while(entries.hasMoreElements())
            {
                ZipArchiveEntry entry = (ZipArchiveEntry)entries.nextElement();
                if (!entry.isDirectory())
                {
                    // Locate xml file in root of .acp
                    String entryName = entry.getName();
                    if (entryName.endsWith(".xml") && entryName.indexOf('/') == -1 && entryName.indexOf('\\') == -1)
                    {
                        if (xmlMetaDataEntry != null)
                        {
                            throw new ImporterException("Failed to find unique xml meta-data file within .acp package - multiple xml meta-data files exist.");
                        }
                        xmlMetaDataEntry = entry;
                    }
                }
            }

            // oh dear, there's no data file
            if (xmlMetaDataEntry == null)
            {
                throw new ImporterException("Failed to find xml meta-data file within .acp package");
            }
            
            // open the meta-data xml file
            InputStream dataStream = zipFile.getInputStream(xmlMetaDataEntry);
            Reader inputReader = (dataFileEncoding == null) ? new InputStreamReader(dataStream, DEFAULT_ENCODING) : new InputStreamReader(dataStream, dataFileEncoding);
            return new BufferedReader(inputReader);
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ImporterException("Encoding " + dataFileEncoding + " is not supported");
        }
        catch(IOException e)
        {
            throw new ImporterException("Failed to open xml meta-data file within .acp package due to " + e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImportStreamHandler#importStream(java.lang.String)
     */
    public InputStream importStream(String content)
    {
        ZipArchiveEntry zipEntry = zipFile.getEntry(content);
        if (zipEntry == null)
        {
            // Note: for some reason, when modifying a zip archive the path seperator changes
            // TODO: Need to investigate further as to why and whether this workaround is enough 
            content = content.replace('\\', '/');
            zipEntry = zipFile.getEntry(content);
            if (zipEntry == null)
            {
                throw new ImporterException("Failed to find content " + content + " within zip package");
            }
        }
        
        try
        {
            return zipFile.getInputStream(zipEntry);
        }
        catch (IOException e)
        {
            throw new ImporterException("Failed to open content " + content + " within zip package due to " + e.getMessage(), e);
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

