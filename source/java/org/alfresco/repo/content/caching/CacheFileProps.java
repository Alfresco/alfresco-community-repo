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
     * @param cacheFile - cache file
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
     * Size of the properties file or 0 if it does not exist.
     * 
     * @return file size in bytes.
     */
    public long fileSize()
    {
        return propsFile.length();
    }
    
    /**
     * Set the value of the contentUrl property.
     * 
     * @param url String
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
     * @param watchCount Integer
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
