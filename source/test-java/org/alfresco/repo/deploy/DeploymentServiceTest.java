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
package org.alfresco.repo.deploy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.JNDIConstants;
import org.alfresco.jlan.smb.dcerpc.UUID;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentReportCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.NameMatcher;
import org.alfresco.wcm.AbstractWCMServiceImplTest;
import org.alfresco.wcm.actions.WCMSandboxRevertSnapshotAction;
import org.alfresco.wcm.actions.WCMSandboxSubmitAction;
import org.alfresco.wcm.actions.WCMSandboxUndoAction;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test of deployment to a Test Server.   
 * 
 * Through the WCM application rather than directly through AVM.
 * 
 * @author Mark Rogers
 */
public class DeploymentServiceTest extends AbstractWCMServiceImplTest
{
    private static Log logger = LogFactory.getLog(DeploymentServiceTest.class);
    
    // base sandbox
    private static final String TEST_SANDBOX = TEST_WEBPROJ_DNS+"-sandbox";
    
    private static final int SCALE_USERS = 5;
    private static final int SCALE_WEBPROJECTS = 2;
    
    //
    // services
    //
    
    private AVMService avmService; // non-locking-aware
    private DeploymentService deploymentService = null;
    private NameMatcher matcher = null;
    
    private File log = null;
    private File metadata = null;
    private File data = null;
    private File target = null;
       
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Get the required services
        avmService = (AVMService)ctx.getBean("AVMService");
        deploymentService = (DeploymentService)ctx.getBean("DeploymentService"); 
        NameMatcher matcher = (NameMatcher)ctx.getBean("globalPathExcluder");
        
        super.setUp();
        log = new File("deplog");
        log.mkdir();
        metadata = new File("depmetadata");
        metadata.mkdir();
        data = new File("depdata");
        data.mkdir();
        target = new File("sampleTarget");
        target.mkdir();
        
        /**
         * Start the FSR
         */
        @SuppressWarnings("unused")
        ClassPathXmlApplicationContext receiverContext =
            new ClassPathXmlApplicationContext("application-context.xml"); // Fetch application context from deployment
 
        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (CLEAN)
        {
            // Switch back to Admin
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            deleteUser(USER_ONE);
            deleteUser(USER_TWO);
            deleteUser(USER_THREE);
            deleteUser(USER_FOUR);
        }
        
        super.tearDown();
    }
    
    private String TEST_USER = "Giles";
    private String TEST_PASSWORD = "Watcher";
    private String TEST_TARGET = "sampleTarget";    
    
    public void testSimple() throws Exception
    {
  
        int storeCnt = avmService.getStores().size();
        
        String projectName = "DeploymentServiceTestSimple";
        
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(projectName, projectName, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String wpStoreId = wpInfo.getStoreId();
        
        // list 2 sandboxes
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // list 4 extra AVM stores (2 per sandbox)
        assertEquals(storeCnt+4, avmService.getStores().size()); // 2x stating (main,preview), 2x admin author (main, preview)
        
        // get admin's sandbox
        SandboxInfo authorInfo = sbService.getAuthorSandbox(wpStoreId);
        assertNotNull(authorInfo);
        
        // get staging sandbox
        SandboxInfo stagingInfo = sbService.getStagingSandbox(wpStoreId);
        assertNotNull(stagingInfo);
        
        DeploymentReport report = new DeploymentReport();
        List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
        callbacks.add(new DeploymentReportCallback(report));
        
        /**
         * Test Server Admin Sandbox Webapps
         */
        String srcPath = projectName + "--admin:/www/avm_webapps";
        deploymentService.deployDifferenceFS(-1, srcPath, "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
  
        /**
         * Test Server Admin Sandbox ROOT Webapp
         */
        assetService.createFolder(authorInfo.getSandboxId(), "/", "hello", null);
        srcPath = projectName + "--admin:/www/avm_webapps/ROOT";
        deploymentService.deployDifferenceFS(-1, srcPath, "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);

        /**
         * Live Server - Staging ROOT
         */
        srcPath = projectName + ":/www/avm_webapps/ROOT";
        deploymentService.deployDifferenceFS(-1, srcPath, "default", "localhost", 44100, TEST_USER, TEST_PASSWORD, TEST_TARGET, matcher, false, false, false, callbacks);
        
        wpService.deleteWebProject(projectName);
    }
    
}
