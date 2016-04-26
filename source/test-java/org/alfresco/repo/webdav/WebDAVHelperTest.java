package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests for the WebDAVHelper class.
 * 
 * @see RenameShuffleDetectionTest
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class WebDAVHelperTest
{
    private WebDAVHelper davHelper;
    
    @Before
    public void setUp() throws Exception
    {
        davHelper = new WebDAVHelper();
    }

    @Test
    public void canGetUrlPathPrefixWhenExplicitlySet()
    {
        // Path prefix explicitly set on helper.
        davHelper.setUrlPathPrefix("/my/prefix");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/my/prefix/folder/filename.txt");
        String prefix = davHelper.getUrlPathPrefix(request);
        assertEquals("/my/prefix/", prefix);
    }

    @Test
    public void canGetUrlPathPrefixFromServletPath()
    {
        // Path prefix not explicitly set on helper.
        davHelper.setUrlPathPrefix("");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/before/the-servlet/folder/filename.txt");
        // Servlet path will be used to determine path prefix.
        request.setServletPath("/the-servlet");
        String prefix = davHelper.getUrlPathPrefix(request);
        assertEquals("/before/the-servlet/", prefix);
    }
    
    @Test
    public void canGetDestinationPathWhenNoServletName()
    {
        assertPathForURL("/the-tenant.com/the-site/path/to/file",
                    "http://webdav.alfresco.com/the-tenant.com/the-site/path/to/file");
     
    }
    
    /**
     * THOR-1459: WebDAV: site names cannot start with 'webdav'.
     * <p>
     * <code>/webdav-test</code> begins with servlet path <code>/webdav</code>
     */
    @Test
    public void canGetDestinationPathWhenPathElementStartsWithServletPath()
    {
        assertPathForURL("/t/webdav-test/path/to/file",
                    "http://webdav.alfresco.com/t/webdav-test/path/to/file");

        // Looks like /contextPath/servletName in URL's path prefix, but isn't
        assertPathForURL("/alfresco/webdav-test/path/to/file",
                    "http://webdav.alfresco.com/alfresco/webdav-test/path/to/file");
    }
    
    @Test
    public void canGetDestinationPathWhenPrefixedWithContextPathAndServletName()
    {
        assertPathForURL("/path/to/file",
                    "http://webdav.alfresco.com/alfresco/webdav/path/to/file");
        
        assertPathForURL("/alfresco/webdav/path/to/file",
                    "http://webdav.alfresco.com/alfresco/webdav/alfresco/webdav/path/to/file");

        assertPathForURL("/my/folder/alfresco/webdav/path/to/file",
                    "http://webdav.alfresco.com/alfresco/webdav/my/folder/alfresco/webdav/path/to/file");
    }

    /**
     * Check that the expected path was extracted from a given URL.
     * 
     * @param path The expected path.
     * @param url URL to extract the path from.
     */
    private void assertPathForURL(String path, String url)
    {
        assertEquals(path, davHelper.getDestinationPath("/alfresco", "/webdav", url));
    }
}
