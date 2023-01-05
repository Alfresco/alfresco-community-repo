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

import static org.alfresco.utility.constants.UserRole.SiteManager;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import javax.json.Json;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestCategoryLinkBodyModel;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.rest.model.RestCategoryModelsCollection;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestRatingModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LinkToCategoriesTests extends CategoriesRestTest
{
    private static final String ASPECTS_FIELD = "aspectNames";
    private static final String PROPERTIES_FIELD = "properties";

    private UserModel user;
    private SiteModel site;
    private FolderModel folder;
    private FileModel file;
    private RestCategoryModel category;

    @BeforeClass(alwaysRun = true)
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
     * Link content to category and verify if this category is present in node's properties
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory()
    {
        STEP("Check if file is not linked to any category");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();

        fileNode.assertThat().field(ASPECTS_FIELD).notContains("cm:generalclassifiable");
        fileNode.assertThat().field(PROPERTIES_FIELD).notContains("cm:categories");

        STEP("Link content to created category and expect 201");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(category.getId());
        final RestCategoryModel linkedCategory = restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(CREATED);
        linkedCategory.assertThat().isEqualTo(category);

        STEP("Verify if category is present in file metadata");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();

        fileNode.assertThat().field(ASPECTS_FIELD).contains("cm:generalclassifiable");
        fileNode.assertThat().field(PROPERTIES_FIELD).contains("cm:categories");
        fileNode.assertThat().field(PROPERTIES_FIELD).contains(category.getId());
    }

    /**
     * Link content to two categories and verify if both are present in node's properties
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToMultipleCategories()
    {
        STEP("Check if file is not linked to any category");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();

        fileNode.assertThat().field(ASPECTS_FIELD).notContains("cm:generalclassifiable");
        fileNode.assertThat().field(PROPERTIES_FIELD).notContains("cm:categories");

        STEP("Create second category under root");
        final RestCategoryModel secondCategory = prepareCategoryUnderRoot();

        STEP("Link content to created categories and expect 201");
        final List<RestCategoryLinkBodyModel> categoryLinks = List.of(
            createCategoryLinkWithId(category.getId()),
            createCategoryLinkWithId(secondCategory.getId())
        );
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategories(categoryLinks);

        restClient.assertStatusCodeIs(CREATED);
        linkedCategories.getEntries().get(0).onModel().assertThat().isEqualTo(category);
        linkedCategories.getEntries().get(1).onModel().assertThat().isEqualTo(secondCategory);

        STEP("Verify if both categories are present in file metadata");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();

        fileNode.assertThat().field(ASPECTS_FIELD).contains("cm:generalclassifiable");
        fileNode.assertThat().field(PROPERTIES_FIELD).contains("cm:categories");
        fileNode.assertThat().field(PROPERTIES_FIELD).contains(category.getId());
        fileNode.assertThat().field(PROPERTIES_FIELD).contains(secondCategory.getId());
    }

    /**
     * Link content, which already has some linked category to new ones and verify if all categories are present in node's properties
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_usingContentWithAlreadyLinkedCategories()
    {
        STEP("Link content to created category");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(category.getId());
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);
        restClient.assertStatusCodeIs(CREATED);

        STEP("Create second and third category under root, link content to them and expect 201");
        final RestCategoryModel secondCategory = prepareCategoryUnderRoot();
        final RestCategoryModel thirdCategory = prepareCategoryUnderRoot();
        final List<RestCategoryLinkBodyModel> categoryLinks = List.of(
            createCategoryLinkWithId(secondCategory.getId()),
            createCategoryLinkWithId(thirdCategory.getId())
        );
        final RestCategoryModelsCollection linkedCategories = restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategories(categoryLinks);

        restClient.assertStatusCodeIs(CREATED);
        linkedCategories.assertThat().entriesListCountIs(2);
        linkedCategories.getEntries().get(0).onModel().assertThat().isEqualTo(secondCategory);
        linkedCategories.getEntries().get(1).onModel().assertThat().isEqualTo(thirdCategory);

        STEP("Verify if all three categories are present in file metadata");
        final RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();

        fileNode.assertThat().field(PROPERTIES_FIELD).contains("cm:categories");
        fileNode.assertThat().field(PROPERTIES_FIELD).contains(category.getId());
        fileNode.assertThat().field(PROPERTIES_FIELD).contains(secondCategory.getId());
        fileNode.assertThat().field(PROPERTIES_FIELD).contains(thirdCategory.getId());
    }

    /**
     * Try to link content to category as a user without read permission and expect 403 (Forbidden)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_asUserWithoutReadPermissionAndExpect403()
    {
        STEP("Try to link content to a category using user without read permission and expect 403");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(category.getId());
        final UserModel userWithoutRights = dataUser.createRandomTestUser();
        restClient.authenticateUser(userWithoutRights).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Try to link content to category as a user without change and expect 403 (Forbidden)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_asUserWithoutChangePermissionAndExpect403()
    {
        STEP("Create another user as a consumer for file");
        final UserModel consumer = dataUser.createRandomTestUser();
        addPermissionsForUser(consumer.getUsername(), "Consumer", file);

        STEP("Try to link content to a category using user without change permission and expect 403");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(category.getId());
        restClient.authenticateUser(consumer).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Try to link content to category as a owner and expect 403 (Forbidden)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_asOwnerAndExpect403()
    {
        STEP("Use admin to create a private site");
        final SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        STEP("Add the user to the site, let him create a folder and then evict him from the site again");
        dataUser.addUserToSite(user, privateSite, SiteManager);
        final FolderModel privateFolder = dataContent.usingUser(user).usingSite(privateSite).createFolder();
        final FileModel privateFile = dataContent.usingUser(user).usingResource(privateFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataUser.removeUserFromSite(user, privateSite);

        STEP("Try to link content to a category as owner and expect 403");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(category.getId());
        restClient.authenticateUser(user).withCoreAPI().usingNode(privateFile).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(CREATED);
    }

    /**
     * Try to link content to category using non-existing category and expect 404 (Not Found)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_usingNonExistingCategoryAndExpect404()
    {
        STEP("Try to link content to non-existing category and expect 404");
        final String nonExistingCategoryId = "non-existing-dummy-id";
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(nonExistingCategoryId);
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Try to link content to category passing empty list as input and expect 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_passingEmptyListAndExpect400()
    {
        STEP("Try to call link content API with empty list and expect 400");
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategories(Collections.emptyList());

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to link content to category passing invalid ID in input list and expect 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_passingEmptyIdAndExpect400()
    {
        STEP("Try to call link content API with empty category ID and expect 400");
        final String nonExistingCategoryId = StringUtils.EMPTY;
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(nonExistingCategoryId);
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to link non-content node to category and expect 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_usingTagInsteadOfContentAndExpect400()
    {
        STEP("Try to link a tag to category and expect 400");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(category.getId());
        final RestTagModel tag = restClient.authenticateUser(user).withCoreAPI().usingNode(file).addTag("someTag");
        final RepoTestModel tagNode = new RepoTestModel() {};
        tagNode.setNodeRef(tag.getId());
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingNode(tagNode).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to link content to non-category node and expect 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_usingFolderInsteadOfCategoryAndExpect400()
    {
        STEP("Try to link content to non-category and expect 400");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(folder.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to link content to root category and expect 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API})
    public void testLinkContentToCategory_usingRootCategoryAndExpect400()
    {
        STEP("Try to link content to root category and expect 400");
        final RestCategoryLinkBodyModel categoryLink = createCategoryLinkWithId(ROOT_CATEGORY_ID);
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).linkToCategory(categoryLink);

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    private RestCategoryLinkBodyModel createCategoryLinkWithId(final String id)
    {
        final RestCategoryLinkBodyModel categoryLink = new RestCategoryLinkBodyModel();
        categoryLink.setCategoryId(id);
        return categoryLink;
    }

    private void addPermissionsForUser(final String username, final String role, final FileModel file)
    {
        final String putPermissionsBody = Json.createObjectBuilder().add("permissions",
                Json.createObjectBuilder()
                    .add("isInheritanceEnabled", true)
                    .add("locallySet", Json.createObjectBuilder()
                        .add("authorityId", username)
                        .add("name", role).add("accessStatus", "ALLOWED")))
            .build()
            .toString();

        restClient.authenticateUser(user).withCoreAPI().usingNode(file).updateNode(putPermissionsBody);
    }

    private RestRuleModel createRuleModel()
    {
        final RestActionBodyExecTemplateModel actions = new RestActionBodyExecTemplateModel();
        actions.setActionDefinitionId("add-features");
        actions.setParams(Map.of("aspect-name", "audio:audio"));

        final RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");
        ruleModel.setActions(List.of(actions));
        //ruleModel.setTriggers(List.of("inbound"));

        /*RestActionConstraintModel constraintDef = getConstraintsForActionParam(user, actionId, paramId);
        RestActionConstraintDataModel constraintDataModel = constraintDef.getConstraintValues().stream().filter(constraintValue -> constraintValue.getLabel().equals(constraintLabel)).findFirst().get();
        return constraintDataModel.getValue()*/

        //ruleModel.setErrorScript("fake-script");
        return ruleModel;
    }
}
