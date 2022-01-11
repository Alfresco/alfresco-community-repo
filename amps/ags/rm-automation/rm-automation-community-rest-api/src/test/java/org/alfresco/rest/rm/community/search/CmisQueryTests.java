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

package org.alfresco.rest.rm.community.search;

import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;


import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test to check that RM doesn't break CMIS query
 *
 * @author jcule, Rodica Sutu
 * @since 2.5.4
 * @since 3.3
 */
public class CmisQueryTests extends BaseRMRestTest
{
    private static final String SEARCH_TERM = generateTestPrefix(CmisQueryTests.class);
    private static final String sqlWithName =
            "SELECT cmis:name FROM cmis:document where CONTAINS('cmis:name:*" + SEARCH_TERM + "*')";

    private SiteModel collaborationSite;
    private UserModel nonRMUser, rmUser;
    private RecordCategoryChild recordFolder;

    @Autowired
    private ContentActions contentActions;
    @Autowired
    private RoleService roleService;

    /**
     * Create  some test data:
     * <pre>
     *     - a collaboration site with documents
     *     - in place records
     *     - category with folder and records
     *     - a user with no rm rights (no rights to see the record from file plan)
     *     - a user with rights to see the records and the other documents created
     * </pre>
     */
    @BeforeClass (alwaysRun = true)
    public void setupCmisQuery() throws Exception
    {
        STEP("Create a collaboration site");
        collaborationSite = dataSite.usingAdmin().createPrivateRandomSite();

        STEP("Create 10 documents ending with SEARCH_TERM");
        for (int i = 0; ++i <= 10; )
        {
            FileModel fileModel = new FileModel(String.format("%s%s%s.%s", "Doc", i, SEARCH_TERM,
                    FileType.TEXT_PLAIN.extension));
            dataContent.usingAdmin().usingSite(collaborationSite).createContent(fileModel);
        }

        STEP("Create a collaborator user for the collaboration site");
        nonRMUser = getDataUser().createRandomTestUser();
        getDataUser().addUserToSite(nonRMUser, collaborationSite, UserRole.SiteCollaborator);

        STEP("Create 10 documents and declare as records");
        for (int i = 0; ++i <= 10; )
        {
            FileModel fileModel = new FileModel(String.format("%s%s%s.%s", "InPlace ", SEARCH_TERM, i,
                    FileType.TEXT_PLAIN.extension));
            fileModel = dataContent.usingUser(nonRMUser).usingSite(collaborationSite).createContent(fileModel);
            getRestAPIFactory().getFilesAPI(nonRMUser).declareAsRecord(fileModel.getNodeRefWithoutVersion());
        }

        STEP("Create record folder and some records ");
        recordFolder = createCategoryFolderInFilePlan();
        for (int i = 0; ++i <= 10; )
        {
            createElectronicRecord(recordFolder.getId(), String.format("%s%s%s.%s", "Record ", SEARCH_TERM, i,
                    FileType.TEXT_PLAIN.extension));
        }
        STEP("Create an rm user with read permission over the category created and contributor role within the " +
                "collaboration site");
        rmUser = roleService.createUserWithSiteRoleRMRoleAndPermission(collaborationSite, UserRole.SiteContributor,
                recordFolder.getParentId(), ROLE_RM_MANAGER, UserPermissions.PERMISSION_READ_RECORDS);

        //do a cmis query to wait for solr indexing
        Utility.sleep(5000, 80000, () ->
        {
            ItemIterable<QueryResult> results =
                    contentActions.getCMISSession(getAdminUser().getUsername(), getAdminUser().getPassword()).query(sqlWithName,
                            false);
            assertEquals("Total number of items is not 30, got  " + results.getTotalNumItems() + " total items",
                    30, results.getTotalNumItems());
        });
    }

    /**
     * <pre>
     * Given the RM site created
     * When I execute a cmis query to get all the documents names
     * Then I get all documents names 100 per page
     * </pre>
     */
    @Test
    @AlfrescoTest (jira = "MNT-19442")
    public void getAllDocumentsNamesCmisQuery()
    {
        // execute the cmis query
        String cq = "SELECT cmis:name FROM cmis:document";
        ItemIterable<QueryResult> results =
                contentActions.getCMISSession(getAdminUser().getUsername(), getAdminUser().getPassword()).query(cq,
                        false);

        // check the total number of items is greater than 100 and has more items is true
        assertTrue("Has more items not true.", results.getHasMoreItems());
        assertTrue("Total number of items is not greater than 100. Total number of items received" + results.getTotalNumItems(),
                results.getTotalNumItems() > 100);
        assertEquals("Expected 100 items per page and got " + results.getPageNumItems() + " per page.", 100,
                results.getPageNumItems());
    }

    /**
     * <pre>
     * Given the RM site created
     * When I execute a cmis query to get all the documents names with a particular name
     * Then I get all documents names user has permission
     * </pre>
     */
    @Test
    @AlfrescoTest (jira = "MNT-19442")
    public void getDocumentsWithSpecificNamesCmisQuery()
    {
        // execute the cmis query
        ItemIterable<QueryResult> results =
                contentActions.getCMISSession(nonRMUser.getUsername(), nonRMUser.getPassword()).query(sqlWithName,
                        false);
        assertEquals("Total number of items is not 20, got  " + results.getTotalNumItems() + " total items",
                20, results.getTotalNumItems());
        // check the has more items is false
        assertFalse("Has more items not false.", results.getHasMoreItems());
        assertEquals("Expected 20 items per page and got " + results.getPageNumItems() + " per page.", 20,
                results.getPageNumItems());
    }

    /**
     * <pre>
     * Given the RM site created
     * When I execute a cmis query to get all the documents names with a specific number per page
     * Then I get all documents names paged as requested that the user has permission
     * </pre>
     */
    @Test
    @AlfrescoTest (jira = "MNT-19442")
    public void getDocumentsCmisQueryWithPagination()
    {
        OperationContext oc = new OperationContextImpl();
        oc.setMaxItemsPerPage(10);
        ItemIterable<QueryResult> results =
                contentActions.getCMISSession(rmUser.getUsername(), rmUser.getPassword()).query(sqlWithName,
                        false, oc);

        // check the total number of items and has more items is true
        assertTrue("Has more items not true. ", results.getHasMoreItems());
        assertEquals("Total number of items is not 30, got " + results.getTotalNumItems(), 30,
                results.getTotalNumItems());
        assertEquals("Expected 10 items per page and got " + results.getPageNumItems() + " per page.",
                10, results.getPageNumItems());
    }

    @AfterClass
    private void clearCmisQueryTests()
    {
        dataSite.usingAdmin().deleteSite(collaborationSite);
        deleteRecordCategory(recordFolder.getParentId());
        getDataUser().usingAdmin().deleteUser(rmUser);
        getDataUser().usingAdmin().deleteUser(nonRMUser);
    }
}
