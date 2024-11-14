package org.alfresco.rest.nodes;

import static java.util.Objects.requireNonNull;

import static org.alfresco.utility.report.log.Step.STEP;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestSizeDetailsModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;

public class NodeSizeDetailsTests extends RestTest
{
    private UserModel user1;
    private SiteModel siteModel;
    private FolderModel folder;
    private String jobId;
    private FileModel sampleFileToCreate;
    private long sampleFileSize;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws IOException
    {
        user1 = dataUser.createRandomTestUser("User-1");
        siteModel = dataSite.usingUser(user1).createPublicRandomSite();
        folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());
        String fileName = "sampleLargeContent.txt";
        final byte[] sampleFileContent = getSampleFileContent(fileName);
        sampleFileSize = sampleFileContent.length;
        sampleFileToCreate = new FileModel(fileName, FileType.TEXT_PLAIN, new String(sampleFileContent));
    }

    /**
     * calculateNodeSizeForSingleFile testcase
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void calculateNodeSizeForSingleFile() throws Exception
    {

        STEP("1. Create a folder in the test site.");
        folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Upload a text document to the folder.");
        dataContent.usingUser(user1)
                .usingSite(siteModel)
                .usingResource(folder)
                .createContent(sampleFileToCreate);

        STEP("3. Wait for 30 seconds so that the content is indexed in Search Service.");
        Utility.waitToLoopTime(30);

        RestSizeDetailsModel restSizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();

        jobId = restSizeDetailsModel.getJobId();

        STEP("4. Wait for 5 seconds for the processing to complete.");
        Awaitility
                .await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Durations.ONE_SECOND)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    RestSizeDetailsModel sizeDetailsModel = restClient.authenticateUser(user1)
                            .withCoreAPI()
                            .usingNode(folder)
                            .getSizeDetails(jobId);
                    restClient.assertStatusCodeIs(HttpStatus.OK);
                    sizeDetailsModel.assertThat()
                            .field("sizeInBytes")
                            .isNotEmpty();
                    Assert.assertEquals(sizeDetailsModel.getSizeInBytes(), sampleFileSize,
                            "Value of sizeInBytes " + sizeDetailsModel.getSizeInBytes()
                                    + " is not equal to " + sampleFileSize);
                });
    }

    /**
     * checkJobIdPresentInCache testcase
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void checkJobIdPresentInCache() throws Exception
    {
        STEP("1. Verifying that same JobId is coming or not");
        RestSizeDetailsModel restSizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();
        Assert.assertEquals(restSizeDetailsModel.getJobId(), jobId, "jobId should be present in cache, actual :" + restSizeDetailsModel.getJobId() + " expected: " + jobId);
    }

    /**
     * checkSizeDetailsWithInvalidJobId testcase
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void checkSizeDetailsWithInvalidJobId() throws Exception
    {

        STEP("1. Create a folder in the test site.");
        folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Upload a text document to the folder.");
        dataContent.usingUser(user1)
                .usingSite(siteModel)
                .usingResource(folder)
                .createContent(sampleFileToCreate);

        STEP("3. Wait for 30 seconds so that the content is indexed in Search Service.");
        Utility.waitToLoopTime(30);

        RestSizeDetailsModel restSizeDetailsModel = restClient.authenticateUser(user1).withCoreAPI().usingNode(folder).executeSizeDetails();
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();
        jobId = restSizeDetailsModel.getJobId();

        STEP("4. Adding random content to jobId ");
        jobId += RandomStringUtils.randomAlphanumeric(2);

        STEP("5. Wait for 10 seconds for the processing to complete.");
        Awaitility
                .await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Durations.ONE_SECOND)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    restClient.authenticateUser(user1)
                            .withCoreAPI()
                            .usingNode(folder)
                            .getSizeDetails(jobId);
                    restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
                });

    }

    /**
     * checkSizeDetailsWithoutExecuteSizeDetails testcase
     */

    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void checkSizeDetailsWithoutExecuteSizeDetails() throws Exception
    {

        STEP("1. Create a folder in the test site.");
        folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Upload a text document to the folder.");
        String status = "NOT_INITIATED";

        dataContent.usingUser(user1)
                .usingSite(siteModel)
                .usingResource(folder)
                .createContent(sampleFileToCreate);

        STEP("3. Wait for 30 seconds so that the content is indexed in Search Service.");
        Awaitility
                .await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Durations.ONE_SECOND)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    RestSizeDetailsModel sizeDetailsModel = restClient.authenticateUser(user1)
                            .withCoreAPI()
                            .usingNode(folder)
                            .getSizeDetails(jobId);
                    restClient.assertStatusCodeIs(HttpStatus.OK);
                    Assert.assertNotNull(sizeDetailsModel, "SizeDetailsModel should not be null");
                    sizeDetailsModel.assertThat().field("status").isNotEmpty();
                    Assert.assertEquals(sizeDetailsModel.getStatus().toString(), status, "Value of status should be same, actual :" + sizeDetailsModel.getStatus().toString() + " expected: " + status);
                });

    }

    /**
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
     * calculateNodeSizeForMultipleFiles testCase
     */
    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void calculateNodeSizeForMultipleFiles() throws InterruptedException
    {
        STEP("1. Create a parent folder in the test site.");
        FolderModel folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Creating a 5 nested folders in the folder-1");
        AtomicLong fileSize = new AtomicLong(0);

        IntStream.rangeClosed(1, 5).forEach(i -> {
            String folder0Name = "childFolder" + i + RandomStringUtils.randomAlphanumeric(2);
            FolderModel folderModel = new FolderModel();
            folderModel.setName(folder0Name);

            FolderModel childFolder = dataContent.usingUser(user1)
                    .usingSite(siteModel)
                    .usingResource(folder)
                    .createFolder(folderModel);

            STEP("3. Upload a text document to the childFolders.");
            dataContent.usingUser(user1)
                    .usingSite(siteModel)
                    .usingResource(childFolder)
                    .createContent(sampleFileToCreate);
            fileSize.addAndGet(sampleFileSize);
        });

        STEP("4. Wait for 30 seconds so that the content is indexed in Search Service.");
        Utility.waitToLoopTime(30);

        RestSizeDetailsModel restSizeDetailsModel = restClient
                .authenticateUser(user1)
                .withCoreAPI()
                .usingNode(folder)
                .executeSizeDetails();

        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();

        String jobId = restSizeDetailsModel.getJobId();

        STEP("5. Wait for 5 seconds for the processing to complete.");
        Awaitility
                .await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Durations.ONE_SECOND)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    RestSizeDetailsModel sizeDetailsModel = restClient.authenticateUser(user1)
                            .withCoreAPI()
                            .usingNode(folder)
                            .getSizeDetails(jobId);
                    restClient.assertStatusCodeIs(HttpStatus.OK);
                    sizeDetailsModel.assertThat()
                            .field("sizeInBytes")
                            .isNotEmpty();
                    Assert.assertEquals(sizeDetailsModel.getSizeInBytes(), fileSize.get(),
                            "Value of sizeInBytes " + sizeDetailsModel.getSizeInBytes()
                                    + " is not equal to " + fileSize.get());
                });
    }

    /**
     *
     * checkNumberOfFiles testCase
     */
    @TestRail(section = {TestGroup.REST_API, TestGroup.NODES}, executionType = ExecutionType.SANITY)
    @Test(groups = {TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void checkNumberOfFiles() throws InterruptedException
    {
        STEP("1. Create a parent folder in the test site.");
        FolderModel folder = dataContent.usingUser(user1).usingSite(siteModel).createFolder(FolderModel.getRandomFolderModel());

        STEP("2. Creating a 10 nested folders in the folder-1");

        IntStream.rangeClosed(1, 10).forEach(i -> {
            String folder0Name = "childFolder" + i + RandomStringUtils.randomAlphanumeric(2);
            FolderModel folderModel = new FolderModel();
            folderModel.setName(folder0Name);

            FolderModel childFolder = dataContent.usingUser(user1)
                    .usingSite(siteModel)
                    .usingResource(folder)
                    .createFolder(folderModel);

            STEP("3. Upload a text document to the childFolders.");
            dataContent.usingUser(user1)
                    .usingSite(siteModel)
                    .usingResource(childFolder)
                    .createContent(sampleFileToCreate);
        });

        STEP("4. Wait for 30 seconds so that the content is indexed in Search Service.");
        Utility.waitToLoopTime(30);

        RestSizeDetailsModel restSizeDetailsModel = restClient
                .authenticateUser(user1)
                .withCoreAPI()
                .usingNode(folder)
                .executeSizeDetails();

        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restSizeDetailsModel.assertThat().field("jobId").isNotEmpty();

        String jobId = restSizeDetailsModel.getJobId();

        STEP("5. Wait for 10 seconds for the processing to complete.");
        Awaitility
                .await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Durations.ONE_SECOND)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    RestSizeDetailsModel sizeDetailsModel = restClient.authenticateUser(user1)
                            .withCoreAPI()
                            .usingNode(folder)
                            .getSizeDetails(jobId);
                    restClient.assertStatusCodeIs(HttpStatus.OK);
                    sizeDetailsModel.assertThat().field("numberOfFiles").isNotEmpty();
                    Assert.assertEquals(sizeDetailsModel.getNumberOfFiles(), 10, "Value of NumberOfFiles " + sizeDetailsModel.getNumberOfFiles() + " is not equal to " + 10);
                });
    }

    private byte[] getSampleFileContent(String templateName) throws IOException
    {
        final String templateClasspathLocation = "/shared-resources/testdata/" + templateName;
        try (InputStream templateStream = getClass().getResourceAsStream(templateClasspathLocation))
        {
            requireNonNull(templateStream, "Couldn't locate `" + templateClasspathLocation + "`");
            return templateStream.readAllBytes();
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception
    {
        dataSite.usingUser(user1).deleteSite(siteModel);
    }
}
