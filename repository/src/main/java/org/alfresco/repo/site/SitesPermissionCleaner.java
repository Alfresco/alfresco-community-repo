/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.site;

import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeIdAndAclId;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * This class handles the permissions cleanup which is sometimes necessary after a node has been moved
 * or copied between sites. It removes any permissions that pertain to the former site that may be present
 * on the relocated node.
 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 */
public class SitesPermissionCleaner
{
    private NodeService nodeService;
    private PermissionService permissionService;
    private PublicServiceAccessService publicServiceAccessService;
    private SiteServiceImpl siteServiceImpl;
    
    private AclDAO aclDAO;
    private NodeDAO nodeDAO;
    private TenantService tenantService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setSiteServiceImpl(SiteServiceImpl siteServiceImpl)
    {
        this.siteServiceImpl = siteServiceImpl;
    }
    
    public void setPublicServiceAccessService(PublicServiceAccessService publicServiceAccessService)
    {
        this.publicServiceAccessService = publicServiceAccessService;
    }
    
    public void setAclDAO(AclDAO aclDAO)
    {
        this.aclDAO = aclDAO;
    }
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    
    public void cleanSitePermissions(final NodeRef targetNode, SiteInfo containingSite)
    {
        if (!nodeDAO.exists(targetNode))
        {
            return;
        }
        // We can calculate the containing site at the start of a recursive call & then reuse it on subsequent calls.
        if (containingSite == null)
        {
            containingSite = siteServiceImpl.getSite(targetNode);
        }

        // Short-circuit at this point if the node is not in a Site.
        if (containingSite == null)
        {
            return;
        }
        // For performance reasons we navigate down the containment hierarchy using the DAOs
        // rather than the NodeService. Note: direct use of NodeDAO requires tenantService (ALF-12732).
        final Long targetNodeID = nodeDAO.getNodePair(tenantService.getName(targetNode)).getFirst();
        final Long targetNodeAclID = nodeDAO.getNodeAclId(targetNodeID);
        Acl targetNodeAcl = aclDAO.getAcl(targetNodeAclID);

        // Nodes that don't have defining ACLs do not need to be considered.
        if (targetNodeAcl.getAclType() == ACLType.DEFINING)
        {
            AccessControlList targetNodeAccessControlList = aclDAO.getAccessControlList(targetNodeAclID);
            List<AccessControlEntry> targetNodeAclEntries = targetNodeAccessControlList.getEntries();
            for (AccessControlEntry entry : targetNodeAclEntries)
            {
                String authority = entry.getAuthority();

                String thisSiteGroupPrefix = siteServiceImpl.getSiteGroup(containingSite.getShortName(), true);
                String anySiteGroupPrefix = thisSiteGroupPrefix.substring(0, thisSiteGroupPrefix.lastIndexOf(containingSite.getShortName()));

                // If it's a group site permission for a site other than the current site
                if (authority.startsWith(anySiteGroupPrefix) &&
                        !authority.startsWith(thisSiteGroupPrefix) &&
                        //  And if the current user has permissions to do it
                        publicServiceAccessService.hasAccess("PermissionService", "clearPermission", targetNode, authority) == AccessStatus.ALLOWED)
                {
                    // Then remove it.
                    permissionService.clearPermission(targetNode, authority);
                }

                if (!permissionService.getInheritParentPermissions(targetNode))
                {
                    // The site manager from the new site, where this node was moved to, has to have permission to this node
                    String siteManagerAuthority = thisSiteGroupPrefix + "_" + SiteModel.SITE_MANAGER;
                    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                    {
                        public Void doWork() throws Exception
                        {
                            permissionService.setPermission(targetNode, siteManagerAuthority, SiteModel.SITE_MANAGER, true);
                            return null;
                        }
                    }, AuthenticationUtil.getSystemUserName());
                }
            }
        }

        // Recurse
        List<NodeIdAndAclId> childNodeIds = nodeDAO.getPrimaryChildrenAcls(targetNodeID);
        for (NodeIdAndAclId nextChild : childNodeIds)
        {
            cleanSitePermissions(nodeDAO.getNodePair(nextChild.getId()).getSecond(), containingSite);
        }
    }
}
