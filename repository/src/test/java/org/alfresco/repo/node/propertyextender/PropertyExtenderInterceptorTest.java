/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.node.propertyextender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class PropertyExtenderInterceptorTest
{
    private static final NodeRef NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id");
    private static final NodeRef PARENT_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "parent-node-id");
    private static final QName PROP_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "name");
    private static final QName PROP_TITLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "title");
    private static final QName PROP_DESCRIPTION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "description");
    private static final QName ASSOC_TYPE = ContentModel.ASSOC_CONTAINS;
    private static final QName ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "child");
    private static final QName NODE_TYPE = ContentModel.TYPE_CONTENT;
    private static final String METHOD_ADD_PROPERTIES = "addProperties";
    private static final Map<QName, Serializable> PROPERTY_CHANGES = Map.of(PROP_NAME, "test");

    private NodeService nodeService;
    private PropertyExtendersHolder extendersHolder;
    private PropertyExtenderInterceptor sut;
    private ArgumentCaptor<Map<QName, Serializable>> captor;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp()
    {
        nodeService = mock(NodeService.class);
        extendersHolder = spy(new PropertyExtendersHolder());
        sut = new PropertyExtenderInterceptor(nodeService, extendersHolder);
        captor = ArgumentCaptor.forClass(Map.class);
    }

    // --- addProperties ---

    @Test
    public void testAddProperties_noExtenders_shouldPassOriginalProperties() throws Throwable
    {
        // given
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(NODE_REF, PROPERTY_CHANGES);
    }

    @Test
    public void testAddProperties_withExtender_shouldMergeAdditionalProperties() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_TITLE, "calculated-title")));
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(eq(NODE_REF), captor.capture());
        assertThat(captor.getValue())
                .containsEntry(PROP_NAME, "test")
                .containsEntry(PROP_TITLE, "calculated-title");
    }

    @Test
    public void testAddProperties_extenderReturnsNoOp_shouldPassOriginalProperties() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of()));
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(NODE_REF, PROPERTY_CHANGES);
    }

    // --- setProperties ---

    @Test
    public void testSetProperties_noExtenders_shouldPassOriginalProperties() throws Throwable
    {
        // given
        var invocation = mockInvocation("setProperties", new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).setProperties(NODE_REF, PROPERTY_CHANGES);
    }

    @Test
    public void testSetProperties_withExtender_shouldMergeAdditionalProperties() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_TITLE, "calculated-title")));
        var invocation = mockInvocation("setProperties", new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).setProperties(eq(NODE_REF), captor.capture());
        assertThat(captor.getValue())
                .containsEntry(PROP_NAME, "test")
                .containsEntry(PROP_TITLE, "calculated-title");
    }

    // --- createNode ---

    @Test
    public void testCreateNode_noExtenders_shouldPassOriginalProperties() throws Throwable
    {
        // given
        var expectedResult = mock(ChildAssociationRef.class);
        when(nodeService.createNode(PARENT_NODE_REF, ASSOC_TYPE, ASSOC_QNAME, NODE_TYPE, PROPERTY_CHANGES))
                .thenReturn(expectedResult);
        var invocation = mockInvocation("createNode", new Object[]{PARENT_NODE_REF, ASSOC_TYPE, ASSOC_QNAME, NODE_TYPE, PROPERTY_CHANGES});

        // when
        var result = sut.invoke(invocation);

        // then
        assertThat(result).isSameAs(expectedResult);
        verify(nodeService).createNode(PARENT_NODE_REF, ASSOC_TYPE, ASSOC_QNAME, NODE_TYPE, PROPERTY_CHANGES);
    }

    @Test
    public void testCreateNode_withExtender_shouldMergeAdditionalProperties() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_TITLE, "calculated-title")));
        var invocation = mockInvocation("createNode", new Object[]{PARENT_NODE_REF, ASSOC_TYPE, ASSOC_QNAME, NODE_TYPE, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).createNode(eq(PARENT_NODE_REF), eq(ASSOC_TYPE), eq(ASSOC_QNAME), eq(NODE_TYPE), captor.capture());
        assertThat(captor.getValue())
                .containsEntry(PROP_NAME, "test")
                .containsEntry(PROP_TITLE, "calculated-title");
    }

    // --- setProperty ---

    @Test
    public void testSetProperty_noExtenders_shouldCallSetPropertyOnly() throws Throwable
    {
        // given
        var invocation = mockInvocation("setProperty", new Object[]{NODE_REF, PROP_NAME, "test"});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).setProperty(NODE_REF, PROP_NAME, "test");
        verify(nodeService, never()).addProperties(any(), any());
    }

    @Test
    public void testSetProperty_withExtender_shouldAddAdditionalPropertiesAndSetOriginal() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_TITLE, "calculated-title")));
        var invocation = mockInvocation("setProperty", new Object[]{NODE_REF, PROP_NAME, "test"});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(eq(NODE_REF), captor.capture());
        assertThat(captor.getValue()).containsEntry(PROP_TITLE, "calculated-title");
        verify(nodeService).setProperty(NODE_REF, PROP_NAME, "test");
    }

    @Test
    public void testSetProperty_extenderReturnsNoOp_shouldCallSetPropertyOnly() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of()));
        var invocation = mockInvocation("setProperty", new Object[]{NODE_REF, PROP_NAME, "test"});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).setProperty(NODE_REF, PROP_NAME, "test");
        verify(nodeService, never()).addProperties(any(), any());
    }

    // --- removeProperty ---

    @Test
    public void testRemoveProperty_noExtenders_shouldCallRemovePropertyOnly() throws Throwable
    {
        // given
        var invocation = mockInvocation("removeProperty", new Object[]{NODE_REF, PROP_NAME});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).removeProperty(NODE_REF, PROP_NAME);
        verify(nodeService, never()).addProperties(any(), any());
    }

    @Test
    public void testRemoveProperty_withExtender_shouldAddAdditionalPropertiesAndRemoveOriginal() throws Throwable
    {
        // given
        Map<QName, Serializable> additionalProps = new HashMap<>();
        additionalProps.put(PROP_TITLE, null);
        additionalProps.put(PROP_DESCRIPTION, null);
        extendersHolder.registerExtender(extenderReturning(additionalProps));
        var invocation = mockInvocation("removeProperty", new Object[]{NODE_REF, PROP_NAME});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(eq(NODE_REF), captor.capture());
        assertThat(captor.getValue())
                .containsEntry(PROP_TITLE, null)
                .containsEntry(PROP_DESCRIPTION, null);
        verify(nodeService).removeProperty(NODE_REF, PROP_NAME);
    }

    // --- multiple extenders ---

    @Test
    public void testAddProperties_multipleExtenders_shouldMergeAllAdditionalProperties() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_TITLE, "title-from-ext1")));
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_DESCRIPTION, "desc-from-ext2")));
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(eq(NODE_REF), captor.capture());
        assertThat(captor.getValue())
                .containsEntry(PROP_NAME, "test")
                .containsEntry(PROP_TITLE, "title-from-ext1")
                .containsEntry(PROP_DESCRIPTION, "desc-from-ext2");
    }

    // --- additional properties override original ---

    @Test
    public void testAddProperties_extenderOverridesOriginal_shouldUseCalculatedValue() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_NAME, "overridden-name")));
        Map<QName, Serializable> properties = Map.of(PROP_NAME, "original-name");
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, properties});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(eq(NODE_REF), captor.capture());
        assertThat(captor.getValue()).containsEntry(PROP_NAME, "overridden-name");
    }

    // --- unmatched methods ---

    @Test
    public void testUnmatchedMethod_shouldProceedWithInvocation() throws Throwable
    {
        // given
        var unmatchedInvocations = List.of(
                mockInvocation("getProperties", new Object[]{NODE_REF}),
                mockInvocation("getProperty", new Object[]{NODE_REF, PROP_NAME}),
                mockInvocation("exists", new Object[]{NODE_REF}),
                mockInvocation("getType", new Object[]{NODE_REF}),
                mockInvocation("deleteNode", new Object[]{NODE_REF}),
                mockInvocation("getAspects", new Object[]{NODE_REF}),
                mockInvocation("hasAspect", new Object[]{NODE_REF, PROP_NAME}),
                mockInvocation("getPrimaryParent", new Object[]{NODE_REF}),
                mockInvocation("getPath", new Object[]{NODE_REF}),
                mockInvocation("getChildAssocs", new Object[]{NODE_REF}),
                mockInvocation("getParentAssocs", new Object[]{NODE_REF}),
                mockInvocation("createNode", new Object[]{PARENT_NODE_REF, ASSOC_TYPE, ASSOC_QNAME, NODE_TYPE}));

        // when, then
        for (var invocation : unmatchedInvocations)
        {
            sut.invoke(invocation);
            verify(invocation).proceed();
            verify(extendersHolder, never()).getExtenders();
        }
    }

    // --- error handling ---

    @Test
    public void testAddProperties_extenderThrowsAlfrescoRuntimeException_shouldRethrow() throws Throwable
    {
        // given
        var extender = mock(PropertyExtender.class);
        when(extender.calculate(any())).thenThrow(new AlfrescoRuntimeException("expected error"));
        extendersHolder.registerExtender(extender);
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when, then
        assertThatThrownBy(() -> sut.invoke(invocation))
                .isInstanceOf(AlfrescoRuntimeException.class)
                .hasMessageContaining("expected error");
    }

    @Test
    public void testAddProperties_extenderThrowsRuntimeException_shouldWrapInAlfrescoRuntimeException() throws Throwable
    {
        // given
        var extender = mock(PropertyExtender.class);
        when(extender.calculate(any())).thenThrow(new IllegalStateException("unexpected error"));
        extendersHolder.registerExtender(extender);
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, PROPERTY_CHANGES});

        // when, then
        assertThatThrownBy(() -> sut.invoke(invocation))
                .isInstanceOf(AlfrescoRuntimeException.class)
                .hasMessageContaining("Unexpected failure during properties calculation process")
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    // --- empty properties ---

    @Test
    public void testAddProperties_emptyProperties_shouldPassWithoutCalculation() throws Throwable
    {
        // given
        extendersHolder.registerExtender(extenderReturning(Map.of(PROP_TITLE, "should-not-appear")));
        Map<QName, Serializable> properties = Map.of();
        var invocation = mockInvocation(METHOD_ADD_PROPERTIES, new Object[]{NODE_REF, properties});

        // when
        sut.invoke(invocation);

        // then
        verify(nodeService).addProperties(NODE_REF, properties);
    }

    // --- helpers ---

    private MethodInvocation mockInvocation(String methodName, Object[] args) throws NoSuchMethodException
    {
        var invocation = mock(MethodInvocation.class);
        Method method = findMethod(methodName, args.length);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(args);
        return invocation;
    }

    private Method findMethod(String methodName, int argCount) throws NoSuchMethodException
    {
        for (Method method : NodeService.class.getMethods())
        {
            if (method.getName().equals(methodName) && method.getParameterCount() == argCount)
            {
                return method;
            }
        }
        throw new NoSuchMethodException("No method " + methodName + " with " + argCount + " args found in NodeService");
    }

    private PropertyExtender extenderReturning(Map<QName, Serializable> additionalProperties)
    {
        return new StubPropertyExtender(additionalProperties);
    }

    private record StubPropertyExtender(Map<QName, Serializable> additionalProperties) implements PropertyExtender
    {
        @Override
        public CalculationResult calculate(CalculationContext context)
        {
            return new CalculationResult(additionalProperties);
        }
    }
}
