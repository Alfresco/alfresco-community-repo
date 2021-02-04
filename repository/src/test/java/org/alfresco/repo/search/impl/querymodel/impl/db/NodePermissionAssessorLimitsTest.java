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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.permissions.AclCrudDAO;
import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;

public class NodePermissionAssessorLimitsTest
{
    private NodePermissionAssessor assessor;
    private Node node;
    
    @Before
    public void setup()
    {
        node = mock(Node.class);
        assessor = createAssessor();
    }
    
    @Test
    public void shouldNotQuitAssessingPermissionsWhenMaxPermissionChecksLimitIsNotReached()
    {
        assessor.setMaxPermissionChecks(5);
        
        performChecks(3);
        
        assertFalse(assessor.shouldQuitChecks());
        verify(assessor, times(3)).isReallyIncluded(node);
    }
    
    @Test
    public void shouldQuitAssessingPermissionsWhenMaxPermissionChecksLimitIsReached()
    {
        assessor.setMaxPermissionChecks(5);
        
        performChecks(20);
        
        assertTrue(assessor.shouldQuitChecks());
    }
    
    @Test
    public void shouldNotAssessPermissionsWhenMaxPermissionCheckTimeIsUp() throws Exception 
    {
        assessor.setMaxPermissionCheckTimeMillis(100);
        
        assessor.isIncluded(node);
        Thread.sleep(200);
        
        assertTrue(assessor.shouldQuitChecks());
        verify(assessor).isReallyIncluded(node);
        
    }
    
    @Test
    public void shouldAssessPermissionsWhenMaxPermissionCheckTimeIsNotUp() throws Exception 
    {
        assessor.setMaxPermissionCheckTimeMillis(500);
        Thread.sleep(200);
        
        assessor.isIncluded(node);
        
        assertFalse(assessor.shouldQuitChecks());
        verify(assessor, atLeastOnce()).isReallyIncluded(node);
        
    }

    private void performChecks(int checks)
    {
        for (int i=0; i < checks; i++)
        {
            assessor.isIncluded(node);
        }
    }
    
    private NodePermissionAssessor createAssessor()
    {
        AclCrudDAO aclCrudDAO = mock(AclCrudDAO.class);
        PermissionService permissionService = mock(PermissionService.class);

        DBQueryEngine engine = new DBQueryEngine();
        engine.setPermissionService(permissionService);
        engine.setAclCrudDAO(aclCrudDAO);
        
        NodeService nodeService = mock(NodeService.class);
        Authority authority = mock(Authority.class);
        EntityLookupCache<Long, Node, NodeRef> nodeCache = mock(EntityLookupCache.class);
        NodePermissionAssessor assessor = spy(new NodePermissionAssessor(nodeService, permissionService, authority, nodeCache));
        doReturn(true).when(assessor).isReallyIncluded(any(Node.class));
        return assessor;
    }
}
