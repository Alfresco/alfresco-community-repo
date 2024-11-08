package org.alfresco.rest.nodes;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.utility.model.FileModel;
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
}
