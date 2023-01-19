/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.categories;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import javax.json.Json;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestCategoryLinkBodyModel;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ListCategoriesForNodeTests extends CategoriesRestTest
{

    private SiteModel site;
    private FolderModel folder;
    private FileModel file;
    private RestCategoryModel category;

    @BeforeClass(alwaysRun = true)
    @Override
    public void dataPreparation()
    {
        STEP("Create user and a site");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp()
    {
        STEP("Create a folder, file in it and a category under root");
        folder = dataContent.usingUser(user).usingSite(site).createFolder();
        file = dataContent.usingUser(user).usingResource(folder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        category = prepareCategoryUnderRoot();
    }

    /**
     * Get one linked category using file
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListSingleCategoryForNode_usingFile()
    {
        STEP("Link file to category");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkModelWithId(category.getId());
        final RestCategoryModel linkedCategory = restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        STEP("Get linked category");
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getLinkedCategories();

        restClient.assertStatusCodeIs(OK);
        linkedCategories.assertThat().entriesListCountIs(1);
        linkedCategories.getEntries().get(0).onModel().assertThat().isEqualTo(linkedCategory);
    }

    /**
     * Get one linked category using folder
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListSingleCategoryForNode_usingFolder()
    {
        STEP("Link folder to category");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkModelWithId(category.getId());
        final RestCategoryModel linkedCategory = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).linkToCategory(categoryLink);

        STEP("Get linked category");
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).getLinkedCategories();

        restClient.assertStatusCodeIs(OK);
        linkedCategories.assertThat().entriesListCountIs(1);
        linkedCategories.getEntries().get(0).onModel().assertThat().isEqualTo(linkedCategory);
    }

    /**
     * Get multiple linked categories using file
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListMultipleCategoriesForNode_usingFile()
    {
        STEP("Create multiple categories under root");
        final List<RestCategoryModel> createdCategories = prepareCategoriesUnderRoot(10);

        STEP("Link file to created categories");
        final List<RestCategoryLinkBodyModel> categoryLinkModels = createdCategories.stream()
            .map(RestCategoryModel::getId)
            .map(this::createCategoryLinkModelWithId)
            .collect(Collectors.toList());
        final List<RestCategoryModel> createdCategoryLinks = restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategories(
            categoryLinkModels
        ).getEntries();

        STEP("Get categories which are linked from file and compare them to created category links");
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getLinkedCategories();

        restClient.assertStatusCodeIs(OK);
        linkedCategories.assertThat().entriesListCountIs(createdCategoryLinks.size());
        IntStream.range(0, createdCategoryLinks.size()).forEach(i ->
            linkedCategories.getEntries().get(i).onModel().assertThat().isEqualTo(createdCategoryLinks.get(i).onModel())
        );
    }

    /**
     * Try to get linked categories for content which is not linked to any category
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListCategoriesForNode_withoutLinkedCategories()
    {
        STEP("Try to get linked categories and expect empty list");
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getLinkedCategories();

        restClient.assertStatusCodeIs(OK);
        linkedCategories.assertThat().entriesListIsEmpty();
    }

    /**
     * Try to get linked categories using non-existing node and expect 404 (Not Found)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListCategoriesForNode_usingNonExistingNodeAndExpect404()
    {
        STEP("Try to get linked categories for non-existing node and expect 404");
        final RepoTestModel nonExistingNode = createNodeModelWithId("non-existing-id");
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistingNode).getLinkedCategories();

        restClient.assertStatusCodeIs(NOT_FOUND);
        linkedCategories.assertThat().entriesListIsEmpty();
    }

    /**
     * Try to get multiple linked categories as user without read permission and expect 403 (Forbidden)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListCategoriesForNode_asUserWithoutReadPermissionAndExpect403()
    {
        STEP("Link content to category");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkModelWithId(category.getId());
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        STEP("Create another user and deny consumer rights");
        final UserModel userWithoutRights = dataUser.createRandomTestUser();
        denyPermissionsForUser(userWithoutRights.getUsername(), "Consumer", file);

        STEP("Try to get linked categories using user without read permission and expect 403");
        restClient.authenticateUser(userWithoutRights).withCoreAPI().usingNode(file).getLinkedCategories();

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Try to get linked categories using tag instead of a content and expect 422 (Unprocessable Entity)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testListCategoriesForNode_usingTagInsteadOfContentAndExpect422()
    {
        STEP("Add tag to file");
        final RestTagModel tag = restClient.authenticateUser(user).withCoreAPI().usingNode(file).addTag("someTag");
        final RepoTestModel tagNode = createNodeModelWithId(tag.getId());

        STEP("Try to get linked categories for a tag and expect 422");
        restClient.authenticateUser(user).withCoreAPI().usingNode(tagNode).getLinkedCategories();

        restClient.assertStatusCodeIs(UNPROCESSABLE_ENTITY);
    }

    private void denyPermissionsForUser(final String username, final String role, final FileModel file)
    {
        final String putPermissionsBody = Json.createObjectBuilder().add("permissions",
                Json.createObjectBuilder()
                    .add("isInheritanceEnabled", true)
                    .add("locallySet", Json.createObjectBuilder()
                        .add("authorityId", username)
                        .add("name", role)
                        .add("accessStatus", "DENIED")))
            .build().toString();
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).updateNode(putPermissionsBody);
    }
}
