/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodePermissionAssessor
{
    protected static final Log logger = LogFactory.getLog(NodePermissionAssessor.class);

    private final boolean isSystemReading;
    private final boolean isAdminReading;
    private final boolean isNullReading;
    private final Authority authority;
    private final Map<Long, Boolean> aclReadCache = new HashMap<>();
    private int checksPerformed;
    private long startTime;
    private int maxPermissionChecks;
    private long maxPermissionCheckTimeMillis;
    
    private EntityLookupCache<Long, Node, NodeRef> nodesCache;
    private NodeService nodeService;
    private PermissionService permissionService;

    public NodePermissionAssessor(NodeService nodeService, PermissionService permissionService,
            Authority authority, EntityLookupCache<Long, Node, NodeRef> nodeCache)
    {
        this.permissionService = permissionService;
        this.nodesCache = nodeCache;
        this.nodeService = nodeService;
        
        this.checksPerformed = 0;
        this.maxPermissionChecks = Integer.MAX_VALUE;
        this.maxPermissionCheckTimeMillis = Long.MAX_VALUE;

        Set<String> authorisations = permissionService.getAuthorisations();
        this.isSystemReading = AuthenticationUtil.isRunAsUserTheSystemUser();
        this.isAdminReading = authorisations.contains(AuthenticationUtil.getAdminRoleName());
        this.isNullReading = AuthenticationUtil.getRunAsUser() == null;

        this.authority = authority;
    }

    public boolean isIncluded(Node node)
    { 
        if (isFirstRecord())
        {
            this.startTime = System.currentTimeMillis();
        }
        
        checksPerformed++;
        return isReallyIncluded(node);
    }

    public boolean isFirstRecord()
    {
        return checksPerformed == 0;
    }

    protected boolean isOwnerReading(Node node, Authority authority)
    {
        if (authority == null)
        {
            return false;
        }

        String owner = getOwner(node);
        return EqualsHelper.nullSafeEquals(authority.getAuthority(), owner);
    }
    
    private String getOwner(Node node)
    {
        nodesCache.setValue(node.getId(), node);
        Set<QName> nodeAspects = nodeService.getAspects(node.getNodeRef());
        
        String userName = null;
        if (nodeAspects.contains(ContentModel.ASPECT_AUDITABLE))
        {
            userName = node.getAuditableProperties().getAuditCreator();
        }
        else if (nodeAspects.contains(ContentModel.ASPECT_OWNABLE))
        {
            Serializable owner = nodeService.getProperty(node.getNodeRef(), ContentModel.PROP_OWNER);
            userName = DefaultTypeConverter.INSTANCE.convert(String.class, owner);
        }
        
        return userName;
    }
    
    boolean isReallyIncluded(Node node)
    {
        if (isNullReading)
        {
            return false;
        }
        
        return  isSystemReading ||
                isAdminReading ||
                canRead(node.getAclId()) ||
                isOwnerReading(node, authority);
    }

    public void setMaxPermissionChecks(int maxPermissionChecks)
    {
        if (maxPermissionChecks == Integer.MAX_VALUE)
        {
            this.maxPermissionChecks = maxPermissionChecks;
        }
        else
        {
            this.maxPermissionChecks = maxPermissionChecks + 1;
        }
    }
    
    public boolean shouldQuitChecks()
    {
        if (checksPerformed >= maxPermissionChecks)
        {
            logger.warn("Maximum permission checks exceeded (" + maxPermissionChecks + ")");
            return true;
        }

        if ((System.currentTimeMillis() - startTime) >= maxPermissionCheckTimeMillis)
        {
            logger.warn("Maximum permission checks time exceeded (" + maxPermissionCheckTimeMillis + ")");
            return true;
        }

        return false;
    }

    public void setMaxPermissionCheckTimeMillis(long maxPermissionCheckTimeMillis)
    {
        this.maxPermissionCheckTimeMillis = maxPermissionCheckTimeMillis;
    }
            
    protected boolean canRead(Long aclId)
    {
        Boolean res = aclReadCache.get(aclId);
        if (res == null)
        {
            res = canCurrentUserRead(aclId);
            aclReadCache.put(aclId, res);
        }
        return res;
    }
    
    protected boolean canCurrentUserRead(Long aclId)
    {
        // cache resolved ACLs
        Set<String> authorities = permissionService.getAuthorisations();

        Set<String> aclReadersDenied = permissionService.getReadersDenied(aclId);
        for (String auth : aclReadersDenied)
        {
            if (authorities.contains(auth))
            {
                return false; 
            }
        }

        Set<String> aclReaders = permissionService.getReaders(aclId);
        for (String auth : aclReaders)
        {
            if (authorities.contains(auth))
            {
                return true; 
            }
        }

        return false;
    }
}