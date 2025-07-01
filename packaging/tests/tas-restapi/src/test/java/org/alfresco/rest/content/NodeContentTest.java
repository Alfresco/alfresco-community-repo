package org.alfresco.rest.content;

import static org.testng.Assert.*;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchNodeModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.utility.RetryOperation;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;

public class NodeContentTest extends RestTest
{

    @TestRail(section = {TestGroup.REST_API,
            TestGroup.SEARCH}, executionType = ExecutionType.SANITY, description = "Check basic functionality of GET queries/sites")
    @Test(groups = {TestGroup.REST_API, TestGroup.RATINGS, TestGroup.CORE})
    public void testNodeContent() throws Exception
    {

        UserModel adminUser = dataContent.getAdminUser();
        final String fileName = "nodecontent.pdf";
        final String term = "babekyrtso";

        FileModel fileModel = FileModel.getFileModelBasedOnTestDataFile(fileName);
        restClient.authenticateUser(adminUser)
                .configureRequestSpec()
                .addMultiPart("filedata", fileModel.toFile());
        RestNodeModel node = restClient.authenticateUser(adminUser).withCoreAPI().usingNode(ContentModel.my()).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setLanguage("afts");
        queryModel.setQuery(term);
        query.setQuery(queryModel);

        RetryOperation op = () -> {
            List<SearchNodeModel> entries = restClient.authenticateUser(adminUser)
                    .withSearchAPI()
                    .search(query).getEntries();

            assertFalse(CollectionUtils.isEmpty(entries), "Search results should not be empty");
            boolean fileFound = entries.stream()
                    .map(SearchNodeModel::getModel)
                    .anyMatch(e -> fileName.equals(e.getName()));
            assertTrue(fileFound, "Search results should contain the file: " + fileName);

            restClient.assertStatusCodeIs(HttpStatus.OK);
        };
        Utility.sleep(300, 100000, op);

        restClient.authenticateUser(adminUser)
                .withCoreAPI()
                .usingNode(ContentModel.my())
                .deleteNode(node);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }
}
