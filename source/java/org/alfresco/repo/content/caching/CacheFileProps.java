/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.content.caching;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manage a cache file's associated properties.
 * 
 * @author Matt Ward
 */
public class CacheFileProps
{
    private static final String CONTENT_URL = "contentUrl";
    private static final String DELETE_WATCH_COUNT = "deleteWatchCount";
    private static final Log log = LogFactory.getLog(CacheFileProps.class);
    private final Properties properties = new Properties();
    private final File cacheFile;
    private final File propsFile;
    
    /**
     * Construct a CacheFileProps specifying which cache file the properties belong to.
     * 
     * @param cachedFile
     */
    public CacheFileProps(File cacheFile)
    {
        this.cacheFile = cacheFile;
        this.propsFile = fileForCacheFile();
    }

    /**
     * Load properties from the cache file's associated properties file.
     */
    public void load()
    {
        properties.clear();
        
        if (propsFile.exists())
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(propsFile));
                properties.load(reader);
                reader.close();
            }
            catch (FileNotFoundException error)
            {
                log.error("File disappeared after exists() check: " + cacheFile);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to read properties file " + cacheFile, e);
            }
        }
    }

    /**
     * Save properties to the cache file's associated properties file.
     */
    public void store()
    {
        BufferedOutputStream out = null;
        try
        {
            out = new BufferedOutputStream(new FileOutputStream(propsFile));
            properties.store(out, "Properties for " + cacheFile);
        }
        catch(FileNotFoundException e)
        {
            throw new RuntimeException("Couldn't create output stream for file: " + propsFile, e);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Couldn't write file: " + propsFile, e);
        }
        finally 
        {
            try
            {
                if (out != null) out.close();
            }
            catch(IOException e)
            {
                // Couldn't close it, just log that it wasn't possible.
                if (log.isErrorEnabled())
                {
                    log.error("Couldn't close file: " + propsFile);
                }
            }
        }
    }
    
    /**
     * Delete the cache file's associated properties file.
     */
    public void delete()
    {
        propsFile.delete();
    }
    
    /**
     * Does a properties file exist for the cache file?
     * 
     * @return true if the file exists
     */
    public boolean exists()
    {
        return propsFile.exists();
    }
    
    /**
     * Set the value of the contentUrl property.
     * 
     * @param url
     */
    public void setContentUrl(String url)
    {
        properties.setProperty(CONTENT_URL, url);
    }
    
    /**
     * Get the value of the contentUrl property.
     * 
     * @return contentUrl
     */
    public String getContentUrl()
    {
        return properties.getProperty(CONTENT_URL);
    }
    
    /**
     * Set the value of the deleteWatchCount property.
     * 
     * @param watchCount
     */
    public void setDeleteWatchCount(Integer watchCount)
    {
        properties.setProperty(DELETE_WATCH_COUNT, watchCount.toString());
    }
    
    /**
     * Get the value of the deleteWatchCount property.
     * 
     * @return deleteWatchCount
     */
    public Integer getDeleteWatchCount()
    {
        String watchCountStr = properties.getProperty(DELETE_WATCH_COUNT, "0");
        return Integer.parseInt(watchCountStr);
    }

    // Generate the path for the properties file, based upon the cache file's path.
    private File fileForCacheFile()
    {
        return new File(cacheFile.getAbsolutePath() + ".properties");
    }
}
