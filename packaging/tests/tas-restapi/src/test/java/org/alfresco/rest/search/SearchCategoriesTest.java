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

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

/**
 * Migration test class for category-based search scenarios on Elasticsearch.
 */
public class SearchCategoriesTest extends AbstractSearchServicesE2ETest
{
    private FolderModel folder;
    private FileModel fileWithPrimaryCategory;
    private FileModel anotherFileWithPrimaryCategory;
    private FileModel fileWithCategoryAndTag;
    private FileModel fileWithBothCategories;
    private FileModel fileForCategoryRemoval;
    private FileModel fileForLateAssignment;
    private FileModel isolationFileA;
    private FileModel isolationFileB;

    private String primaryCategoryNodeRef;
    private String secondaryCategoryNodeRef;

    private static final String COMBINED_TAG = "acsmigrationcombinedtag";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        folder = dataContent.usingUser(testUser).usingSite(testSite).createFolderCmisApi("categories-folder");

        primaryCategoryNodeRef = lookupCategoryRefByName("Software Document Classification");
        secondaryCategoryNodeRef = lookupCategoryRefByName("Regions");

        if (primaryCategoryNodeRef == null || secondaryCategoryNodeRef == null)
        {
            SearchResponse anyCats = queryAsUser(dataUser.getAdminUser(), "TYPE:'cm:category'");
            Assert.assertTrue(anyCats.getPagination().getCount() >= 2,
                    "Test setup requires at least two cm:category nodes in the repository");
            if (primaryCategoryNodeRef == null)
            {
                primaryCategoryNodeRef = "workspace://SpacesStore/" + anyCats.getEntries().get(0).getModel().getId();
            }
            if (secondaryCategoryNodeRef == null)
            {
                secondaryCategoryNodeRef = "workspace://SpacesStore/" + anyCats.getEntries().get(1).getModel().getId();
            }
        }

        fileWithPrimaryCategory = createFileClassifiedInto("file-with-primary-category.txt",
                "File classified into primary category", primaryCategoryNodeRef);

        anotherFileWithPrimaryCategory = createFileClassifiedInto("another-file-with-primary-category.txt",
                "Second file classified into primary category", primaryCategoryNodeRef);

