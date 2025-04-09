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

package org.alfresco.repo.rendition;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 *
 * @deprecated We are introducing the new async RenditionService2.
 */
@Deprecated
public class RenditionNodeManagerTest extends TestCase
{
    private final NodeService nodeService = mock(NodeService.class);
    private final RenditionService renditionService = mock(RenditionService.class);
    private final BehaviourFilter behaviourFilter = mock(BehaviourFilter.class);

    private final NodeRef source = new NodeRef("http://test/sourceId");
    private final NodeRef destination = new NodeRef("http://test/destinationId");
    private final NodeRef oldRendition = new NodeRef("http://test/oldRenditionId");

    private final QName renditionName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "renditionName");
    private final RenditionDefinition definition = new RenditionDefinitionImpl("definitionId", renditionName, "engineName");

    // Check findOrCreateRenditionNode() works when there is
    // no old rendition and a destination node is specified and
    public void testNoOldRenditionAndDestinationSpecified()
    {
        ChildAssociationRef parentAssoc = makeAssoc(source, destination, true);
        when(nodeService.getPrimaryParent(any(NodeRef.class))).thenReturn(parentAssoc);
        RenditionLocation location = new RenditionLocationImpl(source, destination, "destinationName");
        RenditionNodeManager manager = new RenditionNodeManager(source, null, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
    }

    // Check findOrCreateRenditionNode() works when there is
    // no old rendition and a destination node is not specified.
    // the parent node is the source node.
    public void testNoOldRenditionAndNoDestinationSpecifiedAndParentIsSource()
    {
        Map<QName, Serializable> indexProps = Collections.singletonMap(ContentModel.PROP_IS_INDEXED, (Serializable) Boolean.FALSE);
        ChildAssociationRef parentAssoc = makeAssoc(source, destination, true);
        when(nodeService.createNode(source, RenditionModel.ASSOC_RENDITION, renditionName, ContentModel.TYPE_CONTENT, indexProps))
                .thenReturn(parentAssoc);

        RenditionLocation location = new RenditionLocationImpl(source, null, renditionName.getLocalName());
        RenditionNodeManager manager = new RenditionNodeManager(source, null, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
    }

    // Check findOrCreateRenditionNode() works when there is
    // no old rendition and a destination node is not specified.
    // the parent node is not the source node.
    public void testNoOldRenditionAndNoDestinationSpecifiedAndParentIsNotSource()
    {
        String localName = "Foo";
        QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName);
        NodeRef parent = new NodeRef("http://test/parentId");
        ChildAssociationRef parentAssoc = makeAssoc(parent, destination, assocName, false);

        Map<QName, Serializable> indexProps = Collections.singletonMap(ContentModel.PROP_IS_INDEXED, (Serializable) Boolean.FALSE);
        when(nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, assocName, ContentModel.TYPE_CONTENT, indexProps))
                .thenReturn(parentAssoc);

        RenditionLocation location = new RenditionLocationImpl(parent, null, localName);
        RenditionNodeManager manager = new RenditionNodeManager(source, null, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
        // Check the rendition association is created.
        verify(nodeService).addChild(source, destination, RenditionModel.ASSOC_RENDITION, renditionName);
    }

    // Check findOrCreateRenditionNode() works when there is
    // an old rendition which is specified as the destination
    // node in the location.
    public void testHasOldRenditionMatchesSpecifiedDestinationNode()
    {
        ChildAssociationRef renditionAssoc = makeAssoc(source, oldRendition, true);
        when(renditionService.getRenditionByName(source, renditionName)).thenReturn(renditionAssoc);
        RenditionLocation location = new RenditionLocationImpl(source, oldRendition, renditionName.getLocalName());
        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(renditionAssoc);
        RenditionNodeManager manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(renditionAssoc, result);
    }

    // Check findOrCreateRenditionNode() works when there is
    // an old rendition which has the specified parent folder.
    // If no name is specified and the parent folder is correct then the location should match.
    public void testHasOldRenditionCorrectParentNoNameSpecified()
    {
        NodeRef parent = new NodeRef("http://test/parentId");
        ChildAssociationRef renditionAssoc = makeAssoc(source, oldRendition, true);
        when(renditionService.getRenditionByName(source, renditionName)).thenReturn(renditionAssoc);
        ChildAssociationRef parentAssoc = makeAssoc(parent, oldRendition, false);
        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(parentAssoc);

        RenditionLocation location = new RenditionLocationImpl(parent, null, null);
        RenditionNodeManager manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
        verify(nodeService, times(2)).getPrimaryParent(oldRendition);
    }

    // Check findOrCreateRenditionNode() works when there is
    // an old rendition which has the specified parent folder.
    // If the correct name is specified and the parent folder is correct then the location should match.
    public void testHasOldRenditionCorrectParentCorrectNameSpecified()
    {
        String rendName = "Rendition Name";
        NodeRef parent = new NodeRef("http://test/parentId");
        ChildAssociationRef renditionAssoc = makeAssoc(source, oldRendition, true);
        when(renditionService.getRenditionByName(source, renditionName)).thenReturn(renditionAssoc);

        QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, rendName);
        ChildAssociationRef parentAssoc = makeAssoc(parent, oldRendition, assocName, false);
        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(parentAssoc);
        when(nodeService.getProperty(oldRendition, ContentModel.PROP_NAME))
                .thenReturn(rendName);

        RenditionLocationImpl location = new RenditionLocationImpl(parent, null, rendName);
        RenditionNodeManager manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
        verify(nodeService, times(2)).getPrimaryParent(oldRendition);
    }

    // Check findOrCreateRenditionNode() works when there is
    // an old rendition which has the wrong parent folder.
    public void testHasOldRenditionWrongParentSpecified()
    {
        NodeRef parent = new NodeRef("http://test/parentId");
        ChildAssociationRef parentAssoc = makeAssoc(parent, oldRendition, false);
        ChildAssociationRef sourceAssoc = makeAssoc(source, oldRendition, true);
        when(renditionService.getRenditionByName(source, renditionName)).thenReturn(sourceAssoc);

        // The old rendition is under the source node but should be under parent node.
        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(sourceAssoc);
        when(nodeService.moveNode(oldRendition, parent, ContentModel.ASSOC_CONTAINS, renditionName))
                .thenReturn(parentAssoc);
        RenditionLocationImpl location = new RenditionLocationImpl(parent, null, null);
        RenditionNodeManager manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
        verify(nodeService).moveNode(oldRendition, parent, ContentModel.ASSOC_CONTAINS, renditionName);

        // The old rendition is under the parent node but should be under the source node.
        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(parentAssoc);
        when(nodeService.moveNode(oldRendition, source, RenditionModel.ASSOC_RENDITION, renditionName))
                .thenReturn(sourceAssoc);
        location = new RenditionLocationImpl(source, null, null);
        manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        result = manager.findOrCreateRenditionNode();
        assertEquals(sourceAssoc, result);
        verify(nodeService).moveNode(oldRendition, source, RenditionModel.ASSOC_RENDITION, renditionName);

        // The old rendition is under the parent node but should be under the new parent node.
        NodeRef newParent = new NodeRef("http://test/newParentId");
        ChildAssociationRef newParentAssoc = makeAssoc(newParent, oldRendition, false);

        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(parentAssoc);
        when(nodeService.moveNode(oldRendition, newParent, ContentModel.ASSOC_CONTAINS, renditionName))
                .thenReturn(newParentAssoc);
        location = new RenditionLocationImpl(newParent, null, null);
        manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        result = manager.findOrCreateRenditionNode();
        assertEquals(newParentAssoc, result);
        verify(nodeService).moveNode(oldRendition, newParent, ContentModel.ASSOC_CONTAINS, renditionName);
    }

    // Check findOrCreateRenditionNode() works when there is
    // an old rendition which has the correct parent folder
    // but the wrong name
    public void testHasOldRenditionCorrectParentWrongNameSpecified()
    {
        NodeRef parent = new NodeRef("http://test/parentId");
        ChildAssociationRef parentAssoc = makeAssoc(parent, oldRendition, false);
        ChildAssociationRef renditionAssoc = makeAssoc(source, oldRendition, true);

        String newName = "newName";
        QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName);
        when(renditionService.getRenditionByName(source, renditionName)).thenReturn(renditionAssoc);
        when(nodeService.getPrimaryParent(oldRendition)).thenReturn(parentAssoc);
        when(nodeService.moveNode(oldRendition, parent, ContentModel.ASSOC_CONTAINS, assocName))
                .thenReturn(parentAssoc);
        when(nodeService.getProperty(oldRendition, ContentModel.PROP_NAME))
                .thenReturn("oldName");

        RenditionLocationImpl location = new RenditionLocationImpl(parent, null, newName);
        RenditionNodeManager manager = new RenditionNodeManager(source, oldRendition, location, definition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef result = manager.findOrCreateRenditionNode();
        assertEquals(parentAssoc, result);
        verify(nodeService).moveNode(oldRendition, parent, ContentModel.ASSOC_CONTAINS, assocName);
    }

    private ChildAssociationRef makeAssoc(NodeRef parent, NodeRef child, boolean isRenditionAssoc)
    {
        return makeAssoc(parent, child, renditionName, isRenditionAssoc);
    }

    private ChildAssociationRef makeAssoc(NodeRef parent, NodeRef child, QName assocName, boolean isRenditionAssoc)
    {
        QName assocType = isRenditionAssoc ? RenditionModel.ASSOC_RENDITION : ContentModel.ASSOC_CONTAINS;
        return new ChildAssociationRef(assocType, parent, assocName, child);
    }
}
