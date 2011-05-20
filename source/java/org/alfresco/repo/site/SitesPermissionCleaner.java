/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.site;

import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeIdAndAclId;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
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
    
    public void cleanSitePermissions(final NodeRef targetNode, SiteInfo containingSite)
    {
        if (nodeService.exists(targetNode))
        {
            // We can calculate the containing site at the start of a recursive call & then reuse it on subsequent calls.
            if (containingSite == null)
            {
                containingSite = siteServiceImpl.getSite(targetNode);
            }

            // Short-circuit at this point if the node is not in a Site.
            if (containingSite != null)
            {
                // For performance reasons we navigate down the containment hierarchy using the DAOs
                // rather than the NodeService.
                final Long targetNodeID = nodeDAO.getNodePair(targetNode).getFirst();
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
                        
                        // If it's a group site permission for a site other than the current site
                        if (authority.startsWith(PermissionService.GROUP_PREFIX) &&
                                !authority.startsWith(PermissionService.ALL_AUTHORITIES) && // And it's not GROUP_EVERYONE
                                !authority.startsWith(thisSiteGroupPrefix))
                        {
                            // And if the current user has permissions to do it
                            if (publicServiceAccessService.hasAccess("PermissionService", "clearPermission", targetNode, authority) == AccessStatus.ALLOWED)
                            {
                                // Then remove it.
                                permissionService.clearPermission(targetNode, authority);
                            }
                            if (publicServiceAccessService.hasAccess("PermissionService", "setInheritParentPermissions", targetNode, true) == AccessStatus.ALLOWED)
                            {
                                // And reenable permission inheritance.
                                permissionService.setInheritParentPermissions(targetNode, true);
                            }
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
    }
}
