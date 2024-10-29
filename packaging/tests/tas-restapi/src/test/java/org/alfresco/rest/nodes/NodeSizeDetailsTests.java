package org.alfresco.rest.nodes;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestSizeDetailsModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;

public class NodeSizeDetailsTests extends RestTest
{
    private UserModel user1;
    private SiteModel siteModel;
    private FolderModel folder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        user1 = dataUser.createRandomTestUser("User-1");
        siteModel = dataSite.usingUser(user1).createPublicRandomSite();
    }

    /**
     * Sanity check for the following api endpoint POST /nodes/{nodeId}/size-details GET /nodes/{nodeId}/size-details/{jobId}
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void createSizeDetails() throws Exception
    {

        STEP("Create a folder in the test site.");
        folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("Upload a text document to the folder.");
        restClient.authenticateUser(user1).configureRequestSpec().addMultiPart("filedata", Utility.getResourceTestDataFile("sampleContent.txt"));
        RestNodeModel fileNode = restClient.withCoreAPI().usingNode(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        fileNode.assertThat().field("id").isNotNull()
                .and().field("name").is("sampleContent.txt")
                .and().field("content.mimeType").is(FileType.TEXT_PLAIN.mimeType);

        RestSizeDetailsModel restSizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();

        String jobId = restSizeDetailsModel.getJobId();
        restSizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).getSizeDetails(jobId);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSizeDetailsModel.assertThat().field("sizeInBytes").isNotEmpty();
        restSizeDetailsModel.assertThat().field("sizeInBytes").isGreaterThan(0);

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
    public void performanceTestCase() throws InterruptedException
    {
        STEP("1. Create a parent folder in the test site.");
        FolderModel folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Creating a 200 nested folders in the folder-1");

        IntStream.rangeClosed(1, 200).forEach(i -> {
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

        RestSizeDetailsModel restSizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();
        String jobId = restSizeDetailsModel.getJobId();

        Utility.sleep(2000, 60000, () -> {
            RestSizeDetailsModel sizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).getSizeDetails(jobId);
            restClient.assertStatusCodeIs(HttpStatus.OK);
            sizeDetailsModel.assertThat().field("sizeInBytes").isNotEmpty();
            sizeDetailsModel.assertThat().field("sizeInBytes").isGreaterThan(0);
        });

    }
}
