package org.alfresco.cmis;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Consumer;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

public class SearchQueryService {

    RestWrapper client = new RestWrapper();

    /** Assert that the query returns something, without checking exactly what it returns. */
    public void expectSomeResultsFromQuery(SearchRequest searchRequest, UserModel testUser)
    {
        Consumer<SearchResponse> assertNotEmpty = searchResponse -> assertFalse(searchResponse.isEmpty());
        expectResultsFromQuery(searchRequest, testUser, assertNotEmpty);
    }

    private void expectResultsFromQuery(SearchRequest searchRequest, org.alfresco.utility.model.UserModel user, Consumer<SearchResponse> assertionMethod)
    {
        try
        {
            Utility.sleep(1000, 20000, () ->
            {
                SearchResponse response = client.authenticateUser(user)
                        .withSearchAPI()
                        .search(searchRequest);
                client.assertStatusCodeIs(HttpStatus.OK);
                assertionMethod.accept(response);
            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }
    }

    public static SearchRequest req(String language, String query)
    {
        RestRequestQueryModel restRequestQueryModel = new RestRequestQueryModel();
        restRequestQueryModel.setQuery(query);
        Optional.ofNullable(language).ifPresent(restRequestQueryModel::setLanguage);
        return new SearchRequest(restRequestQueryModel);
    }
}
