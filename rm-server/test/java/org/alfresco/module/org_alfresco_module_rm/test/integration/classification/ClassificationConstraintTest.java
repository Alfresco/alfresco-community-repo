/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Integration tests that require the constraints to be executed by a user other than Admin.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class ClassificationConstraintTest implements RMPermissionModel
{
    /* test data */
    private static final String CLASSIFICATION_REASON = "Test Reason 1";
    private static final String CLASSIFIED_BY = "classified by text";
    private static final String RECORD_NAME = "recordname.txt";

    /* Application context */
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            "classpath:alfresco/application-context.xml",
            "classpath:test-context.xml"
        };
    }
    private ApplicationContext applicationContext;

    /** Common test utils */
    private CommonRMTestUtils utils;

    /* Services */
    private NodeService nodeService;
    private MutableAuthenticationService authenticationService;
    private SiteService siteService;
    private PersonService personService;

    /* RM Services */
    private FilePlanRoleService filePlanRoleService;
    private FilePlanPermissionService filePlanPermissionService;
    private FilePlanService filePlanService;
    private RecordFolderService recordFolderService;
    private SecurityClearanceService securityClearanceService;
    private ContentClassificationService contentClassificationService;

    /* test data */
    private String siteId;
    private NodeRef filePlan;
    private NodeRef rmContainer;
    private NodeRef rmFolder;

    /** Load the application context and create the test data. */
    @Before
    public void setUp() throws Exception
    {
        // Get the application context
        applicationContext = ApplicationContextHelper.getApplicationContext(getConfigLocations());
        utils = new CommonRMTestUtils(applicationContext);

        // Initialise the service beans
        initServices();

        // Setup test data
        setupTestData();
    }

    /** Initialise the service beans. */
    protected void initServices()
    {
        // Get services
        nodeService = (NodeService)applicationContext.getBean("NodeService");
        siteService = (SiteService)this.applicationContext.getBean("SiteService");
        authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("AuthenticationService");
        personService = (PersonService)this.applicationContext.getBean("PersonService");

        // Get RM services
        filePlanRoleService = (FilePlanRoleService)this.applicationContext.getBean("FilePlanRoleService");
        filePlanPermissionService = (FilePlanPermissionService)this.applicationContext.getBean("FilePlanPermissionService");
        filePlanService = (FilePlanService) applicationContext.getBean("FilePlanService");
        recordFolderService = (RecordFolderService) applicationContext.getBean("RecordFolderService");
        securityClearanceService = (SecurityClearanceService) applicationContext.getBean("SecurityClearanceService");
        contentClassificationService = (ContentClassificationService) applicationContext.getBean("ContentClassificationService");
    }

    /** Setup test data for tests */
    protected void setupTestData()
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                AuthorityDAO authDao = (AuthorityDAO)applicationContext.getBean("authorityDAO");
                if (!authDao.authorityExists(AuthenticationUtil.getSystemUserName()))
                {
                    createPerson(AuthenticationUtil.getSystemUserName(), false);
                }
                assertTrue("No person object for System available.", authDao.authorityExists(AuthenticationUtil.getSystemUserName()));

                siteId = GUID.generate();
                siteService.createSite(
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

                // Create RM folder
                rmFolder = recordFolderService.createRecordFolder(rmContainer, "rmFolder");
                assertNotNull("Could not create rm folder", rmFolder);

                return null;
            }
        });
    }

    /**
     * Mid-level user further downgrading a downgraded record.
     * <p>
     * <a href="https://issues.alfresco.com/jira/browse/RM-2502">RM-2502</a><pre>
     * Given I have secret clearance
     * And a record has an initial classification of top secret
     * And the record has a current classification of secret
     * When I try to downgrade the record to confidential
     * Then I am successful.
     * </pre>
     */
    @Test
    public void testInitialClassificationConstraint()
    {
        // Given I set up some test data (admin at TS, midLevelUser at S and a new record).
        final String midLevelUser = GUID.generate();
        final NodeRef record = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork()
            {
                // Ensure admin is TS cleared.
                securityClearanceService.setUserSecurityClearance(AuthenticationUtil.getAdminUserName(), "TS");
                // Create user with S clearance.
                createPerson(midLevelUser, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, midLevelUser);
                filePlanPermissionService.setPermission(rmContainer, midLevelUser, FILING);
                securityClearanceService.setUserSecurityClearance(midLevelUser, "S");
                // Create a record to be classified during the test.
                return utils.createRecord(rmFolder, RECORD_NAME);
            }
        });

        // And admin creates a downgraded record.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                // Create a TS record and downgrade it to S.
                classifyAs(record, "TS");
                classifyAs(record, "S");

                assertTrue("Record should have been classified.",
                            nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
                assertEquals("Record have initial classification of 'TS'.", "TS",
                            nodeService.getProperty(record, ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION));
                assertEquals("Record should be 'S' classified.", "S",
                            nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

        // When the mid-level user downgrades the record to C.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                // Check that the mid-clearance user can further downgrade the classification (even though the initial
                // classification was above their clearance).
                classifyAs(record, "C");
                return null;
            }
        }, midLevelUser);

        // Then the record is classified at C (with initial classification TS).
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                assertTrue("Record should still be classified.",
                            nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
                assertEquals("Record have initial classification of 'TS'.", "TS",
                            nodeService.getProperty(record, ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION));
                assertEquals("Record should be 'C' classified.", "C",
                            nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    /**
     * Util method to create a person.
     *
     * @param userName user name
     * @param createAuth Whether to give the user a password or not.
     * @return NodeRef user node reference
     */
    private NodeRef createPerson(String userName, boolean createAuth)
    {
        if (createAuth)
        {
            authenticationService.createAuthentication(userName, "password".toCharArray());
        }
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return personService.createPerson(properties);
    }

    /**
     * Classify the given node.
     *
     * @param node The node to classify.
     * @param level The id of the classification level to use.
     */
    private void classifyAs(final NodeRef node, final String level)
    {
        ClassificationAspectProperties propertiesDTO = new ClassificationAspectProperties();
        propertiesDTO.setClassificationLevelId(level);
        propertiesDTO.setClassifiedBy(CLASSIFIED_BY);
        propertiesDTO.setClassificationReasonIds(Collections.singleton(CLASSIFICATION_REASON));
        contentClassificationService.editClassifiedContent(propertiesDTO, node);
    }
}
