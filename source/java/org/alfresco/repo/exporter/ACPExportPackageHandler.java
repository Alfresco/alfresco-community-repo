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
import java.io.FileInputStream;
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
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;


/**
 * Handler for exporting Repository to ACP (Alfresco Content Package) file
 * 
 * @author David Caruana
 */
public class ACPExportPackageHandler
    implements ExportPackageHandler
{
    /** ACP File Extension */
    public final static String ACP_EXTENSION = "acp";
    
    protected MimetypeService mimetypeService;
    protected OutputStream outputStream;
    protected File dataFile;
    protected File contentDir;
    protected File tempDataFile;
    protected OutputStream tempDataFileStream;
    protected ZipOutputStream zipStream;
    protected int iFileCnt = 0;

        
    /**
     * Construct
     * 
     * @param destDir
     * @param zipFile
     * @param dataFile
     * @param contentDir
     */
    public ACPExportPackageHandler(File destDir, File zipFile, File dataFile, File contentDir, boolean overwrite, MimetypeService mimetypeService)
    {
        try
        {
            // Ensure ACP file has appropriate ACP extension
            String zipFilePath = zipFile.getPath();
            if (!zipFilePath.endsWith("." + ACP_EXTENSION))
            {
                zipFilePath += (zipFilePath.charAt(zipFilePath.length() -1) == '.') ? ACP_EXTENSION : "." + ACP_EXTENSION;
            }

            File absZipFile = new File(destDir, zipFilePath);
            log("Exporting to package zip file " + absZipFile.getAbsolutePath());

            if (absZipFile.exists())
            {
                if (overwrite == false)
                {
                    throw new ExporterException("Package zip file " + absZipFile.getAbsolutePath() + " already exists.");
                }
                log("Warning: Overwriting existing package zip file " + absZipFile.getAbsolutePath());
            }
            
            this.outputStream = new FileOutputStream(absZipFile);
            this.dataFile = dataFile;
            this.contentDir = contentDir;
            this.mimetypeService = mimetypeService;
        }
        catch (FileNotFoundException e)
        {
            throw new ExporterException("Failed to create zip file", e);
        }
    }

    /**
     * Construct
     * 
     * @param outputStream
     * @param dataFile
     * @param contentDir
     */
    public ACPExportPackageHandler(OutputStream outputStream, File dataFile, File contentDir, MimetypeService mimetypeService)
    {
        this.outputStream = outputStream;
        this.dataFile = dataFile;
        this.contentDir = contentDir;
        this.mimetypeService = mimetypeService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportPackageHandler#startExport()
     */
    public void startExport()
    {
        zipStream = new ZipOutputStream(outputStream);
        // NOTE: This encoding allows us to workaround bug...
        //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
        zipStream.setEncoding("Cp437");
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportPackageHandler#createDataStream()
     */
    public OutputStream createDataStream()
    {
        tempDataFile = TempFileProvider.createTempFile("exportDataStream", ".xml");
        try
        {
            tempDataFileStream = new FileOutputStream(tempDataFile); 
            return tempDataFileStream;
        }
        catch (FileNotFoundException e)
        {
            throw new ExporterException("Failed to create data file stream", e);
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
        
        // create zip entry for stream to export
        String contentDirPath = contentDir.getPath();
        if (contentDirPath.charAt(contentDirPath.length() -1) != '.' && contentDirPath.lastIndexOf('.') != -1)
        {
            contentDirPath = contentDirPath.substring(0, contentDirPath.lastIndexOf("."));
        }
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
        File file = new File(contentDirPath, "content" + iFileCnt++ + "." + extension);
        
        try
        {
            ZipEntry zipEntry = new ZipEntry(file.getPath());
            zipStream.putNextEntry(zipEntry);
            
            // copy export stream to zip
            copyStream(zipStream, content);
        }
        catch (IOException e)
        {
            throw new ExporterException("Failed to zip export stream", e);
        }
        
        return new ContentData(file.getPath(), contentData.getMimetype(), contentData.getSize(), contentData.getEncoding());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExportPackageHandler#endExport()
     */
    public void endExport()
    {
        // ensure data file has .xml extension
        String dataFilePath = dataFile.getPath();
        if (!dataFilePath.endsWith(".xml"))
        {
        	dataFilePath  += (dataFilePath .charAt(dataFilePath .length() -1) == '.') ? "xml" : ".xml";
        }
        
        // add data file to zip stream
        ZipEntry zipEntry = new ZipEntry(dataFilePath);
        
        try
        {
            // close data file stream and place temp data file into zip output stream
            tempDataFileStream.close();
            zipStream.putNextEntry(zipEntry);
            InputStream dataFileStream = new FileInputStream(tempDataFile);
            copyStream(zipStream, dataFileStream);
            dataFileStream.close();
        }
        catch (IOException e)
        {
            throw new ExporterException("Failed to zip data stream file", e);
        }
        
        try
        {
            // close zip stream
            zipStream.close();
        }
        catch(IOException e)
        {
            throw new ExporterException("Failed to close zip package stream", e);
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

    /**
     * Copy input stream to output stream
     * 
     * @param output  output stream
     * @param in  input stream
     * @throws IOException
     */
    private void copyStream(OutputStream output, InputStream in)
        throws IOException
    {
        byte[] buffer = new byte[2048 * 10];
        int read = in.read(buffer, 0, 2048 *10);
        while (read != -1)
        {
            output.write(buffer, 0, read);
            read = in.read(buffer, 0, 2048 *10);
        }
    }
    
}
