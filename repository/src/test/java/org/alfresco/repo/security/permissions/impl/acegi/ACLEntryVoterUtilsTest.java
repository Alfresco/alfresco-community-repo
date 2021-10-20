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

import static org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterUtils.getNodeRef;
import static org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterUtils.shouldAbstainOrDeny;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import net.sf.acegisecurity.vote.AccessDecisionVoter;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
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

@RunWith(MockitoJUnitRunner.class)
public class ACLEntryVoterUtilsTest {

  private final NodeRef testNodeRef = new NodeRef(
    "workspace://testNodeRef/testNodeRef"
  );
  private final NodeRef rootNodeRef = new NodeRef(
    "workspace://rootNodeRef/rootNodeRef"
  );
  private final NodeRef refNodeForTestObject = new NodeRef(
    "workspace://refNodeForTestObject/refNodeForTestObject"
  );
  private final NodeRef childRefNode = new NodeRef(
    "workspace://childRefNode/childRefNode"
  );
  private final StoreRef testStoreNodeRef = new StoreRef(
    "system://testStoreRefMock/testStoreRefMock"
  );
  private final SimplePermissionReference simplePermissionReference = SimplePermissionReference.getPermissionReference(
    QName.createQName("uri", "local"),
    "Write"
  );
  private final QName qNameToAbstain1 = QName.createQName("{test}testnode1");
  private final QName qNameToAbstain2 = QName.createQName("{test}testnode2");
  private final QName qNameToAbstain3 = QName.createQName("{test}testnode3");
  private final QName qNameNotFromTheAbstainSet = QName.createQName(
    "{test}testnodeAbstain"
  );
  private final Set<QName> qNamesToAbstain = Set.of(
    qNameToAbstain1,
    qNameToAbstain2,
    qNameToAbstain3
  );

  @Mock
  private PermissionService permissionServiceMock;

  @Mock
  private NodeService nodeServiceMock;

  @Mock
  private ChildAssociationRef childAssocRefMock;

  @Before
  public void setUp() {
    when(nodeServiceMock.exists(testStoreNodeRef)).thenReturn(Boolean.TRUE);
    when(nodeServiceMock.exists(testNodeRef)).thenReturn(Boolean.TRUE);
    when(nodeServiceMock.getRootNode(testStoreNodeRef)).thenReturn(rootNodeRef);
    when(nodeServiceMock.getType(testNodeRef))
      .thenReturn(qNameNotFromTheAbstainSet);
    when(nodeServiceMock.getAspects(testNodeRef))
      .thenReturn(Set.of(qNameNotFromTheAbstainSet));
    when(
      permissionServiceMock.hasPermission(
        eq(testNodeRef),
        nullable(String.class)
      )
    )
      .thenReturn(AccessStatus.DENIED);
  }

  @Test
  public void returnsAccessDeniedFromPermissionService() {
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        testNodeRef,
        qNamesToAbstain,
        nodeServiceMock,
        permissionServiceMock
      ),
      is(AccessDecisionVoter.ACCESS_DENIED)
    );
  }

  @Test
  public void returnsNullOnNullTestObject() {
    assertThat(getNodeRef(null, nodeServiceMock), is(nullValue()));
  }

  @Test(expected = ACLEntryVoterException.class)
  public void throwsExceptionWhenParameterIsNotNodeRefOrChildAssociationRef() {
    getNodeRef("TEST", nodeServiceMock);
  }

  @Test
  public void returnsGivenTestNodeRefWhenStoreRefDoesNotExist() {
    when(nodeServiceMock.exists(testStoreNodeRef)).thenReturn(Boolean.FALSE);
    assertThat(getNodeRef(testStoreNodeRef, nodeServiceMock), is(nullValue()));
  }

  @Test
  public void returnsRootNode() {
    assertThat(getNodeRef(testStoreNodeRef, nodeServiceMock), is(rootNodeRef));
  }

  @Test
  public void returnsNodeRefFromTestObject() {
    assertThat(
      getNodeRef(refNodeForTestObject, nodeServiceMock),
      is(refNodeForTestObject)
    );
  }

  @Test
  public void returnsChildRefFromChildAssocRef() {
    when(childAssocRefMock.getChildRef()).thenReturn(childRefNode);
    assertThat(
      getNodeRef(childAssocRefMock, nodeServiceMock),
      is(childRefNode)
    );
  }

  @Test
  public void returnsNullOnNullTestNodeRef() {
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        null,
        qNamesToAbstain,
        nodeServiceMock,
        permissionServiceMock
      ),
      is(nullValue())
    );
  }

  @Test
  public void returnsNullOnAbstainClassQnamesIsEmptyAndThereAreNoDeniedPermissions() {
    when(
      permissionServiceMock.hasPermission(
        eq(testNodeRef),
        nullable(String.class)
      )
    )
      .thenReturn(AccessStatus.ALLOWED);
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        testNodeRef,
        Collections.emptySet(),
        nodeServiceMock,
        permissionServiceMock
      ),
      is(nullValue())
    );
  }

  @Test
  public void returnsNullOnTestNodeRefDoesNotExistAndThereAreNoDeniedPermissions() {
    when(nodeServiceMock.exists(testNodeRef)).thenReturn(Boolean.FALSE);
    when(
      permissionServiceMock.hasPermission(
        eq(testNodeRef),
        nullable(String.class)
      )
    )
      .thenReturn(AccessStatus.ALLOWED);
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        testNodeRef,
        qNamesToAbstain,
        nodeServiceMock,
        permissionServiceMock
      ),
      is(nullValue())
    );
  }

  @Test
  public void returnsNullOnNodeTypeAndNodeAspectsAreNotInSetToAbstainAndThereAreNoDeniedPermissions() {
    when(
      permissionServiceMock.hasPermission(
        eq(testNodeRef),
        nullable(String.class)
      )
    )
      .thenReturn(AccessStatus.ALLOWED);
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        testNodeRef,
        qNamesToAbstain,
        nodeServiceMock,
        permissionServiceMock
      ),
      is(nullValue())
    );
  }

  @Test
  public void returnsAbstainWhenNodeRefTypeIsInSetToAbstain() {
    when(nodeServiceMock.getType(testNodeRef)).thenReturn(qNameToAbstain2);
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        testNodeRef,
        qNamesToAbstain,
        nodeServiceMock,
        permissionServiceMock
      ),
      is(AccessDecisionVoter.ACCESS_ABSTAIN)
    );
  }

  @Test
  public void returnsAbstainWhenAtLeastOneAspectIsInSetToAbstain() {
    when(nodeServiceMock.getAspects(testNodeRef))
      .thenReturn(Set.of(qNameNotFromTheAbstainSet, qNameToAbstain3));
    assertThat(
      shouldAbstainOrDeny(
        simplePermissionReference,
        testNodeRef,
        qNamesToAbstain,
        nodeServiceMock,
        permissionServiceMock
      ),
      is(AccessDecisionVoter.ACCESS_ABSTAIN)
    );
  }
}
