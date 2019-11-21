package org.alfresco.rest.workflow.deployments;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 12/7/2016.
 */
public class GetDeploymentsCoreTests extends RestTest
{

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS },
            executionType = ExecutionType.REGRESSION, 
            description = "Verify non admin user is not able to get non-network deployments using REST API and status code is Forbidden")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS, TestGroup.REGRESSION})
    public void nonAdminUserCanNotGetNonNetworkDeployments() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        restClient.authenticateUser(userModel).withWorkflowAPI().getDeployments();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

}
