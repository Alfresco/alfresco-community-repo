package org.alfresco.cmis;

import org.alfresco.rest.search.SearchRequest;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.cmis.SearchQueryService.req;

public class selectFieldSearchTests extends CmisTest {


    SearchQueryService bla = new SearchQueryService();

    UserModel testUser;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.getAdminUser();
        cmisApi.authenticateUser(testUser);
    }

    @TestRail(description = "Verify that we can return a list of fields from a document via a CMIS query against Elasticsearch.",
            section = TestGroup.SEARCH, executionType = ExecutionType.REGRESSION)
    @Test(groups = TestGroup.SEARCH)
    public void returnFieldListFromDoc()
    {
        SearchRequest query = req("cmis", "SELECT cmis:name, cmis:objectId, cmis:lastModifiedBy, cmis:creationDate, cmis:contentStreamFileName FROM cmis:document WHERE CONTAINS('*')");
        bla.expectSomeResultsFromQuery(query, testUser);
    }

    @TestRail (description = "Verify that we can return a list of fields from a folder via a CMIS query against Elasticsearch.",
            section = TestGroup.SEARCH, executionType = ExecutionType.REGRESSION)
    @Test (groups = TestGroup.SEARCH)
    public void returnFieldListFromFolder()
    {
        SearchRequest query = req("cmis", "SELECT cmis:name, cmis:parentId, cmis:path, cmis:allowedChildObjectTypeIds FROM cmis:folder WHERE CONTAINS('*')");
        bla.expectSomeResultsFromQuery(query, testUser);
    }


}
