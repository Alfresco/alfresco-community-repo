/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.search.impl.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

import org.alfresco.repo.search.QueryParserException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/** Tests for the {@link AbstractSolrQueryHTTPClient}. */
public class AbstractSolrQueryHTTPClientTest
{
    /** A URL for use in the tests. */
    private static final String URL = "http://this/is/a/url";

    /** The abstract class under test. */
    private AbstractSolrQueryHTTPClient abstractSolrQueryHTTPClient = spy(AbstractSolrQueryHTTPClient.class);
    @Mock
    private HttpClient httpClient;
    @Mock
    private JSONObject body;
    @Mock
    private PostMethod postMethod;
    @Mock
    private Header header;

    @Before
    public void setUp() throws Exception
    {
        openMocks(this);

        doReturn(postMethod).when(abstractSolrQueryHTTPClient).createNewPostMethod(URL);
        when(postMethod.getResponseCharSet()).thenReturn("UTF-8");
    }

    /** Check postQuery works as expected for the success case. */
    @Test
    public void testPostQuery_success() throws Exception
    {
        when(body.toString()).thenReturn("Example body");
        when(postMethod.getStatusCode()).thenReturn(HttpServletResponse.SC_OK);
        when(postMethod.getResponseBodyAsStream()).thenReturn(convertStringToInputStream("{}"));

        JSONObject response = abstractSolrQueryHTTPClient.postQuery(httpClient, URL, body);

        assertEquals("Unexpected JSON response received.", "{}", response.toString());
    }

    /** Check that the status code is usually passed through from Solr. */
    @Test
    public void testPostQuery_failure() throws Exception
    {
        String failureMessage = "{\"error\": {\"trace\": \"ExceptionClass: Stacktrace\"}}";

        when(body.toString()).thenReturn("Example body");
        when(postMethod.getStatusCode()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
        when(postMethod.getResponseBodyAsStream()).thenReturn(convertStringToInputStream(failureMessage));
        when(postMethod.getResponseBodyAsString()).thenReturn(failureMessage);

        try
        {
            abstractSolrQueryHTTPClient.postQuery(httpClient, URL, body);
            fail("Expected a QueryParserException to be thrown.");
        }
        catch (QueryParserException e)
        {
            assertEquals("Unexpected status code in exception.", e.getHttpStatusCode(), HttpServletResponse.SC_NOT_FOUND);
            verify(postMethod).releaseConnection();
        }
    }

    /** Check that the status code is replaced with "Not Implemented" for an unsupported query option. */
    @Test
    public void testPostQuery_unsupportedOperation() throws Exception
    {
        String failureMessage = "{\"error\": {\"trace\": \"java.lang.UnsupportedOperationException: Stacktrace\"}}";

        when(body.toString()).thenReturn("Example body");
        when(postMethod.getStatusCode()).thenReturn(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        when(postMethod.getResponseBodyAsStream()).thenReturn(convertStringToInputStream(failureMessage));
        when(postMethod.getResponseBodyAsString()).thenReturn(failureMessage);

        try
        {
            abstractSolrQueryHTTPClient.postQuery(httpClient, URL, body);
            fail("Expected a QueryParserException to be thrown.");
        }
        catch (QueryParserException e)
        {
            assertEquals("Unexpected status code in exception.", e.getHttpStatusCode(), HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    /** Check that a redirect can be followed if the endpoint reports that it's moved. */
    @Test
    public void testPostQuery_moved() throws Exception
    {
        when(body.toString()).thenReturn("Example body");
        // Report "moved" for the first invocation and then OK for subsequent requests.
        when(postMethod.getStatusCode()).thenReturn(HttpServletResponse.SC_MOVED_PERMANENTLY).thenReturn(HttpServletResponse.SC_OK);
        when(postMethod.getResponseBodyAsStream()).thenReturn(convertStringToInputStream("{}"));
        when(postMethod.getResponseHeader("location")).thenReturn(header);
        when(header.getValue()).thenReturn("http://new/URL");

        JSONObject response = abstractSolrQueryHTTPClient.postQuery(httpClient, URL, body);

        verify(postMethod).setURI(new URI("http://new/URL", true));
        assertEquals("Unexpected JSON response received.", "{}", response.toString());
    }

    /** Create an input stream containing the given string. */
    private ByteArrayInputStream convertStringToInputStream(String message)
    {
        return new ByteArrayInputStream(message.getBytes());
    }
}
