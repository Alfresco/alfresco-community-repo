/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the WebDAVMethod class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class WebDAVMethodTest
{
    private WebDAVMethod method;
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    private @Mock WebDAVHelper davHelper;
    
    @Test
    public void canGetStatusForAccessDeniedException()
    {
        // Initially Mac OS X Finder uses a different UA string than for subsequent requests.
        assertStatusCode(500, "WebDAVLib/1.3");
        
        // Current UA string at time of writing test.
        assertStatusCode(500, "WebDAVFS/1.9.0 (01908000) Darwin/11.4.0 (x86_64)");
        
        // A fictitious version number long in the future.
        assertStatusCode(500, "WebDAVFS/100.10.5 (01908000) Darwin/11.4.0 (x86_64)");

        // Other processor architectures, e.g. x86_32 should work too.
        assertStatusCode(500, "WebDAVFS/100.10.5 (01908000) Darwin/109.6.3 (some_other_processor_arch)");
        
        // Other clients should give 403.
        assertStatusCode(403, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6; en-us)");
        // Mozilla-based Windows browser.
        assertStatusCode(403, "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.12)");
        assertStatusCode(403, "SomeBrowser/1.0 (Macintosh; U; Intel Mac OS X 10_6; en-us)");
        assertStatusCode(403, "SomeBrowser/1.9.0 (01908000) Darwin/11.4.0 (x86_64)");
        assertStatusCode(403, "Cyberduck/4.2.1 (Mac OS X/10.7.4) (i386)");
        // Chrome
        assertStatusCode(403, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.54 Safari/536.5");
        // Safari
        assertStatusCode(403, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2");
    }
    
    private void assertStatusCode(int expectedStatusCode, String userAgent)
    {
        // Fresh objects needed for each status code test.
        createRequestObjects();
        req.addHeader("User-Agent", userAgent);
        method.setDetails(req, resp, davHelper, null);
        
        int statusCode = method.getStatusForAccessDeniedException();
        
        assertEquals("Incorrect status code for user-agent string \"" + userAgent + "\"",
                    expectedStatusCode,
                    statusCode);
    }

    private void createRequestObjects()
    {
        method = new TestWebDAVMethod();
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
    }

    
    /**
     * Empty subclass of abstract base class for testing base class' behaviour.
     */
    private static class TestWebDAVMethod extends WebDAVMethod
    {
        @Override
        protected void executeImpl() throws WebDAVServerException, Exception
        {
        }

        @Override
        protected void parseRequestBody() throws WebDAVServerException
        {
        }

        @Override
        protected void parseRequestHeaders() throws WebDAVServerException
        {
        }   
    }
}
