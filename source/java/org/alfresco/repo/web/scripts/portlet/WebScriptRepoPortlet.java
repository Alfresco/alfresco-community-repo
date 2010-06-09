/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
