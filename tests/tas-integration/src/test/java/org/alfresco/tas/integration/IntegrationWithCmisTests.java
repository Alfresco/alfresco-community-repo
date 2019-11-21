package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import io.restassured.RestAssured;
import java.util.HashMap;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class IntegrationWithCmisTests extends IntegrationTest
{
    private UserModel user;
    private SiteModel site;

    @BeforeClass(alwaysRun = true)
    public void createUserAndSite() throws DataPreparationException
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

//    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CMIS, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.CMIS }, executionType = ExecutionType.REGRESSION, description = "Verify getChildren action for a large number of files from CMIS returns only unique values with few retries")
    public void verifyGetChildrenReturnsUniqueValues() throws Exception
    {
        STEP("1. Create user, site, folder.");
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder = dataContent.usingUser(user).usingSite(site).createFolder(folder);

        STEP("2. Create 5000 files in folder using webscript");
        int totalFiles = 5000;
        String fileCreationWebScript = "alfresco/s/api/model/filefolder/load";
        HashMap<String, String> input = new HashMap<String, String>();
        input.put("folderPath", folder.getCmisLocation());
        input.put("fileCount", String.valueOf(totalFiles));
        String postBody = JsonBodyGenerator.keyValueJson(input);

        RestAssured.basePath = "";
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, fileCreationWebScript);
        RestResponse response = restAPI.authenticateUser(user).process(request);
        Assert.assertEquals(response.getResponse().getStatusCode(), HttpStatus.OK.value());

        STEP("3. Verify getChildren from CMIS returns unique values");
        cmisAPI.authenticateUser(user).usingSite(site).usingResource(folder);
        for (int i = 1; i <= 20; i++)
        {
            System.out.println(String.format("Try no: %d", i));
            cmisAPI.usingResource(folder).assertThat().hasUniqueChildren(totalFiles);
        }
    }

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CMIS, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.CMIS }, executionType = ExecutionType.REGRESSION, description = "Verify content and thumbnail of TIF files are retrieved by CMIS ")
    @Bug(id = "REPO-2042", description = "Should fail only on MAC OS System and Linux")
    public void verifyContentAndThumbnailForTifFile() throws Exception
    {
        STEP("1. Create user, site and a folder ");
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder = dataContent.usingUser(user).usingSite(site).createFolder(folder);

        STEP("2. Upload existing TIF file using RESTAPI");
        restAPI.authenticateUser(user).configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("my-file.tif"));

        RestNodeModel fileNode = restAPI.authenticateUser(user).withCoreAPI().usingNode(folder).createNode();
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        FileModel file = new FileModel("my-file.tif");
        file.setCmisLocation(folder.getCmisLocation() + "/my-file.tif");
        file.setNodeRef(fileNode.getId());

        STEP("3. Create thumbnail and content of TIF files using file");
        restAPI.withCoreAPI().usingNode(file).createNodeRendition("pdf");
        restAPI.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restAPI.withCoreAPI().usingNode(file).createNodeRendition("doclib");
        restAPI.assertStatusCodeIs(HttpStatus.ACCEPTED);

        STEP("4. Verify thumbnail and content of TIF files are created using RESTAPI");
        cmisAPI.authenticateUser(user).usingSite(site).usingResource(folder).usingResource(file).assertThat().contentContains("Adobe Photoshop CC 2015");
        RestRenditionInfoModel renditionInfo = restAPI.withCoreAPI().usingNode(file).getNodeRendition("pdf");
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        renditionInfo.assertThat().field("status").is("CREATED");
        renditionInfo = restAPI.withCoreAPI().usingNode(file).getNodeRendition("doclib");
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        renditionInfo.assertThat().field("status").is("CREATED");
    }

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CMIS, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.CMIS }, executionType = ExecutionType.REGRESSION, description = "Verify getChildren action for a large number of files from CMIS returns only unique values with few retries")
    public void verifyContentDispositionForContentThatAreWhiteListed() throws Exception
    {

        STEP("1. Create a .pdf and a .html file in the Shared folder in CMIS");
        FolderModel sharedFolder = FolderModel.getSharedFolderModel();
        FileModel pdfFile = FileModel.getRandomFileModel(FileType.PDF);
        FileModel htmlFile = FileModel.getRandomFileModel(FileType.HTML);

        STEP("2. Upload the .pdf file and verify the reponse header adding the download=inline/attachement parameters (accepted both).");
        cmisAPI.authenticateUser(user).usingResource(sharedFolder).createFile(pdfFile);

        RestResponse response = restAPI.authenticateUser(user).withCMISApi().getRootObjectByLocation(pdfFile);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().header("Content-Disposition", String.format("inline; filename=%s", pdfFile.getName()));

        response = restAPI.authenticateUser(user).withCMISApi().usingParams("download=inline").getRootObjectByLocation(pdfFile);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().header("Content-Disposition", String.format("inline; filename=%s", pdfFile.getName()));

        response = restAPI.authenticateUser(user).withCMISApi().usingParams("download=attachment").getRootObjectByLocation(pdfFile);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().header("Content-Disposition", String.format("attachment; filename=%s", pdfFile.getName()));

        STEP("3. Upload the .html file and verify the reponse header adding the download=inline/attachement parameters (accepted only attachment) .");
        cmisAPI.usingResource(sharedFolder).createFile(htmlFile);

        response = restAPI.authenticateUser(user).withCMISApi().getRootObjectByLocation(htmlFile);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().header("Content-Disposition", String.format("attachment; filename=%s", htmlFile.getName()));

        response = restAPI.authenticateUser(user).withCMISApi().usingParams("download=attachment").getRootObjectByLocation(htmlFile);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().header("Content-Disposition", String.format("attachment; filename=%s", htmlFile.getName()));

        response = restAPI.authenticateUser(user).withCMISApi().usingParams("download=inline").getRootObjectByLocation(htmlFile);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().header("Content-Disposition", String.format("attachment; filename=%s", htmlFile.getName()));
    }

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CMIS, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.CMIS }, executionType = ExecutionType.SANITY, description = "Verify that alfresco returns the correct encoding for files created via CMIS.")
    public void verifyFileEncodingUsingCMIS() throws Exception
    {
        STEP("1. Create a folder, two text file with specific encoding content and define the expected encoding.");
        FileModel utf8File = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, " ∮ E⋅da = Q");
        FileModel iso8859File = FileModel.getRandomFileModel(FileType.TEXT_PLAIN,
                "<html><head><title>aegif Mind Share Leader Generating New Paradigms by aegif corporation</title></head><body><p> Test html</p></body></html></body></html>");
        String utf8Type = "text/plain;charset=UTF-8";
        String iso8859Type = "text/plain;charset=ISO-8859-1";

        STEP("2. Upload the files via CMIS.");
        cmisAPI.authenticateUser(user).usingSite(site).createFile(utf8File);
        cmisAPI.createFile(iso8859File);

        String fileCreationWebScript = "alfresco/service/api/node/content/workspace/SpacesStore/";
        RestAssured.basePath = "";

        STEP("3. Retrieve the nodes via webscripts and verify that the content type is the expected one (GET alfresco/service/api/node/content/workspace/SpacesStore/).");
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, fileCreationWebScript + utf8File.getNodeRefWithoutVersion());
        RestResponse response = restAPI.authenticateUser(user).process(request);
        Assert.assertEquals(response.getResponse().getStatusCode(), HttpStatus.OK.value());
        Assert.assertEquals(response.getResponse().getContentType(), utf8Type);

        request = RestRequest.simpleRequest(HttpMethod.GET, fileCreationWebScript + iso8859File.getNodeRefWithoutVersion());
        response = restAPI.process(request);
        Assert.assertEquals(response.getResponse().getStatusCode(), HttpStatus.OK.value());
        Assert.assertEquals(response.getResponse().getContentType(), iso8859Type);

        // Commented the use of v1 RestAPI GET (nodes/{nodeId}), because it works on alfresco 5.2.N or above.
        // STEP("3. Retrieve the nodes and verify that the content type is the expected one (GET nodes/{nodeId}).");
        // restAPI.authenticateUser(user).withCoreAPI().usingNode(utf8File).getNodeContent().assertThat().contentType(utf8Type);
        // restAPI.authenticateUser(user).withCoreAPI().usingNode(iso8859File).getNodeContent().assertThat().contentType(iso8859Type);

    }
}
