/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm;

import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Abstract WCM Service implementation unit test
 * 
 * @author janv
 */
public class AbstractWCMServiceImplTest extends TestCase
{
    private static final String PREVIEW_CONFIG_LOCATION = "classpath:wcm/wcm-test-preview-context.xml";
    
    // override jbpm.job.executor idleInterval to 5s (was 1.5m) for WCM unit tests
    private static final String SUBMIT_CONFIG_LOCATION = "classpath:wcm/wcm-jbpm-context.xml";
    protected static final long SUBMIT_DELAY = 15000L; // (in millis) 15s - to allow async submit direct workflow to complete (as per 5s idleInterval above)
    
    // note: all tests share same context (when run via WCMTestSuite)
    protected static ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {ApplicationContextHelper.CONFIG_LOCATIONS[0], SUBMIT_CONFIG_LOCATION, PREVIEW_CONFIG_LOCATION});;
    
    //
    // test data
    //
    
    protected static final String TEST_RUN = ""+System.currentTimeMillis();
    protected static final boolean CLEAN = true; // cleanup during teardown
    
    // base web project
    protected static final String TEST_WEBPROJ_DNS  = "testWebProj-"+TEST_RUN;
    
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
    
    protected AuthenticationService authenticationService;
    protected PersonService personService;
    
    protected TransactionService transactionService;
    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        wpService = (WebProjectService)ctx.getBean("WebProjectService");
        sbService = (SandboxService)ctx.getBean("SandboxService");
        assetService = (AssetService)ctx.getBean("AssetService");
        
        authenticationService = (AuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
        transactionService = (TransactionService)ctx.getBean("TransactionService");
        
        // By default run as Admin
        USER_ADMIN = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        createUser(USER_FOUR);
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
            
            deleteUser(USER_ONE);
            deleteUser(USER_TWO);
            deleteUser(USER_THREE);
            deleteUser(USER_FOUR);
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    protected void createUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
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
}
