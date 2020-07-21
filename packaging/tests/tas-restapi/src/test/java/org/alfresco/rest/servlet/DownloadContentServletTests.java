package org.alfresco.rest.servlet;

import io.restassured.RestAssured;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DownloadContentServletTests extends RestTest
{
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FILENAME_HEADER = "filename=\"%s\"";
    private static final String ATTACHMENT = "attachment";
    private static final String FILE_CONTENT = "The content of the file.";

    private static String downloadContentServletAttach = "alfresco/d/a/workspace/SpacesStore/";
    private static String downloadContentServletDirect = "alfresco/d/d/workspace/SpacesStore/";

    private UserModel testUser;
    private FileModel testContentFile;
    private String authHeaderEncoded;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        SiteModel testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        FolderModel testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        testContentFile = dataContent.usingUser(testUser).usingResource(testFolder)
            .createContent(new FileModel("hotOuside", FileType.TEXT_PLAIN, FILE_CONTENT));

        String authHeader = String.format("%s:%s", testUser.getUsername(), testUser.getPassword());
        authHeaderEncoded = new String(Base64.encodeBase64(authHeader.getBytes()));

        RestAssured.basePath = "";
        restClient.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using short descriptor and attach short descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSShortAttachShort()
    {
        authenticateTestUser();
        RestRequest request = RestRequest.simpleRequest(
            HttpMethod.GET, downloadContentServletAttach + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using short descriptor and attach long descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSShortAttachLong()
    {
        authenticateTestUser();
        String downloadContentServletAttachLong = "alfresco/d/attach/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentServletAttachLong + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using short descriptor and direct short descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSShortDirectShort()
    {
        authenticateTestUser();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentServletDirect + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using short descriptor and direct long descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSShortDirectLong()
    {
        authenticateTestUser();
        String downloadContentServletDirectLong = "alfresco/d/direct/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentServletDirectLong + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using long descriptor and attach short descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSLongAttachShort()
    {
        authenticateTestUser();
        String downloadContentLongServletAttach = "alfresco/download/a/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLongServletAttach + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using long descriptor and attach long descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSLongAttachLong()
    {
        authenticateTestUser();
        String downloadContentLongServletAttachLong = "alfresco/download/attach/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLongServletAttachLong + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using long descriptor and direct short descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSLongDirectShort()
    {
        authenticateTestUser();
        String downloadContentLongServletDirect = "alfresco/download/d/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLongServletDirect + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using long descriptor and direct long descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSLongDirectLong()
    {
        authenticateTestUser();
        String downloadContentLongServletDirectLong = "alfresco/download/direct/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLongServletDirectLong + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using short descriptor and attach short uppercase descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSShortAttachUppercaseShort()
    {
        authenticateTestUser();
        String downloadContentAttachUppercase = "alfresco/d/A/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentAttachUppercase + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using short descriptor and direct short uppercase descriptor.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSShortDirectUppercaseShort()
    {
        authenticateTestUser();
        String downloadContentDirectUppercase = "alfresco/d/D/workspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentDirectUppercase + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        RestResponse response = restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertEquals(FILE_CONTENT, response.getResponse().body().asString());
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, ATTACHMENT);
        restClient.assertHeaderValueContains(CONTENT_DISPOSITION, String.format(FILENAME_HEADER, testContentFile.getName()));
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using attach without specifying {storeType}.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSAttachWithoutStoreType()
    {
        authenticateTestUser();
        String downloadContentLessPathAttach = "alfresco/d/a/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLessPathAttach + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using direct without specifying {storeType}.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSDirectWithoutStoreType()
    {
        authenticateTestUser();
        String downloadContentLessPathDirect = "alfresco/d/d/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLessPathDirect + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using direct without specifying {storeType}.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSDirectWithInvalidStoreType()
    {
        authenticateTestUser();
        String downloadContentLessPathDirect = "alfresco/download/d/badWorkspace/SpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLessPathDirect + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using direct without specifying {storeType}.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSDirectWithInvalidStoreId()
    {
        authenticateTestUser();
        String downloadContentLessPathDirect = "alfresco/download/d/workspace/badSpacesStore/";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentLessPathDirect + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using attach without authentication.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSAttachWithoutAuthentication()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentServletAttach + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API },
        executionType = ExecutionType.REGRESSION,
        description = "Verify DownloadContentServlet retrieve content using direct without authentication.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug(id ="MNT-21602", status=Bug.Status.FIXED)
    public void verifyDCSDirectWithoutAuthentication()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            downloadContentServletDirect + testContentFile.getNodeRef() + "/" + testContentFile.getName());
        restClient.process(request);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    private void authenticateTestUser()
    {
        restClient.configureRequestSpec()
            .addHeader("Authorization", String.format("Basic %s", authHeaderEncoded))
            .build();
    }
}
