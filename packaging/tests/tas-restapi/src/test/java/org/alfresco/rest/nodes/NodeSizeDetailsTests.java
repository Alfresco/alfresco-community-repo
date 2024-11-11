package org.alfresco.rest.nodes;

import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestSizeDetailsModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import static org.alfresco.utility.report.log.Step.STEP;

public class NodeSizeDetailsTests extends RestTest
{
    private UserModel user1;
    private SiteModel siteModel;
    private FolderModel folder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        user1=dataUser.getAdminUser();
        siteModel = dataSite.usingUser(user1).createPublicRandomSite();
        folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());
    }

    /**
     *
     * Unauthenticated user not able to execute POST /nodes/{nodeId}/size-details: 401 STATUS CODE
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void unauthenticatedUserIsNotAbleGetSizeDetails()
    {
        restClient.authenticateUser(new UserModel("random user", "random password"));
        restClient.withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Node Id Not Exist: 404 STATUS CODE
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void nodeIdNotExist()
    {
        folder.setNodeRef(RandomStringUtils.randomAlphanumeric(20));
        restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    /**
     *
     * Value of nodeId is invalid: 422 STATUS CODE
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void nodeIdNotValid()
    {
        FileModel document = dataContent.usingSite(siteModel).usingUser(user1).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(user1).withCoreAPI().usingNode(document).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     *
     * Performance testCase
     */
    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void calculateNodeSizeForMultipleFiles() throws InterruptedException
    {
        STEP("1. Create a parent folder in the test site.");
        FolderModel folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Creating a 51 nested folders in the folder-1");
        AtomicLong fileSize = new AtomicLong(0);

        IntStream.rangeClosed(1, 5).forEach(i -> {
            String folder0Name = "childFolder" + i + RandomStringUtils.randomAlphanumeric(2);
            FolderModel folderModel = new FolderModel();
            folderModel.setName(folder0Name);

            FolderModel childFolder = dataContent.usingUser(user1)
                    .usingSite(siteModel)
                    .usingResource(folder)
                    .createFolder(folderModel);

            STEP("Upload a text document to the childFolders.");
            restClient.authenticateUser(user1)
                    .configureRequestSpec()
                    .addMultiPart("filedata", Utility.getResourceTestDataFile("sampleLargeContent.txt"));
            fileSize.addAndGet(Utility.getResourceTestDataFile("sampleLargeContent.txt").length());
            RestNodeModel newNode = restClient.authenticateUser(user1)
                    .withCoreAPI()
                    .usingNode(childFolder)
                    .createNode();

            restClient.assertStatusCodeIs(HttpStatus.CREATED);

            newNode.assertThat()
                    .field("id")
                    .isNotNull()
                    .and()
                    .field("name")
                    .is("sampleLargeContent.txt")
                    .and()
                    .field("content.mimeType")
                    .is(FileType.TEXT_PLAIN.mimeType);
        });

        STEP("Wait for 30 seconds so that the content is indexed in Search Service.");
        Thread.sleep(30000);

        RestSizeDetailsModel restSizeDetailsModel = restClient
                .authenticateUser(user1)
                .withCoreAPI()
                .usingNode(folder)
                .executeSizeDetails();

        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();

        String jobId = restSizeDetailsModel.getJobId();

        STEP("Wait for 3 seconds for the processing to complete.");
        Thread.sleep(3000);

        RestSizeDetailsModel sizeDetailsModel = restClient
                .withCoreAPI()
                .usingNode(folder)
                .getSizeDetails(jobId);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        sizeDetailsModel.assertThat().field("sizeInBytes").isNotEmpty();
        Assert.assertEquals(sizeDetailsModel.getSizeInBytes(),fileSize.get(),"Value of sizeInBytes " + sizeDetailsModel.getSizeInBytes() + " is not equal to " + fileSize);
    }

    @AfterClass(alwaysRun=true)
    public void cleanup() throws Exception
    {
        dataSite.usingAdmin().deleteSite(siteModel);
    }
}
