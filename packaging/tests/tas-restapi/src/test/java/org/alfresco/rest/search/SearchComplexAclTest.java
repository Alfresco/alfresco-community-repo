/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.rest.search;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataGroup;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.UserModel;

/**
 * Migration test class for complex ACL structures on Elasticsearch.
 */
public class SearchComplexAclTest extends AbstractSearchServicesE2ETest
{
    @Autowired
    protected DataGroup dataGroup;

    private UserModel groupUser;
    private UserModel secondGroupUser;
    private UserModel individualUser;
    private UserModel outsiderUser;
    private FolderModel folderA;
    private FileModel groupProtectedFile;
    private FileModel mixedPermsFile;
    private FileModel inheritedProtectedFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        groupUser = dataUser.createRandomTestUser("GroupMemberUser");
        secondGroupUser = dataUser.createRandomTestUser("SecondGroupMemberUser");
        individualUser = dataUser.createRandomTestUser("IndividualUser");
        outsiderUser = dataUser.createRandomTestUser("OutsiderUser");

        GroupModel authorizedGroup = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(authorizedGroup, groupUser);
        dataGroup.addListOfUsersToGroup(authorizedGroup, secondGroupUser);

        dataUser.addUserToSite(groupUser, testSite, UserRole.SiteContributor);
        dataUser.addUserToSite(secondGroupUser, testSite, UserRole.SiteContributor);
        dataUser.addUserToSite(individualUser, testSite, UserRole.SiteContributor);
        dataUser.addUserToSite(outsiderUser, testSite, UserRole.SiteContributor);

        folderA = dataContent.usingUser(testUser).usingSite(testSite).createFolderCmisApi("acl-folder-a");
        FolderModel folderB = dataContent.usingUser(testUser).usingSite(testSite).createFolderCmisApi("acl-folder-b");

        groupProtectedFile = new FileModel("group-protected-file.txt", FileType.TEXT_PLAIN, "Group ACL protected");
        mixedPermsFile = new FileModel("mixed-perms-file.txt", FileType.TEXT_PLAIN, "Mixed ALLOW/DENY ACL");
        inheritedProtectedFile = new FileModel("inherited-protected-file.txt", FileType.TEXT_PLAIN, "Inherits parent ACL");

        dataContent.usingUser(testUser).usingResource(folderA).createContent(groupProtectedFile);
        dataContent.usingUser(testUser).usingResource(folderB).createContent(mixedPermsFile);

        JsonObject folderAPerm = Json.createObjectBuilder()
                .add("permissions", Json.createObjectBuilder()
                        .add("isInheritanceEnabled", false)
                        .add("locallySet", Json.createObjectBuilder()
                                .add("authorityId", "GROUP_" + authorizedGroup.getGroupIdentifier())
                                .add("name", "SiteContributor")
                                .add("accessStatus", "ALLOWED")))
                .build();
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(folderA).updateNode(folderAPerm.toString());
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(groupProtectedFile).updateNode(folderAPerm.toString());

        dataContent.usingUser(testUser).usingResource(folderA).createContent(inheritedProtectedFile);

        JsonObject mixedPerm = Json.createObjectBuilder()
                .add("permissions", Json.createObjectBuilder()
                        .add("isInheritanceEnabled", false)
                        .add("locallySet", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("authorityId", individualUser.getUsername())
                                        .add("name", "SiteContributor")
                                        .add("accessStatus", "ALLOWED"))
                                .add(Json.createObjectBuilder()
                                        .add("authorityId", outsiderUser.getUsername())
                                        .add("name", "SiteContributor")
                                        .add("accessStatus", "DENIED"))))
                .build();
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(mixedPermsFile).updateNode(mixedPerm.toString());

        waitForMetadataIndexing(groupProtectedFile.getName(), true);
        waitForMetadataIndexing(mixedPermsFile.getName(), true);
        waitForMetadataIndexing(inheritedProtectedFile.getName(), true);
    }

    @Test(priority = 1)
    public void testGroupBasedPermissions()
    {
        SearchResponse groupMember = queryAsUser(groupUser, "cm:name:'" + groupProtectedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(groupMember.getPagination().getCount(), 1,
                "Group member should find the group-protected file");

        SearchResponse outsider = queryAsUser(outsiderUser, "cm:name:'" + groupProtectedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(outsider.getPagination().getCount(), 0,
                "Non-group user should NOT find the group-protected file");
    }

    @Test(priority = 2)
    public void testInheritedGroupPermissionOnFolder()
    {
        SearchResponse groupMember = queryAsUser(groupUser, "cm:name:'" + inheritedProtectedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(groupMember.getPagination().getCount(), 1,
                "Group member should find the file that inherits the folder ACL");

        SearchResponse outsider = queryAsUser(outsiderUser, "cm:name:'" + inheritedProtectedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(outsider.getPagination().getCount(), 0,
                "Non-group user should not find the inherited-permission file");
    }

    @Test(priority = 3)
    public void testMixedAllowDenyOnSameNode()
    {
        SearchResponse allowed = queryAsUser(individualUser, "cm:name:'" + mixedPermsFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(allowed.getPagination().getCount(), 1,
                "Explicitly ALLOWED user should find the file");

        SearchResponse denied = queryAsUser(outsiderUser, "cm:name:'" + mixedPermsFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(denied.getPagination().getCount(), 0,
                "Explicitly DENIED user should not find the file");
    }

    @Test(priority = 4)
    public void testAdminSeesAllRegardlessOfAcl()
    {
        SearchResponse groupProtected = queryAsUser(dataUser.getAdminUser(),
                "cm:name:'" + groupProtectedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(groupProtected.getPagination().getCount() >= 1,
                "Admin should find the group-protected file regardless of ACL");

        SearchResponse mixedPerms = queryAsUser(dataUser.getAdminUser(),
                "cm:name:'" + mixedPermsFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(mixedPerms.getPagination().getCount() >= 1,
                "Admin should also find the mixed-permission file");
    }

    @Test(priority = 5)
    public void testMultipleUsersInGroupSeeSameFile()
    {
        SearchResponse firstMember = queryAsUser(groupUser, "cm:name:'" + groupProtectedFile.getName() + "'");
        SearchResponse secondMember = queryAsUser(secondGroupUser, "cm:name:'" + groupProtectedFile.getName() + "'");

        Assert.assertEquals(firstMember.getPagination().getCount(), 1,
                "First group member should find the group-protected file");
        Assert.assertEquals(secondMember.getPagination().getCount(), 1,
                "Second group member should also find the group-protected file");
    }

    @Test(priority = 6)
    public void testNewFileInProtectedFolderInheritsGroupAcl()
    {
        FileModel newlyCreatedFile = new FileModel("newly-created-in-folder-a.txt", FileType.TEXT_PLAIN,
                "Created after ACL was applied");
        dataContent.usingUser(testUser).usingResource(folderA).createContent(newlyCreatedFile);
        waitForMetadataIndexing(newlyCreatedFile.getName(), true);

        SearchResponse groupMember = queryAsUser(groupUser, "cm:name:'" + newlyCreatedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(groupMember.getPagination().getCount(), 1,
                "Group member should find the newly created file inheriting the folder ACL");

        SearchResponse outsider = queryAsUser(outsiderUser, "cm:name:'" + newlyCreatedFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(outsider.getPagination().getCount(), 0,
                "Non-group user should not find the newly created file inheriting the folder ACL");
    }
}
