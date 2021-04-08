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
package org.alfresco.repo.domain.permissions;

import static org.alfresco.model.ContentModel.TYPE_BASE;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_ARCHIVE_SPACESSTORE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.permissions.FixedAclUpdater.AclWorker;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnInheritPermissionsDisabled;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.util.PolicyIgnoreUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Mock-based unit tests for {@link FixedAclUpdater}. */
public class FixedAclUpdaterUnitTest
{
    private static final NodeRef NODE_REF = new NodeRef("test://node/ref");
    private static final long NODE_ID = 123L;
    private static final NodeRef ARCHIVED_NODE = new NodeRef(STORE_REF_ARCHIVE_SPACESSTORE, "archived");

    @InjectMocks
    private FixedAclUpdater fixedAclUpdater = new FixedAclUpdater();
    /** The inner class under test. */
    private AclWorker aclWorker = fixedAclUpdater.createAclWorker();
    @Mock
    private NodeDAO nodeDAO;
    @Mock
    private AccessControlListDAO accessControlListDAO;
    @Mock
    private PolicyIgnoreUtil policyIgnoreUtil;
    @Mock
    private ClassPolicyDelegate<OnInheritPermissionsDisabled> onInheritPermissionsDisabledDelegate;
    @Mock
    private OnInheritPermissionsDisabled onInheritPermissionsDisabled;
    /** A pair of mock listeners. */
    @Mock
    private FixedAclUpdaterListener listenerA, listenerB;

    @Before
    public void setUp()
    {
        openMocks(this);

        fixedAclUpdater.registerListener(listenerA);
        fixedAclUpdater.registerListener(listenerB);
    }

    /** Check that when the AclWorker successfully processes a node then the listeners are notified. */
    @Test
    public void testListenersNotifiedAboutUpdate() throws Throwable
    {
        when(nodeDAO.getNodePair(NODE_REF)).thenReturn(new Pair<>(NODE_ID, NODE_REF));
        when(onInheritPermissionsDisabledDelegate.get(TYPE_BASE)).thenReturn(onInheritPermissionsDisabled);

        aclWorker.process(NODE_REF);

        verify(listenerA).permissionsUpdatedAsynchronously(NODE_REF);
        verify(listenerB).permissionsUpdatedAsynchronously(NODE_REF);
    }

    /** Check that archived nodes get the "Pending ACL" aspect removed without further updates, and the listeners are not notified. */
    @Test
    public void testListenersNotNotifiedAboutArchivedNode() throws Throwable
    {
        when(nodeDAO.getNodePair(ARCHIVED_NODE)).thenReturn(new Pair<>(NODE_ID, ARCHIVED_NODE));
        when(onInheritPermissionsDisabledDelegate.get(TYPE_BASE)).thenReturn(onInheritPermissionsDisabled);

        aclWorker.process(ARCHIVED_NODE);

        verify(accessControlListDAO).removePendingAclAspect(NODE_ID);
        verify(listenerA, never()).permissionsUpdatedAsynchronously(any(NodeRef.class));
        verify(listenerB, never()).permissionsUpdatedAsynchronously(any(NodeRef.class));
    }
}
