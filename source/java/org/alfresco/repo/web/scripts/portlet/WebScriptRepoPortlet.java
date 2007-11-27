/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import org.alfresco.web.scripts.portlet.WebScriptPortlet;


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
        super.processAction(req, res);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.portlet.WebScriptPortlet#render(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    @Override
    public void render(RenderRequest req, RenderResponse res) throws PortletException, PortletSecurityException, IOException
    {
        Application.setInPortalServer(true);
        super.render(req, res);
    }
    
}
