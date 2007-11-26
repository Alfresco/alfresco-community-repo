package org.alfresco.repo.web.scripts.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.web.app.Application;
import org.alfresco.web.scripts.portlet.WebScriptPortlet;


public class WebScriptRepoPortlet extends WebScriptPortlet
{

    @Override
    public void processAction(ActionRequest req, ActionResponse res) throws PortletException, PortletSecurityException, IOException
    {
        Application.setInPortalServer(true);
        super.processAction(req, res);
    }

    @Override
    public void render(RenderRequest req, RenderResponse res) throws PortletException, PortletSecurityException, IOException
    {
        Application.setInPortalServer(true);
        super.render(req, res);
    }
    
}
