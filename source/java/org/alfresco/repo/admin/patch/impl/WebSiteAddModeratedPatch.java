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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;
import java.util.Set;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;



/**
 * Patch that changes the web site visibility from a boolean 
 * (isPublic) to an enum (PUBLIC, PRIVATE, MODERATED).
 * 
 * @author mrogers
 */
public class WebSiteAddModeratedPatch extends AbstractPatch
{
	private PermissionService permissionService;
	private SiteService siteService;
	
    private static final String MSG_SUCCESS = "patch.webSiteAddModerated.result";

	@Override
	protected String applyInternal() throws Exception 
	{
		// for all web sites
		String nameFilter = null; 
		String sitePresetFilter = null;
		List<SiteInfo> sites = getSiteService().listSites(nameFilter, sitePresetFilter);
		
		for(SiteInfo site : sites)
		{
			 SiteVisibility visibility = SiteVisibility.PRIVATE;
			 NodeRef siteNodeRef = site.getNodeRef();
			 
		    // Get the visibility value stored in the repo
		    String visibilityValue = (String)this.nodeService.getProperty(siteNodeRef, SiteModel.PROP_SITE_VISIBILITY);
	        // To maintain backwards compatibility calculate the visibility from the permissions
	        // if there is no value specified on the site node
	        if (visibilityValue == null)
	        {
		        // Examine each permission to see if this is a public site or not
		        Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(siteNodeRef);
		        for (AccessPermission permission : permissions)
		        {
		            if (permission.getAuthority().equals(PermissionService.ALL_AUTHORITIES) == true && 
		                permission.getPermission().equals(SiteModel.SITE_CONSUMER) == true)
		            {
		                    visibility = SiteVisibility.PUBLIC;
		                    break;
		                }
		            }
		            
		            // Store the visibility value on the node ref for next time
		            this.nodeService.setProperty(siteNodeRef, SiteModel.PROP_SITE_VISIBILITY, visibility.toString());            
		        }		 
		}
		
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        return msg;
			
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

}
