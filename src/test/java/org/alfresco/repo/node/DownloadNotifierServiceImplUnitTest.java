/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.node;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import org.alfresco.repo.node.NodeServicePolicies.OnDownloadNodePolicy;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PolicyIgnoreUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Chris Shields
 * @author Sara Aspery
 */
public class DownloadNotifierServiceImplUnitTest extends TestCase
{
    private DownloadNotifierServiceImpl downloadNotifierService;
    private NodeService nodeService = mock(NodeService.class);
    private PolicyComponent policyComponent = mock(PolicyComponent.class);
    private PolicyIgnoreUtil policyIgnoreUtil = mock(PolicyIgnoreUtil.class);
    private ClassPolicyDelegate<OnDownloadNodePolicy> onDownloadNodeDelegate = mock(ClassPolicyDelegate.class);
    private NodeServicePolicies.OnDownloadNodePolicy policy = mock(NodeServicePolicies.OnDownloadNodePolicy.class);

    @Before
    public void setUp()
    {
        downloadNotifierService = new DownloadNotifierServiceImpl();
        downloadNotifierService.setNodeService(nodeService);
        downloadNotifierService.setPolicyComponent(policyComponent);
        downloadNotifierService.setPolicyIgnoreUtil(policyIgnoreUtil);
    }
    
    @Test
    public void testDownloadNotify()
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Set<QName> qnames = new HashSet<>();
        QName typeQName = null;
        
        when(policyComponent.registerClassPolicy(NodeServicePolicies.OnDownloadNodePolicy.class)).thenReturn(onDownloadNodeDelegate);
        when(policyIgnoreUtil.ignorePolicy(nodeRef)).thenReturn(false);
        when(nodeService.getAspects(nodeRef)).thenReturn(qnames);
        when(nodeService.getType(nodeRef)).thenReturn(typeQName);
        when(onDownloadNodeDelegate.get(eq(nodeRef), any(Set.class))).thenReturn(policy);
        
        downloadNotifierService.init();
        downloadNotifierService.downloadNotify(nodeRef);
    
        verify(policy).onDownloadNode(nodeRef);
    }

    @Test
    public void testDownloadNotifyIgnorePolicy()
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Set<QName> qnames = new HashSet<>();

        when(policyComponent.registerClassPolicy(NodeServicePolicies.OnDownloadNodePolicy.class)).thenReturn(onDownloadNodeDelegate);
        when(policyIgnoreUtil.ignorePolicy(nodeRef)).thenReturn(true);

        downloadNotifierService.init();
        downloadNotifierService.downloadNotify(nodeRef);

        verify(policy, never()).onDownloadNode(nodeRef);
    }
}
