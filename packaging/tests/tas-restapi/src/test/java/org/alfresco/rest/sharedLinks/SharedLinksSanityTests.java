package org.alfresco.rest.sharedLinks;

import javax.json.Json;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.rest.model.RestSharedLinksModel;
import org.alfresco.rest.model.RestSharedLinksModelCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class includes Sanity tests for the shared-links api. Detailed tests would be covered in the alfresco-remote-api test project
 * 
 * @author meenal bhave
 */
public class SharedLinksSanityTests extends RestTest
{
    private UserModel adminUser;
    private UserModel testUser1;

    private SiteModel siteModel1;

    private FolderModel folder1;

    private FileModel file1;
    private FileModel file2;
    private FileModel file3;
    private FileModel file4;
    private FileModel file5;
    private FileModel file6;
    private FileModel file7;
    private FileModel file8;

    private RestSharedLinksModel sharedLink1;
    private RestSharedLinksModel sharedLink2;
    private RestSharedLinksModel sharedLink3;
    private RestSharedLinksModel sharedLink4;
    private RestSharedLinksModel sharedLink5;
    private RestSharedLinksModel sharedLink6;
    private RestSharedLinksModel sharedLink7;
    private RestSharedLinksModel sharedLink8;
    private RestRenditionInfoModel nodeRenditionInfo;

    private RestSharedLinksModelCollection sharedLinksCollection;
    private RestRenditionInfoModelCollection nodeRenditionInfoCollection;

    private String expiryDate = "2027-03-23T23:00:00.000+0000";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {

        adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);

        // Create Standard User
        testUser1 = dataUser.usingUser(adminUser).createRandomTestUser();

        // Create Site
        siteModel1 = dataSite.usingUser(testUser1).createPublicRandomSite();

        folder1 = dataContent.usingUser(adminUser).usingSite(siteModel1).createFolder();

