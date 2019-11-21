package org.alfresco.rest.nodes;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeBodyModel;
import org.alfresco.rest.model.RestNodeChildAssocModelCollection;
import org.alfresco.rest.model.RestNodeChildAssociationModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Handles tests related to
 * api-explorer/#!/nodes/children
 * api-explorer/#!/nodes/secondary-children
 * api-explorer/#!/nodes/parents
 */
public class NodesParentChildrenTests extends RestTest
{
    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify new folder node is created as children on -my- posting as JSON content type")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})    
    public void createNewFolderNodeViaJason() throws Exception
    {
        restClient.authenticateUser(dataContent.getAdminUser());

        RestNodeBodyModel node = new RestNodeBodyModel();
        node.setName("My Folder");
        node.setNodeType("cm:folder");

        RestNodeModel newNode = restClient.withParams("autoRename=true").withCoreAPI().usingNode(ContentModel.my()).createNode(node);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        newNode.assertThat().field("aspectNames").contains("cm:auditable")
               .assertThat().field("isFolder").is(true)
               .assertThat().field("isFile").is(false)
               .assertThat().field("name").contains(node.getName());
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.REGRESSION,
            description = "Verify new folder node is created as children on -my- posting as MultiPart content type")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.REGRESSION})
    public void createNewFolderNodeWithMultiPartForms() throws Exception
    {
        //configuring multipart form
        restClient.authenticateUser(dataContent.getAdminUser())
                  .configureRequestSpec()
                    .addMultiPart("filedata", Utility.getResourceTestDataFile("restapi-resource"))
                    .addFormParam("renditions", "doclib")
                    .addFormParam("autoRename", true);

        RestNodeModel newNode = restClient.withCoreAPI().usingNode(ContentModel.my()).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED); 
        newNode.assertThat().field("aspectNames").contains("cm:auditable")
               .assertThat().field("isFolder").is(false)
               .assertThat().field("isFile").is(true)
               .assertThat().field("name").contains("restapi-resource");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.NODES }, executionType = ExecutionType.SANITY,
            description = "Verify list children when listing with relativePath and pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY})
    public void checkRelativePathAndPaginationOnCreateChildrenNode() throws Exception
    {
        /*
         * Given we have a folder hierarchy folder1/folder2/folder3 and folder3 containing 3 files file1, file2, and file3
         */
        NodesBuilder nodesBuilder = restClient.authenticateUser(dataUser.getAdminUser())
                                              .withCoreAPI().usingNode(ContentModel.my())
                                              .defineNodes();
        nodesBuilder
            .folder("F1")
            .folder("F2")
            .folder("F3")
                .file("f1")
                .file("f2")
                .file("f3");
              
        RestNodeModelsCollection returnedFiles = restClient.withParams("maxItems=2", 
                                                               "skipCount=1", 
                                                               String.format("relativePath=%s/%s", nodesBuilder.getNode("F2").getName(), nodesBuilder.getNode("F3").getName()))
                                                               .withCoreAPI().usingNode(nodesBuilder.getNode("F1").toContentModel()).listChildren();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        /*
         * Then I receive file2 and file3
         */
        returnedFiles.assertThat().entriesListCountIs(2);
        returnedFiles.getEntries().get(0).onModel().assertThat().field("id").is(nodesBuilder.getNode("f2").getId());
        returnedFiles.getEntries().get(1).onModel().assertThat().field("id").is(nodesBuilder.getNode("f3").getId());
    }

    /**
     * Sanity check for the following api endpoints
     * POST       /nodes/{nodeId}/secondary-children
     * GET        /nodes/{nodeId}/secondary-children
     * DELETE     /nodes/{nodeId}/secondary-children/{childId}
     */
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Check /secondary-children (create, list, delete) api calls")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    public void checkSecondaryChildrenApi() throws Exception
    {
        STEP("1. Create a folder hierarchy folder1/folder2, with folder2 containing 3 files: f1, f2, and f3");
        NodesBuilder nodesBuilder = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI().usingNode(ContentModel.my())
                .defineNodes();
        nodesBuilder
                .folder("F1")
                .folder("F2")
                       .file("f1")
                       .file("f2")
                       .file("f3");

        STEP("2. Create secondary child associations model objects");
        RestNodeChildAssociationModel childAssoc1 = new RestNodeChildAssociationModel(nodesBuilder.getNode("f1").getId(), "cm:contains");
        RestNodeChildAssociationModel childAssoc2 = new RestNodeChildAssociationModel(nodesBuilder.getNode("f2").getId(), "cm:contains");
        RestNodeChildAssociationModel childAssoc3 = new RestNodeChildAssociationModel(nodesBuilder.getNode("f3").getId(), "cm:preferenceImage");
        String secondaryChildrenBody = "[" + childAssoc1.toJson() + "," + childAssoc2.toJson() + "," + childAssoc3.toJson() + "]";

        STEP("3. Create secondary child associations using POST /nodes/{nodeId}/secondary-children");
        RestNodeChildAssocModelCollection secondaryChildAssoc = restClient.withCoreAPI().usingNode(nodesBuilder.getNode("F1").toContentModel())
                .createSecondaryChildren(secondaryChildrenBody);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        secondaryChildAssoc.getEntryByIndex(0).assertThat().field("childId").is(childAssoc1.getChildId());
        secondaryChildAssoc.getEntryByIndex(1).assertThat().field("childId").is(childAssoc2.getChildId());
        secondaryChildAssoc.getEntryByIndex(2).assertThat().field("childId").is(childAssoc3.getChildId());

        STEP("4. Check using GET /nodes/{nodeId}/secondary-children that the secondary 'cm:contains' child associations were created");
        RestNodeAssociationModelCollection secondaryChildren = restClient.withParams("where=(assocType='cm:contains')").withCoreAPI()
                .usingNode(nodesBuilder.getNode("F1").toContentModel()).getSecondaryChildren();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        secondaryChildren.assertThat().entriesListCountIs(2);

        STEP("5. Check using DELETE /nodes/{nodeId}/secondary-children/{childId} that a secondary child can be deleted");
        restClient.withCoreAPI().usingNode(nodesBuilder.getNode("F1").toContentModel()).deleteSecondaryChild(secondaryChildren.getEntryByIndex(0));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("6. Check using GET /nodes/{nodeId}/secondary-children that a secondary child association was deleted");
        secondaryChildren = restClient.withCoreAPI().usingNode(nodesBuilder.getNode("F1").toContentModel())
                .getSecondaryChildren();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        secondaryChildren.assertThat().entriesListCountIs(2);
        secondaryChildren.getEntryByIndex(0).assertThat()
                                            .field("id").is(secondaryChildAssoc.getEntryByIndex(1).getChildId()).and()
                                            .field("parentId").is(nodesBuilder.getNode("F2").getId())
                                            .getAssociation().assertThat()
                                                             .field("isPrimary").is(false).and()
                                                             .field("assocType").is("cm:contains");
        secondaryChildren.getEntryByIndex(1).assertThat()
                                            .field("id").is(secondaryChildAssoc.getEntryByIndex(2).getChildId())
                                            .getAssociation().assertThat()
                                                             .field("assocType").is("cm:preferenceImage");
    }

    /**
     * Sanity check for the following api endpoint
     * GET        /nodes/{nodeId}/parents
     */
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Check that GET /parents retrieves primary and secondary parents for a node")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    public void checkGetNodeParents() throws Exception
    {
        STEP("1. Create a folder hierarchy folder1/folder2, with folder2 containing file f1");
        NodesBuilder nodesBuilder = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI().usingNode(ContentModel.my())
                .defineNodes();
        nodesBuilder
                .folder("F1")
                .folder("F2")
                       .file("f1");

        STEP("2. Create secondary child associations using POST /nodes/{nodeId}/secondary-children");
        RestNodeChildAssociationModel childAssoc = new RestNodeChildAssociationModel(nodesBuilder.getNode("f1").getId(), "cm:contains");
        restClient.withCoreAPI().usingNode(nodesBuilder.getNode("F1").toContentModel()).createSecondaryChildren(childAssoc.toJson());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("3. Get all parents for file 'f1' - both primary and secondary");
        RestNodeAssociationModelCollection parents = restClient.withCoreAPI().usingNode(nodesBuilder.getNode("f1").toContentModel()).getParents();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        parents.assertThat().entriesListCountIs(2);

        STEP("4. Check using GET /nodes/{nodeId}/parents that the parent (not primary) for f1 is found");
        parents = restClient.withParams("where=(isPrimary=false)").withCoreAPI().usingNode(nodesBuilder.getNode("f1").toContentModel()).getParents();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        parents.assertThat().entriesListCountIs(1);
        parents.getEntryByIndex(0).assertThat()
               .field("isFolder").is("true").and()
               .field("isFile").is(false).and()
               .field("name").is(nodesBuilder.getNode("F1").getName())
               .getAssociation().assertThat()
                                 .field("isPrimary").is(false).and()
                                 .field("assocType").is("cm:contains");
    }
}
