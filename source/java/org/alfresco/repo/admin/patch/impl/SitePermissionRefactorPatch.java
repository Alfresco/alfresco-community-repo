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

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch's the site permission model to use groups to contain users.
 * 
 * @author Roy Wetherall
 */
public class SitePermissionRefactorPatch extends AbstractPatch
{
    /** Messages */
    private static final String STATUS_MSG = "patch.sitePermissionRefactorPatch.result";
    
    /** Services */
    private SiteService siteService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    
    /**
     * Set site service
     * 
     * @param siteService   the site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService     the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * The authority service
     * 
     * @param authorityService  the authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
    	// NOTE: SiteService is not currently MT-enabled (eg. getSiteRoot) so skip if applied to tenant
        if (AuthenticationUtil.isRunAsUserTheSystemUser() || !AuthenticationUtil.isMtEnabled())
        {
	        // Set all the sites in the repository
	        List<SiteInfo> sites = this.siteService.listSites(null, null);
	        for (SiteInfo siteInfo : sites)
	        {
	            // Create the site's groups
	            String siteGroup = authorityService.createAuthority(
	                    AuthorityType.GROUP, 
	                    ((SiteServiceImpl)this.siteService).getSiteGroup(siteInfo.getShortName(), 
	                    false));
	            QName siteType = nodeService.getType(siteInfo.getNodeRef());
	            Set<String> permissions = permissionService.getSettablePermissions(siteType);
	            for (String permission : permissions)
	            {
	                // Create a group for the permission
	                String permissionGroup = authorityService.createAuthority(
	                                            AuthorityType.GROUP, 
	                                            ((SiteServiceImpl)this.siteService).getSiteRoleGroup(
	                                                    siteInfo.getShortName(), 
	                                                    permission, 
	                                                    false));
	                authorityService.addAuthority(siteGroup, permissionGroup);
	                
	                // Assign the group the relevant permission on the site
	                permissionService.setPermission(siteInfo.getNodeRef(), permissionGroup, permission, true);
	            }
	            
	            // Take the current members and assign them to the appropriate groups
	            Set<AccessPermission> currentPermissions = this.permissionService.getAllSetPermissions(siteInfo.getNodeRef());
	            for (AccessPermission permission : currentPermissions)
	            {
	                // Only support user's being transfered (if public the everyone group will stay on the node)
	                if (permission.getAuthorityType() == AuthorityType.USER)
	                {
	                    // Add this authority to the appropriate group
	                    String group = ((SiteServiceImpl)this.siteService).getSiteRoleGroup(
	                            siteInfo.getShortName(), 
	                            permission.getPermission(), 
	                            true);
	                   this.authorityService.addAuthority(group, permission.getAuthority()); 
	                   
	                   // Remove the permission from the node
	                   this.permissionService.deletePermission(siteInfo.getNodeRef(), permission.getAuthority(), permission.getPermission());
	                }
	            }
	        }
    	}
     
        // Report status
        return I18NUtil.getMessage(STATUS_MSG);
    }
}
