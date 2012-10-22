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
package org.alfresco.repo.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handler for exporting node content to a ZIP file
 * 
 * @author Alex Miller
 */
public class ZipDownloadExporter extends BaseExporter
{
    private static Logger log = LoggerFactory.getLogger(ZipDownloadExporter.class);
    
    private static final String PATH_SEPARATOR = "/";

    protected ZipArchiveOutputStream zipStream;

    private NodeRef downloadNodeRef;
    private int sequenceNumber = 1;
    private long total;
    private long done;
    private long totalFileCount;
    private long filesAddedCount;
    
    private RetryingTransactionHelper transactionHelper;
    private DownloadStorage downloadStorage;
    private DownloadStatusUpdateService updateService;

    private Deque<Pair<String, NodeRef>> path = new LinkedList<Pair<String, NodeRef>>();
    private String currentName;

    private OutputStream outputStream;

    /**
     * Construct
     * 
     * @param destDir
     * @param zipFile
     * @param transactionHelper 
     * @param l 
     * @param actionedUponNodeRef 
     * @param dataFile
     * @param contentDir
     */
    public ZipDownloadExporter(File zipFile, CheckOutCheckInService checkOutCheckInService, NodeService nodeService, RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService, DownloadStorage downloadStorage, NodeRef downloadNodeRef, long total, long totalFileCount)
    {
        super(checkOutCheckInService, nodeService);
        try
        {
            this.outputStream = new FileOutputStream(zipFile);
            this.updateService = updateService;
            this.transactionHelper = transactionHelper;
            this.downloadStorage = downloadStorage;
            
            this.downloadNodeRef = downloadNodeRef;
            this.total = total;
            this.totalFileCount = totalFileCount;
        }
        catch (FileNotFoundException e)
        {
            throw new ExporterException("Failed to create zip file", e);
        }
    }

    @Override
    public void start(final ExporterContext context)
    {
        zipStream = new ZipArchiveOutputStream(outputStream);
        // NOTE: This encoding allows us to workaround bug...
        //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
        zipStream.setEncoding("UTF-8");
        zipStream.setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy.ALWAYS);
        zipStream.setUseLanguageEncodingFlag(true);
        zipStream.setFallbackToUTF8(true);
    }

    @Override
    public void startNode(NodeRef nodeRef)
    {
        this.currentName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        path.push(new Pair<String, NodeRef>(currentName, nodeRef));
        if (ContentModel.TYPE_FOLDER.equals(nodeService.getType(nodeRef)))
        {
            String path = getPath() + PATH_SEPARATOR;
            ZipArchiveEntry archiveEntry = new ZipArchiveEntry(path);
            try
            {
                zipStream.putArchiveEntry(archiveEntry);
                zipStream.closeArchiveEntry();
            }
            catch (IOException e)
            {
                throw new ExporterException("Unexpected IOException adding folder entry", e);
            }
        }
    }
    
    @Override
    public void contentImpl(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
        // if the content stream to output is empty, then just return content descriptor as is
        if (content == null)
        {
            return;
        }
        
        try
        {
            // ALF-2016
            ZipArchiveEntry zipEntry=new ZipArchiveEntry(getPath());
            zipStream.putArchiveEntry(zipEntry);
            
            // copy export stream to zip
            copyStream(zipStream, content);
            
            zipStream.closeArchiveEntry();
            filesAddedCount = filesAddedCount + 1;
        }
        catch (IOException e)
        {
            throw new ExporterException("Failed to zip export stream", e);
        }
    }
    
    @Override
    public void endNode(NodeRef nodeRef)
    {
        path.pop();
    }

    @Override
    public void end()
    {
        try
        {
            zipStream.close();
        }
        catch (IOException error)
        {
            throw new ExporterException("Unexpected error closing zip stream!", error);
        }
    }

    private String getPath()
    {
        if (path.size() < 1) 
        {
            throw new IllegalStateException("No elements in path!");    
        }
        
        Iterator<Pair<String, NodeRef>> iter = path.descendingIterator();
        StringBuilder pathBuilder = new StringBuilder();
        
        while (iter.hasNext())
        {
            Pair<String, NodeRef> element = iter.next();
            
            pathBuilder.append(element.getFirst());
            if (iter.hasNext())
            {
                pathBuilder.append(PATH_SEPARATOR);
            }
        }
        
        return pathBuilder.toString();
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
        int i = 0;
        while (read != -1)
        {
            output.write(buffer, 0, read);
            done = done + read;
            
            // ALF-16289 - only update the status every 10MB
            if (i++%500 == 0)
            {
                updateStatus();
                checkCancelled();
            }
            
            read = in.read(buffer, 0, 2048 *10);
        }
    }
    
    private void checkCancelled()
    {
        boolean downloadCancelled = transactionHelper.doInTransaction(new RetryingTransactionCallback<Boolean>()
        {
            @Override
            public Boolean execute() throws Throwable
            {
                return downloadStorage.isCancelled(downloadNodeRef);                
            }
        }, true, true);

        if ( downloadCancelled == true)
        {
            log.debug("Download cancelled");
            throw new DownloadCancelledException();
        }
    }

    private void updateStatus()
    {
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                DownloadStatus status = new DownloadStatus(Status.IN_PROGRESS, done, total, filesAddedCount, totalFileCount);
                
                updateService.update(downloadNodeRef, status, getNextSequenceNumber());
                return null;
            }
        }, false, true);
    }

    public int getNextSequenceNumber()
    {
        return sequenceNumber++;
    }

    public long getDone()
    {
        return done;
    }

    public long getTotal()
    {
        return total;
    }

    public long getFilesAdded()
    {
        return filesAddedCount;
    }

    public long getTotalFiles()
    {
        return totalFileCount;
    }
}
