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

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * QuickShare/PublicView
 * 
 * DELETE web script to "Unshare" access to some content (ie. disable unauthenticated access to this node)
 * 
 * Note: authenticated web script
 * 
 * @author janv
 * @since Cloud/4.2
 */
public class UnshareContentDelete extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(ShareContentPost.class);
    
    private NodeService nodeService;
    private SiteService siteService;
    private AuthenticationService authenticationService;
    
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (! isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }
        
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final String sharedId = params.get("shared_id");
        if (sharedId == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "A valid sharedId must be specified !");
        }
        
        NodeRef nodeRef = quickShareService.getTenantNodeRefFromSharedId(sharedId).getSecond();
        String currentUser = authenticationService.getCurrentUserName();
        
        String siteName = getSiteName(nodeRef);
        String sharedBy = (String) nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDBY);
        if (!currentUser.equals(sharedBy) && siteName != null)
        {
            String role = siteService.getMembersRole(siteName, currentUser);
            if (role.equals(SiteModel.SITE_CONSUMER) || role.equals(SiteModel.SITE_CONTRIBUTOR))
            {
                throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Can't perform unshare action: "+sharedId);
            }
        }
        
        try
        {
            quickShareService.unshareContent(sharedId);
            
            Map<String, Object> model = new HashMap<String, Object>(1);
            model.put("success", Boolean.TRUE);
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: "+sharedId);
        }
    }
    
    private String getSiteName(NodeRef nodeRef)
    {
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        while (parent != null && !nodeService.getType(parent).equals(SiteModel.TYPE_SITE))
        {
            String parentName = (String) nodeService.getProperty(parent, ContentModel.PROP_NAME);
            if (nodeService.getPrimaryParent(nodeRef) != null)
            {
                parent = nodeService.getPrimaryParent(parent).getParentRef();
            }
        }
        
        if (parent == null)
        {
            return null;
        }
        
        return nodeService.getProperty(parent, ContentModel.PROP_NAME).toString();
    }
}