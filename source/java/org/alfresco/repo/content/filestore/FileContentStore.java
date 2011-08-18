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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStoreCreatedEvent;
import org.alfresco.repo.content.EmptyContentReader;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Provides a store of node content directly to the file system.  The writers
 * are generated using information from the {@link ContentContext simple content context}.
 * <p>
 * The file names obey, as they must, the URL naming convention
 * as specified in the {@link org.alfresco.repo.content.ContentStore ContentStore interface}.
 * 
 * @author Derek Hulley
 */
public class FileContentStore
        extends AbstractContentStore
        implements ApplicationContextAware, ApplicationListener<ApplicationEvent>
{
    /**
     * <b>store</b> is the new prefix for file content URLs
     * @see ContentStore#PROTOCOL_DELIMITER
     */
    public static final String STORE_PROTOCOL = "store";
    
    private static final Log logger = LogFactory.getLog(FileContentStore.class);
    
    private File rootDirectory;
    private String rootAbsolutePath;
    private boolean allowRandomAccess;
    private boolean readOnly;
    private ApplicationContext applicationContext;
    private boolean deleteEmptyDirs = true;

    /**
     * Private: for Spring-constructed instances only.
     * 
     * @param rootDirectoryStr
     *            the root under which files will be stored. The directory will be created if it does not exist.
     * @see FileContentStore#FileContentStore(File)
     */
    private FileContentStore(String rootDirectoryStr)
    {
        this(new File(rootDirectoryStr));
    }

    /**
     * Private: for Spring-constructed instances only.
     * 
     * @param rootDirectory
     *            the root under which files will be stored. The directory will be created if it does not exist.
     */
    private FileContentStore(File rootDirectory)
    {
        if (!rootDirectory.exists())
        {
            if (!rootDirectory.mkdirs())
            {
                throw new ContentIOException("Failed to create store root: " + rootDirectory, null);
            }
        }
        this.rootDirectory = rootDirectory.getAbsoluteFile();
        rootAbsolutePath = rootDirectory.getAbsolutePath();
        allowRandomAccess = true;
        readOnly = false;
    }
    
    /**
     * Public constructor for programmatic use.
     * 
     * @param context
     *            application context through which events can be published
     * @param rootDirectoryStr
     *            the root under which files will be stored. The directory will be created if it does not exist.
     * @see FileContentStore#FileContentStore(File)
     */
    public FileContentStore(ApplicationContext context, String rootDirectoryStr)
    {
        this(rootDirectoryStr);
        setApplicationContext(context);
        publishEvent(context);
    }

    /**
     * Public constructor for programmatic use.
     * 
     * @param context
     *            application context through which events can be published
     * @param rootDirectory
     *            the root under which files will be stored. The directory will be created if it does not exist.
     */
    public FileContentStore(ApplicationContext context, File rootDirectory)
    {
        this(rootDirectory);        
        setApplicationContext(context);
        publishEvent(context);
    }
    
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(36);
        sb.append("FileContentStore")
          .append("[ root=").append(rootDirectory)
          .append(", allowRandomAccess=").append(allowRandomAccess)
          .append(", readOnly=").append(readOnly)
          .append("]");
        return sb.toString();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Stores may optionally produce readers and writers that support random access.
     * Switch this off for this store by setting this to <tt>false</tt>.
     * <p>
     * This switch is primarily used during testing to ensure that the system has the
     * ability to spoof random access in cases where the store is unable to produce
     * readers and writers that allow random access.  Typically, stream-based access
     * would be an example.
     * 
     * @param allowRandomAccess true to allow random access, false to have it faked
     */
    public void setAllowRandomAccess(boolean allowRandomAccess)
    {
        this.allowRandomAccess = allowRandomAccess;
    }

    /**
     * File stores may optionally be declared read-only.  This is useful when configuring
     * a store, possibly temporarily, to act as a source of data but to preserve it against
     * any writes.
     * 
     * @param readOnly      <tt>true</tt> to force the store to only allow reads.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * Generates a new URL and file appropriate to it.
     * 
     * @return Returns a new and unique file
     * @throws IOException if the file or parent directories couldn't be created
     */
    private File createNewFile() throws IOException
    {
        String contentUrl = FileContentStore.createNewFileStoreUrl();
        return createNewFile(contentUrl);
    }
    
    /**
     * Creates a file for the specifically provided content URL.  The URL may
     * not already be in use.
     * <p>
     * The store prefix is stripped off the URL and the rest of the URL
     * used directly to create a file.
     * 
     * @param newContentUrl the specific URL to use, which may not be in use
     * @return Returns a new and unique file
     * @throws IOException
     *             if the file or parent directories couldn't be created or if the URL is already in use.
     * @throws UnsupportedOperationException
     *             if the store is read-only
     * 
     * @see #setReadOnly(boolean)
     */
    private File createNewFile(String newContentUrl) throws IOException
    {
        if (readOnly)
        {
            throw new UnsupportedOperationException("This store is currently read-only: " + this);
        }
        
        File file = makeFile(newContentUrl);

        // create the directory, if it doesn't exist
        File dir = file.getParentFile();
        if (!dir.exists())
        {
            makeDirectory(dir);
        }
        
        // create a new, empty file
        boolean created = file.createNewFile();
        if (!created)
        {
            throw new ContentIOException(
                    "When specifying a URL for new content, the URL may not be in use already. \n" +
                    "   store: " + this + "\n" +
                    "   new URL: " + newContentUrl);
        }
        
        // done
        return file;
    }
    
    /**
     * Synchronized and retrying directory creation.  Repeated attempts will be made to create the
     * directory, subject to a limit on the number of retries.
     * 
     * @param dir               the directory to create
     * @throws IOException      if an IO error occurs
     */
    private synchronized void makeDirectory(File dir) throws IOException
    {
        /*
         * Once in this method, the only contention will be from other file stores or processes.
         * This is OK as we have retrying to sort it out.
         */
        if (dir.exists())
        {
            // Beaten to it during synchronization
            return;
        }
        // 20 attempts with 20 ms wait each time
        for (int i = 0; i < 20; i++)
        {
            boolean created = dir.mkdirs();
            if (created)
            {
                // Successfully created
                return;
            }
            // Wait
            try { this.wait(20L); } catch (InterruptedException e) {}
            // Did it get created in the meantime
            if (dir.exists())
            {
                // Beaten to it while asleep
                return;
            }
        }
        // It still didn't succeed
        throw new ContentIOException("Failed to create directory for file storage: " +  dir);
    }
    
    /**
     * Takes the file absolute path, strips off the root path of the store
     * and appends the store URL prefix.
     * 
     * @param file the file from which to create the URL
     * @return Returns the equivalent content URL
     * @throws Exception
     */
    private String makeContentUrl(File file)
    {
        String path = file.getAbsolutePath();
        // check if it belongs to this store
        if (!path.startsWith(rootAbsolutePath))
        {
            throw new AlfrescoRuntimeException(
                    "File does not fall below the store's root: \n" +
                    "   file: " + file + "\n" +
                    "   store: " + this);
        }
        // strip off the file separator char, if present
        int index = rootAbsolutePath.length();
        if (path.charAt(index) == File.separatorChar)
        {
            index++;
        }
        // strip off the root path and adds the protocol prefix
        String url = FileContentStore.STORE_PROTOCOL + ContentStore.PROTOCOL_DELIMITER + path.substring(index);
        // replace '\' with '/' so that URLs are consistent across all filesystems
        url = url.replace('\\', '/');
        // done
        return url;
    }
    
    /**
     * Creates a file from the given relative URL.
     * 
     * @param contentUrl    the content URL including the protocol prefix
     * @return              Returns a file representing the URL - the file may or may not
     *                      exist
     * @throws UnsupportedContentUrlException
     *                      if the URL is invalid and doesn't support the
     *                      {@link FileContentStore#STORE_PROTOCOL correct protocol}
     * 
     * @see #checkUrl(String)
     */
    private File makeFile(String contentUrl)
    {
        // take just the part after the protocol
        Pair<String, String> urlParts = super.getContentUrlParts(contentUrl);
        String protocol = urlParts.getFirst();
        String relativePath = urlParts.getSecond();
        // Check the protocol
        if (!protocol.equals(FileContentStore.STORE_PROTOCOL))
        {
            throw new UnsupportedContentUrlException(this, contentUrl);
        }
        // get the file
        File file = new File(rootDirectory, relativePath);
        // done
        return file;
    }

    /**
     * @return Returns <tt>true</tt> always
     */
    public boolean isWriteSupported()
    {
        return !readOnly;
    }

    /**
     * Performs a direct check against the file for its existence.
     */
    @Override
    public boolean exists(String contentUrl)
    {
        File file = makeFile(contentUrl);
        return file.exists();
    }

    /**
     * Recursive directory size calculation
     */
    private long calculateDirectorySize(File dir)
    {
        int size = 0;
        File[] files = dir.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                size += calculateDirectorySize(file);
            }
            else
            {
                size += file.length();
            }
        }
        return size;
    }

    /**
     * Performs a full, deep size calculation
     */
    @Override
    public long getSpaceUsed()
    {
        return calculateDirectorySize(rootDirectory);
    }

    /**
     * Get the filesystem's free space.
     * 
     * @return          Returns the root directory partition's {@link File#getFreeSpace() free space}
     */
    @Override
    public long getSpaceFree()
    {
        return rootDirectory.getFreeSpace();
    }

    /**
     * Get the filesystem's total space.
     * 
     * @return          Returns the root directory partition's {@link File#getTotalSpace() total space}
     */
    @Override
    public long getSpaceTotal()
    {
        return rootDirectory.getTotalSpace();
    }

    /**
     * @return          Returns the canonical path to the root directory
     */
    @Override
    public String getRootLocation()
    {
        try
        {
            return rootDirectory.getCanonicalPath();
        }
        catch (Throwable e)
        {
            logger.warn("Unabled to return root location", e);
            return super.getRootLocation();
        }
    }

    /**
     * This implementation requires that the URL start with
     * {@link FileContentStore#STORE_PROTOCOL }.
     */
    public ContentReader getReader(String contentUrl)
    {
        try
        {
            File file = makeFile(contentUrl);
            ContentReader reader = null;
            if (file.exists())
            {
                FileContentReader fileContentReader = new FileContentReader(file, contentUrl);
                fileContentReader.setAllowRandomAccess(allowRandomAccess);
                reader = fileContentReader;
            }
            else
            {
                reader = new EmptyContentReader(contentUrl);
            }
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created content reader: \n" +
                        "   url: " + contentUrl + "\n" +
                        "   file: " + file + "\n" +
                        "   reader: " + reader);
            }
            return reader;
        }
        catch (UnsupportedContentUrlException e)
        {
            // This can go out directly
            throw e;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to get reader for URL: " + contentUrl, e);
        }
    }
    
    /**
     * Returns a writer onto a location based on the date.
     * 
     * @param existingContentReader
     *            the existing content reader
     * @param newContentUrl
     *            the new content url
     * @return Returns a writer onto a location based on the date
     */
    public ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
    {
        try
        {
            File file = null;
            String contentUrl = null;
            if (newContentUrl == null)              // a specific URL was not supplied
            {
                // get a new file with a new URL
                file = createNewFile();
                // make a URL
                contentUrl = makeContentUrl(file);
            }
            else                                    // the URL has been given
            {
                file = createNewFile(newContentUrl);
                contentUrl = newContentUrl;
            }
            // create the writer
            FileContentWriter writer = new FileContentWriter(file, contentUrl, existingContentReader);
            writer.setAllowRandomAccess(allowRandomAccess);
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created content writer: \n" +
                        "   writer: " + writer);
            }
            return writer;
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to get writer", e);
        }
    }

    /**
     * Gets the urls.
     * 
     * @param createdAfter
     *            the created after date
     * @param createdBefore
     *            the created before dat6e
     * @param handler
     *            the handler
     * @return the urls
     */
    public void getUrls(Date createdAfter, Date createdBefore, ContentUrlHandler handler)
    {
        // recursively get all files within the root
        getUrls(rootDirectory, handler, createdAfter, createdBefore);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Listed all content URLS: \n" +
                    "   store: " + this);
        }
    }
    
    /**
     * Returns a list of all files within the given directory and all subdirectories.
     * @param directory the current directory to get the files from
     * @param handler the callback to use for each URL
     * @param createdAfter only get URLs for content create after this date
     * @param createdBefore only get URLs for content created before this date
     * @return a list of all files within the given directory and all subdirectories
     */
    private void getUrls(File directory, ContentUrlHandler handler, Date createdAfter, Date createdBefore)
    {
        File[] files = directory.listFiles();
        if (files == null)
        {
            // the directory has disappeared
            throw new ContentIOException("Failed list files in folder: " + directory);
        }
        for (File file : files)
        {
            if (file.isDirectory())
            {
                // we have a subdirectory - recurse
                getUrls(file, handler, createdAfter, createdBefore);
            }
            else
            {
                // check the created date of the file
                long lastModified = file.lastModified();
                if (createdAfter != null && lastModified < createdAfter.getTime())
                {
                    // file is too old
                    continue;
                }
                else if (createdBefore != null && lastModified > createdBefore.getTime())
                {
                    // file is too young
                    continue;
                }
                // found a file - create the URL
                String contentUrl = makeContentUrl(file);
                // Callback
                handler.handle(contentUrl);
            }
        }
    }
    
    /**
     * Attempts to delete the content.  The actual deletion is optional on the interface
     * so it just returns the success or failure of the underlying delete.
     * 
     * @throws UnsupportedOperationException        if the store is read-only
     * 
     * @see #setReadOnly(boolean)
     */
    public boolean delete(String contentUrl)
    {
        if (readOnly)
        {
            throw new UnsupportedOperationException("This store is currently read-only: " + this);
        }
        // ignore files that don't exist
        File file = makeFile(contentUrl);
        boolean deleted = false;
        if (!file.exists())
        {
            deleted = true;
        }
        else
        {
            deleted = file.delete();
        }
        
        // Delete empty parents regardless of whether the file was ignore above.
        if (deleteEmptyDirs && deleted)
        {
            deleteEmptyParents(file);
        }

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Delete content directly: \n" +
                    "   store: " + this + "\n" +
                    "   url: " + contentUrl);
        }
        return deleted;
    }

    /**
     * Deletes the parents of the specified file. The file itself must have been
     * deleted before calling this method - since only empty directories can be deleted.
     * 
     * @param file
     */
    private void deleteEmptyParents(File file)
    {
        String root = getRootLocation();
        File parent = file.getParentFile();
        boolean deleted = false;
        do
        {
            try
            {
                if (parent.isDirectory() && !parent.getCanonicalPath().equals(root))
                {
                    // Only an empty directory will successfully be deleted.
                    deleted = parent.delete();
                }
            }
            catch (IOException error)
            {
                logger.error("Unable to construct canonical path for " + parent.getAbsolutePath());
                break;
            }
            
            parent = parent.getParentFile();
        }
        while(deleted);

    }

    /**
     * Creates a new content URL.  This must be supported by all
     * stores that are compatible with Alfresco.
     * 
     * @return Returns a new and unique content URL
     */
    public static String createNewFileStoreUrl()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL)
          .append(ContentStore.PROTOCOL_DELIMITER)
          .append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(hour).append('/')
          .append(minute).append('/')
          .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }

    /**
     * Publishes an event to the application context that will notify any interested parties of the existence of this
     * content store.
     * 
     * @param context
     *            the application context
     */
    private void publishEvent(ApplicationContext context)
    {
        context.publishEvent(new ContentStoreCreatedEvent(this));
    }

    public void onApplicationEvent(ApplicationEvent event)
    {
        // Once the context has been refreshed, we tell other interested beans about the existence of this content store
        // (e.g. for monitoring purposes)
        if (event instanceof ContextRefreshedEvent && event.getSource() == this.applicationContext)
        {
            publishEvent(((ContextRefreshedEvent) event).getApplicationContext());
        }
    }

    /**
     * Configure the FileContentStore to delete empty parent directories upon deleting a content URL.
     * 
     * @param deleteEmptyDirs the deleteEmptyDirs to set
     */
    public void setDeleteEmptyDirs(boolean deleteEmptyDirs)
    {
        this.deleteEmptyDirs = deleteEmptyDirs;
    }
}
