package org.alfresco.rest.nodes;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeAssocTargetModel;
import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.model.builder.NodesBuilder;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NodesTargetSourcesTests extends RestTest
{

    private UserModel adminUserModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
    }

    /**
     * Sanity check for the following api endpoints 
     * POST /nodes/{nodeId}/targets
     * GET /nodes/{nodeId}/targets 
     * DELETE /nodes/{nodeId}/targets/{targetId}
     */

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Check /targets (create, list, delete) api calls")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    public void checkTargetsNodeApi() throws Exception
    {
        STEP("1.Create folder1 which contains 3 files: f1, f2, and f3");
        NodesBuilder nodesBuilder = restClient.authenticateUser(adminUserModel).withCoreAPI().usingNode(ContentModel.my()).defineNodes();
        nodesBuilder.folder("F1").file("f1").file("f2").file("f3");

        STEP("2. Create target associations model objects");
        RestNodeAssocTargetModel assocDocTarget1 = new RestNodeAssocTargetModel(nodesBuilder.getNode("f2").toContentModel().getNodeRef(), "cm:references");
        RestNodeAssocTargetModel assocDocTarget2 = new RestNodeAssocTargetModel(nodesBuilder.getNode("f3").toContentModel().getNodeRef(), "cm:references");

        STEP("3. Create target  associations using POST /nodes/{nodeId}/targets");
        restClient.withCoreAPI().usingResource(nodesBuilder.getNode("f1").toContentModel()).createTargetForNode(assocDocTarget1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(nodesBuilder.getNode("f1").toContentModel()).createTargetForNode(assocDocTarget2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. Check using GET /nodes/{nodeId}/targets targets associations were created");
        RestNodeAssociationModelCollection targetsRes = restClient.withParams("where=(assocType='cm:references')").withCoreAPI().usingResource(nodesBuilder.getNode("f1").toContentModel()).getNodeTargets();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        targetsRes.assertThat().entriesListCountIs(2);
        targetsRes.getEntryByIndex(0).assertThat().field("association.assocType").is("cm:references").and().field("name")
                .is(nodesBuilder.getNode("f2").getName());
        targetsRes.getEntryByIndex(1).assertThat().field("association.assocType").is("cm:references").and().field("name")
                .is(nodesBuilder.getNode("f3").getName());

        STEP("5. Check using DELETE /nodes/{nodeId}/targets/{targetId} that a target can be deleted");
        restClient.authenticateUser(adminUserModel);
        restClient.withCoreAPI().usingResource(nodesBuilder.getNode("f1").toContentModel()).deleteTarget(assocDocTarget1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("6. Check using GET /nodes/{nodeId}/targets that target association was deleted");
        targetsRes = restClient.withParams("where=(assocType='cm:references')").withCoreAPI()
                .usingResource(nodesBuilder.getNode("f1").toContentModel()).getNodeTargets();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        targetsRes.assertThat().entriesListCountIs(1);
        targetsRes.getEntryByIndex(0).assertThat().field("association.assocType").is("cm:references");
    }

    /**
     * Sanity check for the following api endpoint 
     * GET /nodes/{nodeId}/sources
     */
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.NODES }, executionType = ExecutionType.SANITY, description = "Check that source objects are retrieved using GET /nodes/{nodeId}/sources")
    @Test(groups = { TestGroup.REST_API, TestGroup.NODES, TestGroup.SANITY })
    public void checkGetNodeSources() throws Exception
    {
        STEP("1.Create a folder hierarchy folder1 containing 4 files: f1, f2, and f3");
        NodesBuilder nodesBuilder = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingNode(ContentModel.my()).defineNodes();
        nodesBuilder.folder("F1").file("f1").file("f2").file("f3");

        STEP("2. Create target associations model objects");
        RestNodeAssocTargetModel assocDocTarget1 = new RestNodeAssocTargetModel(nodesBuilder.getNode("f3").toContentModel().getNodeRef(), "cm:references");
        RestNodeAssocTargetModel assocDocTarget2 = new RestNodeAssocTargetModel(nodesBuilder.getNode("f3").toContentModel().getNodeRef(), "cm:preferenceImage");

        STEP("3. Create target  associations using POST /nodes/{nodeId}/targets");
        restClient.authenticateUser(adminUserModel);
        restClient.withCoreAPI().usingResource(nodesBuilder.getNode("f1").toContentModel()).createTargetForNode(assocDocTarget1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(nodesBuilder.getNode("f2").toContentModel()).createTargetForNode(assocDocTarget2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. Check using GET /nodes/{nodeId}/sources that all source associations are displayed");
        RestNodeAssociationModelCollection sources = restClient.withCoreAPI().usingResource(nodesBuilder.getNode("f3").toContentModel()).getNodeSources();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sources.assertThat().entriesListCountIs(2);

        STEP("5. Check using GET /nodes/{nodeId}/sources with params that sources and fields can be filtered in the response");
        sources = restClient.withParams("where=(assocType='cm:references')", "fields=isFile,name,association,id,nodeType,parentId").withCoreAPI().usingResource(nodesBuilder.getNode("f3").toContentModel()).getNodeSources();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sources.assertThat().entriesListCountIs(1);

        sources.getEntryByIndex(0).assertThat()
                                  .field("isFile").is(true).and()
                                  .field("name").is(nodesBuilder.getNode("f1").getName()).and()
                                  .field("id").is(nodesBuilder.getNode("f1").getId()).and()
                                  .field("id").is(nodesBuilder.getNode("f1").getId()).and()
                                  .field("nodeType").is("cm:content")
                                  .getAssociation().assertThat()
                                                   .field("assocType").is("cm:references");
    }
}
