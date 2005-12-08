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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a store of node content directly to the file system.
 * <p>
 * The file names obey, as they must, the URL naming convention
 * as specified in the {@link org.alfresco.repo.content.ContentStore}.
 * 
 * @author Derek Hulley
 */
public class FileContentStore extends AbstractContentStore
{
    private static final Log logger = LogFactory.getLog(FileContentStore.class);
    
    private File rootDirectory;
    private String rootAbsolutePath;

    /**
     * @param rootDirectory the root under which files will be stored.  The
     *      directory will be created if it does not exist.
     */
    public FileContentStore(String rootDirectoryStr)
    {
        rootDirectory = new File(rootDirectoryStr);
        if (!rootDirectory.exists())
        {
            if (!rootDirectory.mkdirs())
            {
                throw new ContentIOException("Failed to create store root: " + rootDirectory, null);
            }
        }
        rootDirectory = rootDirectory.getAbsoluteFile();
        rootAbsolutePath = rootDirectory.getAbsolutePath();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(36);
        sb.append("FileContentStore")
          .append("[ root=").append(rootDirectory)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Generates a new URL and file appropriate to it.
     * 
     * @return Returns a new and unique file
     * @throws IOException if the file or parent directories couldn't be created
     */
    private File createNewFile() throws IOException
    {
        String contentUrl = createNewUrl();
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
     * @throws IOException if the file or parent directories couldn't be created or
     *      if the URL is already in use.
     */
    public File createNewFile(String newContentUrl) throws IOException
    {
        File file = makeFile(newContentUrl);

        // create the directory, if it doesn't exist
        File dir = file.getParentFile();
        if (!dir.exists())
        {
            dir.mkdirs();
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
        String url = AbstractContentStore.STORE_PROTOCOL + path.substring(index);
        // replace '\' with '/' so that URLs are consistent across all filesystems
        url = url.replace('\\', '/');
        // done
        return url;
    }
    
    /**
     * Creates a file from the given relative URL.  The URL must start with
     * the required {@link FileContentStore#STORE_PROTOCOL protocol prefix}.
     * 
     * @param contentUrl the content URL including the protocol prefix
     * @return Returns a file representing the URL - the file may or may not
     *      exist 
     * 
     * @see #checkUrl(String)
     */
    private File makeFile(String contentUrl)
    {
        // take just the part after the protocol
        String relativeUrl = getRelativePart(contentUrl);
        // get the file
        File file = new File(rootDirectory, relativeUrl);
        // done
        return file;
    }
    
    /**
     * Performs a direct check against the file for its existence.
     */
    @Override
    public boolean exists(String contentUrl) throws ContentIOException
    {
        File file = makeFile(contentUrl);
        return file.exists();
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
            FileContentReader reader = new FileContentReader(file, contentUrl);
            
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
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to get reader for URL: " + contentUrl, e);
        }
    }
    
    /**
     * @return Returns a writer onto a location based on the date
     */
    public ContentWriter getWriter(ContentReader existingContentReader, String newContentUrl)
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

    public Set<String> getUrls()
    {
        // recursively get all files within the root
        Set<String> contentUrls = new HashSet<String>(1000);
        getUrls(rootDirectory, contentUrls);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Listed all content URLS: \n" +
                    "   store: " + this + "\n" +
                    "   count: " + contentUrls.size());
        }
        return contentUrls;
    }
    
    /**
     * @param directory the current directory to get the files from
     * @param contentUrls the list of current content URLs to add to
     * @return Returns a list of all files within the given directory and all subdirectories
     */
    private void getUrls(File directory, Set<String> contentUrls)
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
                getUrls(file, contentUrls);
            }
            else
            {
                // found a file - create the URL
                String contentUrl = makeContentUrl(file);
                contentUrls.add(contentUrl);
            }
        }
    }
    
    /**
     * Attempts to delete the content.  The actual deletion is optional on the interface
     * so it just returns the success or failure of the underlying delete.
     */
    public boolean delete(String contentUrl) throws ContentIOException
    {
        // ignore files that don't exist
        File file = makeFile(contentUrl);
        if (!file.exists())
        {
            return true;
        }
        // attempt to delete the file directly
        boolean deleted = file.delete();

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Delete content directly: \n" +
                    "   store: " + this + "\n" +
                    "   url: " + contentUrl);
        }
        return deleted;
    }
}
