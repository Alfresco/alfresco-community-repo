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
package org.alfresco.repo.web.scripts.quickshare;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * QuickShare/PublicView
 * 
 * POST web script to "Share" access to some content (ie. enable unauthenticated access to this node)
 * 
 * Note: authenticated web script
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class ShareContentPost extends AbstractQuickShareContent
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (! isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }
        
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final NodeRef nodeRef = WebScriptUtil.getNodeRef(params);
        if (nodeRef == null)
        {
            String msg = "A valid NodeRef must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        
        try
        {
            QuickShareDTO dto = quickShareService.shareContent(nodeRef);
            
            Map<String, Object> model = new HashMap<String, Object>(1);
            model.put("sharedDTO", dto);
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
        	throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + nodeRef);
        }
    }
}