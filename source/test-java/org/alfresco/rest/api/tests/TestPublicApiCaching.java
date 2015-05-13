package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.junit.Test;

/**
 * HTTP cache header public api tests.
 * 
 * @author Gavin Cornwell
 *
 */
public class TestPublicApiCaching extends EnterpriseTestApi
{
    @Test
    public void testMNT13938() throws Exception
    {
        Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
        final TestNetwork account1 = accountsIt.next();
        Iterator<String> personIt1 = account1.getPersonIds().iterator();
        final String person1 = personIt1.next();

        // make a request to any API (we'll get our own profile)
        {
            publicApiClient.setRequestContext(new RequestContext(account1.getId(), person1));
            HttpResponse response = publicApiClient.get("public", "people", person1, null, null, null);
            
            int responseCode = response.getStatusCode();
            // make sure request was successful
            assertTrue("Response code should be 200", responseCode == HttpServletResponse.SC_OK);
            
            Map<String, String> headers = response.getHeaders();
            // assert headers are present
            assertNotNull("HTTP headers should be present on response", headers);
            
            // assert the cache headers are present
            String cacheControlHeader = headers.get("Cache-Control");
            assertNotNull("Cache-Control header should be present", cacheControlHeader);
            assertTrue("Cache-Control header should be set to no-cache but it was: " + cacheControlHeader, 
                        cacheControlHeader.equals("no-cache"));
            
            String pragmaHeader = headers.get("Pragma");
            assertNotNull("Pragma header should be present", pragmaHeader);
            assertTrue("Pragma header should be set to no-cache but it was: " + pragmaHeader, 
                        pragmaHeader.equals("no-cache"));
            
            String expiresHeader = headers.get("Expires");
            assertNotNull("Expires header should be present", expiresHeader);
        }
    }
}
