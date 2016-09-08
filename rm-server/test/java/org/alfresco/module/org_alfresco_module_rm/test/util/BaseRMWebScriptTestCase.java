/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RmSiteType;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public class BaseRMWebScriptTestCase extends BaseWebScriptTest
{
    /** Collab site id */
    protected static final String COLLAB_SITE_ID = "myCollabSite";

	/** Common test utils */
	protected CommonRMTestUtils utils;

	/** Application context */
	protected ApplicationContext applicationContext;

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
    protected TaggingService taggingService;

    /** RM Services */
    protected RecordsManagementService rmService;
    protected DispositionService dispositionService;
    protected RecordsManagementEventService eventService;
    protected RecordsManagementAdminService adminService;
    protected RecordsManagementActionService actionService;
    protected RecordsManagementSearchService rmSearchService;
    protected FilePlanRoleService filePlanRoleService;
    protected FilePlanPermissionService filePlanPermissionService;
    protected RecordsManagementAuditService auditService;
    protected CapabilityService capabilityService;
    protected VitalRecordService vitalRecordService;
    protected FilePlanService filePlanService;

    /** test data */
    protected String siteId;
    protected StoreRef storeRef;
    protected NodeRef rootNodeRef;
    protected SiteInfo siteInfo;
    protected NodeRef folder;
    protected NodeRef filePlan;
    protected NodeRef recordSeries;			// A category with no disposition schedule
    protected NodeRef recordCategory;
    protected DispositionSchedule dispositionSchedule;
    protected NodeRef recordFolder;
    protected NodeRef recordFolder2;

    /**
     * Indicates whether the test collaboration site should be created
     * or not.
     */
    protected boolean isCollaborationSiteTest()
    {
        return false;
    }

    @Override
    protected void setUp() throws Exception
    {
    	super.setUp();

        // Initialise the service beans
        initServices();

        // Setup test data
        setupTestData();
    }

    /**
     * Initialise the service beans.
     */
    protected void initServices()
    {
    	applicationContext = getServer().getApplicationContext();

    	// Common test utils
    	utils = new CommonRMTestUtils(applicationContext);

        // Get services
        nodeService = (NodeService)applicationContext.getBean("NodeService");
        contentService = (ContentService)applicationContext.getBean("ContentService");
        retryingTransactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
        namespaceService = (NamespaceService)applicationContext.getBean("NamespaceService");
        searchService = (SearchService)applicationContext.getBean("SearchService");
        policyComponent = (PolicyComponent)applicationContext.getBean("policyComponent");
        dictionaryService = (DictionaryService)applicationContext.getBean("DictionaryService");
        siteService = (SiteService)applicationContext.getBean("SiteService");
        authorityService = (AuthorityService)applicationContext.getBean("AuthorityService");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        personService = (PersonService)applicationContext.getBean("PersonService");
        transactionService = (TransactionService)applicationContext.getBean("TransactionService");
        taggingService = (TaggingService)applicationContext.getBean("TaggingService");

        // Get RM services
        rmService = (RecordsManagementService)applicationContext.getBean("RecordsManagementService");
        dispositionService = (DispositionService)applicationContext.getBean("DispositionService");
        eventService = (RecordsManagementEventService)applicationContext.getBean("RecordsManagementEventService");
        adminService = (RecordsManagementAdminService)applicationContext.getBean("RecordsManagementAdminService");
        actionService = (RecordsManagementActionService)applicationContext.getBean("RecordsManagementActionService");
        rmSearchService = (RecordsManagementSearchService)applicationContext.getBean("RecordsManagementSearchService");
        filePlanRoleService = (FilePlanRoleService)applicationContext.getBean("FilePlanRoleService");
        filePlanPermissionService = (FilePlanPermissionService)applicationContext.getBean("FilePlanPermissionService");
        auditService = (RecordsManagementAuditService)applicationContext.getBean("RecordsManagementAuditService");
        capabilityService = (CapabilityService)applicationContext.getBean("CapabilityService");
        vitalRecordService = (VitalRecordService)applicationContext.getBean("VitalRecordService");
        filePlanService = (FilePlanService)applicationContext.getBean("FilePlanService");
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
        siteService.deleteSite(siteId);

        // Delete the collaboration site (if required)
        if (isCollaborationSiteTest() == true)
        {
            siteService.deleteSite(COLLAB_SITE_ID);
        }
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

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_ADMIN, "rmadmin");

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
        siteId = GUID.generate();
        siteInfo = siteService.createSite("rm-site-dashboard", siteId, "title", "descrition", SiteVisibility.PUBLIC, RecordsManagementModel.TYPE_RM_SITE);
        filePlan = siteService.getContainer(siteId, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);
        assertNotNull("Site document library container was not created successfully.", filePlan);

        recordSeries = filePlanService.createRecordCategory(filePlan, "recordSeries");
        assertNotNull("Could not create record category with no disposition schedule", recordSeries);

        recordCategory = filePlanService.createRecordCategory(recordSeries, "rmContainer");
        assertNotNull("Could not create record category", recordCategory);

        // Make vital record
        vitalRecordService.setVitalRecordDefintion(recordCategory, true, new Period("week|1"));

        // Create disposition schedule
        dispositionSchedule = utils.createBasicDispositionSchedule(recordCategory);

        // Create RM folder
        recordFolder = rmService.createRecordFolder(recordCategory, "rmFolder");
        assertNotNull("Could not create rm folder", recordFolder);
        recordFolder2 = rmService.createRecordFolder(recordCategory, "rmFolder2");
        assertNotNull("Could not create rm folder 2", recordFolder2);

        // Create collaboration data
        if (isCollaborationSiteTest() == true)
        {
            setupCollaborationSiteTestData();
        }
    }

    protected void setupCollaborationSiteTestData()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                setupCollaborationSiteTestDataImpl();

                return null;
            }
        });
    }

    protected void setupCollaborationSiteTestDataImpl()
    {
        // create collaboration site
        siteService.createSite("preset", COLLAB_SITE_ID, "title", "description", SiteVisibility.PRIVATE);
        NodeRef documentLibrary = SiteServiceImpl.getSiteContainer(
                COLLAB_SITE_ID,
                SiteService.DOCUMENT_LIBRARY,
                true,
                siteService,
                transactionService,
                taggingService);

        assertNotNull("Collaboration site document library component was not successfully created.", documentLibrary);
    }

    protected void createUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);
        }
    }

    protected void deleteUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == true)
        {
            personService.deletePerson(userName);
        }
    }

    protected void createGroup(String groupName)
    {
        if (authorityService.authorityExists(groupName) == false)
        {
            authorityService.createAuthority(AuthorityType.GROUP, groupName);
        }
    }

    protected void deleteGroup(String groupName)
    {
        if (authorityService.authorityExists(groupName) == true)
        {
            authorityService.deleteAuthority(groupName, true);
        }
    }
}
