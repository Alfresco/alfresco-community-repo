package org.alfresco.rest.nodes;


import static org.testng.Assert.assertFalse;

import org.alfresco.rest.RestTest;

import org.alfresco.rest.model.RestActionDefinitionModelsCollection;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.rest.model.builder.NodesBuilder.NodeDetail;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

public class NodesActionDefinitionTests extends RestTest
{
    
    
    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify actions")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY}) 
    public void testActionDefinition() throws Exception
    {
        restClient.authenticateUser(dataContent.getAdminUser());

        /*
         * Create the following file structure for preconditions : 
         *   - sourceFolder
         *     - file
         */
        NodesBuilder nodesBuilder = restClient.withCoreAPI().usingNode(ContentModel.my()).defineNodes();
        NodeDetail sourceFolder = nodesBuilder.folder("sourceFolder");
        NodeDetail file = sourceFolder.file("file");
        ContentModel fileActionDefinitions = new ContentModel();
        fileActionDefinitions.setNodeRef(file.getId());

        RestActionDefinitionModelsCollection restActionDefinitions =  restClient.withCoreAPI().usingNode(fileActionDefinitions).getActionDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        assertFalse(restActionDefinitions.isEmpty());
        restActionDefinitions.assertThat().entriesListContains("name", "copy");
        restActionDefinitions.assertThat().entriesListContains("name", "move");
        restActionDefinitions.assertThat().entriesListContains("name", "check-out");
        restActionDefinitions.assertThat().entriesListContains("name", "check-in");
       
    }
    
    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.REGRESSION,
            description = "Verify actions negative request")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.REGRESSION}) 
    public void testActionDefinitionNegative() throws Exception{

        NodesBuilder nodesBuilder = restClient.withCoreAPI().usingNode(ContentModel.my()).defineNodes();
        NodeDetail sourceFolder = nodesBuilder.folder("sourceFolder");
        ContentModel validNode = new ContentModel();
        validNode.setNodeRef(sourceFolder.getId());

        // Non-existent node ID
        {
            ContentModel fakeNode = new ContentModel();
            fakeNode.setNodeRef("750a2867-ecfa-478c-8343-fa0e39d27be3");
            restClient.authenticateUser(dataContent.getAdminUser()).withCoreAPI().usingNode(fakeNode).getActionDefinitions();
            restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        }

        // Badly formed request -> 400
        {
            restClient.authenticateUser(dataContent.getAdminUser()).withParams("skipCount=-1").withCoreAPI().usingNode(validNode)
                    .getActionDefinitions();
            restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        }

        // Unauthorized -> 401
        {

            UserModel userUnauthorized = new UserModel("invalid-user", "invalid-pasword");
            restClient.authenticateUser(userUnauthorized).withCoreAPI().usingNode(validNode).getActionDefinitions();
            restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);

        }
    }

}
