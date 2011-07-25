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
package org.alfresco.wcm;

import java.util.List;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Abstract WCM Service implementation unit test
 * 
 * @author janv
 */
public class AbstractWCMServiceImplTest extends TestCase
{
    protected static Log logger = LogFactory.getLog(AbstractWCMServiceImplTest.class);
    
    private static final String PREVIEW_CONFIG_LOCATION = "classpath:wcm/wcm-test-preview-context.xml";
    
    // override jbpm.job.executor idleInterval to 1s (was 1.5m) for WCM unit tests
    private static final String SUBMIT_CONFIG_LOCATION = "classpath:wcm/wcm-jbpm-context.xml";
    
    protected static final long POLL_DELAY = 1500L; // (in millis) 1.5s
    protected static final int POLL_MAX_ATTEMPTS = 20;
    
    // note: all tests share same context (when run via WCMTestSuite)
    protected static ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {ApplicationContextHelper.CONFIG_LOCATIONS[0], SUBMIT_CONFIG_LOCATION, PREVIEW_CONFIG_LOCATION});;
    
    //
    // test data
    //
    
    protected static final String TEST_RUN = ""+System.currentTimeMillis();
    protected static final boolean CLEAN = true; // cleanup during teardown
    
    // base web project
    protected static final String TEST_WEBPROJ_DNS  = "testWP-"+TEST_RUN;
    
    protected static final String TEST_WEBPROJ_NAME = "testSandbox Web Project Display Name - "+TEST_RUN;
    protected static final String TEST_WEBPROJ_TITLE = "This is my title";
    protected static final String TEST_WEBPROJ_DESCRIPTION = "This is my description";
    protected static final String TEST_WEBPROJ_DEFAULT_WEBAPP = WCMUtil.DIR_ROOT;
    protected static final boolean TEST_WEBPROJ_USE_AS_TEMPLATE = true;
    protected static final boolean TEST_WEBPROJ_DONT_USE_AS_TEMPLATE = false;
    
    // base web users
    protected static String USER_ADMIN;
    
    protected static final String TEST_USER = "testWebUser-"+TEST_RUN;
    
    protected static final String USER_ONE   = TEST_USER+"-One";
    protected static final String USER_TWO   = TEST_USER+"-Two";
    protected static final String USER_THREE = TEST_USER+"-Three";
    protected static final String USER_FOUR  = TEST_USER+"-Four";
    
    //
    // services
    //

    protected WebProjectService wpService;
    protected SandboxService sbService;
    protected AssetService assetService;
    
    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;
    
    protected TransactionService transactionService;
    protected AVMLockingService avmLockingService;
    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        wpService = (WebProjectService)ctx.getBean("WebProjectService");
        sbService = (SandboxService)ctx.getBean("SandboxService");
        assetService = (AssetService)ctx.getBean("AssetService");
        
        authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
        transactionService = (TransactionService)ctx.getBean("TransactionService");
        avmLockingService = (AVMLockingService)ctx.getBean("AVMLockingService");
        
        // By default run as Admin
        USER_ADMIN = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        RetryingTransactionCallback<Void> createUsersCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                createUser(USER_ONE);
                createUser(USER_TWO);
                createUser(USER_THREE);
                createUser(USER_FOUR);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createUsersCallback);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (CLEAN)
        {
            // Switch back to Admin
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            List<WebProjectInfo> webProjects = wpService.listWebProjects();
            for (final WebProjectInfo wpInfo : webProjects)
            {
                if (wpInfo.getStoreId().startsWith(TEST_WEBPROJ_DNS))
                {
                    // note: added retry for now, due to intermittent concurrent update (during tearDown) possibly due to OrphanReaper ?
                    // org.hibernate.StaleObjectStateException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [org.alfresco.repo.avm.PlainFileNodeImpl#3752]
                    RetryingTransactionCallback<Object> deleteWebProjectWork = new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Exception
                        {
                            wpService.deleteWebProject(wpInfo.getNodeRef());
                            return null;
                        }
                    };
                    transactionService.getRetryingTransactionHelper().doInTransaction(deleteWebProjectWork);
                }
            }
            
            RetryingTransactionCallback<Void> deleteUsersCallback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    deleteUser(USER_ONE);
                    deleteUser(USER_TWO);
                    deleteUser(USER_THREE);
                    deleteUser(USER_FOUR);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(deleteUsersCallback);
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    protected void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    protected void deleteUser(String userName)
    {
        personService.deletePerson(userName);
    }
    
    protected int pollForSnapshotCount(final String stagingStoreId, final int expectedCnt) throws InterruptedException
    {
        long start = System.currentTimeMillis();
        
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        
        int attempts = 0;
        
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            int cnt = 0;
            
            while (cnt != expectedCnt)
            {
                Thread.sleep(POLL_DELAY);
                
                cnt = sbService.listSnapshots(stagingStoreId, false).size();
                
                attempts++;
                
                if (attempts > POLL_MAX_ATTEMPTS)
                {
                    throw new AlfrescoRuntimeException("Too many poll attempts: "+attempts);
                }
            }
        }
        finally
        {
            AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
        }
        
        logger.debug("pollForSnapshotCount: "+stagingStoreId+" in "+(System.currentTimeMillis()-start)+" msecs ("+attempts+" attempts)");
        
        return attempts;
    }
}
