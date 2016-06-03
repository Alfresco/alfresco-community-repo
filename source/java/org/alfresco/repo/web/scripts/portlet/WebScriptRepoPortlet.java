package org.alfresco.repo.web.scripts.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.web.app.Application;
import org.springframework.extensions.webscripts.portlet.WebScriptPortlet;


/**
 * Repository (server-tier) implementation of Web Script Portlet
 * 
 * Informs Web Client Application of Portlet request.
 * 
 * @author davidc
 */
public class WebScriptRepoPortlet extends WebScriptPortlet
{

	/* (non-Javadoc)
	 * @see org.alfresco.web.scripts.portlet.WebScriptPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
    @Override
    public void processAction(ActionRequest req, ActionResponse res) throws PortletException, PortletSecurityException, IOException
    {
        Application.setInPortalServer(true);
        try
        {
            super.processAction(req, res);
        }
        finally
        {
            Application.setInPortalServer(false);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.portlet.WebScriptPortlet#render(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    @Override
    public void render(RenderRequest req, RenderResponse res) throws PortletException, PortletSecurityException, IOException
    {
        Application.setInPortalServer(true);
        try
        {
            super.render(req, res);
        }
        finally
        {
            Application.setInPortalServer(false);
        }
    }
    
}
