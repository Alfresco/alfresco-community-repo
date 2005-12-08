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
            Reader inputReader = (dataFileEncoding == null) ? new InputStreamReader(inputStream) : new InputStreamReader(inputStream, dataFileEncoding);
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

