/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A helper class that provides temporary files, providing a common point to clean
 * them up.
 * 
 * <p>
 * The contents of ALFRESCO_TEMP_FILE_DIR [%java.io.tmpdir%/Alfresco] are managed by this 
 * class.  Temporary files and directories are cleaned by TempFileCleanerJob so that 
 * after a delay [default 1 hour] the contents of the alfresco temp dir, 
 * both files and directories are removed.
 * 
 * <p>
 * Some temporary files may need to live longer than 1 hour.   The temp file provider allows special sub folders which 
 * are cleaned less frequently.    By default, files in the long life folders will remain for 24 hours 
 * unless cleaned by the application code earlier.
 * 
 * <p>
 * The other contents of %java.io.tmpdir% are not touched by the cleaner job.
 * 
 * <p>TempFileCleanerJob Job Data: protectHours, number of hours to keep temporary files, default 1 hour.
 *  
 * @author derekh
 * @author mrogers
 */
@AlfrescoPublicApi
public class TempFileProvider
{
    private static final int BUFFER_SIZE = 40 * 1024;

    /** 
     * subdirectory in the temp directory where Alfresco temporary files will go 
     */
    public static final String ALFRESCO_TEMP_FILE_DIR = "Alfresco";
    
    /**
     * The prefix for the long life temporary files.
     */
    public static final String ALFRESCO_LONG_LIFE_FILE_DIR = "longLife";

    /** the system property key giving us the location of the temp directory */
    public static final String SYSTEM_KEY_TEMP_DIR = "java.io.tmpdir";

    private static final Log logger = LogFactory.getLog(TempFileProvider.class);
    
    private static int MAX_RETRIES = 3;

    /**
     * Static class only
     */
    private TempFileProvider()
    {
    }

    /**
     * Get the Java Temp dir e.g. java.io.tempdir
     * 
     * @return Returns the system temporary directory i.e. <code>isDir == true</code>
     */
    public static File getSystemTempDir()
    {
        String systemTempDirPath = System.getProperty(SYSTEM_KEY_TEMP_DIR);
        if (systemTempDirPath == null)
        {
            throw new AlfrescoRuntimeException("System property not available: " + SYSTEM_KEY_TEMP_DIR);
        }
        File systemTempDir = new File(systemTempDirPath);
        if (logger.isDebugEnabled())
        {
            logger.debug("Created system temporary directory: " + systemTempDir);
        }
        return systemTempDir;
    }
    
    /**
     * Get the Alfresco temp dir, by defaut %java.io.tempdir%/Alfresco.  
     * Will create the temp dir on the fly if it does not already exist.
     * 
     * @return Returns a temporary directory, i.e. <code>isDir == true</code>
     */
    public static File getTempDir()
    {
        return getTempDir(ALFRESCO_TEMP_FILE_DIR);
    }
    
    /**
     * Get the specified temp dir, %java.io.tempdir%/dirName.  
     * Will create the temp dir on the fly if it does not already exist.
     * 
     * @param dirName the name of sub-directory in %java.io.tempdir%
     * 
     * @return Returns a temporary directory, i.e. <code>isDir == true</code>
     */
    public static File getTempDir(String dirName)
    {
        File systemTempDir = getSystemTempDir();
        // append the Alfresco directory
        File tempDir = new File(systemTempDir, dirName);
        // ensure that the temp directory exists
        if (tempDir.exists())
        {
            // nothing to do
        }
        else
        {
            // not there yet
            if (!tempDir.mkdirs())
            {
                // We didn't create it but perhaps it was made by some other thread
                if (!tempDir.exists())
                {
                    // It's definitely not there
                    throw new AlfrescoRuntimeException("Failed to create temp directory: " + tempDir);
                }
            }
            else
            {
                // This thread created it
                if (logger.isDebugEnabled())
                {
                    logger.debug("Created temp directory: " + tempDir);
                }
            }
        }
        // done
        return tempDir;
    }
    
