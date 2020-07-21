package org.alfresco.rest.sharedLinks;

import javax.json.Json;
import javax.json.JsonObject;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestSharedLinksModelCollection;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetSharedLinksFullTests extends RestTest
{
    private SiteModel privateSite;
    private UserModel adminUser, userModel;
    protected FileModel file;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        privateSite = dataSite.usingUser(adminUser).createPrivateRandomSite();
    }

    @Bug(id="REPO-2365")
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SHAREDLINKS }, executionType = ExecutionType.REGRESSION, description = "Verify that a user with permission can get allowableOperations on sharedLinks")
    @Test(groups = { TestGroup.REST_API, TestGroup.SHAREDLINKS, TestGroup.REGRESSION })
    public void getSharedLinksWithAllowableOperations() throws Exception
    {
        file = dataContent.usingUser(adminUser).usingSite(privateSite).createContent(DocumentType.TEXT_PLAIN);
        
        /*
         * { "permissions": { "isInheritanceEnabled": true, "locallySet": { "authorityId": "userModel.getUsername()",
         * "name": "SiteConsumer", "accessStatus":"ALLOWED" } } }
         */
        JsonObject userPermission = Json.createObjectBuilder().add("permissions",
                Json.createObjectBuilder().add("isInheritanceEnabled", true).add("locallySet", Json.createObjectBuilder()
                        .add("authorityId", userModel.getUsername()).add("name", "SiteConsumer").add("accessStatus", "ALLOWED")))
                .build();
        String putBody = userPermission.toString();
        restClient.authenticateUser(adminUser).withCoreAPI().usingNode(file).updateNode(putBody);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.authenticateUser(adminUser).withCoreAPI().usingSharedLinks().createSharedLink(file);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        // verify that the permision exists
        RestNodeModel permissionsRequest = restClient.authenticateUser(userModel).withCoreAPI().usingNode(file)
                .usingParams("include=permissions").getNode();
        permissionsRequest.assertThat().field("permissions.locallySet.authorityId").is("[" + userModel.getUsername() + "]");

        // because the sharedLink take time to be created, we may need to wait for solr to index it.
        int numberOfTries = 0;
        RestSharedLinksModelCollection operationsRequest;
        do
        {
            Thread.sleep(1000);
            numberOfTries++;
            operationsRequest = restClient.authenticateUser(userModel).withCoreAPI().usingSharedLinks().usingParams("include=allowableOperations").getSharedLinks();
            restClient.assertStatusCodeIs(HttpStatus.OK);
        } while (operationsRequest.getPagination().getCount() == 0 && numberOfTries < 15);
        operationsRequest.getPagination().assertThat().field("count").is(1);
    }
}
