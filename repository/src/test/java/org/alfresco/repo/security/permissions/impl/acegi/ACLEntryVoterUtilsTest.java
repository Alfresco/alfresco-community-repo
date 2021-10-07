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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Collections;
import java.util.Set;

import net.sf.acegisecurity.vote.AccessDecisionVoter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterUtils.getNodeRef;
import static org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterUtils.shouldAbstainOrDeny;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ACLEntryVoterUtilsTest
{
    private static final String REQUIRED_PERMISSION_REFERENCE_STRING = "REQUIRED_PERMISSION_REFERENCE_STRING";
    private NodeRef testNodeRefMock = new NodeRef("workspace://testNodeRefMock/testNodeRefMock");
    private NodeRef rootNodeMock = new NodeRef("workspace://rootNodeMock/rootNodeMock");
    private NodeRef refNodeTestObjectMock = new NodeRef("workspace://refNodeTestObjectMock/refNodeTestObjectMock");
    private NodeRef childRefMock = new NodeRef("workspace://childRefMock/childRefMock");
    private StoreRef testStoreRefMock = new StoreRef("system://testStoreRefMock/testStoreRefMock");
    private QName qNameMock1 = QName.createQName("{test}testnode1");
    private QName qNameMock2 = QName.createQName("{test}testnode2");
    private QName qNameMock3 = QName.createQName("{test}testnode3");
    private QName qNameNotFromTheAbstainSet = QName.createQName("{test}testnodeAbstain");
    private Set<QName> qNames = Set.of(qNameMock1, qNameMock2, qNameMock3);
    @Mock
    private PermissionService permissionServiceMock;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private ChildAssociationRef childAssocRefMock;

    @Before
    public void setUp()
    {
        when(nodeServiceMock.exists(testStoreRefMock)).thenReturn(Boolean.TRUE);
        when(nodeServiceMock.exists(testNodeRefMock)).thenReturn(Boolean.TRUE);
        when(nodeServiceMock.getRootNode(testStoreRefMock)).thenReturn(rootNodeMock);
        when(nodeServiceMock.getType(testNodeRefMock)).thenReturn(qNameNotFromTheAbstainSet);
        when(nodeServiceMock.getAspects(testNodeRefMock)).thenReturn(Set.of(qNameNotFromTheAbstainSet));
        when(permissionServiceMock.hasPermission(testNodeRefMock, REQUIRED_PERMISSION_REFERENCE_STRING)).thenReturn(AccessStatus.DENIED);
    }

    @Test
    public void returnsAccessDeniedFromPermissionService()
    {
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, testNodeRefMock, qNames, nodeServiceMock, permissionServiceMock),
                   is(AccessDecisionVoter.ACCESS_DENIED));
    }

    @Test
    public void returnsNullOnNullTestObject()
    {
        assertThat(getNodeRef(null, testNodeRefMock, nodeServiceMock), is(nullValue()));
    }

    @Test(expected = ACLEntryVoterException.class)
    public void throwsExceptionWhenParameterIsNotNodeRefOrChildAssociationRef()
    {
        getNodeRef("TEST", testNodeRefMock, nodeServiceMock);
    }

    @Test
    public void returnsGivenTestNodeRefWhenStoreRefDoesNotExist()
    {
        when(nodeServiceMock.exists(testStoreRefMock)).thenReturn(Boolean.FALSE);
        assertThat(getNodeRef(testStoreRefMock, testNodeRefMock, nodeServiceMock), is(testNodeRefMock));
    }

    @Test
    public void returnsRootNode()
    {
        assertThat(getNodeRef(testStoreRefMock, testNodeRefMock, nodeServiceMock), is(rootNodeMock));
    }

    @Test
    public void returnsNodeRefFromTestObject()
    {
        assertThat(getNodeRef(refNodeTestObjectMock, testNodeRefMock, nodeServiceMock), is(refNodeTestObjectMock));
    }

    @Test
    public void returnsChildRefFromChildAssocRef()
    {
        when(childAssocRefMock.getChildRef()).thenReturn(childRefMock);
        assertThat(getNodeRef(childAssocRefMock, testNodeRefMock, nodeServiceMock), is(childRefMock));
    }

    @Test
    public void returnsNullOnNullTestNodeRef()
    {
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, null, qNames, nodeServiceMock, permissionServiceMock),
                   is(nullValue()));
    }

    @Test
    public void returnsNullOnAbstainClassQnamesIsEmptyAndThereAreNoDeniedPermissions()
    {
        when(permissionServiceMock.hasPermission(testNodeRefMock, REQUIRED_PERMISSION_REFERENCE_STRING)).thenReturn(AccessStatus.ALLOWED);
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, testNodeRefMock, Collections.emptySet(), nodeServiceMock, permissionServiceMock),
                   is(nullValue()));
    }

    @Test
    public void returnsNullOnTestNodeRefDoesNotExistAndThereAreNoDeniedPermissions()
    {
        when(nodeServiceMock.exists(testNodeRefMock)).thenReturn(Boolean.FALSE);
        when(permissionServiceMock.hasPermission(testNodeRefMock, REQUIRED_PERMISSION_REFERENCE_STRING)).thenReturn(AccessStatus.ALLOWED);
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, testNodeRefMock, qNames, nodeServiceMock, permissionServiceMock),
                   is(nullValue()));
    }

    @Test
    public void returnsNullOnNodeTypeAndNodeAspectsAreNotInSetToAbstainAndThereAreNoDeniedPermissions()
    {
        when(permissionServiceMock.hasPermission(testNodeRefMock, REQUIRED_PERMISSION_REFERENCE_STRING)).thenReturn(AccessStatus.ALLOWED);
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, testNodeRefMock, qNames, nodeServiceMock, permissionServiceMock),
                   is(nullValue()));
    }

    @Test
    public void returnsAbstainWhenNodeRefTypeIsInSetToAbstain()
    {
        when(nodeServiceMock.getType(testNodeRefMock)).thenReturn(qNameMock2);
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, testNodeRefMock, qNames, nodeServiceMock, permissionServiceMock),
                   is(AccessDecisionVoter.ACCESS_ABSTAIN));
    }

    @Test
    public void returnsAbstainWhenAspectIsInSetToAbstain()
    {
        when(nodeServiceMock.getAspects(testNodeRefMock)).thenReturn(Set.of(qNameMock2, qNameMock3));
        assertThat(shouldAbstainOrDeny(REQUIRED_PERMISSION_REFERENCE_STRING, testNodeRefMock, qNames, nodeServiceMock, permissionServiceMock),
                   is(AccessDecisionVoter.ACCESS_ABSTAIN));
    }

}