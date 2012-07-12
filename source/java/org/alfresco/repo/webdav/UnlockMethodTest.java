package org.alfresco.repo.webdav;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class UnlockMethodTest
{
    private UnlockMethod unlockMethod;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private @Mock WebDAVHelper davHelper;
    
    @Before
    public void setUp() throws Exception
    {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        unlockMethod = new UnlockMethod();
        unlockMethod.setDetails(request, response, davHelper, null);
    }

    @Test
    public void parseValidLockTokenHeader() throws WebDAVServerException
    {
        String lockToken = "976e2f82-40ab-4852-a867-986e9ce11f82:admin";
        String lockHeaderValue = "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">";
        request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
        unlockMethod.parseRequestHeaders();
        
        assertEquals(lockToken, unlockMethod.getLockToken());
    }
    
    @Test
    public void parseInvalidLockTokenHeader()
    {        
        String lockToken = "976e2f82-40ab-4852-a867-986e9ce11f82:admin";
        String lockHeaderValue = "<wrongprefix:" + lockToken + ">";
        request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
        try
        {
            unlockMethod.parseRequestHeaders();
            fail("Exception should have been thrown, but wasn't.");
        }
        catch (WebDAVServerException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatusCode());
        }
    }
    
    @Test
    public void parseMissingLockTokenHeader()
    {        
        // Note: we're not adding the lock token header
        try
        {
            unlockMethod.parseRequestHeaders();
            fail("Exception should have been thrown, but wasn't.");
        }
        catch (WebDAVServerException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatusCode());
        }
    }
    
    /**
     * OpenOffice.org on Windows 7 results in a lock token header that is NOT enclosed in
     * the required &lt; and &gt; characters. Whilst technically an invalid header, we treat
     * this case specially for reasons of interoperability (ALF-13904)
     * 
     * @throws WebDAVServerException 
     */
    @Test
    public void parseLockTokenHeaderFromOOoOnWindows7() throws WebDAVServerException
    {        
        String lockToken = "976e2f82-40ab-4852-a867-986e9ce11f82:admin";
        // Note the missing enclosing < and > characters
        String lockHeaderValue = WebDAV.OPAQUE_LOCK_TOKEN + lockToken;
        request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
        unlockMethod.parseRequestHeaders();
        
        assertEquals(lockToken, unlockMethod.getLockToken());
    }
}
