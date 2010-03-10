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

import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.rendition.RenditionDefinitionImpl;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Nick Smith
 */
public class AbstractRenderingEngineTest extends TestCase
{
    private ContentService contentService;
    private NodeService nodeService;
    private TestRenderingEngine engine;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.contentService = mock(ContentService.class);
        NodeService nodeService1 = makeNodeService();
        this.nodeService = nodeService1;
        engine = new TestRenderingEngine();
        engine.setContentService(contentService);
        engine.setNodeService(nodeService);
    }

    /**
     * Creates a mock node service which fails with a helpful message by default if createNode is called.
     * @return
     */
    private NodeService makeNodeService()
    {
        NodeService nodeService1 = mock(NodeService.class);
        return nodeService1;
    }

    @SuppressWarnings("unchecked")
    public void testCreateRenditionNodeAssoc() throws Exception
    {
        NodeRef source = new NodeRef("http://test/sourceId");
        when(nodeService.exists(source)).thenReturn(true);

        QName assocType = ContentModel.ASSOC_CONTAINS;
        QName assocName = QName.createQName("url", "renditionName");
        QName nodeType = ContentModel.TYPE_CONTENT;
        ChildAssociationRef renditionAssoc = mock(ChildAssociationRef.class);

        // Set up the rendition definition.
        String id = "definitionId";
        RenditionDefinition definition = new RenditionDefinitionImpl(id, assocName, TestRenderingEngine.NAME);
        definition.setRenditionAssociationType(assocType);
        definition.setRenditionParent(source);
        
        // Stub the createNode() method to return renditionAssoc.
        when(nodeService
                    .createNode(eq(source), eq(assocType), any(QName.class), any(QName.class), anyMap()))
                    .thenReturn(renditionAssoc);
        engine.execute(definition, source);

        // Check the createNode method was called with the correct parameters.
        // Check the nodeType defaults to cm:content.
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(nodeService).createNode(eq(source), eq(assocType), any(QName.class), eq(nodeType), captor.capture());
        Map<String, Serializable> props = captor.getValue();

        // Check the node name is set to match teh rendition name local name.
        assertEquals(assocName.getLocalName(), props.get(ContentModel.PROP_NAME));
        
        // Check content property name defaults to cm:content
        assertEquals(ContentModel.PROP_CONTENT, props.get(ContentModel.PROP_CONTENT_PROPERTY_NAME));

        // Check the returned result is the association created by the call to nodeServcie.createNode().
        Serializable result = definition.getParameterValue(ActionExecuter.PARAM_RESULT);
        assertEquals("The returned rendition association is not the one created by the node service!",
                    renditionAssoc, result);
        
        // Check that setting the default content property and default node type
        // on the rendition engine works.
        nodeType = QName.createQName("url", "someNodeType");
        QName contentPropName = QName.createQName("url", "someContentProp");
        engine.setDefaultRenditionContentProp(contentPropName.toString());
        engine.setDefaultRenditionNodeType(nodeType.toString());
        engine.execute(definition, source);
        verify(nodeService).createNode(eq(source), eq(assocType), any(QName.class), eq(nodeType), captor.capture());
        props=captor.getValue();
        assertEquals(contentPropName, props.get(ContentModel.PROP_CONTENT_PROPERTY_NAME));

        // Check that settign the rendition node type param works.
        nodeType = ContentModel.TYPE_THUMBNAIL;
        contentPropName = ContentModel.PROP_CONTENT;
        definition.setParameterValue(RenditionService.PARAM_RENDITION_NODETYPE, nodeType);
        definition.setParameterValue(AbstractRenderingEngine.PARAM_TARGET_CONTENT_PROPERTY, contentPropName);
        engine.execute(definition, source);
        verify(nodeService).createNode(eq(source), eq(assocType), any(QName.class), eq(nodeType), captor.capture());
        props=captor.getValue();
        assertEquals(contentPropName, props.get(ContentModel.PROP_CONTENT_PROPERTY_NAME));
    }

    private static class TestRenderingEngine extends AbstractRenderingEngine
    {
        private RenderingContext context;

        public static String NAME = "Test";

        @Override
        protected void render(RenderingContext context1)
        {
            this.context = context1;
        }

        public RenderingContext getContext()
        {
            return context;
        }
    }
}
