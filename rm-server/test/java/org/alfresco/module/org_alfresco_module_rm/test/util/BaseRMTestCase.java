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
package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RmSiteType;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.RetryingTransactionHelperTestCase;
import org.springframework.context.ApplicationContext;

/**
 * Base test case class to use for RM unit tests.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseRMTestCase extends RetryingTransactionHelperTestCase 
                                     implements RecordsManagementModel, ContentModel
{    
    /** Application context */
    protected static final String[] CONFIG_LOCATIONS = new String[] 
    { 
        "classpath:alfresco/application-context.xml",
        "classpath:test-context.xml"
    };
    protected ApplicationContext applicationContext;
    
    /** Test model contants */
    protected String URI = "http://www.alfresco.org/model/rmtest/1.0";
    protected String PREFIX = "rmt";
    protected QName TYPE_CUSTOM_TYPE = QName.createQName(URI, "customType");
    protected QName ASPECT_CUSTOM_ASPECT = QName.createQName(URI, "customAspect");
    protected QName ASPECT_RECORD_META_DATA = QName.createQName(URI, "recordMetaData");
    
    /** Site id */
    protected static final String SITE_ID = "mySite";
    
    /** Common test utils */
    protected CommonRMTestUtils utils;
    
    /** Services */
    protected NodeService nodeService;
    protected ContentService contentService;
    protected DictionaryService dictionaryService;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected PolicyComponent policyComponent;
    protected NamespaceService namespaceService;
    protected SearchService searchService;
    protected SiteService siteService;
    protected MutableAuthenticationService authenticationService;
    protected AuthorityService authorityService;
    protected PersonService personService;
    protected TransactionService transactionService;
    
    /** RM Services */
    protected RecordsManagementService rmService;
    protected DispositionService dispositionService;
    protected RecordsManagementEventService eventService;
    protected RecordsManagementAdminService adminService;    
    protected RecordsManagementActionService actionService;
    protected RecordsManagementSearchService rmSearchService;
    protected RecordsManagementSecurityService securityService;
    protected CapabilityService capabilityService;
    protected VitalRecordService vitalRecordService;
    
    /** test data */
    protected StoreRef storeRef;
    protected NodeRef rootNodeRef;   
    protected SiteInfo siteInfo;
    protected NodeRef folder;
    protected NodeRef filePlan;
    protected NodeRef rmContainer;
    protected DispositionSchedule dispositionSchedule;
    protected NodeRef rmFolder;
    
    /** multi-hierarchy test data 
     *
     *   |--rmRootContainer
     *      |
     *      |--mhContainer
     *         |
     *         |--mhContainer-1-1 (has schedule - folder level)
     *         |  |
     *         |  |--mhContainer-2-1
     *         |     |
     *         |     |--mhContainer-3-1
     *         |         
     *         |--mhContainer-1-2 (has schedule - folder level)
     *            |
     *            |--mhContainer-2-2
     *            |  |
     *            |  |--mhContainer-3-2
     *            |  |
     *            |  |--mhContainer-3-3 (has schedule - record level)
     *            |  
     *            |--mhContainer-2-3 (has schedule - folder level)
     *               |
     *               |--mhContainer-3-4
     *               |     
     *               |--mhContainer-3-5 (has schedule- record level)        
     */
    
    protected NodeRef mhContainer;
    
    protected NodeRef mhContainer11;
    protected DispositionSchedule mhDispositionSchedule11;
    protected NodeRef mhContainer12;
    protected DispositionSchedule mhDispositionSchedule12;
    
    protected NodeRef mhContainer21;
    protected NodeRef mhContainer22;
    protected NodeRef mhContainer23;
    protected DispositionSchedule mhDispositionSchedule23;
    
    protected NodeRef mhContainer31;
    protected NodeRef mhContainer32;
    protected NodeRef mhContainer33;
    protected DispositionSchedule mhDispositionSchedule33;
    protected NodeRef mhContainer34;
    protected NodeRef mhContainer35;
    protected DispositionSchedule mhDispositionSchedule35;
    
    protected NodeRef mhRecordFolder41;
    protected NodeRef mhRecordFolder42;
    protected NodeRef mhRecordFolder43;
    protected NodeRef mhRecordFolder44;
    protected NodeRef mhRecordFolder45;
    
    /** test user names */
    protected String[] testUsers;
    protected String userName;    
    protected String rmUserName;
    protected String powerUserName;
    protected String securityOfficerName;
    protected String recordsManagerName;
    protected String rmAdminName;
    
    /** test people */
    protected NodeRef userPerson;
    protected NodeRef rmUserPerson;
    protected NodeRef powerUserPerson;
    protected NodeRef securityOfficerPerson;
    protected NodeRef recordsManagerPerson;
    protected NodeRef rmAdminPerson;
    
    /**
     * Indicates whether this is a multi-hierarchy test or not.  If it is then the multi-hierarchy record
     * taxonomy test data is loaded.
     */
    protected boolean isMultiHierarchyTest()
    {
        return false;
    }
    
    /**
     * Indicates whether the test users should be created or not.
     * @return
     */
    protected boolean isUserTest()
    {
        return false;
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        // Get the application context
        applicationContext = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
        utils = new CommonRMTestUtils(applicationContext);
        
        // Initialise the service beans
        initServices();
        
        // Setup test data
        setupTestData();
        if (isMultiHierarchyTest() == true)
        {
            setupMultiHierarchyTestData();
        }        
        // Create the users here
        if (isUserTest() == true)
        {
            setupTestUsers(filePlan);
        }
    }
    
    /**
     * Initialise the service beans.
     */
    protected void initServices()
    {
        // Get services
        nodeService = (NodeService)applicationContext.getBean("NodeService");
        contentService = (ContentService)applicationContext.getBean("ContentService");
        retryingTransactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
        namespaceService = (NamespaceService)this.applicationContext.getBean("NamespaceService");
        searchService = (SearchService)this.applicationContext.getBean("SearchService");
        policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");  
        dictionaryService = (DictionaryService)this.applicationContext.getBean("DictionaryService");
        siteService = (SiteService)this.applicationContext.getBean("SiteService");
        authorityService = (AuthorityService)this.applicationContext.getBean("AuthorityService");
        authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("AuthenticationService");
        personService = (PersonService)this.applicationContext.getBean("PersonService");
        transactionService = (TransactionService)applicationContext.getBean("TransactionService");
        
        // Get RM services
        rmService = (RecordsManagementService)applicationContext.getBean("RecordsManagementService");
        dispositionService = (DispositionService)applicationContext.getBean("DispositionService");
        eventService = (RecordsManagementEventService)applicationContext.getBean("RecordsManagementEventService");
        adminService = (RecordsManagementAdminService)applicationContext.getBean("RecordsManagementAdminService");
        actionService = (RecordsManagementActionService)this.applicationContext.getBean("RecordsManagementActionService");
        rmSearchService = (RecordsManagementSearchService)this.applicationContext.getBean("RecordsManagementSearchService");
        securityService = (RecordsManagementSecurityService)this.applicationContext.getBean("RecordsManagementSecurityService");
        capabilityService = (CapabilityService)this.applicationContext.getBean("CapabilityService");
        vitalRecordService = (VitalRecordService)this.applicationContext.getBean("VitalRecordService");
    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
                // Do the tear down 
                tearDownImpl();
                
                return null;
            }
        });       
    }
    
    /**
     * Tear down implementation
     */
    protected void tearDownImpl() 
    {
        // Delete the folder
        nodeService.deleteNode(folder);
        
        // Delete the site
        siteService.deleteSite(SITE_ID);
    }
    
    /**
     * @see org.alfresco.util.RetryingTransactionHelperTestCase#getRetryingTransactionHelper()
     */
    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return retryingTransactionHelper;
    }
    
    /**
     * Setup test data for tests
     */
    protected void setupTestData()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                setupTestDataImpl();
                return null;
            }
        });
    }
    
    /**
     * Impl of test data setup
     */
    protected void setupTestDataImpl()
    {
        storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        // Create folder
        String containerName = "RM2_" + System.currentTimeMillis();
        Map<QName, Serializable> containerProps = new HashMap<QName, Serializable>(1);
        containerProps.put(ContentModel.PROP_NAME, containerName);
        folder = nodeService.createNode(
              rootNodeRef, 
              ContentModel.ASSOC_CHILDREN, 
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, containerName), 
              ContentModel.TYPE_FOLDER,
              containerProps).getChildRef();
        assertNotNull("Could not create base folder", folder);
        
        // Create the site
        siteInfo = siteService.createSite("preset", SITE_ID, "title", "descrition", SiteVisibility.PUBLIC, RecordsManagementModel.TYPE_RM_SITE);
        filePlan = siteService.getContainer(SITE_ID, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);
        assertNotNull("Site document library container was not created successfully.", filePlan);
                        
        // Create RM container
        rmContainer = rmService.createRecordCategory(filePlan, "rmContainer");
        assertNotNull("Could not create rm container", rmContainer);
        
        // Create disposition schedule
        dispositionSchedule = utils.createBasicDispositionSchedule(rmContainer);
        
        // Create RM folder
        rmFolder = rmService.createRecordFolder(rmContainer, "rmFolder");
        assertNotNull("Could not create rm folder", rmFolder);
    }
    
    protected void setupTestUsers(final NodeRef filePlan)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                setupTestUsersImpl(filePlan);
                return null;
            }
        });
    }
    
    /**
     * 
     * @param filePlan
     */
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        userName = GUID.generate();
        userPerson = createPerson(userName);
        
        rmUserName = GUID.generate();
        rmUserPerson = createPerson(rmUserName);
        securityService.assignRoleToAuthority(filePlan, "User", rmUserName);
        
        powerUserName = GUID.generate();
        powerUserPerson = createPerson(powerUserName);
        securityService.assignRoleToAuthority(filePlan, "PowerUser", powerUserName);
        
        securityOfficerName = GUID.generate();
        securityOfficerPerson = createPerson(securityOfficerName);
        securityService.assignRoleToAuthority(filePlan, "SecurityOfficer", securityOfficerName);
        
        recordsManagerName = GUID.generate();
        recordsManagerPerson = createPerson(recordsManagerName);
        securityService.assignRoleToAuthority(filePlan, "RecordsManager", recordsManagerName);
        
        rmAdminName = GUID.generate();
        rmAdminPerson = createPerson(rmAdminName);
        securityService.assignRoleToAuthority(filePlan, "Administrator", rmAdminName);  
        
        testUsers = new String[]
        {
                userName,
                rmUserName, 
                powerUserName, 
                securityOfficerName, 
                recordsManagerName, 
                rmAdminName
        };
    }
    
    protected NodeRef createPerson(String userName)
    {
        authenticationService.createAuthentication(userName, "password".toCharArray());
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return personService.createPerson(properties);
    }
    
    /**
     * Setup multi hierarchy test data
     */
    protected void setupMultiHierarchyTestData()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
                // Do setup
                setupMultiHierarchyTestDataImpl();
                
                return null;
            }
        });
    }
    
    /**
     * Impl of multi hierarchy test data
     */
    protected void setupMultiHierarchyTestDataImpl()
    {
        // Create root mh container
        mhContainer = rmService.createRecordCategory(filePlan, "mhContainer");                
        
        // Level 1
        mhContainer11 = rmService.createRecordCategory(mhContainer, "mhContainer11");
        mhDispositionSchedule11 = utils.createBasicDispositionSchedule(mhContainer11, "ds11", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, false, true);
        mhContainer12 = rmService.createRecordCategory(mhContainer, "mhContainer12");
        mhDispositionSchedule12 = utils.createBasicDispositionSchedule(mhContainer12, "ds12", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, false, true);
        
        // Level 2
        mhContainer21 = rmService.createRecordCategory(mhContainer11, "mhContainer21");
        mhContainer22 = rmService.createRecordCategory(mhContainer12, "mhContainer22");
        mhContainer23 = rmService.createRecordCategory(mhContainer12, "mhContainer23");
        mhDispositionSchedule23 = utils.createBasicDispositionSchedule(mhContainer23, "ds23", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, false, true);

        // Level 3
        mhContainer31 = rmService.createRecordCategory(mhContainer21, "mhContainer31");
        mhContainer32 = rmService.createRecordCategory(mhContainer22, "mhContainer32");
        mhContainer33 = rmService.createRecordCategory(mhContainer22, "mhContainer33");
        mhDispositionSchedule33 = utils.createBasicDispositionSchedule(mhContainer33, "ds33", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, true, true);
        mhContainer34 = rmService.createRecordCategory(mhContainer23, "mhContainer34");
        mhContainer35 = rmService.createRecordCategory(mhContainer23, "mhContainer35");
        mhDispositionSchedule35 = utils.createBasicDispositionSchedule(mhContainer35, "ds35", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, true, true);
        
        // Record folders
        mhRecordFolder41 = rmService.createRecordFolder(mhContainer31, "mhFolder41");
        mhRecordFolder42 = rmService.createRecordFolder(mhContainer32, "mhFolder42");
        mhRecordFolder43 = rmService.createRecordFolder(mhContainer33, "mhFolder43");
        mhRecordFolder44 = rmService.createRecordFolder(mhContainer34, "mhFolder44");
        mhRecordFolder45 = rmService.createRecordFolder(mhContainer35, "mhFolder45");        
    }
}
