/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Unit tests for the checkNotSystemPath method in {@link NodesImpl}. Validates that REST API operations are blocked on nodes under system paths (e.g., Data Dictionary).
 */
@RunWith(MockitoJUnitRunner.class)
public class NodesImplSystemPathTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private Repository repositoryHelper;

    @InjectMocks
    private NodesImpl nodesImpl;

    private NodeRef companyHomeRef;
    private NodeRef dataDictionaryRef;

    private Method checkNotSystemPathMethod;

    @Before
    public void setUp() throws Exception
    {
        companyHomeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "company-home");
        dataDictionaryRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "data-dictionary");

        when(repositoryHelper.getCompanyHome()).thenReturn(companyHomeRef);

        // Setup Data Dictionary as child of Company Home (used by isSpecialNode)
        ChildAssociationRef ddToCompanyHome = new ChildAssociationRef(
                ContentModel.ASSOC_CONTAINS, companyHomeRef,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"),
                dataDictionaryRef);
        when(nodeService.getChildAssocs(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary")))
                        .thenReturn(Collections.singletonList(ddToCompanyHome));

        checkNotSystemPathMethod = NodesImpl.class.getDeclaredMethod("checkNotSystemPath", NodeRef.class);
        checkNotSystemPathMethod.setAccessible(true);
    }

    private void invokeCheckNotSystemPath(NodeRef nodeRef) throws Exception
    {
        try
        {
            checkNotSystemPathMethod.invoke(nodesImpl, nodeRef);
        }
        catch (java.lang.reflect.InvocationTargetException e)
        {
            if (e.getCause() instanceof PermissionDeniedException)
            {
                throw (PermissionDeniedException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Node whose ancestor (grandparent) is a special node (Data Dictionary) should be blocked.
     */
    @Test
    public void testNodeWithSpecialAncestor_PermissionDenied() throws Exception
    {
        NodeRef parentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "parent-uuid");
        NodeRef childRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "child-uuid");

        // child -> parent -> dataDictionary (special node)
        when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(childRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentRef, null, childRef));

        when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(parentRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, dataDictionaryRef, null, parentRef));

        when(nodeService.getType(dataDictionaryRef)).thenReturn(ContentModel.TYPE_FOLDER);

        assertThrows(PermissionDeniedException.class, () -> invokeCheckNotSystemPath(childRef));
    }

    /**
     * Node under normal folders (no special ancestors) should pass without exception.
     */
    @Test
    public void testNodeWithNormalAncestors_Succeed() throws Exception
    {
        NodeRef parentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "normal-parent");
        NodeRef childRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "normal-child");

        // child -> parent -> companyHome (loop breaks at companyHome)
        when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(childRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentRef, null, childRef));

        when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(parentRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, companyHomeRef, null, parentRef));

        invokeCheckNotSystemPath(childRef); // should not throw
    }

    /**
     * When node is Company Home itself, loop should break immediately without traversing further.
     */
    @Test
    public void testCompanyHome_ShouldNotTraverseFurther() throws Exception
    {
        invokeCheckNotSystemPath(companyHomeRef); // should not throw

        verify(nodeService, never()).getPrimaryParent(companyHomeRef);
    }

    /**
     * Node whose direct parent is a special node (Data Dictionary) should be blocked.
     */
    @Test
    public void testDirectParentIsSpecialNode_ThrowPermissionDenied() throws Exception
    {
        NodeRef childRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "child-under-dd");

        // child -> dataDictionary (special node)
        when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(childRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, dataDictionaryRef, null, childRef));

        when(nodeService.getType(dataDictionaryRef)).thenReturn(ContentModel.TYPE_FOLDER);

        assertThrows(PermissionDeniedException.class, () -> invokeCheckNotSystemPath(childRef));
    }
}
