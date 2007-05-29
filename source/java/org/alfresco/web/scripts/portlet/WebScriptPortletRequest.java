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
package org.alfresco.web.scripts.portlet;

import javax.portlet.PortletRequest;

import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptURLRequest;


/**
 * JSR-168 Web Script Request
 * 
 * @author davidc
 */
public class WebScriptPortletRequest extends WebScriptURLRequest
{
    /** Portlet Request */
    private PortletRequest req;
    
    
    /**
     * Construct
     * 
     * @param req
     * @param scriptUrl
     * @param serviceMatch
     */
    public WebScriptPortletRequest(PortletRequest req, String scriptUrl, WebScriptMatch serviceMatch)
    {
        this(req, splitURL(scriptUrl), serviceMatch);
    }
    
    /**
     * Construct
     * 
     * @param req
     * @param scriptUrlParts
     * @param serviceMatch
     */
    public WebScriptPortletRequest(PortletRequest req, String[] scriptUrlParts, WebScriptMatch serviceMatch)
    {
        super(scriptUrlParts, serviceMatch);
        this.req = req;
    }

    /**
     * Gets the Portlet Request
     * 
     * @return  Portlet Request
     */
    public PortletRequest getPortletRequest()
    {
        return req;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServerPath()
     */
    public String getServerPath()
    {
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getAgent()
     */
    public String getAgent()
    {
        // NOTE: rely on default agent mappings
        return null;
    }
    
}
