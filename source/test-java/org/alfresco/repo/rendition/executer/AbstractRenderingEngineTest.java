/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.rendition.executer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.RenditionDefinitionImpl;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.mockito.ArgumentCaptor;

/**
 * @author Nick Smith
 */
public class AbstractRenderingEngineTest extends TestCase
{
    private final NodeRef source = new NodeRef("http://test/sourceId");
    private ContentService contentService;
    private NodeService nodeService;
    private TestRenderingEngine engine;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.contentService = mock(ContentService.class);
        this.nodeService = mock(NodeService.class);
        engine = new TestRenderingEngine();
        engine.setContentService(contentService);
        engine.setNodeService(nodeService);
        engine.setBehaviourFilter(mock(BehaviourFilter.class));
    }

    @SuppressWarnings({"unchecked" , "rawtypes"})
    public void testCreateRenditionNodeAssoc() throws Exception
    {
        QName assocType = RenditionModel.ASSOC_RENDITION;
        when(nodeService.exists(source)).thenReturn(true);
        QName nodeType = ContentModel.TYPE_CONTENT;
        ChildAssociationRef renditionAssoc = makeRenditionAssoc();
        RenditionDefinition definition = makeRenditionDefinition(renditionAssoc);

        // Stub the createNode() method to return renditionAssoc.
        when(nodeService.createNode(eq(source), eq(assocType), any(QName.class), any(QName.class), anyMap()))
            .thenReturn(renditionAssoc);
        engine.execute(definition, source);

        // Check the createNode method was called with the correct parameters.
        // Check the nodeType defaults to cm:content.
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(nodeService).createNode(eq(source), eq(assocType), any(QName.class), eq(nodeType), captor.capture());
        Map<String, Serializable> props = captor.getValue();

        // Check the node name is set to match teh rendition name local name.
        assertEquals(renditionAssoc.getQName().getLocalName(), props.get(ContentModel.PROP_NAME));

        // Check content property name defaults to cm:content
        assertEquals(ContentModel.PROP_CONTENT, props.get(ContentModel.PROP_CONTENT_PROPERTY_NAME));

        // Check the returned result is the association created by the call to
        // nodeServcie.createNode().
        Serializable result = definition.getParameterValue(ActionExecuter.PARAM_RESULT);
        assertEquals("The returned rendition association is not the one created by the node service!", renditionAssoc,
                    result);

        // Check that setting the default content property and default node type
        // on the rendition engine works.
        nodeType = QName.createQName("url", "someNodeType");
        QName contentPropName = QName.createQName("url", "someContentProp");
        engine.setDefaultRenditionContentProp(contentPropName.toString());
        engine.setDefaultRenditionNodeType(nodeType.toString());
        engine.execute(definition, source);
        verify(nodeService).createNode(eq(source), eq(assocType), any(QName.class), eq(nodeType), captor.capture());
        props = captor.getValue();
        assertEquals(contentPropName, props.get(ContentModel.PROP_CONTENT_PROPERTY_NAME));

        // Check that setting the rendition node type param works.
        nodeType = ContentModel.TYPE_THUMBNAIL;
        contentPropName = ContentModel.PROP_CONTENT;
        definition.setParameterValue(RenditionService.PARAM_RENDITION_NODETYPE, nodeType);
        definition.setParameterValue(AbstractRenderingEngine.PARAM_TARGET_CONTENT_PROPERTY, contentPropName);
        engine.execute(definition, source);
        verify(nodeService).createNode(eq(source), eq(assocType), any(QName.class), eq(nodeType), captor.capture());
        props = captor.getValue();
        assertEquals(contentPropName, props.get(ContentModel.PROP_CONTENT_PROPERTY_NAME));
    }

    public void testCheckSourceNodeExists()
    {
        when(nodeService.exists(any(NodeRef.class))).thenReturn(false);
        RenditionDefinitionImpl definition = new RenditionDefinitionImpl("id", null, TestRenderingEngine.NAME);
        try
        {
            engine.executeImpl(definition, source);
            fail("Should have thrown an exception here!");
        } catch(RenditionServiceException e)
        {
            assertTrue(e.getMessage().endsWith("Cannot execute action as node does not exist: http://test/sourceId"));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testRenderingContext()
    {
        when(nodeService.exists(source)).thenReturn(true);
        ChildAssociationRef renditionAssoc = makeRenditionAssoc();
        RenditionDefinition definition = makeRenditionDefinition(renditionAssoc);
        // Stub the createNode() method to return renditionAssoc.
        when(nodeService.createNode(eq(source), eq(renditionAssoc.getTypeQName()), any(QName.class), any(QName.class), anyMap()))
                    .thenReturn(renditionAssoc);
        engine.execute(definition, source);

        RenderingContext context = engine.getContext();
        assertEquals(definition, context.getDefinition());
        assertEquals(renditionAssoc.getChildRef(), context.getDestinationNode());
        assertEquals(source, context.getSourceNode());
    }

    @SuppressWarnings("unchecked")
    public void testGetParameterWithDefault()
    {
        when(nodeService.exists(source)).thenReturn(true);
        ChildAssociationRef renditionAssoc = makeRenditionAssoc();
        RenditionDefinition definition = makeRenditionDefinition(renditionAssoc);
        // Stub the createNode() method to return renditionAssoc.
        when(nodeService.createNode(eq(source), eq(renditionAssoc.getTypeQName()), any(QName.class), any(QName.class), anyMap()))
                    .thenReturn(renditionAssoc);
        engine.executeImpl(definition, source);
        RenderingContext context = engine.getContext();
        
        // Check default value works.
        String paramName = "Some-param";
        String defaultValue = "default";
        Object result = context.getParamWithDefault(paramName, defaultValue);
        assertEquals(defaultValue, result);
        
        // Check specific value overrides default.
        String value = "value";
        definition.setParameterValue(paramName, value);
        engine.executeImpl(definition, source);
        context = engine.getContext();
        result = context.getParamWithDefault(paramName, defaultValue);
        assertEquals(value, result);
        
        // Check null default value throws exception.
        try
        {
            result = context.getParamWithDefault(paramName, null);
            fail("Should throw an Exception if default value is null!");
        } catch(RenditionServiceException e)
        {
            assertTrue(e.getMessage().endsWith("The defaultValue cannot be null!"));
        }
        
        // Check wrong type of default value throws exception.
        try
        {
            result = context.getParamWithDefault(paramName, Boolean.TRUE);
            fail("Should throw an exception if default value is of incoorect type!");
        } catch (RenditionServiceException e)
        {
            assertTrue(e.getMessage().endsWith("The parameter: Some-param must be of type: java.lang.Booleanbut was of type: java.lang.String"));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testGetCheckedParameter()
    {
        when(nodeService.exists(source)).thenReturn(true);
        ChildAssociationRef renditionAssoc = makeRenditionAssoc();
        RenditionDefinition definition = makeRenditionDefinition(renditionAssoc);
        // Stub the createNode() method to return renditionAssoc.
        when(nodeService.createNode(eq(source), eq(renditionAssoc.getTypeQName()), any(QName.class), any(QName.class), anyMap()))
                    .thenReturn(renditionAssoc);
        engine.executeImpl(definition, source);
        RenderingContext context = engine.getContext();
        String paramName = "Some param";
        
        // Check returns null by default.
        String result = context.getCheckedParam(paramName, String.class);
        assertNull(result);
        
        // Check can set a value to return.
        String value = "value";
        definition.setParameterValue(paramName, value);
        engine.executeImpl(definition, source);
        context = engine.getContext();
        result = context.getCheckedParam(paramName, String.class);
        assertEquals(value, result);
        
        // Check throws an exception if value is of wrong type.
        try
        {
            context.getCheckedParam(paramName, Boolean.class);
            fail("Should throw an exception if type is wrong!");
        } catch(RenditionServiceException e)
        {
            assertTrue(e.getMessage().endsWith("The parameter: Some param must be of type: java.lang.Booleanbut was of type: java.lang.String"));
        }
        
        // Check throws an exception if value is of wrong type.
        try
        {
            context.getCheckedParam(paramName, null);
            fail("Should throw an exception if type is wrong!");
        } catch(RenditionServiceException e)
        {
            assertTrue(e.getMessage().endsWith("The class must not be null!"));
        }
    }
    
    /**
     * Set up the rendition definition.
     * @param renditionAssoc
     * @return
     */
    private RenditionDefinition makeRenditionDefinition(ChildAssociationRef renditionAssoc)
    {
        String id = "definitionId";
        RenditionDefinition definition = new RenditionDefinitionImpl(id, renditionAssoc.getQName(), TestRenderingEngine.NAME);
        definition.setRenditionAssociationType(renditionAssoc.getTypeQName());
        definition.setRenditionParent(source);
        return definition;
    }

    /**
     * Create the rendition association and destination node.
     */
    private ChildAssociationRef makeRenditionAssoc()
    {
        QName assocType = RenditionModel.ASSOC_RENDITION;
        QName assocName = QName.createQName("url", "renditionName");
        NodeRef destination = new NodeRef("http://test/destinationId");
        return new ChildAssociationRef(assocType, source, assocName, destination);
    }
    
    private static class TestRenderingEngine extends AbstractRenderingEngine
    {
        public static String NAME = "Test";
        
        private RenderingContext context;

        @Override
        protected void render(RenderingContext context1)
        {
            this.context = context1;
        }

        public RenderingContext getContext()
        {
            return context;
        }
        
        @Override
        protected void switchToFinalRenditionNode(RenditionDefinition renditionDef, NodeRef actionedUponNodeRef)
        {
            // Do nothing!
        }
    }
}
