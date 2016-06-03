package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * WebScript java backed bean implementation - to simple return a STATUS_OK message
 * as a touch point for SSO authentication mechanisms on the web-tier. Such as NTLM.
 * 
 * @author Kevin Roast
 */
public class Touch extends AbstractWebScript
{
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        res.setStatus(Status.STATUS_OK);
        res.getWriter().close();
    }
}