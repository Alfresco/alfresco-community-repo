package org.alfresco.rest.search;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;

/**
 * Base corpus for Exact Term tests.
 * Creates 5 documents with known name/title/description/content values
 * used to verify exact term search behaviour against standard cm:* properties.
 */
public abstract class AbstractSearchExactTermTest extends AbstractSearchServicesE2ETest
{
    private void prepareExactSearchData(FolderModel testFolder)
    {
        List<Map<String, String>> exactSearchData = asList(
                // Document #1
                of("name", "Running",
                        "description", "Running is a sport is a nice activity",
                        "content", "when you are running you are doing an amazing sport",
                        "title", "Running jumping"),
                // Document #2
                of("name", "Run",
                        "description", "you are supposed to run jump",
                        "content", "after many runs you are tired and if you jump it happens the same",
                        "title", "Run : a philosophy"),
                // Document #3
                of("name", "Poetry",
                        "description", "a document about poetry and jumpers",
                        "content", "poetry is unrelated to sport",
                        "title", "Running jumping twice jumpers"),
                // Document #4
                of("name", "Jump",
                        "description", "a document about jumps",
                        "content", "runnings jumpings",
                        "title", "Running"),
                // Document #5
                of("name", "Running jumping",
                        "description", "runners jumpers runs everywhere",
                        "content", "run is Good as jump",
                        "title", "Running the art of jumping"));

        List<FileModel> createdFileModels = new ArrayList<>();
        range(0, exactSearchData.size())
                .forEach(id -> {
                    Map<String, String> record = exactSearchData.get(id);

                    FileModel fileModel = new FileModel(
                            record.get("name"),
                            record.get("title"),
                            record.get("description"),
                            FileType.TEXT_PLAIN,
                            record.get("content"));

                    dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFolder)
                            .createContent(fileModel);

                    createdFileModels.add(fileModel);
                });

        waitForContentIndexing(createdFileModels.getLast().getName(), true);
    }

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        serverHealth.assertServerIsOnline();

        FolderModel testFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
        prepareExactSearchData(testFolder);
    }

    protected void assertResponseCardinality(String query, int num)
    {
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), num, query);
    }
}