package org.alfresco.filesys.repo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.FilteringDirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

import java.io.File;

/**
 * Compares content for to see if content is equal.
 * <p>
 * Most mimetypes can simply be binary compared but for some mimetypes
 * there may be trivial differences so a binary compare is not sufficient.
 * <p>
 * In particular MS Project and MS Excel write to header fields without changing content. 
 * 
 * @author mrogers
 *
 */
public class CIFSContentComparator implements ContentComparator
{
    // TODO Externalize Map of mimetype to comparator
    private Map<String, ContentComparator> customComparators = new HashMap<String, ContentComparator>();
    
    private static final Log logger = LogFactory.getLog(CIFSContentComparator.class);
    
    /**
     * 
     */
    public void init()
    {   
        customComparators.put("application/vnd.ms-project", new MPPContentComparator());
    }  

    @Override
    public boolean isContentEqual(ContentReader existingContent,
            File newFile)
    {
        String mimetype = existingContent.getMimetype();
        logger.debug("isContentEqual mimetype=" + mimetype);
        
        long newSize = newFile.length();
   
        ContentComparator custom = customComparators.get(mimetype);
        
        if(custom == null)
        {
            // No custom comparator - check length then do a binary diff
            if(existingContent.getSize() != newSize)
            {
                // Different size
                logger.debug("generic comparision, size is different - not equal");
                return false;
            }
            
            InputStream rightIs = null;
            InputStream leftIs = null;
            try
            {   
                rightIs = new BufferedInputStream(new FileInputStream(newFile));
                leftIs = existingContent.getContentInputStream();
                boolean retVal = EqualsHelper.binaryStreamEquals(leftIs, rightIs);
                rightIs = null;
                leftIs = null;
                
                if(logger.isDebugEnabled())
                {
                    logger.debug("generic comparision, binary content comparison equal=" + retVal);
                }
                return retVal;
            }
            catch (IOException e)
            {

                logger.debug("Unable to compare contents", e);
                return false;
            }
            finally
            {
                if(leftIs != null)
                {
                    try
                    {
                        leftIs.close();
                    } 
                    catch (IOException e)
                    {
                        // Do nothing this is cleanup code
                    }
                }
                if(rightIs != null)
                {
                    try
                    {
                        rightIs.close();
                    } 
                    catch (IOException e)
                    {
                        // Do nothing this is cleanup code
                    }
                }
            }
        }
        else
        {
            // there is a custom comparator for this mimetype
            return custom.isContentEqual(existingContent, newFile);
        }
    }

    // Comparator for MS Project
    private class MPPContentComparator implements ContentComparator
    {
     
        @Override
        public boolean isContentEqual(ContentReader existingContent,
                File newFile)
        {
            long newSize = newFile.length();
            
            if(logger.isDebugEnabled())
            {
                logger.debug("comparing two project files size:" + existingContent.getSize() + ", and " + newFile.length());
            }
           
            if(existingContent.getSize() != newSize)
            {
                logger.debug("project files are different size");
                // Different size
                return false;
            }
            
            /**
             * Use POI to compare the content of the MPP file, exluding certain properties
             */
            InputStream leftIs = null;
            try
            {  
                Collection<String> excludes = new HashSet<String>();
                excludes.add("Props");
                excludes.add("Props12");
                excludes.add("Props9");
                
                leftIs = existingContent.getContentInputStream();
                NPOIFSFileSystem fs2 = new NPOIFSFileSystem(leftIs);              
                NPOIFSFileSystem fs1 = new NPOIFSFileSystem(newFile);                
                
                DirectoryEntry de1 = fs1.getRoot();
                DirectoryEntry de2 = fs2.getRoot();
                
                FilteringDirectoryNode fs1Filtered = new FilteringDirectoryNode(de1, excludes);
                FilteringDirectoryNode fs2Filtered = new FilteringDirectoryNode(de2, excludes);
                
                boolean retVal = EntryUtils.areDirectoriesIdentical(fs1Filtered, fs2Filtered);
                if(logger.isDebugEnabled())
                {
                    logger.debug("returning equal="+ retVal);
                }
                
                return retVal;
            }
            catch (ContentIOException ce)
            {
                logger.debug("Unable to compare contents", ce);
                return false;
            }
            catch (IOException e)
            {
                logger.debug("Unable to compare contents", e);
                return false;
            }
            finally
            {
                if(leftIs != null)
                {
                    try
                    {
                        leftIs.close();
                    } 
                    catch (IOException e)
                    {
                       // Ignore
                    }
                }
            }
        }
    }
}
