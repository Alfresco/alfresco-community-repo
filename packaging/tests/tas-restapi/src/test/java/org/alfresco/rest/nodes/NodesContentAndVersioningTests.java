package org.alfresco.rest.nodes;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestVersionModel;
import org.alfresco.rest.model.RestVersionModelsCollection;
import org.alfresco.rest.model.body.RestNodeLockBodyModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 
 * @author mpopa
 *
 */
public class NodesContentAndVersioningTests extends RestTest
{
    private UserModel user1, user2;
    private SiteModel site1, site2;
    private FileModel file1, file2;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        user1 = dataUser.createRandomTestUser();
        user2 = dataUser.createRandomTestUser();
        site1 = dataSite.usingUser(user1).createPublicRandomSite();
        site2 = dataSite.usingUser(user2).createPublicRandomSite();
        file1 = dataContent.usingUser(user1).usingSite(site1).createContent(DocumentType.TEXT_PLAIN);
        file2 = dataContent.usingUser(user2).usingSite(site2).createContent(DocumentType.TEXT_PLAIN);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Verify file name in Content-Disposition header")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    public void checkFileNameWithRegularCharsInHeader() throws Exception
    {
        restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("attachment=false").getNodeContent();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Disposition", String.format("filename=\"%s\"", file1.getName()));
    }




    @Bug(id = "MNT-17545", description = "HTTP Header Injection in ContentStreamer", status = Bug.Status.FIXED)
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.REGRESSION, description = "Verify file name with special chars is escaped in Content-Disposition header")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.REGRESSION })
    public void checkFileNameWithSpecialCharsInHeader() throws Exception
    {
        char c1 = 127;
        char c2 = 31;
        char c3 = 256;
        FileModel file = dataContent.usingUser(user2).usingSite(site2).createContent(new FileModel("\ntest" + c1 + c2 + c3, FileType.TEXT_PLAIN));
        restClient.authenticateUser(user2).withCoreAPI().usingNode(file).usingParams("attachment=false").getNodeContent();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Disposition", "filename=\" test   \"");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Verify that alfresco returns the correct encoding for files created via REST.")
    public void verifyFileEncodingUsingRestAPI() throws Exception
    {
        STEP("1. Create a folder, two text file templates and define the expected encoding.");
        FileModel utf8File = new FileModel("utf8File", FileType.TEXT_PLAIN);
        FileModel iso8859File = new FileModel("iso8859File", FileType.TEXT_PLAIN);
        FolderModel folder = dataContent.usingUser(user1).usingSite(site1).createFolder(FolderModel.getRandomFolderModel());
        String utf8Type = "text/plain;charset=UTF-8";
        String iso8859Type = "text/plain;charset=ISO-8859-1";

        STEP("2. Using multipart data upload (POST nodes/{nodeId}/children) the UTF-8 encoded file.");
        restClient.authenticateUser(user1).configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("UTF-8File.txt"));
        RestNodeModel fileNode = restClient.withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        utf8File.setNodeRef(fileNode.getId());

        STEP("3. Using multipart data upload (POST nodes/{nodeId}/children) the ISO-8859-1 file.");
        restClient.configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("iso8859File.txt"));
        fileNode = restClient.withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        iso8859File.setNodeRef(fileNode.getId());

        STEP("4. Retrieve the nodes and verify that the content type is the expected one (GET nodes/{nodeId}).");
        restClient.withCoreAPI().usingNode(utf8File).getNodeContent().assertThat().contentType(utf8Type);
        restClient.withCoreAPI().usingNode(iso8859File).getNodeContent().assertThat().contentType(iso8859Type);
    }


    // This test takes around 30 minutes to complete so it will be ignored by default
    @Test(enabled=false, groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES },
                executionType = ExecutionType.REGRESSION, description = "Verify that the node content is streamed directly to the client and not buffered in memory.")
    public void verifyUploadDownloadLargeFileUsingRestAPI() throws Exception
    {
        Integer largeFileSizeBytes = Integer.MAX_VALUE;
        String largeFileName = "largeFile.tmp";
        String tempFolderPath = getSystemTempDir().getAbsolutePath();

        STEP("1. Create a folder and a large file");
        FolderModel folder = dataContent.usingUser(user1).usingSite(site1).createFolder(FolderModel.getRandomFolderModel());
        createRandomFileInDirectory(tempFolderPath, largeFileName, largeFileSizeBytes);

        STEP("2. Using multipart data upload for the large file (POST nodes/{nodeId}/children).");
        File largeFile = new File(tempFolderPath, largeFileName);
        FileModel largeFileModel = new FileModel(largeFileName, FileType.UNDEFINED);
        restClient.authenticateUser(user1).configureRequestSpec().addMultiPart("filedata", largeFile);
        RestNodeModel fileNode = restClient.withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        largeFileModel.setNodeRef(fileNode.getId());

        STEP("3. Retrieve the content of the node without running out of memory (GET nodes/{nodeId}/content).");
        RestResponse nodeContent = restClient.withCoreAPI().usingNode(largeFileModel).getNodeContent();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(Integer.valueOf(nodeContent.getResponse().getHeader("Content-Length")), largeFileSizeBytes);

        largeFile.delete();
    }

    public static File getSystemTempDir() throws Exception
    {
        String systemTempDirPath = System.getProperty("java.io.tmpdir");
        if (systemTempDirPath == null)
        {
            throw new Exception("System property not available: " + "java.io.tmpdir");
        }
        File systemTempDir = new File(systemTempDirPath);
        return systemTempDir;
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Verify updating a node content.")
    public void testUpdateNodeContent() throws Exception
    {
        STEP("1. Retrieve the node in order to get data to compare after update GET /nodes/{nodeId}?include=path.");
        RestNodeModel initialNode = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=path").getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("2. Update the node content (different from the initial one) PUT /nodes/{nodeId}/content?majorVersion=true&name=newfile.txt.");
        File updatedConentFile = Utility.getResourceTestDataFile("sampleContent.txt");
        RestNodeModel updatedBodyNode = restClient.withCoreAPI().usingNode(file1).usingParams("majorVersion=true&name=newfile.txt").updateNodeContent(updatedConentFile);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("3. Compare contentSize, modifiedAt, name and version, they should be different.");
        assertNotSame(initialNode.getContent().getSizeInBytes(), updatedBodyNode.getContent().getSizeInBytes());
        assertNotSame(initialNode.getModifiedAt(), updatedBodyNode.getModifiedAt());
        assertNotSame(initialNode.getName(), updatedBodyNode.getName());

        String initialNodeVersion = new JSONObject(initialNode.toJson()).getJSONObject("properties").getString("cm:versionLabel");
        String updatedBodyNodeVersion = new JSONObject(updatedBodyNode.toJson()).getJSONObject("properties").getString("cm:versionLabel");
        assertTrue(updatedBodyNodeVersion.charAt(0) > initialNodeVersion.charAt(0));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Test copy a node.")
    public void testCopyNode() throws Exception
    {
        STEP("1. Create a lock and lock the node POST /nodes/{nodeId}/lock?include=path,isLocked.");
        RestNodeLockBodyModel lockBodyModel = new RestNodeLockBodyModel();
        lockBodyModel.setLifetime("EPHEMERAL");
        lockBodyModel.setTimeToExpire(20);
        lockBodyModel.setType("FULL");
        RestNodeModel initialNode = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=path,isLocked").lockNode(lockBodyModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("2. With another user(that has access to the file), copy the node to another path POST /nodes/{nodeId}/copy?include=path,isLocked.");
        String postBody = JsonBodyGenerator.keyValueJson("targetParentId", site2.getGuid());
        RestNodeModel copiedNode = restClient.authenticateUser(user2).withCoreAPI().usingNode(file1).usingParams("include=path,isLocked").copyNode(postBody);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("3. ParentId, createdAt, path and lock are different, but the nodes have the same contentSize.");
        assertNotSame(copiedNode.getParentId(), initialNode.getParentId());
        assertNotSame(copiedNode.getCreatedAt(), initialNode.getCreatedAt());
        assertNotSame(copiedNode.getPath(), initialNode.getPath());
        assertTrue(initialNode.getIsLocked());
        assertSame(copiedNode.getContent().getSizeInBytes(), initialNode.getContent().getSizeInBytes());
        assertFalse(copiedNode.getIsLocked());

        STEP("4. Unlock the node (this node may be used in the next tests).");
        initialNode = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=isLocked").unlockNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertFalse(initialNode.getIsLocked());
    }

    @Bug(id = "REPO-4050")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Test retrieving node versions, a specific version and version content.")
    public void testGetVersionContent() throws Exception
    {
        file2 = dataContent.usingUser(user2).usingSite(site2).createContent(DocumentType.TEXT_PLAIN);
        File sampleFile = Utility.getResourceTestDataFile("sampleContent.txt");

        STEP("1. Update the node content in order to increase version(one minor and one major) PUT /nodes/{nodeId}/content.");
        // minor version update
        restClient.authenticateUser(user2).withCoreAPI().usingNode(file2).updateNodeContent(sampleFile);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        // major version update
        restClient.authenticateUser(user2).withCoreAPI().usingParams("majorVersion=true").usingNode(file2).updateNodeContent(sampleFile);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("2. List node version history GET /nodes/{nodeId}/versions. And verify that first (in the list) version is 2.0 and last is 1.0.");
        RestVersionModelsCollection versionListing = restClient.withCoreAPI().usingNode(file2).listVersionHistory();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        // additional check that the properties are not displayed by default.
        assertEquals(versionListing.getEntries().get(versionListing.getPagination().getCount() - 1).onModel().getId(), "1.0");
        assertEquals(versionListing.getEntries().get(0).onModel().getId(), "2.0");
        assertNull(versionListing.getEntries().get(0).onModel().getProperties());

        STEP("3. List node version using skipCount(1),maxItems(1) and include(properties) GET /nodes/{nodeId}/versions?include=properties&skipCount=1&maxItems=1");
        versionListing = restClient.withCoreAPI().usingParams("include=properties&skipCount=1&maxItems=1").usingNode(file2).listVersionHistory();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        // we expect only version 1.1 to be retrieved with the value for properties.
        RestVersionModel version = versionListing.getEntries().get(0).onModel();
        assertNotNull(version.getProperties());
        assertEquals(version.getId(), "1.1");
        assertEquals(versionListing.getPagination().getMaxItems(), 1);
        assertEquals(versionListing.getPagination().getSkipCount(), 1);
        assertEquals(versionListing.getPagination().getCount(), 1);

        STEP("4. Get version information for version 1.1 GET /nodes/{nodeId}/versions/{versionId} .");
        RestVersionModel version11 = restClient.withCoreAPI().usingNode(file2).getVersionInformation("1.1");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        // aspectNames and properties are displayed by default. We compare
        // id,contentSize and name with the values from v1.1, taken from listing all
        // versions.
        assertNotNull(version11.getAspectNames());
        assertNotNull(version11.getProperties());
        assertEquals(version.getId(), version11.getId());
        assertEquals(version.getContent().getSizeInBytes(), version11.getContent().getSizeInBytes());
        assertEquals(version.getName(), version11.getName());

        STEP("5. Retrieve version 2.0 content GET /nodes/{nodeId}/versions/{versionId}/content");
        // verify the content is the same as the uploaded file and check in headers for
        // Content-Disposition to validate the download as attachment and fileName.
        // wait for content to be picked up on AWS QS stacks
        Utility.sleep(500, 60000, () -> {
            RestResponse versionContent = restClient.withCoreAPI().usingNode(file2).getVersionContent("2.0");
            restClient.assertStatusCodeIs(HttpStatus.OK);

            assertEquals("Sample text.\n", versionContent.getResponse().body().asString());
            restClient.assertHeaderValueContains("Content-Disposition", "attachment");
            restClient.assertHeaderValueContains("Content-Disposition", String.format("filename=\"%s\"", file2.getName()));
        });
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Test revert and delete a node version.")
    public void testRevertDeleteVersion() throws Exception
    {
        STEP("1. Revert to version 1.0 POST /nodes/{nodeId}/versions/{versionId}/revert");
        RestVersionModel version = restClient.authenticateUser(user2).withCoreAPI().usingNode(file2).revertVersion("1.0", new String("{}"));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        String nodeVersionType = new JSONObject(version.toJson()).getJSONObject("properties").getString("cm:versionType");
        assertEquals(nodeVersionType, "MINOR");
        String nodeVersion = version.getId();

        STEP("2. Revert to last minor version /nodes/{nodeId}/versions/{versionId}/revert");
        version = restClient.withCoreAPI().usingNode(file2).revertVersion(nodeVersion, new String("{\"majorVersion\": true}"));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        nodeVersionType = new JSONObject(version.toJson()).getJSONObject("properties").getString("cm:versionType");
        assertEquals(nodeVersionType, "MAJOR");
        assertTrue(nodeVersion.charAt(0) + 1 == version.getId().charAt(0));

        STEP("3. Delete last MINOR version DELETE /nodes/{nodeId}/versions/{versionId}");
        restClient.withCoreAPI().usingNode(file2).deleteNodeVersion(nodeVersion);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingNode(file2).getVersionInformation(nodeVersion);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Verify file name in Content Range header")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    public void checkFileNameContentRangeHeader() throws Exception
    {
        restClient.configureRequestSpec().addHeader("content-range", "bytes=1-10");
        restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).getNodeContent();
        restClient.assertStatusCodeIs(HttpStatus.PARTIAL_CONTENT);
        restClient.assertHeaderValueContains("content-range", "bytes 1-10");
        restClient.assertHeaderValueContains("content-length", String.valueOf(10));
    }


    private void createRandomFileInDirectory(String path, String fileName, int size) throws IOException {
        String fullPath = new File(path, fileName).getPath();

        RandomAccessFile file = new RandomAccessFile(fullPath,"rw");
        file.setLength(size);
        file.close();
    }
}
