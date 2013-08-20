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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.wcm;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.transaction.UserTransaction;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxService;

/**
 */
public class WCMConcurrentTest extends AbstractWCMServiceImplTest
{
    private SandboxService sbService;
    private AssetService assetService;
    
    private final static String ADMIN = "admin";
    
    private final static String WP = TEST_WEBPROJ_DNS;
    
    private final static String SB_STG = WP;
    private final static String SB_ADMIN = WP+"--"+ADMIN;
    
    private final static String AVM_WEBAPPS_PATH = JNDIConstants.DIR_DEFAULT_WWW+"/"+JNDIConstants.DIR_DEFAULT_APPBASE;
    private final static String ROOT_PATH = AVM_WEBAPPS_PATH+"/"+"ROOT";
    
    //protected static final boolean CLEAN = false; // cleanup during teardown
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Get the required services
        sbService = (SandboxService)ctx.getBean("SandboxService");
        assetService = (AssetService)ctx.getBean("AssetService");
    }
    
    
    /**
     */
    public void test_ETWOTWO_1224() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(ADMIN);
                
            wpService.createWebProject(WP, WP, "title", "description");
            
            assertEquals(1, wpService.listWebApps(WP).size());
            
            assertEquals(0, assetService.listAssets(SB_STG, ROOT_PATH, false).size());
            assertEquals(0, assetService.listAssets(SB_ADMIN, ROOT_PATH, false).size());
            
            long start = System.currentTimeMillis();
            
            logger.debug("Bulk import started ...");
            
            // bulk import
            
            // ETWOTWO-1224 / WCM-948 - also fails with AVMNotFoundException !!!
            int listingRepeat = 100; // listing web apps
            String testFile = System.getProperty("user.dir") + "/source/test-resources/wcm/small-61-items.zip";
            
            File zipFile = new File(testFile);
            assetService.bulkImport(SB_ADMIN, ROOT_PATH, zipFile, false);
            
            int itemCount = assetService.listAssets(SB_ADMIN, ROOT_PATH, false).size();
            
            logger.debug("Items below ROOT: "+itemCount);
            
            assert(itemCount > 0);
            
            logger.debug("... bulk import finished in "+(System.currentTimeMillis()-start)+" msecs");
            
            assertEquals(0, assetService.listAssets(SB_STG, ROOT_PATH, false).size());
            assertEquals(itemCount, assetService.listAssets(SB_ADMIN, ROOT_PATH, false).size());
            
            UserTransaction tx = transactionService.getUserTransaction();
            
            tx.begin();
            
            start = System.currentTimeMillis();
            
            logger.debug("Submit initiation started ...");
            
            // submit (from workflow sandbox to staging sandbox and also update user sandbox)
            sbService.submitAll(SB_ADMIN , "s1", "s1");
            
            tx.commit();
            
            logger.debug("... submit initiation finished in "+(System.currentTimeMillis()-start)+" msecs");
            
            
            int threadCount = 1;
            
            Thread[] threads = new Thread[threadCount];
            
            WCMConcurrentTestListing[] listings = new WCMConcurrentTestListing[threadCount];
            
            for (int i = 0; i < threadCount; i++)
            {
                WCMConcurrentTestListing listing = new WCMConcurrentTestListing(i, listingRepeat);
                listings[i] = listing;
                threads[i] = new Thread(listing);
                threads[i].start();
            }
            
            // join each thread so that we wait for them all to finish
            for (int i = 0; i < threads.length; i++)
            {
                try
                {
                    threads[i].join();
                    
                    if (listings[i].getErrorStackTrace() != null)
                    {
                        throw new RuntimeException(listings[i].getErrorStackTrace());
                    }
                }
                catch (InterruptedException e)
                {
                    // not too serious - the worker threads are non-daemon
                }
            }
            
            assertEquals(1, wpService.listWebApps(WP).size());
            
            assertEquals(itemCount, assetService.listAssets(SB_STG, ROOT_PATH, false).size());
            assertEquals(itemCount, assetService.listAssets(SB_ADMIN, ROOT_PATH, false).size());
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }
    
    private class WCMConcurrentTestListing implements Runnable
    {
        private int id;
        private int repeat;
        private String errorStackTrace = null;
        
        public WCMConcurrentTestListing(int id, int repeat)
        {
            this.id = id;
            this.repeat = repeat;
        }
        
        public String getErrorStackTrace()
        {
            return errorStackTrace;
        }
        
        public void run()
        {
            UserTransaction tx = null;
            try
            {
                AuthenticationUtil.setFullyAuthenticatedUser(ADMIN);
                
                long start = System.currentTimeMillis();
                
                for (int i = 1; i <= repeat; i++)
                {
                    tx = transactionService.getUserTransaction();
                    
                    try
                    {
                        tx.begin();
                        
                        logger.debug("Start: id: "+id+" - loop="+i+" ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                        
                        assertEquals(1, wpService.listWebApps(WP).size());
                        
                        Thread.sleep(500);
                        
                        logger.debug("Finish: id: "+id+" - loop="+i+" ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                        
                        tx.commit();
                    }
                    catch (Throwable t)
                    {
                        if (errorStackTrace == null) 
                        {
                            StringWriter sw = new StringWriter();
                            t.printStackTrace(new PrintWriter(sw));
                            
                            errorStackTrace = sw.toString();
                        }
                        
                        System.err.println("Failed: id: "+id+" - loop="+i+" ["+AlfrescoTransactionSupport.getTransactionId()+"] "+t.getMessage());
                        
                        if (tx != null) { try { tx.rollback(); } catch(Exception e) { }}
                    }
                }
                
                logger.debug("id: "+id+" finished (repeated "+repeat+" times) in "+(System.currentTimeMillis()-start)+" msecs");
            }
            catch (Throwable t)
            {
                logger.debug(t.getMessage());
                        
                if (tx != null) { try { tx.rollback(); } catch(Exception e) { }}
                
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                
                errorStackTrace = sw.toString();
            }
            finally
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        }
    }
}
