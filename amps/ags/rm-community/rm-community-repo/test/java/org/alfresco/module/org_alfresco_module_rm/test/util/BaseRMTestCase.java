/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dataset.DataSetService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType;
import org.alfresco.module.org_alfresco_module_rm.record.InplaceRecordService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.util.RMContainerCacheManager;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.DocumentLinkService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.version.VersionService;
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
                                     implements RecordsManagementModel, ContentModel, RMPermissionModel
{
    /** Application context */
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            "classpath:alfresco/application-context.xml",
            "classpath:alfresco/web-scripts-application-context.xml",
            "classpath:test-context.xml"
        };
    }
    protected ApplicationContext applicationContext;

    /** test model constants */
    protected String URI = "http://www.alfresco.org/model/rmtest/1.0";
    protected String PREFIX = "rmt";
    protected QName TYPE_CUSTOM_TYPE = QName.createQName(URI, "customType");
    protected QName ASPECT_CUSTOM_ASPECT = QName.createQName(URI, "customAspect");
    protected QName ASPECT_RECORD_META_DATA = QName.createQName(URI, "recordMetaData");

    /** test data */
    protected String NAME_DM_DOCUMENT = "collabDocument.txt";
    protected String NAME_DM_DOCUMENT1 = "collabDocument1.txt";

    /** admin user */
    protected static final String ADMIN_USER = "admin";

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
    protected FileFolderService fileFolderService;
    protected PermissionService permissionService;
    protected TaggingService taggingService;
    protected ActionService actionService;
    protected OwnableService ownableService;
    protected VersionService versionService;
    protected DocumentLinkService documentLinkService;
    protected Repository repositoryHelper;

    /** RM Services */
    protected DispositionService dispositionService;
    protected RecordsManagementEventService rmEventService;
    protected RecordsManagementAdminService rmAdminService;
    protected RecordsManagementActionService rmActionService;
    protected RecordsManagementSearchService rmSearchService;
    protected FilePlanRoleService filePlanRoleService;
    protected FilePlanPermissionService filePlanPermissionService;
    protected CapabilityService capabilityService;
    protected VitalRecordService vitalRecordService;
    protected DataSetService dataSetService;
    protected FreezeService freezeService;
    protected RecordService recordService;
    protected FilePlanService filePlanService;
    protected RecordFolderService recordFolderService;
    protected ExtendedSecurityService extendedSecurityService;
    protected ReportService reportService;
    protected RecordsManagementAuditService rmAuditService;
    protected IdentifierService identifierService;
    protected HoldService holdService;
    protected InplaceRecordService inplaceRecordService;
    protected RelationshipService relationshipService;

    /** RM Container Cache Manager */
    protected RMContainerCacheManager rmContainerCacheManager;

    /** test utils */
    protected UserAndGroupsUtils userAndGroupsUtils;

    /** test data */
    protected String siteId;
    protected StoreRef storeRef;
    protected NodeRef rootNodeRef;
    protected SiteInfo siteInfo;
    protected NodeRef folder;
    protected NodeRef filePlan;
    protected NodeRef rmContainer;
    protected NodeRef rm_Container;
    protected DispositionSchedule dispositionSchedule;
    protected NodeRef rmFolder;
    protected NodeRef rm_Folder;
    protected NodeRef unfiledContainer;
    protected String collabSiteId;
    protected NodeRef holdsContainer;
    protected NodeRef transfersContainer;

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

    /** test records */
    protected NodeRef recordOne;
    protected NodeRef recordTwo;
    protected NodeRef recordThree;
    protected NodeRef recordFour;
    protected NodeRef recordFive;
    protected NodeRef recordDeclaredOne;
    protected NodeRef recordDeclaredTwo;

    /** collaboration site artifacts */
    protected SiteInfo collaborationSite;
    protected NodeRef documentLibrary;
    protected NodeRef dmFolder;
    protected NodeRef dmFolder1;
    protected NodeRef dmDocument;
    protected NodeRef dmDocument1;

    /** collaboration site users */
    protected String dmConsumer;
    protected NodeRef dmConsumerNodeRef;
    protected String dmContributor;
    protected NodeRef dmContributorNodeRef;
    protected String dmCollaborator;
    protected NodeRef dmCollaboratorNodeRef;

    /**
     * Indicates whether this is a RM site test or not.  If true then the test RM site is created along with a basic
     * file plan structure, otherwise not.
     */
    protected boolean isRMSiteTest()
    {
        return true;
    }

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
     */
    protected boolean isUserTest()
    {
        return false;
    }

    /**
     * Indicates whether the test records should be created or not.
     */
    protected boolean isRecordTest()
    {
        return false;
    }

    /**
     * Indicates whether the test users should have filling on the file plan structure
     * by default or not.
     */
    protected boolean isFillingForAllUsers()
    {
        return false;
    }

    /**
     * Indicates whether the test collaboration site should be created
     * or not.
     */
    protected boolean isCollaborationSiteTest()
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
        applicationContext = ApplicationContextHelper.getApplicationContext(getConfigLocations());
        utils = new CommonRMTestUtils(applicationContext);
        userAndGroupsUtils = new UserAndGroupsUtils(applicationContext);

        // Initialise the service beans
        initServices();

        // Setup test data
        setupTestData();

        // Create multi hierarchy data
        if (isMultiHierarchyTest())
        {
            setupMultiHierarchyTestData();
        }

        // Create collaboration data
        if (isCollaborationSiteTest())
        {
            setupCollaborationSiteTestData();
        }

        // Create the users here
        if (isUserTest())
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
        fileFolderService = (FileFolderService)applicationContext.getBean("FileFolderService");
        permissionService = (PermissionService)applicationContext.getBean("PermissionService");
        taggingService = (TaggingService)applicationContext.getBean("TaggingService");
        actionService = (ActionService)applicationContext.getBean("ActionService");
        ownableService = (OwnableService)applicationContext.getBean("OwnableService");
        versionService = (VersionService)applicationContext.getBean("VersionService");
        documentLinkService = (DocumentLinkService)applicationContext.getBean("DocumentLinkService");
        repositoryHelper = (Repository)applicationContext.getBean("repositoryHelper");

        // Get RM services
        dispositionService = (DispositionService)applicationContext.getBean("DispositionService");
        rmEventService = (RecordsManagementEventService)applicationContext.getBean("RecordsManagementEventService");
        rmAdminService = (RecordsManagementAdminService)applicationContext.getBean("RecordsManagementAdminService");
        rmActionService = (RecordsManagementActionService)this.applicationContext.getBean("RecordsManagementActionService");
        rmSearchService = (RecordsManagementSearchService)this.applicationContext.getBean("RecordsManagementSearchService");
        filePlanRoleService = (FilePlanRoleService)this.applicationContext.getBean("FilePlanRoleService");
        filePlanPermissionService = (FilePlanPermissionService)this.applicationContext.getBean("FilePlanPermissionService");
        capabilityService = (CapabilityService)this.applicationContext.getBean("CapabilityService");
        vitalRecordService = (VitalRecordService)this.applicationContext.getBean("VitalRecordService");
        dataSetService = (DataSetService) applicationContext.getBean("DataSetService");
        freezeService = (FreezeService) applicationContext.getBean("FreezeService");
        recordService = (RecordService) applicationContext.getBean("RecordService");
        filePlanService = (FilePlanService) applicationContext.getBean("FilePlanService");
        recordFolderService = (RecordFolderService) applicationContext.getBean("RecordFolderService");
        extendedSecurityService = (ExtendedSecurityService) applicationContext.getBean("ExtendedSecurityService");
        reportService = (ReportService) applicationContext.getBean("ReportService");
        rmAuditService = (RecordsManagementAuditService) applicationContext.getBean("RecordsManagementAuditService");
        identifierService = (IdentifierService) applicationContext.getBean("recordsManagementIdentifierService");
        holdService = (HoldService) applicationContext.getBean("HoldService");
        inplaceRecordService = (InplaceRecordService) applicationContext.getBean("InplaceRecordService");
        relationshipService = (RelationshipService) applicationContext.getBean("RelationshipService");

        // RM Container Cache Manager
        rmContainerCacheManager = (RMContainerCacheManager) applicationContext.getBean("rmContainerCacheManager");
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
                // Do the tear down
                AuthenticationUtil.runAsSystem(() ->
                {
                    tearDownImpl();
                    return null;
                });

                return null;
            }
        });
    }

    /**
     * Tear down implementation
     */
    protected void tearDownImpl()
    {
        BehaviourFilter filter = (BehaviourFilter)applicationContext.getBean("policyBehaviourFilter");
        filter.disableBehaviour();
        try
        {
            if (filePlan != null && nodeService.exists(filePlan))
            {
                List<NodeRef> holds = holdService.getHolds(filePlan);
                for (NodeRef hold : holds)
                {
                    holdService.deleteHold(hold);
                }
            }

            if (folder != null && nodeService.exists(folder))
            {
                // Delete the folder
                nodeService.deleteNode(folder);
            }

            if (siteId != null && siteService.getSite(siteId) != null)
            {
                // Delete the site
                siteService.deleteSite(siteId);
            }

            // delete the collaboration site (if required)
            if (isCollaborationSiteTest() && siteService.getSite(collabSiteId) != null)
            {
                siteService.deleteSite(collabSiteId);
            }

            if (rmContainerCacheManager != null)
            {
                rmContainerCacheManager.reset();
            }
        }
        finally
        {
            filter.enableBehaviour();
        }
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
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                setupTestDataImpl();

                if (isRecordTest() && isRMSiteTest())
                {
                    setupTestRecords();
                }

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                if (isRMSiteTest())
                {
                    if (isRecordTest())
                    {
                        // declare a record
                        utils.completeRecord(recordDeclaredOne);
                        utils.completeRecord(recordDeclaredTwo);
                    }

                    // unfiled container
                    unfiledContainer = filePlanService.getUnfiledContainer(filePlan);
                    assertNotNull(unfiledContainer);

                    // holds container
                    holdsContainer = filePlanService.getHoldContainer(filePlan);
                    assertNotNull(holdsContainer);

                    // transfers container
                    transfersContainer = filePlanService.getTransferContainer(filePlan);
                    assertNotNull(transfersContainer);
                }
            }
        }, AuthenticationUtil.getAdminUserName());
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
        Map<QName, Serializable> containerProps = new HashMap<>(1);
        containerProps.put(ContentModel.PROP_NAME, containerName);
        folder = nodeService.createNode(
              rootNodeRef,
              ContentModel.ASSOC_CHILDREN,
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, containerName),
              ContentModel.TYPE_FOLDER,
              containerProps).getChildRef();
        assertNotNull("Could not create base folder", folder);

        permissionService.setPermission(folder, ADMIN_USER, PermissionService.WRITE, true);
        permissionService.setPermission(folder, ADMIN_USER, PermissionService.ADD_CHILDREN, true);

        if (isRMSiteTest())
        {
            siteId = GUID.generate();
            siteInfo = siteService.createSite(
                            "rm-site-dashboard",
                            siteId,
                            "title",
                            "descrition",
                            SiteVisibility.PUBLIC,
                            RecordsManagementModel.TYPE_RM_SITE);

            filePlan = siteService.getContainer(siteId, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);
            assertNotNull("Site document library container was not created successfully.", filePlan);

            // Create RM container
            rmContainer = filePlanService.createRecordCategory(filePlan, "rmContainer");
            assertNotNull("Could not create rm container", rmContainer);

            rm_Container = filePlanService.createRecordCategory(filePlan, "rm Container");
            assertNotNull("Could not create rm container", rm_Container);

            // Create disposition schedule
            dispositionSchedule = utils.createBasicDispositionSchedule(rmContainer);

            // Create RM folder
            rmFolder = recordFolderService.createRecordFolder(rmContainer, "rmFolder");
            assertNotNull("Could not create rm folder", rmFolder);

            rm_Folder = recordFolderService.createRecordFolder(rm_Container, "rm Folder");
            assertNotNull("Could not create rm folder", rm_Folder);
        }
    }

    protected void setupTestRecords()
    {
        recordOne = utils.createRecord(rmFolder, "one.txt");
        recordTwo = utils.createRecord(rmFolder, "two.txt");
        recordThree = utils.createRecord(rmFolder, "three.txt");
        recordFour = utils.createRecord(rmFolder, "four.txt");
        recordFive = utils.createRecord(rmFolder, "five.txt");
        recordDeclaredOne = utils.createRecord(rmFolder, "declaredOne.txt");
        recordDeclaredTwo = utils.createRecord(rmFolder, "declaredTwo.txt");
    }

    protected void setupTestUsers(final NodeRef filePlan)
    {
        AuthenticationUtil.runAs(() ->
        {
            retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
            {
                @Override
                public Object execute() throws Throwable
                {
                    setupTestUsersImpl(filePlan);
                    return null;
                }
            });

            return null;
        },
        AuthenticationUtil.getAdminUserName());
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
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_USER, rmUserName);

        powerUserName = GUID.generate();
        powerUserPerson = createPerson(powerUserName);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_POWER_USER, powerUserName);

        securityOfficerName = GUID.generate();
        securityOfficerPerson = createPerson(securityOfficerName);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_SECURITY_OFFICER, securityOfficerName);

        recordsManagerName = GUID.generate();
        recordsManagerPerson = createPerson(recordsManagerName);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, recordsManagerName);

        rmAdminName = GUID.generate();
        rmAdminPerson = createPerson(rmAdminName);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_ADMIN, rmAdminName);

        testUsers = new String[]
        {
                userName,
                rmUserName,
                powerUserName,
                securityOfficerName,
                recordsManagerName,
                rmAdminName
        };

        if (isFillingForAllUsers())
        {
            // Give all the users file permission objects
            for (String user : testUsers)
            {
                filePlanPermissionService.setPermission(filePlan, user, FILING);
                filePlanPermissionService.setPermission(rmContainer, user, FILING);
                filePlanPermissionService.setPermission(rmFolder, user, FILING);
                filePlanPermissionService.setPermission(unfiledContainer, user, FILING);
            }
        }
    }

    /**
     * Util method to create a person.
     * @param userName  user name
     * @return NodeRef  user node reference
     */
    protected NodeRef createPerson(String userName, boolean createAuth)
    {
        if (createAuth)
        {
            authenticationService.createAuthentication(userName, "password".toCharArray());
        }
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_FIRSTNAME, userName);
        return personService.createPerson(properties);
    }

    protected NodeRef createPerson(String userName)
    {
        return createPerson(userName, true);
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
                return AuthenticationUtil.runAsSystem(() ->
                {
                    // Do setup
                    setupMultiHierarchyTestDataImpl();

                    return null;
                });
            }
        });
    }

    /**
     * Impl of multi hierarchy test data
     */
    protected void setupMultiHierarchyTestDataImpl()
    {
        // Create root mh container
        mhContainer = filePlanService.createRecordCategory(filePlan, "mhContainer");

        // Level 1
        mhContainer11 = filePlanService.createRecordCategory(mhContainer, "mhContainer11");
        mhDispositionSchedule11 = utils.createBasicDispositionSchedule(mhContainer11, "ds11", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, false, true);
        mhContainer12 = filePlanService.createRecordCategory(mhContainer, "mhContainer12");
        mhDispositionSchedule12 = utils.createBasicDispositionSchedule(mhContainer12, "ds12", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, false, true);

        // Level 2
        mhContainer21 = filePlanService.createRecordCategory(mhContainer11, "mhContainer21");
        mhContainer22 = filePlanService.createRecordCategory(mhContainer12, "mhContainer22");
        mhContainer23 = filePlanService.createRecordCategory(mhContainer12, "mhContainer23");
        mhDispositionSchedule23 = utils.createBasicDispositionSchedule(mhContainer23, "ds23", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, false, true);

        // Level 3
        mhContainer31 = filePlanService.createRecordCategory(mhContainer21, "mhContainer31");
        mhContainer32 = filePlanService.createRecordCategory(mhContainer22, "mhContainer32");
        mhContainer33 = filePlanService.createRecordCategory(mhContainer22, "mhContainer33");
        mhDispositionSchedule33 = utils.createBasicDispositionSchedule(mhContainer33, "ds33", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, true, true);
        mhContainer34 = filePlanService.createRecordCategory(mhContainer23, "mhContainer34");
        mhContainer35 = filePlanService.createRecordCategory(mhContainer23, "mhContainer35");
        mhDispositionSchedule35 = utils.createBasicDispositionSchedule(mhContainer35, "ds35", CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, true, true);

        // Record folders
        mhRecordFolder41 = recordFolderService.createRecordFolder(mhContainer31, "mhFolder41");
        mhRecordFolder42 = recordFolderService.createRecordFolder(mhContainer32, "mhFolder42");
        mhRecordFolder43 = recordFolderService.createRecordFolder(mhContainer33, "mhFolder43");
        mhRecordFolder44 = recordFolderService.createRecordFolder(mhContainer34, "mhFolder44");
        mhRecordFolder45 = recordFolderService.createRecordFolder(mhContainer35, "mhFolder45");
    }

    protected void setupCollaborationSiteTestData()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                setupCollaborationSiteTestDataImpl();
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    protected void setupCollaborationSiteTestDataImpl()
    {
        // create collaboration site
        collabSiteId = GUID.generate();
        collaborationSite = siteService.createSite("site-dashboard", collabSiteId, "title", "description", SiteVisibility.PRIVATE);
        documentLibrary = SiteServiceImpl.getSiteContainer(
                collabSiteId,
                SiteService.DOCUMENT_LIBRARY,
                true,
                siteService,
                transactionService,
                taggingService);

        assertNotNull("Collaboration site document library component was not successfully created.", documentLibrary);

        // create a folder and documents
        dmFolder = fileFolderService.create(documentLibrary, "collabFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        dmDocument = fileFolderService.create(dmFolder, NAME_DM_DOCUMENT, ContentModel.TYPE_CONTENT).getNodeRef();
        dmFolder1 = fileFolderService.create(documentLibrary, "collabFolder1", ContentModel.TYPE_FOLDER).getNodeRef();
        dmDocument1 = fileFolderService.create(dmFolder1, NAME_DM_DOCUMENT1, ContentModel.TYPE_CONTENT).getNodeRef();

        dmConsumer = GUID.generate();
        dmConsumerNodeRef = createPerson(dmConsumer);
        siteService.setMembership(collabSiteId, dmConsumer, SiteModel.SITE_CONSUMER);

        dmContributor = GUID.generate();
        dmContributorNodeRef = createPerson(dmContributor);
        siteService.setMembership(collabSiteId, dmContributor, SiteModel.SITE_CONTRIBUTOR);

        dmCollaborator = GUID.generate();
        dmCollaboratorNodeRef = createPerson(dmCollaborator);
        siteService.setMembership(collabSiteId, dmCollaborator, SiteModel.SITE_COLLABORATOR);
    }

    /**
     * Override to ensure the tests are run as the 'admin' user by default.
     */

    @Override
    protected <A> A doTestInTransaction(Test<A> test)
    {
        return super.doTestInTransaction(test, AuthenticationUtil.getAdminUserName());
    }

    @Override
    protected void doTestInTransaction(FailureTest test)
    {
        super.doTestInTransaction(test, AuthenticationUtil.getAdminUserName());
    }

    /**
     * Helper class to try and simplify {@link Void} tests.
     *
     * @author Roy Wetherall
     * @since 2.1
     */
    protected abstract class VoidTest extends Test<Void>
    {
        @Override
        public Void run() throws Exception
        {
            runImpl();
            return null;
        }

        public abstract void runImpl() throws Exception;

        @Override
        public void test(Void result) throws Exception
        {
            testImpl();
        }

        public void testImpl() throws Exception
        {
            // empty implementation
        }
    }

    /**
     * Execute behaviour driven test.
     *
     * @param test
     */
    protected void doBehaviourDrivenTest(BehaviourDrivenTest test)
    {
        try
        {
            test.run();
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Behaviour driven test.
     *
     * @author Roy Wetherall
     * @since 2.2
     */
    protected abstract class BehaviourDrivenTest
    {
        /** run in transaction */
        protected boolean runInTransactionTests = true;

        /** run as user */
        protected String runAsUser = AuthenticationUtil.getAdminUserName();

        /** expected exception */
        protected Class<?> expectedException;

        /**
         * Default constructor
         */
        public BehaviourDrivenTest()
        {
        }

        public BehaviourDrivenTest(Class<?> expectedException)
        {
            this.expectedException = expectedException;
        }

        public BehaviourDrivenTest(Class<?> expectedException, String runAsUser)
        {
            this.expectedException = expectedException;
            this.runAsUser = runAsUser;
        }

        public BehaviourDrivenTest(String runAsUser)
        {
            this.runAsUser = runAsUser;
        }

        public BehaviourDrivenTest(String runAsUser, boolean runInTransactionTests)
        {
            this.runInTransactionTests = runInTransactionTests;
            if (runAsUser != null)
            {
                this.runAsUser = runAsUser;
            }
        }

        public BehaviourDrivenTest(Class<?> expectedException, String runAsUser, boolean runInTransactionTests)
        {
            this.expectedException = expectedException;
            this.runAsUser = runAsUser;
            this.runInTransactionTests = runInTransactionTests;
        }

        public void given() throws Exception { /** empty implementation */ }

        public void when() throws Exception  { /** empty implementation */ }

        public void then() throws Exception  { /** empty implementation */ }

        public void after() throws Exception { /** empty implementation */ }
        
        public void run() throws Exception
        {
            try
            {
                if (runInTransactionTests)
                {
                    doTestInTransaction(new VoidTest()
                    {
                        @Override
                        public void runImpl() throws Exception
                        {
                           given();
                        }
                    }, runAsUser);
                }
                else
                {
                    given();
                }

                if (expectedException == null)
                {
                    if (runInTransactionTests)
                    {
                        doTestInTransaction(new VoidTest()
                        {
                            @Override
                            public void runImpl() throws Exception
                            {
                                when();
                            }
                        }, runAsUser);

                        doTestInTransaction(new VoidTest()
                        {
                            @Override
                            public void runImpl() throws Exception
                            {
                                then();
                            }
                        }, runAsUser);
                    }
                    else
                    {
                        when();
                        then();
                    }
                }
                else
                {
                    doTestInTransaction(new FailureTest(expectedException)
                    {
                        @Override
                        public void run() throws Exception
                        {
                            when();
                        }
                    }, runAsUser);
                }
            }
            finally
            {
                if (runInTransactionTests)
                {
                    doTestInTransaction(new VoidTest()
                    {
                        @Override
                        public void runImpl() throws Exception
                        {
                           after();
                        }
                    }, runAsUser);
                }
                else
                {
                    after();
                }
            }
        }
    }
}