        file1 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        file2 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        file3 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        file4 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        file5 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        file7 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        file8 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        // Create file6 based on existing resource
        FileModel newFile = FileModel.getFileModelBasedOnTestDataFile("sampleContent.txt");
        newFile.setName("sampleContent.txt");
        file6 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(newFile);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.SANITY, description = "Verify create sharedLinks without Path")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.SANITY })
    public void testCreateAndGetSharedLinks() throws Exception
    {
        // Post without includePath
        sharedLink1 = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().createSharedLink(file1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.onResponse().assertThat().body("entry.nodeId", org.hamcrest.Matchers.equalTo(file1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.name", org.hamcrest.Matchers.equalTo(file1.getName()));
        restClient.onResponse().assertThat().body("entry.id", org.hamcrest.Matchers.equalTo(sharedLink1.getId()));
        restClient.onResponse().assertThat().body("entry.path", org.hamcrest.Matchers.nullValue());

        // Same Checks above using sharedLink methods: GET sharedLink: without includePath
        Assert.assertEquals(sharedLink1.getNodeId(), file1.getNodeRefWithoutVersion());
        Assert.assertEquals(sharedLink1.getName(), file1.getName());
        Assert.assertNull(sharedLink1.getPath(), "Path is expected to be null for noauth api: Response shows: " + sharedLink1.toJson());

        // Get without includePath
        restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLink(sharedLink1);
        restClient.onResponse().assertThat().body("entry.nodeId", org.hamcrest.Matchers.equalTo(file1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.name", org.hamcrest.Matchers.equalTo(file1.getName()));
        restClient.onResponse().assertThat().body("entry.id", org.hamcrest.Matchers.equalTo(sharedLink1.getId()));
        restClient.onResponse().assertThat().body("entry.path", org.hamcrest.Matchers.nullValue());

        /*
         * Get all shared-links while allowing indexing to complete and check
         * that the created shared-link is displayed
         */
        Utility.sleep(500, 30000, () ->
            {
                sharedLinksCollection = restClient.withCoreAPI().usingSharedLinks().getSharedLinks();
                restClient.assertStatusCodeIs(HttpStatus.OK);
                sharedLinksCollection.assertThat().entriesListContains("id", sharedLink1.getId()).and()
                                                  .entriesListContains("nodeId", sharedLink1.getNodeId());
            });
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.REGRESSION, description = "Verify create sharedLinks with Path")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.REGRESSION })
    public void testCreateAndGetSharedLinksWithInclude() throws Exception
    {
        // Post with includePath
        sharedLink2 = restClient.authenticateUser(testUser1).withCoreAPI().includePath().usingSharedLinks().createSharedLink(file2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.onResponse().assertThat().body("entry.nodeId", org.hamcrest.Matchers.equalTo(file2.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.name", org.hamcrest.Matchers.equalTo(file2.getName()));
        restClient.onResponse().assertThat().body("entry.id", org.hamcrest.Matchers.equalTo(sharedLink2.getId()));
        restClient.onResponse().assertThat().body("entry.path", org.hamcrest.Matchers.notNullValue());

        // Same Checks above using sharedLink methods: POST sharedLink: includePath
        Assert.assertEquals(sharedLink2.getNodeId(), file2.getNodeRefWithoutVersion());
        Assert.assertEquals(sharedLink2.getName(), file2.getName());
        Assert.assertNotNull(sharedLink2.getPath(), "Path not expected to be null for noauth api: Response shows: " + sharedLink1.toJson());

        // Get with includePath
        restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLink(sharedLink2);
        restClient.onResponse().assertThat().body("entry.nodeId", org.hamcrest.Matchers.equalTo(file2.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.name", org.hamcrest.Matchers.equalTo(file2.getName()));
        restClient.onResponse().assertThat().body("entry.id", org.hamcrest.Matchers.equalTo(sharedLink2.getId()));
        // Verify that path is null since includePath is not supported for this noAuth api
        restClient.onResponse().assertThat().body("entry.path", org.hamcrest.Matchers.nullValue());

        // Get: noAuth with includePath
        sharedLink2 = restClient.withCoreAPI().usingSharedLinks().getSharedLink(sharedLink2);
        restClient.onResponse().assertThat().body("entry.nodeId", org.hamcrest.Matchers.equalTo(file2.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.name", org.hamcrest.Matchers.equalTo(file2.getName()));
        restClient.onResponse().assertThat().body("entry.id", org.hamcrest.Matchers.equalTo(sharedLink2.getId()));
        // Verify that path is null since includePath is not supported for this noAuth api
        restClient.onResponse().assertThat().body("entry.path", org.hamcrest.Matchers.nullValue());

        // Same Checks above using sharedLink methods: GET sharedLink: noAuth
        Assert.assertEquals(sharedLink2.getNodeId(), file2.getNodeRefWithoutVersion());
        Assert.assertEquals(sharedLink2.getName(), file2.getName());
        Assert.assertNull(sharedLink2.getPath(), "Path is expected to be null for noauth api: Response shows: " + sharedLink2.toJson());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.SANITY, description = "Verify delete sharedLinks with and without Path")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.SANITY})
    public void testDeleteSharedLinks() throws Exception
    {
        sharedLink3 = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().createSharedLink(file3);
        restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().deleteSharedLink(sharedLink3);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        sharedLink4 = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().createSharedLink(file4);
        restClient.authenticateUser(testUser1).withCoreAPI().includePath().usingSharedLinks().deleteSharedLink(sharedLink4);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.SANITY, description = "Sanity tests for GET {sharedId}/content and POST {sharedId}/email endpoints")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.SANITY })
    public void testGetSharedLinkContentAndPostEmail() throws Exception
    {
        // Create shared-link
        sharedLink6 = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().createSharedLink(file6);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        // Make GET {sharedId}/content and check file content
        restClient.withCoreAPI().usingSharedLinks().getSharedLinkContent(sharedLink6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Disposition", file6.getName());
        restClient.onResponse().getResponse().body().asString().contains("Sample text.");

        // Make POST {sharedId}/email to send email with created shared-link
        String postBody = Json.createObjectBuilder().add("client", "share")
                                                    .add("recipientEmails", "john.doe@acme.com")
                                                    .build().toString();
        restClient.withCoreAPI().usingSharedLinks().sendSharedLinkEmail(sharedLink6, postBody);
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.REGRESSION, description = "Verify get sharedLink/content and get/renditions")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.REGRESSION, TestGroup.RENDITIONS })
    public void testCreateWithExpiryDateAndGetSharedLinkRendition() throws Exception
    {
        sharedLink5 = restClient.authenticateUser(testUser1).withCoreAPI().includePath().usingSharedLinks().createSharedLinkWithExpiryDate(file5, expiryDate);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        Assert.assertEquals(sharedLink5.getExpiresAt(), expiryDate);
        Assert.assertNotNull(sharedLink5.getPath(), "Path not expected to be null: Response shows: " + sharedLink5.toJson());

        restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLinkRenditions(sharedLink5);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    /**
     * ATS does not support text -> img (doclib)
     */
    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.SANITY, description = "Sanity tests for GET /renditions, GET /renditions/{renditionId} and GET /renditions/{renditionId}/content endpoints")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.SANITY, TestGroup.RENDITIONS, TestGroup.NOT_SUPPORTED_BY_ATS })
    public void testGetSharedLinkRendition() throws Exception
    {
        sharedLink7 = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().createSharedLink(file7);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingNode(file7).createNodeRenditionIfNotExists("doclib");
        restClient.withCoreAPI().usingNode(file7).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        // GET /renditions: wait until all renditions are created and GET all entries
        Utility.sleep(500, 50000, () ->
        {
            nodeRenditionInfoCollection = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLinkRenditions(sharedLink7);
            restClient.assertStatusCodeIs(HttpStatus.OK);

            nodeRenditionInfoCollection.assertThat().entriesListCountIs(2);
            nodeRenditionInfoCollection.getEntryByIndex(0).assertThat().field("id").is("doclib").and()
                                                                       .field("status").is("CREATED");
            nodeRenditionInfoCollection.getEntryByIndex(1).assertThat().field("id").is("pdf").and()
                                                                       .field("status").is("CREATED");
        });

        // GET /renditions/{renditionId}: get specific rendition information for the file with shared link
        nodeRenditionInfo = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLinkRendition(sharedLink7, "pdf");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        nodeRenditionInfo.assertThat().field("id").is("pdf").and()
                                      .field("status").is("CREATED");

        // GET /renditions/{renditionId}/content: get the rendition content for file with shared link
        restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLinkRenditionContent(sharedLink7, "pdf");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Type","application/pdf;charset=UTF-8");
        Assert.assertTrue(restClient.onResponse().getResponse().body().asInputStream().available() > 0);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SHAREDLINKS }, executionType = ExecutionType.SANITY, description = "Sanity tests for Range reuest header on   GET shared-links/{sharedId}/renditions/{renditionId}/content endpoints")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.SANITY, TestGroup.RENDITIONS })
    public void testGetVerifyRangeReguestOnSharedLinks() throws Exception
    {
        sharedLink8 = restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().createSharedLink(file8);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingNode(file8).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        // GET /renditions/{renditionId}/content: get the Range request header for file with shared links endpoints.
        Utility.sleep(500, 30000, () ->
        {
            restClient.configureRequestSpec().addHeader("content-range", "bytes=1-10");
            restClient.authenticateUser(testUser1).withCoreAPI().usingSharedLinks().getSharedLinkRenditionContent(sharedLink8, "pdf");
            restClient.assertStatusCodeIs(HttpStatus.PARTIAL_CONTENT);
            restClient.assertHeaderValueContains("Content-Type","application/pdf;charset=UTF-8");
            restClient.assertHeaderValueContains("content-range", "bytes 1-10");
        });
    }
}