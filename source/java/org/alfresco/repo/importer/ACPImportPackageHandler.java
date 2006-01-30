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
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterException;


/**
 * Handler for importing Repository content from zip package file
 * 
 * @author David Caruana
 */
public class ACPImportPackageHandler
    implements ImportPackageHandler
{
    
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
            zipFile = new ZipFile(file);
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
            ZipEntry xmlMetaDataEntry = null;
            
            // TODO: First, locate xml meta-data file by name
            
            // Scan the zip entries one by one (the slow approach)
            Enumeration entries = zipFile.entries();
            while(entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry)entries.nextElement();
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
            Reader inputReader = (dataFileEncoding == null) ? new InputStreamReader(dataStream) : new InputStreamReader(dataStream, dataFileEncoding);
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
        ZipEntry zipEntry = zipFile.getEntry(content);
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

