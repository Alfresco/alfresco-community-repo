package org.alfresco.cmis.search;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import org.alfresco.utility.Utility;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SearchInFolderTests extends AbstractCmisE2ETest
{
    private FolderModel parentFolder, subFolder1, subFolder2, subFolder3;
    private FileModel subFile1, subFile2, subFile3, subFile4, subFile5;

    /**
     * Create test data in the following format:
     * <pre>
     * testSite
     * +- parentFolder
     *    +- subFile5 (fifthFile.txt: "fifthFile content")
     *    +- subFolder1
     *    +- subFolder2
     *    +- subFolder3 (subFolder)
     *    +- subFile1 (firstFile.xls)
     *    +- subFile2 (.pptx)
     *    +- subFile3 (.txt)
     *    +- subFile4 (fourthFile.docx: "fourthFileTitle", "fourthFileDescription")
     * </pre>
     */
    @BeforeClass(alwaysRun = true)
    public void createTestData() throws Exception
    {
        // create input data
        parentFolder = FolderModel.getRandomFolderModel();
        subFolder1 = FolderModel.getRandomFolderModel();
        subFolder2 = FolderModel.getRandomFolderModel();
        subFolder3 = new FolderModel("subFolder");
        subFile5 = new FileModel("fifthFile.txt",FileType.TEXT_PLAIN, "fifthFile content");
        subFile1 = new FileModel("firstFile", FileType.MSEXCEL);
        subFile2 = FileModel.getRandomFileModel(FileType.MSPOWERPOINT2007);
        subFile3 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        subFile4 = new FileModel("fourthFile", "fourthFileTitle", "fourthFileDescription", FileType.MSWORD2007);

        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(parentFolder)
                .then().usingResource(parentFolder)
                .createFile(subFile5).assertThat().contentIs("fifthFile content")
                .createFolder(subFolder1)
                .createFolder(subFolder2)
                .createFolder(subFolder3)
                .createFile(subFile1)
                .createFile(subFile2)
                .createFile(subFile3)
                .createFile(subFile4);
        // wait for index
        Utility.waitToLoopTime(5);//getElasticWaitTimeInSeconds());
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment()
    {
        dataContent.deleteSite(testSite);
    }

    @Test
    public void executeCMISQuery0()
    {
        String query = "SELECT cmis:name, cmis:parentId, cmis:path, cmis:allowedChildObjectTypeIds" +
                " FROM cmis:folder where IN_FOLDER('%s') AND cmis:name = 'subFolder'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFolder3);
    }

    @Test
    public void executeCMISQuery1()
    {
        String query = "SELECT cmis:name, cmis:objectId, cmis:lastModifiedBy, cmis:creationDate, cmis:contentStreamFileName" +
                " FROM cmis:document where IN_FOLDER('%s') AND cmis:name = 'fourthFile'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile4);
    }

    @Test
    public void executeCMISQuery2()
    {
        String query = "SELECT cmis:parentId FROM cmis:folder where IN_FOLDER('%s')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        // Expect to get the same parent for each of the three matches.
        String parentId = parentFolder.getNodeRef();
        List<String> expectedParentIds = List.of(parentId, parentId, parentId);
        waitForIndexing(query, execution -> execution.isReturningOrderedValues("cmis:parentId", expectedParentIds));
    }

    @Test
    public void executeCMISQuery3()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile1, subFile2, subFile3, subFile4, subFile5);
    }

    @Test
    public void executeCMISQuery4()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name NOT LIKE 'file%%' ORDER BY cmis:name ASC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFile5, subFile1, subFile4);
    }

    @Test
    public void executeCMISQuery5()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') ORDER BY cmis:name DESC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 5), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery6()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') ORDER BY cmis:lastModificationDate ASC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 3), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery7()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') ORDER BY cmis:lastModificationDate DESC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 3), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery8()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') ORDER BY cmis:createdBy DESC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 5), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery9()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name IS NOT NULL";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 5), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery10()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') AND cmis:name IS NOT NULL";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 3), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery11()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name LIKE 'fourthFile'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 1), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery12()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') AND NOT(cmis:name NOT IN ('subFolder'))";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 1), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery13()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name IN ('fourthFile', 'fifthFile.txt')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 2), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery14()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name NOT IN ('fourthFile', 'fifthFile.txt')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 3), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery15()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') AND cmis:name <> 'subFolder'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 2), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery16()
    {
        String query = "SELECT cmis:secondaryObjectTypeIds FROM cmis:folder where IN_FOLDER('%s') AND cmis:name = 'subFolder'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Assert.assertTrue(waitForIndexing(currentQuery, 1), String.format("Result count not as expected for query: %s", currentQuery));
    }
}
