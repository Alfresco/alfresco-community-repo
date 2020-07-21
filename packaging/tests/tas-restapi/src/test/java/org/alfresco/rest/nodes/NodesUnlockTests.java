package org.alfresco.rest.nodes;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.body.RestNodeLockBodyModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NodesUnlockTests extends RestTest
{
    private UserModel user1, user2, adminUser;
    private SiteModel publicSite;
    private FileModel file1;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        publicSite = dataSite.usingUser(adminUser).createPublicRandomSite();
        user1 = dataUser.createRandomTestUser();
        user2 = dataUser.createRandomTestUser();
        dataUser.addUserToSite(user1, publicSite, UserRole.SiteCollaborator);
        user1.setUserRole(UserRole.SiteCollaborator);
        dataUser.addUserToSite(user2, publicSite, UserRole.SiteCollaborator);
        user2.setUserRole(UserRole.SiteCollaborator);  
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section={TestGroup.REST_API, TestGroup.NODES}, executionType= ExecutionType.SANITY,
            description= "Verify Collaborator canot unlock EPHEMERAL lock made by different user, but can unlock EPHEMERAL lock made by same user")
    public void lockEphemeralAndUnlock() throws Exception
    {
        STEP("1. Add user(s) as collaborators to the site created by administrator and add a file in this site.");
        file1 = dataContent.usingUser(adminUser).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);

        STEP("2. Verify with user1 that the file is not locked.");
        RestNodeModel file1Model1 = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=isLocked").getNode();
        file1Model1.assertThat().field("isLocked").is(false);

        STEP("3. Lock the file using mode EPHEMERAL with user1 (POST nodes/{nodeId}/lock).");
        RestNodeLockBodyModel lockBodyModel = new RestNodeLockBodyModel();
        lockBodyModel.setLifetime("EPHEMERAL");
        lockBodyModel.setTimeToExpire(20);
        restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include?isLocked").lockNode(lockBodyModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("4. Verify with user1 that the file is locked.");
        RestNodeModel file1Model2 = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=isLocked").getNode();
        file1Model2.assertThat().field("isLocked").is(true);

        STEP("5. Cannot unlock the file with user2 while the file is still locked");
        restClient.authenticateUser(user2).withCoreAPI().usingNode(file1).usingParams("include=isLocked").unlockNode();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
            .assertLastError()
            .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
            .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);

        STEP("6. Unlock the file with user1 while the file is still locked");
        RestNodeModel file1Model3 = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=isLocked").unlockNode();
        file1Model3.assertThat().field("isLocked").is(false);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    @TestRail(section={TestGroup.REST_API, TestGroup.NODES}, executionType= ExecutionType.SANITY,
            description= "Verify Collaborator canot unlock PERSISTENT lock made by different user, but can unlock PERSISTENT lock made by same user")
    public void lockPersistentAndUnlock() throws Exception{

        STEP("1. Add user(s) as collaborators to the site created by administrator and add a file in this site.");
        file1 = dataContent.usingUser(adminUser).usingSite(publicSite).createContent(DocumentType.TEXT_PLAIN);

        STEP("2. Verify with admin that the file is not locked.");
        RestNodeModel file1Model1 = restClient.authenticateUser(adminUser).withCoreAPI().usingNode(file1).usingParams("include=isLocked").getNode();
        file1Model1.assertThat().field("isLocked").is(false);

        STEP("3. Lock the file using mode PERSISTENT with user1 (POST nodes/{nodeId}/lock).");
        RestNodeLockBodyModel lockBodyModel = new RestNodeLockBodyModel();
        lockBodyModel.setLifetime("PERSISTENT");
        lockBodyModel.setTimeToExpire(20);
        restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include?isLocked").lockNode(lockBodyModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("4. Verify with user1 that the file is locked.");
        RestNodeModel file1Model2 = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=isLocked").getNode();
        file1Model2.assertThat().field("isLocked").is(true);

        STEP("5. Cannot unlock the file with user2 while the file is still locked");
        restClient.authenticateUser(user2).withCoreAPI().usingNode(file1).usingParams("include=isLocked").unlockNode();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
            .assertLastError()
            .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
            .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);

        STEP("6. Unlock the file with user1 while the file is still locked");
        RestNodeModel file1Model3 = restClient.authenticateUser(user1).withCoreAPI().usingNode(file1).usingParams("include=isLocked").unlockNode();
        file1Model3.assertThat().field("isLocked").is(false);
    }

}