    /**
     * creates a longer living temp dir.   Files within the longer living 
     * temp dir will not be garbage collected as soon as "normal" temporary files.
     * By default long life temp files will live for for 24 hours rather than 1 hour.
     * <p>
     * Code using the longer life temporary files should be careful to clean up since 
     * abuse of this feature may result in out of memory/disk space errors.
     * @param key can be blank in which case the system will generate a folder to be used by all processes
     * or can be used to create a unique temporary folder name for a particular process.  At the end of the process 
     * the client can simply delete the entire temporary folder.  
     * @return the long life temporary directory
     */
    public static File getLongLifeTempDir(String key)
    {
        /**
         * Long life temporary directories have a prefix at the start of the 
         * folder name.
         */
        String folderName = ALFRESCO_LONG_LIFE_FILE_DIR + "_" + key;
        
        File tempDir = getTempDir();
        
        // append the Alfresco directory
        File longLifeDir = new File(tempDir, folderName);
        // ensure that the temp directory exists
        
        if (longLifeDir.exists())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Already exists: " + longLifeDir);
            }
            // nothing to do
            return longLifeDir;
        }
        else
        {
            /**
             * We need to create a temporary directory
             * 
             * We may have a race condition here if more than one thread attempts to create 
             * the temp dir.
             *  
             * mkdirs can't be synchronized
             * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4742723
             */
            for(int retry = 0; retry < MAX_RETRIES; retry++)
            {
                boolean created = longLifeDir.mkdirs();
            
                if (created)
                {
                    // Yes we created the temp dir
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Created long life temp directory: " + longLifeDir);
                    }
                    return longLifeDir;
                }
                else
                {   
                    if(longLifeDir.exists())
                    {
                        // created by another thread, but that's O.K.  
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Another thread created long life temp directory: " + longLifeDir);
                        }
                        return longLifeDir;
                    }
                }
            }
        }
        throw new AlfrescoRuntimeException("Failed to create temp directory: " + longLifeDir);
    }

    public static File createTempFile(InputStream in, String namePrefix, String nameSufix) throws Exception
    {
        if (null == in)
        {
            return null;
        }

        File file = createTempFile(namePrefix, nameSufix);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
        try
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            int i;
            while ((i = in.read(buffer)) > -1)
            {
                out.write(buffer, 0, i);
            }
        }
        catch (Exception e)
        {
            file.delete();
            throw e;
        }
        finally
        {
            in.close();
            out.flush();
            out.close();
        }

        return file;
    }

    /**
     * Is this a long life folder ?
     * @param file
     * @return true, this is a long life folder.
     */
    private static boolean isLongLifeTempDir(File file)
    {
        if(file.isDirectory())
        {
            if(file.getName().startsWith(ALFRESCO_LONG_LIFE_FILE_DIR))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    /**
     * Create a temp file in the alfresco temp dir.
     * 
     * @return Returns a temp <code>File</code> that will be located in the
     *         <b>Alfresco</b> subdirectory of the default temp directory
     * 
     * @see #ALFRESCO_TEMP_FILE_DIR
     * @see File#createTempFile(java.lang.String, java.lang.String)
     */
    public static File createTempFile(String prefix, String suffix)
    {
        File tempDir = TempFileProvider.getTempDir();
        // we have the directory we want to use
        return createTempFile(prefix, suffix, tempDir);
    }

    /**
     * @return Returns a temp <code>File</code> that will be located in the
     *         given directory
     * 
     * @see #ALFRESCO_TEMP_FILE_DIR
     * @see File#createTempFile(java.lang.String, java.lang.String)
     */
    public static File createTempFile(String prefix, String suffix, File directory)
    {
        try
        {
            File tempFile = File.createTempFile(prefix, suffix, directory);
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating tmp file: " + tempFile);
            }
            return tempFile;
        } catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to created temp file: \n" +
                    "   prefix: " + prefix + "\n"
                    + "   suffix: " + suffix + "\n" +
                    "   directory: " + directory,
                    e);
        }
    }

    /**
     * Cleans up <b>all</b> Alfresco temporary files that are older than the
     * given number of hours.  Subdirectories are emptied as well and all directories
     * below the primary temporary subdirectory are removed.
     * <p>
     * The job data must include a property <tt>protectHours</tt>, which is the
     * number of hours to protect a temporary file from deletion since its last
     * modification.
     * 
     * @author Derek Hulley
     */
    @AlfrescoPublicApi
    public static class TempFileCleanerJob implements Job
    {
        public static final String KEY_PROTECT_HOURS = "protectHours";
        public static final String KEY_DIRECTORY_NAME = "directoryName";

        /**
         * Gets a list of all files in the {@link TempFileProvider#ALFRESCO_TEMP_FILE_DIR temp directory}
         * and deletes all those that are older than the given number of hours.
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            // get the number of hours to protect the temp files
            String strProtectHours = (String) context.getJobDetail().getJobDataMap().get(KEY_PROTECT_HOURS);
            if (strProtectHours == null)
            {
                throw new JobExecutionException("Missing job data: " + KEY_PROTECT_HOURS);
            }
            int protectHours = -1;
            try
            {
                protectHours = Integer.parseInt(strProtectHours);
            }
            catch (NumberFormatException e)
            {
                throw new JobExecutionException("Invalid job data " + KEY_PROTECT_HOURS + ": " + strProtectHours);
            }
            if (protectHours < 0 || protectHours > 8760)
            {
                throw new JobExecutionException("Hours to protect temp files must be 0 <= x <= 8760");
            }

            String directoryName = (String) context.getJobDetail().getJobDataMap().get(KEY_DIRECTORY_NAME);
            
            if (directoryName == null)
            {
                directoryName = ALFRESCO_TEMP_FILE_DIR;
            }

            long now = System.currentTimeMillis();
            long aFewHoursBack = now - (3600L * 1000L * protectHours);
            
            long aLongTimeBack = now - (24 * 3600L * 1000L);
            
            File tempDir = TempFileProvider.getTempDir(directoryName);
            int count = removeFiles(tempDir, aFewHoursBack, aLongTimeBack, false);  // don't delete this directory
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Removed " + count + " files from temp directory: " + tempDir);
            }
        }
        
        /**
         * Removes all temporary files created before the given time.
         * <p>
         * The delete will cascade down through directories as well.
         * 
         * @param removeBefore only remove files created <b>before</b> this time
         * @return Returns the number of files removed
         */
        public static int removeFiles(long removeBefore)
        {
            File tempDir = TempFileProvider.getTempDir();
            return removeFiles(tempDir, removeBefore, removeBefore, false);
        }
        
        /**
         * @param directory the directory to clean out - the directory will optionally be removed
         * @param removeBefore only remove files created <b>before</b> this time
         * @param removeDir true if the directory must be removed as well, otherwise false
         * @return Returns the number of files removed
         */
        private static int removeFiles(File directory, long removeBefore, long longLifeBefore, boolean removeDir)
        {
            if (!directory.isDirectory())
            {
                throw new IllegalArgumentException("Expected a directory to clear: " + directory);
            }
            // check if there is anything to to
            if (!directory.exists())
            {
                return 0;
            }
            // list all files
            File[] files = directory.listFiles();
            int count = 0;
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if(isLongLifeTempDir(file))
                    {
                        // long life for this folder and its children
                        int countRemoved = removeFiles(file, longLifeBefore, longLifeBefore, true);  
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Removed " + countRemoved + " files from temp directory: " + file);
                        }
                    }
                    else
                    {
                        // enter subdirectory and clean it out and remove itsynetics
                        int countRemoved = removeFiles(file, removeBefore, longLifeBefore, true);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Removed " + countRemoved + " files from directory: " + file);
                        }
                    }
                }
                else
                {
                    // it is a file - check the created time
                    if (file.lastModified() > removeBefore)
                    {
                        // file is not old enough
                        continue;
                    }
                    // it is a file - attempt a delete
                    try
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Deleting temp file: " + file);
                        }
                        file.delete();
                        count++;
                    }
                    catch (Throwable e)
                    {
                        logger.info("Failed to remove temp file: " + file);
                    }
                }
            }
            // must we delete the directory we are in?
            if (removeDir)
            {
                // the directory must be removed if empty
                try
                {
                    File[] listing = directory.listFiles();
                    if(listing != null && listing.length == 0)
                    {
                        // directory is empty
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Deleting empty directory: " + directory);
                        }
                        directory.delete();
                    }
                }
                catch (Throwable e)
                {
                    logger.info("Failed to remove temp directory: " + directory, e);
                }
            }
            // done
            return count;
        }
    }
}
