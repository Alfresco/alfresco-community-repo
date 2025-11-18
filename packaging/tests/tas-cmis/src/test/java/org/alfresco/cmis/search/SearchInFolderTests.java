package org.alfresco.cmis.search;

import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;

public class SearchInFolderTests extends AbstractCmisE2ETest
{
    private FolderModel parentFolder, subFolder1, subFolder2, subFolder3;
    private FileModel subFile1, subFile2, subFile3, subFile4, subFile5;

    /**
     * Create test data in the following format:
     * 
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
        subFile5 = new FileModel("fifthFile.txt", FileType.TEXT_PLAIN, "fifthFile content");
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
        Utility.waitToLoopTime(getElasticWaitTimeInSeconds());
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment()
    {
        dataContent.deleteSite(testSite);
    }

    @Test
    public void executeCMISQuery_selectFieldsFromFolder()
    {
        String query = "SELECT cmis:name, cmis:parentId, cmis:path, cmis:allowedChildObjectTypeIds" +
                " FROM cmis:folder where IN_FOLDER('%s') AND cmis:name = 'subFolder'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFolder3);
    }

    @Test
    public void executeCMISQuery_selectFieldsFromDocument()
    {
        String query = "SELECT cmis:name, cmis:objectId, cmis:lastModifiedBy, cmis:creationDate, cmis:contentStreamFileName" +
                " FROM cmis:document where IN_FOLDER('%s') AND cmis:name = 'fourthFile'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile4);
    }

    @Test
    public void executeCMISQuery_selectParentId()
    {
        String query = "SELECT cmis:parentId FROM cmis:folder where IN_FOLDER('%s')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        // Expect to get the same parent for each of the three matches.
        String parentId = parentFolder.getNodeRef();
        List<String> expectedParentIds = List.of(parentId, parentId, parentId);
        waitForIndexing(currentQuery, execution -> execution.isReturningOrderedValues("cmis:parentId", expectedParentIds));
    }

    @Test
    public void executeCMISQuery_inFolder()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile1, subFile2, subFile3, subFile4, subFile5);
    }

    @Test
    public void executeCMISQuery_orderByNameAsc()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name NOT LIKE 'file%%' ORDER BY cmis:name ASC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFile5, subFile1, subFile4);
    }

    @Test
    public void executeCMISQuery_orderByNameDesc()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name NOT LIKE 'file%%' ORDER BY cmis:name DESC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFile4, subFile1, subFile5);
    }

    @Test
    public void executeCMISQuery_orderByLastModifiedAsc()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') ORDER BY cmis:lastModificationDate ASC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFolder1, subFolder2, subFolder3);
    }

    @Test
    public void executeCMISQuery_orderByLastModifiedDesc()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') ORDER BY cmis:lastModificationDate DESC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFolder3, subFolder2, subFolder1);
    }

    @Test
    public void executeCMISQuery_orderByCreatedBy()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') ORDER BY cmis:createdBy DESC";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        // All the results were created by the same user, so we can't assert anything about the order.
        waitForIndexing(currentQuery, subFile5, subFile1, subFile2, subFile3, subFile4);
    }

    @Test
    public void executeCMISQuery_documentNameNotNull()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name IS NOT NULL";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile1, subFile2, subFile3, subFile4, subFile5);
    }

    @Test
    public void executeCMISQuery_folderNameNotNull()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') AND cmis:name IS NOT NULL";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFolder1, subFolder2, subFolder3);
    }

    @Test
    public void executeCMISQuery_nameLike()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name LIKE 'fourthFile'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFile4);
    }

    @Test
    public void executeCMISQuery_doubleNegative()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') AND NOT(cmis:name NOT IN ('subFolder'))";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexingOrdered(currentQuery, subFolder3);
    }

    @Test
    public void executeCMISQuery_nameInList()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name IN ('fourthFile', 'fifthFile.txt')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile4, subFile5);
    }

    @Test
    public void executeCMISQuery_nameNotInList()
    {
        String query = "SELECT * FROM cmis:document where IN_FOLDER('%s') AND cmis:name NOT IN ('fourthFile', 'fifthFile.txt')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile1, subFile2, subFile3);
    }

    @Test
    public void executeCMISQuery_nameDifferentFrom()
    {
        String query = "SELECT * FROM cmis:folder where IN_FOLDER('%s') AND cmis:name <> 'subFolder'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFolder1, subFolder2);
    }

    @Test
    public void executeCMISQuery_selectSecondaryObjectTypeIds()
    {
        String query = "SELECT cmis:secondaryObjectTypeIds FROM cmis:folder where IN_FOLDER('%s') AND cmis:name = 'subFolder'";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        Set<List<String>> expectedSecondaryObjectTypeIds = Set.of(List.of("P:cm:titled", "P:sys:localized"));
        waitForIndexing(currentQuery, execution -> execution.isReturningValues("cmis:secondaryObjectTypeIds", expectedSecondaryObjectTypeIds, true));
        Assert.assertTrue(waitForIndexing(currentQuery, 1), String.format("Result count not as expected for query: %s", currentQuery));
    }

    @Test
    public void executeCMISQuery_joinTitledAspectByTitle()
    {
        String query = "SELECT * FROM cmis:document AS d JOIN cm:titled as a0 ON d.cmis:objectId = a0.cmis:objectId WHERE CONTAINS(a0, 'cm:title:\\\"fourthFileTitle\\\"')";
        String currentQuery = String.format(query, parentFolder.getNodeRef());
        cmisApi.authenticateUser(testUser);
        waitForIndexing(currentQuery, subFile4);
    }
}
