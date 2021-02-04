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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;

public class NodePermissionAssessorPermissionsTest
{
    private PermissionService permissionService;
    
    @Before
    public void setup()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        permissionService = mock(PermissionService.class);
        DBStats.resetStopwatches();
    }

    @Test
    public void shouldGrantPermissionWhenSystemIsReading()
    {
        // setup
        AuthenticationUtil.setRunAsUserSystem();
        
        Node theNode = mock(Node.class);
        NodePermissionAssessor assessor = createAssessor();
        when(assessor.isOwnerReading(any(Node.class), any(Authority.class))).thenReturn(false);
        when(permissionService.getReaders(anyLong())).thenReturn(Set.of());
        
        // call the assessor
        boolean included = assessor.isIncluded(theNode);
        
        // the node is included
        assertTrue(included);
    }
    
    @Test
    public void shouldDenyPermissionWhenNullUserIsReading()
    {
        // setup - AuthenticationUtil.getRunAsUser() will return null
        Node theNode = mock(Node.class);
        NodePermissionAssessor assessor = createAssessor();
        when(assessor.isOwnerReading(any(Node.class), any(Authority.class))).thenReturn(false);
        when(permissionService.getReaders(anyLong())).thenReturn(Set.of());
        
        // call the assessor
        boolean included = assessor.isIncluded(theNode);
        
        // the node is included
        assertFalse(included);
    }
    
    private NodePermissionAssessor createAssessor()
    {
        NodeService nodeService = mock(NodeService.class);
        Authority authority = mock(Authority.class);
        EntityLookupCache<Long, Node, NodeRef> nodeCache = mock(EntityLookupCache.class);
        return spy(new NodePermissionAssessor(nodeService, permissionService, authority, nodeCache));
    }
}