        fileWithCategoryAndTag = createFileClassifiedInto("file-with-category-and-tag.txt",
                "File with both a category and a tag", primaryCategoryNodeRef);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithCategoryAndTag).addTag(COMBINED_TAG);

        fileWithBothCategories = createFileClassifiedInto("file-with-both-categories.txt",
                "File classified into two categories", primaryCategoryNodeRef, secondaryCategoryNodeRef);

        fileForCategoryRemoval = createFileClassifiedInto("file-for-category-removal.txt",
                "File whose category will be removed to verify de-indexing", primaryCategoryNodeRef);

        fileForLateAssignment = new FileModel("file-for-late-category-assignment.txt");
        fileForLateAssignment.setContent("File that receives a category after creation");
        dataContent.usingUser(testUser).usingResource(folder).createContent(fileForLateAssignment);

        isolationFileA = createFileClassifiedInto("isolation-file-a.txt",
                "First isolation test file (category will be removed)", secondaryCategoryNodeRef);
        isolationFileB = createFileClassifiedInto("isolation-file-b.txt",
                "Second isolation test file (category must stay)", secondaryCategoryNodeRef);

        waitForMetadataIndexing(fileWithPrimaryCategory.getName(), true);
        waitForMetadataIndexing(anotherFileWithPrimaryCategory.getName(), true);
        waitForMetadataIndexing(fileWithCategoryAndTag.getName(), true);
        waitForMetadataIndexing(fileWithBothCategories.getName(), true);
        waitForMetadataIndexing(fileForCategoryRemoval.getName(), true);
        waitForMetadataIndexing(fileForLateAssignment.getName(), true);
        waitForMetadataIndexing(isolationFileA.getName(), true);
        waitForMetadataIndexing(isolationFileB.getName(), true);

        Assert.assertTrue(isContentInSearchResults("cm:categories:\"" + primaryCategoryNodeRef + "\"", fileWithPrimaryCategory.getName(), true),
                "Setup: primary-category association should be searchable after batch-indexing catches up");
        Assert.assertTrue(isContentInSearchResults("cm:categories:\"" + secondaryCategoryNodeRef + "\"", isolationFileA.getName(), true),
                "Setup: secondary-category association should be searchable after batch-indexing catches up");
        Assert.assertTrue(isContentInSearchResults("TAG:'" + COMBINED_TAG + "'", fileWithCategoryAndTag.getName(), true),
                "Setup: combined tag should be searchable after batch-indexing catches up");
    }

    private String lookupCategoryRefByName(String name)
    {
        SearchResponse response = queryAsUser(dataUser.getAdminUser(),
                "TYPE:'cm:category' AND cm:name:'" + name + "'");
        if (response.getPagination().getCount() >= 1)
        {
            return "workspace://SpacesStore/" + response.getEntries().getFirst().getModel().getId();
        }
        return null;
    }

    private FileModel createFileClassifiedInto(String name, String content, String... categoryRefs)
    {
        FileModel file = new FileModel(name);
        file.setContent(content);
        dataContent.usingUser(testUser).usingResource(folder).createContent(file);

        jakarta.json.JsonArrayBuilder catArray = Json.createArrayBuilder();
        for (String ref : categoryRefs)
        {
            catArray.add(ref);
        }

        JsonObject body = Json.createObjectBuilder()
                .add("aspectNames", Json.createArrayBuilder().add("cm:generalclassifiable"))
                .add("properties", Json.createObjectBuilder().add("cm:categories", catArray))
                .build();
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(file).updateNode(body.toString());
        return file;
    }

    @Test(priority = 1)
    public void testSearchByGeneralClassifiableAspect()
    {
        String query = "ASPECT:'cm:generalclassifiable' AND cm:name:'" + fileWithPrimaryCategory.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected file with the cm:generalclassifiable aspect to be findable");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithPrimaryCategory.getName()),
                "Expected " + fileWithPrimaryCategory.getName() + " in the aspect query results");
    }

    @Test(priority = 2)
    public void testSearchByCategoryProperty()
    {
        String query = "cm:categories:\"" + primaryCategoryNodeRef + "\" AND cm:name:'" +
                fileWithPrimaryCategory.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected file classified into the category to be findable via cm:categories property");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithPrimaryCategory.getName()),
                "Expected " + fileWithPrimaryCategory.getName() + " in the cm:categories query results");
    }

    @Test(priority = 3)
    public void testCategoryAndTagCombined()
    {
        String query = "ASPECT:'cm:generalclassifiable' AND TAG:'" + COMBINED_TAG +
                "' AND cm:name:'" + fileWithCategoryAndTag.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected file with both a category and the combined tag to be findable via combined query");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithCategoryAndTag.getName()),
                "Expected " + fileWithCategoryAndTag.getName() + " in the category+tag query results");
    }

    @Test(priority = 4)
    public void testMultipleFilesInSameCategory()
    {
        String pathClause = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() + "/cm:documentLibrary//*\"";
        String query = "cm:categories:\"" + primaryCategoryNodeRef + "\" AND " + pathClause;
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 2,
                "Expected at least two files in this site classified into the primary category");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithPrimaryCategory.getName()),
                "Expected " + fileWithPrimaryCategory.getName() + " among the primary-category files");
        Assert.assertTrue(isContentInSearchResponse(response, anotherFileWithPrimaryCategory.getName()),
                "Expected " + anotherFileWithPrimaryCategory.getName() + " among the primary-category files");
    }

    @Test(priority = 5)
    public void testFileInMultipleCategoriesFoundByEach()
    {
        String queryPrimary = "cm:categories:\"" + primaryCategoryNodeRef + "\" AND cm:name:'" +
                fileWithBothCategories.getName() + "'";
        SearchResponse fromPrimary = queryAsUser(testUser, queryPrimary);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(fromPrimary.getPagination().getCount() >= 1,
                "Expected multi-category file to be findable via the primary category");
        Assert.assertTrue(isContentInSearchResponse(fromPrimary, fileWithBothCategories.getName()),
                "Expected " + fileWithBothCategories.getName() + " when querying the primary category");

        String querySecondary = "cm:categories:\"" + secondaryCategoryNodeRef + "\" AND cm:name:'" +
                fileWithBothCategories.getName() + "'";
        SearchResponse fromSecondary = queryAsUser(testUser, querySecondary);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(fromSecondary.getPagination().getCount() >= 1,
                "Expected multi-category file to be findable via the secondary category");
        Assert.assertTrue(isContentInSearchResponse(fromSecondary, fileWithBothCategories.getName()),
                "Expected " + fileWithBothCategories.getName() + " when querying the secondary category");
    }

    @Test(priority = 6)
    public void testCategoryRemovalUpdatesIndex()
    {
        String queryBefore = "cm:categories:\"" + primaryCategoryNodeRef + "\" AND cm:name:'" +
                fileForCategoryRemoval.getName() + "'";
        SearchResponse before = queryAsUser(testUser, queryBefore);
        Assert.assertTrue(before.getPagination().getCount() >= 1,
                "File should be findable via its category before removal");
        Assert.assertTrue(isContentInSearchResponse(before, fileForCategoryRemoval.getName()),
                "Expected " + fileForCategoryRemoval.getName() + " to be present before category removal");

        JsonObject body = Json.createObjectBuilder()
                .add("properties", Json.createObjectBuilder()
                        .add("cm:categories", Json.createArrayBuilder()))
                .build();
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(fileForCategoryRemoval).updateNode(body.toString());

        Assert.assertTrue(isContentInSearchResults(queryBefore, fileForCategoryRemoval.getName(), false),
                "File should NOT be findable via its category after removal");
    }

    @Test(priority = 7)
    public void testAspectRestrictsSearchToClassifiedFiles()
    {
        // fileForLateAssignment has no aspect yet. The cm:name query tokens ('late', 'assignment')
        // don't appear in any classified file, so a match here would indicate an ES over-match bug.
        String query = "ASPECT:'cm:generalclassifiable' AND cm:name:'" + fileForLateAssignment.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        boolean targetFound = response.getEntries().stream()
                .anyMatch(e -> e.getModel().getName().equals(fileForLateAssignment.getName()));
        Assert.assertFalse(targetFound,
                "Expected uncategorised file to NOT match the cm:generalclassifiable aspect query at this point");
    }

    @Test(priority = 8)
    public void testCategoryAddedToExistingFileGetsIndexed()
    {
        String categoryQuery = "cm:categories:\"" + primaryCategoryNodeRef + "\" AND cm:name:'" +
                fileForLateAssignment.getName() + "'";
        SearchResponse before = queryAsUser(testUser, categoryQuery);
        boolean targetFoundBefore = before.getEntries().stream()
                .anyMatch(e -> e.getModel().getName().equals(fileForLateAssignment.getName()));
        Assert.assertFalse(targetFoundBefore,
                "File should not be findable via the category before assignment");

        JsonObject body = Json.createObjectBuilder()
                .add("aspectNames", Json.createArrayBuilder().add("cm:generalclassifiable"))
                .add("properties", Json.createObjectBuilder()
                        .add("cm:categories", Json.createArrayBuilder().add(primaryCategoryNodeRef)))
                .build();
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(fileForLateAssignment).updateNode(body.toString());

        Assert.assertTrue(isContentInSearchResults(categoryQuery, fileForLateAssignment.getName(), true),
                "File should be findable via the category after assignment");
    }

    @Test(priority = 9)
    public void testCategoryRemovalOnlyAffectsTargetedFile()
    {
        String secondaryPathClause = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary//*\"";

        String queryBoth = "cm:categories:\"" + secondaryCategoryNodeRef + "\" AND " + secondaryPathClause;
        SearchResponse both = queryAsUser(testUser, queryBoth);
        Assert.assertTrue(both.getPagination().getCount() >= 2,
                "Both isolation files should initially be findable via the secondary category");
        Assert.assertTrue(isContentInSearchResponse(both, isolationFileA.getName()),
                "Expected " + isolationFileA.getName() + " to be present before removal");
        Assert.assertTrue(isContentInSearchResponse(both, isolationFileB.getName()),
                "Expected " + isolationFileB.getName() + " to be present before removal");

        JsonObject removeBody = Json.createObjectBuilder()
                .add("properties", Json.createObjectBuilder()
                        .add("cm:categories", Json.createArrayBuilder()))
                .build();
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(isolationFileA).updateNode(removeBody.toString());

        String queryA = "cm:categories:\"" + secondaryCategoryNodeRef + "\" AND cm:name:'" + isolationFileA.getName() + "'";
        Assert.assertTrue(isContentInSearchResults(queryA, isolationFileA.getName(), false),
                "isolationFileA should no longer be findable via the secondary category after removal");

        String queryB = "cm:categories:\"" + secondaryCategoryNodeRef + "\" AND cm:name:'" + isolationFileB.getName() + "'";
        SearchResponse resultB = queryAsUser(testUser, queryB);
        boolean targetBFound = resultB.getEntries().stream()
                .anyMatch(e -> e.getModel().getName().equals(isolationFileB.getName()));
        Assert.assertTrue(targetBFound,
                "isolationFileB should still be findable via the secondary category — removal must not affect siblings");
    }
}
