package org.alfresco.rest.nodes;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.rest.model.builder.NodesBuilder.NodeDetail;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;

/**
 * Handles tests related to api-explorer/#!/nodes
 */
public class NodesTests extends RestTest
{
    private UserModel user1;
    private SiteModel site1;
    private FolderModel folder1;
    private FileModel file1;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        user1 = dataUser.createRandomTestUser();
        site1 = dataSite.usingUser(user1).createPublicRandomSite();
        folder1 = dataContent.usingUser(user1).usingSite(site1).createFolder();
        file1 = dataContent.usingUser(user1).usingResource(folder1).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify files can be moved from one folder to another")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY}) 
    public void testMoveFile() throws Exception
    {
        restClient.authenticateUser(dataContent.getAdminUser());

        /*
         * Create the following file structure for preconditions : 
         *   - sourceFolder
         *     - file
         *   - destinationFolder
         */
        NodesBuilder nodesBuilder = restClient.withCoreAPI().usingNode(ContentModel.my()).defineNodes();
        NodeDetail sourceFolder = nodesBuilder.folder("sourceFolder");
        NodeDetail file = sourceFolder.file("file");
        NodeDetail destinationFolder = nodesBuilder.folder("destinationFolder");

        // Move file from sourceFolder to destinationFolder
        RestNodeBodyMoveCopyModel moveDestinationInfo = new RestNodeBodyMoveCopyModel();
        moveDestinationInfo.setTargetParentId(destinationFolder.getId());

        ContentModel fileToMove = new ContentModel();
        fileToMove.setNodeRef(file.getId());

        RestNodeModel response = restClient.withParams("autoRename=true").withCoreAPI().usingNode(fileToMove).move(moveDestinationInfo);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        /*
         *  Check file's parent has changed to destinationFolder
         *   - sourceFolder
         *   - destinationFolder
         *     - file
         */
        response.assertThat().field("parentId").is(destinationFolder.getId());
    }

    @TestRail(section = { TestGroup.SANITY },
        executionType = ExecutionType.SANITY,
        description = "Verify 403 is received for files where the user lacks permissions.")
    @Test(groups = {TestGroup.SANITY})
    public void siteConsumerWillGet403OnFileWithDisabledInherittedPermissions() throws Exception
    {
        // https://issues.alfresco.com/jira/browse/REPO-4859

        // Authenticate as admin to fulfill the preconditions
        UserModel adminUser = dataContent.getAdminUser();
        RestWrapper restWrapper = this.restClient.authenticateUser(adminUser);

        // Create the file using CMIS
        testSite = dataSite.createPublicRandomSite();
        FileModel file = dataContent
            .usingUser(adminUser)
            .usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // Add a consumer user via CMIS
        DataUser.ListUserWithRoles listUserWithRoles = dataUser.usingUser(adminUser)
            .addUsersWithRolesToSite(testSite, UserRole.SiteConsumer);

        // Disable the permission inheritance
        JsonObject activateModelJson = Json.createObjectBuilder().add("permissions",
            Json.createObjectBuilder().add("isInheritanceEnabled", false))
            .build();

        restWrapper.withCoreAPI().usingNode(file).updateNode(activateModelJson.toString());
        restWrapper.assertStatusCodeIs(HttpStatus.OK);

        // Authenticate as the consumer user
        UserModel consumerUser = listUserWithRoles.getOneUserWithRole(UserRole.SiteConsumer);

        // Assert the consumer gets a 403 VIA REST Call
        RestResponse restApiResponse = restClient.authenticateUser(consumerUser).withCoreAPI()
            .usingNode(file).getNodeContent();

        int restApiStatusCode = restApiResponse.getResponse().getStatusCode();
        logger.info("REST API call response status code is: " + restApiStatusCode);
        assertEquals(HttpStatus.FORBIDDEN.value(), restApiStatusCode);

        // Assert the consumer gets a 403 VIA CMIS API
        // Implement the CMIS call as it is not supported under .withCMISApi()
        // This is done similar to {@link IntegrationWithCmisTests#verifyGetChildrenReturnsUniqueValues}
        RestAssured.basePath = "alfresco/api/-default-/public/cmis/versions/1.1/browser";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);

        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
            "/root/Sites/" + testSite.getTitle() + "/documentLibrary/" + file.getName() + "?cmisselector=object&succinct=true");
        RestResponse cmisApiResponse = restWrapper.authenticateUser(consumerUser).process(request);

        int cmisApiStatusCode = cmisApiResponse.getResponse().getStatusCode();
        logger.info("CMIS API call response status code is: " + cmisApiStatusCode);
        assertEquals(HttpStatus.FORBIDDEN.value(), cmisApiStatusCode);
    }

    @TestRail(section = { TestGroup.SANITY }, executionType = ExecutionType.SANITY,
            description = "Check that the default node storage classes are retrieved - GET /nodes/{nodeId}.")
    @Test(groups = { TestGroup.SANITY })
    public void getNodeStorageClass() throws Exception
    {
        STEP("1. Get storage classes for a node with content.");
        RestNodeModel restResponse = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=storageClasses").getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertTrue(restResponse.getContent().getStorageClasses().contains("default"));

        STEP("2. Check that storage classes for a node with content are not displayed by default.");
        restResponse = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertNull(restResponse.getContent().getStorageClasses());

        STEP("3. Get storage classes for a node without content (e.g folder).");
        restClient.authenticateUser(user1).withCoreAPI().usingNode(folder1).usingParams("include=storageClasses").getNode();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.SANITY }, executionType = ExecutionType.SANITY,
            description = "Check that the storage classes default behavior of PUT /nodes/{nodeId}.")
    @Test(groups = { TestGroup.SANITY })
    public void updateNodeStorageClass() throws Exception
    {
        STEP("1. Update storage classes for a node with existing storage class.");
        JsonObject updateStorageClass = Json.createObjectBuilder().add("content",
                Json.createObjectBuilder().add("storageClasses", Json.createArrayBuilder().add("default")))
                .build();
        RestNodeModel restResponse = restClient.authenticateUser(user1).withCoreAPI()
                .usingNode(file1).usingParams("include=storageClasses").updateNode(updateStorageClass.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertTrue(restResponse.getContent().getStorageClasses().contains("default"));

        STEP("2. Update storage classes for a node and check that storageClass is not displayed by default.");
        // Use existing update body
        restResponse = restClient.authenticateUser(user1).withCoreAPI()
                .usingNode(file1).updateNode(updateStorageClass.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertNull(restResponse.getContent().getStorageClasses());
    }

}
