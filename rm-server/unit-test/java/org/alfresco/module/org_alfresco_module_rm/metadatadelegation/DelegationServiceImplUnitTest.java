/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.metadatadelegation;

import static java.util.Collections.emptyMap;
import static java.util.Arrays.asList;
import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.DelegateNotFound;
import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.DelegationNotFound;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link DelegationServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class DelegationServiceImplUnitTest
{
    @InjectMocks private final DelegationServiceImpl delegationService = new DelegationServiceImpl();

    @Mock DictionaryService          mockDictionaryService;
    @Mock NodeService                mockNodeService;
    @Mock DelegationAdminServiceImpl mockDelegationAdminService;

    /** This node has a delegate node. */
    private final NodeRef nodeWithDelegate    = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "nodeWithDelegate");
    /** This is the delgate for {@link #nodeWithDelegate}. */
    private final NodeRef delegateNode        = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "delegateNode");
    /** This node has no delegate node. */
    private final NodeRef nodeWithoutDelegate = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "nodeWithoutDelegate");

    /** The type of the peer association that links the delegate to its source. */
    private final QName delegateAssocType = QName.createQName("test", "delegateAssocType");
    /** The instance of the association between {@link #nodeWithDelegate} and {@link #delegateNode}. */
    private final AssociationRef delegateAssocRef = new AssociationRef(nodeWithDelegate, delegateAssocType, delegateNode);

    /** Name of an aspect that has been delegated. */
    private final QName delegatedAspect1 = QName.createQName("test", "delegatedAspect1");
    /** Name of an aspect that has been delegated. */
    private final QName delegatedAspect2 = QName.createQName("test", "delegatedAspect2");
    /** Name of a content class (a type in this case) that has not been delegated.
     * N.B. Types can't be delegated currently. */
    private final QName undelegatedType  = QName.createQName("test", "undelegatedType");

    private final QName delegatedProp  = QName.createQName("test", "delegatedProp");
    private final Serializable delegatedPropValue = "hello";
    private final QName undelegatedProp  = QName.createQName("test", "undelegatedProp");

    private final Delegation delegate = new Delegation()
                                           {{
                                               this.setAssocType(delegateAssocType);
                                               this.setAspects(asSet(delegatedAspect1, delegatedAspect2));
                                           }};

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        final PropertyDefinition aspectProp = mock(PropertyDefinition.class);
        final ClassDefinition aspectDefn = mock(ClassDefinition.class);
        when(aspectDefn.getName()).thenReturn(delegatedAspect1);
        when(aspectProp.getContainerClass()).thenReturn(aspectDefn);
        when(aspectDefn.isAspect()).thenReturn(true);

        final PropertyDefinition typeProp = mock(PropertyDefinition.class);
        final ClassDefinition typeDefn = mock(ClassDefinition.class);
        when(typeDefn.getName()).thenReturn(undelegatedType);
        when(typeProp.getContainerClass()).thenReturn(typeDefn);
        when(typeDefn.isAspect()).thenReturn(false);

        when(mockDictionaryService.getProperty(delegatedProp)).thenReturn(aspectProp);

        when(mockDelegationAdminService.getDelegationsFrom(nodeWithDelegate)).thenReturn(asSet(delegate));
        for (QName delegatedAspect : asSet(delegatedAspect1, delegatedAspect2))
        {
            when(mockDelegationAdminService.getDelegationFor(delegatedAspect)).thenReturn(delegate);
            when(mockNodeService.hasAspect(delegateNode, delegatedAspect)).thenReturn(true);
        }
        when(mockNodeService.getSourceAssocs(delegateNode, delegateAssocType)).thenReturn(asList(delegateAssocRef));
        when(mockNodeService.getTargetAssocs(nodeWithDelegate, delegateAssocType)).thenReturn(asList(delegateAssocRef));
        when(mockNodeService.exists(any(NodeRef.class))).thenReturn(true);
        when(mockNodeService.getProperties(delegateNode))
                .thenReturn(new HashMap<QName, Serializable>()
                {{
                    this.put(delegatedProp, delegatedPropValue);
                }});
    }

    @Test public void hasDelegateForAspect()
    {
        assertTrue(delegationService.hasDelegateForAspect(nodeWithDelegate, delegatedAspect1));
        expectedException(DelegationNotFound.class, () -> delegationService.hasDelegateForAspect(nodeWithoutDelegate, undelegatedType));
        assertFalse(delegationService.hasDelegateForAspect(nodeWithoutDelegate, delegatedAspect1));
    }

    @Test public void getDelegateFor()
    {
        assertEquals(delegateNode, delegationService.getDelegateFor(nodeWithDelegate, delegatedAspect1));
        expectedException(DelegationNotFound.class, () ->
        {
            delegationService.getDelegateFor(nodeWithDelegate, undelegatedType);
            return null;
        });
        assertNull(delegationService.getDelegateFor(nodeWithoutDelegate, delegatedAspect1));
    }

    @Test public void getDelegateProperties()
    {
        final Map<QName, Serializable> expectedProps = new HashMap<>();
        expectedProps.put(delegatedProp, delegatedPropValue);

        assertEquals(expectedProps, delegationService.getDelegateProperties(nodeWithDelegate, delegatedAspect1));

        expectedException(DelegationNotFound.class,
                () -> delegationService.getDelegateProperties(nodeWithDelegate, undelegatedType));

        expectedException(DelegateNotFound.class,
                () -> delegationService.getDelegateProperties(nodeWithoutDelegate, delegatedAspect1));
    }

    @Test public void getDelegateProperty()
    {
        assertEquals(delegatedPropValue, delegationService.getDelegateProperty(nodeWithDelegate, delegatedProp));

        expectedException(IllegalArgumentException.class,
                () -> delegationService.getDelegateProperty(nodeWithDelegate, undelegatedProp));

        expectedException(DelegationNotFound.class,
                () -> delegationService.getDelegateProperties(nodeWithoutDelegate, delegatedProp));
    }

    @Test public void hasAspectOnDelegate()
    {
        assertTrue(delegationService.hasAspectOnDelegate(nodeWithDelegate, delegatedAspect1));

        expectedException(DelegationNotFound.class,
                () -> delegationService.hasAspectOnDelegate(nodeWithDelegate, undelegatedType));

        expectedException(DelegateNotFound.class,
                () -> delegationService.hasAspectOnDelegate(nodeWithoutDelegate, delegatedAspect1));
    }

    @Test public void getDelegations()
    {
        final Map<Delegation, NodeRef> expectedDelegations = new HashMap<>();
        expectedDelegations.put(delegate, delegateNode);

        assertEquals(expectedDelegations, delegationService.getDelegations(nodeWithDelegate));
        assertEquals(emptyMap(), delegationService.getDelegations(nodeWithoutDelegate));
    }
}
