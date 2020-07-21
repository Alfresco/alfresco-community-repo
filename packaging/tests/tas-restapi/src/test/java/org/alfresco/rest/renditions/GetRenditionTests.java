package org.alfresco.rest.renditions;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.report.Bug.Status;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Handles tests related to GET api-explorer/#!/renditions
 * @author Cristina Axinte
 *
 */
@Test(groups = {TestGroup.RENDITIONS})
public class GetRenditionTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FileModel file1;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        file1 = dataContent.usingUser(user).usingSite(site).createContent(DocumentType.TEXT_PLAIN);
    }

    @Bug(id = "REPO-2449", status = Status.FIXED)
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY, TestGroup.NOT_SUPPORTED_BY_ATS })
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY, 
        description = "Verify that ZIP document preview is rendered")
    public void verifyPreviewOfZipFile() throws Exception
    {
        STEP("1. Create a folder in existing site");
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder = dataContent.usingUser(user).usingSite(site).createFolder(folder);

        STEP("2. Upload a local ZIP file using RESTAPI");
        restClient.authenticateUser(user).configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("content-zip-test.zip"));

        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        FileModel file = new FileModel("content-zip-test.zip");
        file.setCmisLocation(folder.getCmisLocation() + "/content-zip-test.zip");
        file.setNodeRef(fileNode.getId());

        STEP("3. Create preview of ZIP file using RESTAPI");
        restClient.withCoreAPI().usingNode(file).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        STEP("4. Verify preview of ZIP file is created using RESTAPI");
        RestRenditionInfoModel renditionInfo = restClient.withCoreAPI().usingNode(file).getNodeRenditionUntilIsCreated("pdf");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        renditionInfo.assertThat().field("status").is("CREATED");
    }
    
    @Bug(id = "REPO-2485", status = Status.FIXED)
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY, TestGroup.NOT_SUPPORTED_BY_ATS })
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY, 
        description = "Verify that ZIP document thumbnail is rendered")
    public void verifyThumbnailOfZipFile() throws Exception
    {
        STEP("1. Create a folder in existing site");
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder = dataContent.usingUser(user).usingSite(site).createFolder(folder);

        STEP("2. Upload a local ZIP file using RESTAPI");
        restClient.authenticateUser(user).configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("content-zip-test.zip"));

        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        FileModel file = new FileModel("content-zip-test.zip");
        file.setCmisLocation(folder.getCmisLocation() + "/content-zip-test.zip");
        file.setNodeRef(fileNode.getId());

        STEP("3. Create thumbnail of ZIP file using RESTAPI");
        restClient.withCoreAPI().usingNode(file).createNodeRendition("doclib");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        STEP("4. Verify thumbnail of ZIP file is created and has content using RESTAPI");
        RestRenditionInfoModel renditionInfo = restClient.withCoreAPI().usingNode(file).getNodeRenditionUntilIsCreated("doclib");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        renditionInfo.assertThat().field("status").is("CREATED");
        renditionInfo.assertThat().field("content.sizeInBytes").isGreaterThan(120);
    }

    /**
     * Sanity test for the following endpoint:
     * GET /nodes/{nodeId}/renditions/{renditionId}/content
     * @throws Exception
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY, TestGroup.NOT_SUPPORTED_BY_ATS })
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY,
        description = "Verify that the rendition content can be downloaded using GET /nodes/{nodeId}/renditions/{renditionId}/content")
    public void getRenditionContent() throws Exception
    {
        STEP("1. Create a folder in existing site");
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder = dataContent.usingUser(user).usingSite(site).createFolder(folder);

        STEP("2. Upload a local txt file using RESTAPI");
        restClient.authenticateUser(user).configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("iso8859File.txt"));

        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        FileModel file = new FileModel("iso8859File.txt");
        file.setCmisLocation(folder.getCmisLocation() + "/iso8859File.txt");
        file.setNodeRef(fileNode.getId());

        STEP("3. Create thumbnail of txt file using RESTAPI");
        restClient.withCoreAPI().usingNode(file).createNodeRendition("doclib");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        STEP("4. Verify thumbnail of txt file is created and has content using RESTAPI");
        RestResponse restResponse = restClient.withCoreAPI().usingNode(file).getNodeRenditionContentUntilIsCreated("doclib");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Type","image/png;charset=UTF-8");
        Assert.assertTrue(restResponse.getResponse().body().asInputStream().available() > 0);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY})
    @TestRail(section = {TestGroup.REST_API, TestGroup.RENDITIONS}, executionType = ExecutionType.SANITY,
            description = "Verify the Range request header using GET /nodes/{nodeId}/renditions/{renditionId}/content")
    public void getVerifyRangeRequestHeader() throws Exception
    {
        STEP("1. Create thumbnail on file");
        restClient.authenticateUser(user).withCoreAPI().usingNode(file1).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        STEP("2. Make GET rendition content using content-range header");
        Utility.sleep(500, 30000, () -> {
            restClient.configureRequestSpec().addHeader("content-range", "bytes=1-10");
            restClient.authenticateUser(user).withCoreAPI().usingNode(file1).getNodeRenditionContent("pdf");
            restClient.assertStatusCodeIs(HttpStatus.PARTIAL_CONTENT);
            restClient.assertHeaderValueContains("content-range", "bytes 1-10");
            restClient.assertHeaderValueContains("content-length", String.valueOf(10));
        });
    }
}

