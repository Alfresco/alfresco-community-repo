package org.alfresco.rest.nodes;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.rest.model.builder.NodesBuilder.NodeDetail;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Handles tests related to api-explorer/#!/nodes
 */
public class NodesTests extends RestTest
{
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
}
