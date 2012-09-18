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

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;


/**
 * QuickShare/PublicView
 * 
 * @author janv
 */
public abstract class AbstractQuickShareContent extends DeclarativeWebScript
{
    protected NodeService nodeService;
    protected AttributeService attributeService;
    protected TenantService tenantService;
    
    private boolean enabled = true;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    protected boolean isEnabled()
    {
        return this.enabled;
    }
    
    
    protected Pair<String, NodeRef> getTenantNodeRefFromSharedId(final String sharedId)
    {
        return getTenantNodeRefFromSharedId(attributeService, tenantService, sharedId);
    }
    
    /* package */ static Pair<String, NodeRef> getTenantNodeRefFromSharedId(final AttributeService attributeService, final TenantService tenantService, final String sharedId)
    {
        final NodeRef nodeRef = TenantUtil.runAsDefaultTenant(new TenantRunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return (NodeRef)attributeService.getAttribute(ShareContentPost.ATTR_KEY_SHAREDIDS_ROOT, sharedId);
            }
        });
        
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: " + sharedId);
        }
        
        // note: relies on tenant-specific (ie. mangled) nodeRef
        String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
        
        return new Pair<String, NodeRef>(tenantDomain, tenantService.getBaseName(nodeRef));
    }
}