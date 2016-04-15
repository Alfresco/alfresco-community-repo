/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.repo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.FilteringDirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

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
        customComparators.put("application/vnd.ms-excel", new XLSContentComparator());
        customComparators.put("application/vnd.ms-powerpoint", new PPTContentComparator());
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

    private boolean isContentIdentical(NPOIFSFileSystem fs1, NPOIFSFileSystem fs2, Collection<String> excludes) throws IOException
    {
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

                // this call guarantees that leftIs is closed.
                NPOIFSFileSystem fs2 = new NPOIFSFileSystem(leftIs);
                // this call keeps an open file handle and needs closing.
                NPOIFSFileSystem fs1 = new NPOIFSFileSystem(newFile);  
                try
                {

                    return isContentIdentical(fs1, fs2, excludes);
                }
                finally
                {
                	try
                	{
                		fs1.close();
                	}
                	catch (IOException e)
                	{
                		// ignore
                	}
                	try
                	{
                		fs2.close();
                	}
                	catch (IOException e)
                	{
                		// ignore
                	}
                }
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
    
    // Comparator for MS Excel
    private class XLSContentComparator implements ContentComparator
    {
     
        @Override
        public boolean isContentEqual(ContentReader existingContent,
                File newFile)
        {
            long newSize = newFile.length();
            
            if(logger.isDebugEnabled())
            {
                logger.debug("comparing two excel files size:" + existingContent.getSize() + ", and " + newFile.length());
            }
           
            if(existingContent.getSize() != newSize)
            {
                logger.debug("excel files are different size");
                // Different size
                return false;
            }
            
            /**
             * Use POI to compare the content of the XLS file, exluding certain properties
             */
            File tpm1 = null;
            File tpm2 = null;
            InputStream leftIs = null;
            try 
            {  
                Collection<String> excludes = new HashSet<String>();
                
                tpm1 = TempFileProvider.createTempFile("CIFSContentComparator1", "xls");
                tpm2 = TempFileProvider.createTempFile("CIFSContentComparator2", "xls");
                
                leftIs = existingContent.getContentInputStream();
                HSSFWorkbook wb1 = new HSSFWorkbook(leftIs);
                HSSFWorkbook wb2 = new HSSFWorkbook(new FileInputStream(newFile));
                wb1.writeProtectWorkbook("", "CIFSContentComparator");
                wb2.writeProtectWorkbook("", "CIFSContentComparator");
                
                FileOutputStream os = new FileOutputStream(tpm1);
                try
                {
                	wb1.write(os);
                }
                finally
                {
                	os.close();
                }
                FileOutputStream os2 = new FileOutputStream(tpm2);
                try
                {
                	wb2.write(os2);
                }
                finally
                {
                	os2.close();
                }
                
                NPOIFSFileSystem fs1 = new NPOIFSFileSystem(tpm1);
                NPOIFSFileSystem fs2 = new NPOIFSFileSystem(tpm2);
                
                return isContentIdentical(fs1, fs2, excludes);
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
            	if(tpm1 != null)
            	{
            		try 
            		{
            	        tpm1.delete();
            		}
            		catch (Exception e)
            		{
            			// ignore
            		}
            	}
            	if(tpm2 != null)
            	{
            		try 
            		{
            		    tpm2.delete();
        		    }
        		    catch (Exception e)
        		    {
        			    // ignore
        		    }
            	}
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
                
    // Comparator for MS PowerPoint
    private class PPTContentComparator implements ContentComparator
    {
                
        @Override
        public boolean isContentEqual(ContentReader existingContent, File newFile)
        {
            long fileSizesDifference = newFile.length() - existingContent.getSize();
                
            if(logger.isDebugEnabled())
            {
                logger.debug("comparing two powerpoint files size:" + existingContent.getSize() + ", and " + newFile.length());
            }

            File tpm1 = null;
            File tpm2 = null;
            InputStream leftIs = null;
            try
            {
                if(fileSizesDifference != 0)
                {
                    // ALF-18793
                    // Experience has shown that the size of opened/closed file increases to 3072 bytes.
                    // (That occurs only in case if the file has been created on one MS PowerPoint instance and opened/closed on another
                    // due to change of lastEditUsername property (if they are different)).
                    if (fileSizesDifference > 3072 && fileSizesDifference < 0)
                    {
                        logger.debug("powerpoint files are different size");
                        // Different size
                        return false;
                    }

                    Collection<String> excludes = new HashSet<String>();
                    excludes.add("Current User");

                    leftIs = existingContent.getContentInputStream();
                    HSLFSlideShow slideShow1 = new HSLFSlideShow(leftIs);
                    HSLFSlideShow slideShow2 = new HSLFSlideShow(new FileInputStream(newFile));

                    String lastEditUsername1 = slideShow1.getCurrentUserAtom().getLastEditUsername();
                    String lastEditUsername2 = slideShow2.getCurrentUserAtom().getLastEditUsername();

                    if (lastEditUsername1.equals(lastEditUsername2))
                    {
                        logger.debug("powerpoint files are edited by different users");
                        // Different size
                        return false;
                    }
                    else
                    {
                        //make sure that nothing has been changed except lastEditUsername
                        tpm1 = TempFileProvider.createTempFile("CIFSContentComparator1", "ppt");
                        FileOutputStream os = new FileOutputStream(tpm1);
                        try
                        {
                            slideShow1.write(os);
                        }
                        finally
                        {
                            try
                            {
                            	os.close();
                            }
                            catch (IOException ie)
                            {
                                // ignore
                            }
                        }
                        tpm2 = TempFileProvider.createTempFile("CIFSContentComparator2", "ppt");
                        FileOutputStream os2 = new FileOutputStream(tpm2);
                        try
                        {
                            slideShow2.write(os2);
                        }
                        finally
                        {
                            try
                            {
                                os2.close();
                            }
                            catch (IOException ie)
                            {
                                // ignore
                            }
                        }

                        NPOIFSFileSystem fs1 = new NPOIFSFileSystem(tpm1);
                        NPOIFSFileSystem fs2 = new NPOIFSFileSystem(tpm2);

                        return isContentIdentical(fs1, fs2, excludes);
                    }
                
                }

                return true;
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
            	if(tpm1 != null)
            	{
            		try 
            		{
            	        tpm1.delete();
            		}
            		catch (Exception e)
            		{
            			// ignore
            		}
            	}
            	if(tpm2 != null)
            	{
            		try 
            		{
            		    tpm2.delete();
        		    }
        		    catch (Exception e)
        		    {
        			    // ignore
        		    }
            	}
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
