/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.audit.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for NodeChange which is the main class behind AccessAuditor.
 * 
 * @author Alan Davis
 */
public class NodeChangeTest
{
    private static final StoreRef STORE = new StoreRef("protocol", "store");
    
    private NodeChange nodeChange;
    
    private NodeInfoFactory nodeInfoFactory;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    
    private NodeRef folder1;
    private NodeRef folder2;
    private NodeRef content1;
    private Path folderPath1;
    private Path folderPath2;

    @Before
    public void setUp() throws Exception
    {
        namespaceService = mock(NamespaceService.class);
        Collection<String> cmAlways = new ArrayList<String>();
        cmAlways.add("cm");
        when(namespaceService.getPrefixes(anyString())).thenReturn(cmAlways);
        when(namespaceService.getNamespaceURI(anyString())).thenReturn("cm");
        
        nodeService = mock(NodeService.class);
        
        Path rootPath = newPath(null, "/");
        Path homeFolderPath = newPath(rootPath, "cm:homeFolder");
        folderPath1 = newPath(homeFolderPath, "cm:folder1");
        folderPath2 = newPath(homeFolderPath, "cm:folder2");
        folder1 = newFolder(folderPath1);
        folder2 = newFolder(folderPath2);
        content1 = newContent(folderPath1, "cm:content1");
        
        nodeInfoFactory = new NodeInfoFactory(nodeService, namespaceService);
        nodeChange = new NodeChange(nodeInfoFactory, namespaceService, content1);
    }
    
    private NodeRef newFolder(Path path)
    {
        String name = path.get(path.size()-1).getElementString();
        return newNodeRef(path, name, "folder");
    }
    
    private NodeRef newContent(Path parentPath, String name)
    {
        Path path = newPath(parentPath, name);
        return newNodeRef(path, name, "content");
    }
    
    private NodeRef newNodeRef(Path path, String name, String type)
    {
        NodeRef nodeRef = new NodeRef(STORE, name);
        QName qNameType = QName.createQName("URI", type);
        when(nodeService.getType(nodeRef)).thenReturn(qNameType);
        when(nodeService.getPath(nodeRef)).thenReturn(path);
        return nodeRef;
    }

    @SuppressWarnings("serial")
    private Path newPath(Path parent, final String name)
    {
        Path path = new Path();
        if (parent != null)
        {
            for(Path.Element element: parent)
            {
                path.append(element);
            }
        }
        path.append(new Path.Element()
        {
            @Override
            public String getElementString()
            {
                return name;
            }
        });
        return path;
    }
    
    private void assertStandardData(Map<String, Serializable> auditMap,
            String expectedAction, String expectedSubActions)
    {
        String expectedPath = "/cm:homeFolder/cm:folder1/cm:content1";
        assertEquals(expectedAction, auditMap.get("action"));
        assertEquals(expectedSubActions, auditMap.get("sub-actions"));
        assertEquals(expectedPath, auditMap.get("path"));
        assertEquals(content1, auditMap.get("node"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    private void callCreateNode()
    {
        ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
        when(childAssocRef.getChildRef()).thenReturn(content1);
        nodeChange.onCreateNode(childAssocRef);
    }
    
    private void callDeleteNode()
    {
        nodeChange.beforeDeleteNode(content1);
    }
    
    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public final void testGetAuditDataTrueSubAction()
    {
        callCreateNode();
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(true);

        assertEquals("Should NOT be derived", "createNode", auditMap.get("action"));
        assertFalse("'user' should not exist in a subAction", auditMap.keySet().contains("user"));
        assertFalse("'sub-actions' should not exist in a subAction", auditMap.keySet().contains("sub-actions"));
        assertFalse("'node' should not exist in a subAction", auditMap.keySet().contains("node"));
        assertFalse("'path' should not exist in a subAction", auditMap.keySet().contains("path"));
        assertFalse("'type' should not exist in a subAction", auditMap.keySet().contains("type"));
    }

    @Test
    public final void testGetAuditDataFalseTopLevelAction()
    {
        callCreateNode();
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertEquals("Should be derived", "CREATE", auditMap.get("action"));
        assertTrue("'user' should exist if not a subAction", auditMap.keySet().contains("user"));
        assertTrue("'sub-actions' should exist if not a subAction", auditMap.keySet().contains("sub-actions"));
        assertTrue("'node' should exist if not a subAction", auditMap.keySet().contains("node"));
        assertTrue("'path' should exist if not a subAction", auditMap.keySet().contains("path"));
        assertTrue("'type' should exist if not a subAction", auditMap.keySet().contains("type"));
    }

    @Test
    public final void testSetAuditSubActionsTrue()
    {
        nodeChange.setAuditSubActions(true);
        callCreateNode();
        nodeChange.beforeDeleteNode(content1);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertEquals("subAction audit should exist", "createNode", auditMap.get("sub-action/0/action"));
        assertEquals("subAction audit should exist", "deleteNode", auditMap.get("sub-action/1/action"));
    }

    @Test
    public final void testSetAuditSubActionsFalse()
    {
        nodeChange.setAuditSubActions(false);
        callCreateNode();
        nodeChange.beforeDeleteNode(content1);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertFalse("subAction audit should NOT exist", auditMap.keySet().contains("sub-action/0/action"));
        assertFalse("subAction audit should NOT exist", auditMap.keySet().contains("sub-action/1/action"));
    }

    @Test
    public final void testOnCreateNode()
    {
        callCreateNode();
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "CREATE", "createNode");
    }

    @Test
    public final void testBeforeDeleteNode()
    {
        callDeleteNode();
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "DELETE", "deleteNode");
    }

    @Test
    public final void testIsTemporaryNodeYes()
    {
        callCreateNode();
        nodeChange.beforeDeleteNode(content1);
        
        assertTrue("A node was created and deleted so should have been temporary.",
                nodeChange.isTemporaryNode());
    }

    @Test
    public final void testOnMoveNode()
    {
        ChildAssociationRef fromChildAssocRef = mock(ChildAssociationRef.class);
        when(fromChildAssocRef.getChildRef()).thenReturn(content1); // correct as the move has taken place
        when(fromChildAssocRef.getParentRef()).thenReturn(folder2);
        when(fromChildAssocRef.getQName()).thenReturn(QName.createQName("URI", "content1"));
        
        ChildAssociationRef toChildAssocRef = mock(ChildAssociationRef.class);
        when(toChildAssocRef.getChildRef()).thenReturn(content1);
        when(toChildAssocRef.getParentRef()).thenReturn(folder1);
        when(toChildAssocRef.getQName()).thenReturn(QName.createQName("URI", "content1"));
        
        nodeChange.onMoveNode(fromChildAssocRef, toChildAssocRef);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "MOVE", "moveNode");
        assertEquals("/cm:homeFolder/cm:folder2/cm:content1", auditMap.get("move/from/path"));
        assertEquals(content1, auditMap.get("move/from/node"));
        assertEquals("cm:content", auditMap.get("move/from/type"));
    }

    @Test
    public final void testOnCopyComplete()
    {
        NodeRef content2 = newContent(folderPath2, "cm:content2");
        nodeChange.onCopyComplete(null, content2, content1, true, null);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "COPY", "copyNode");
        assertEquals("/cm:homeFolder/cm:folder2/cm:content2", auditMap.get("copy/from/path"));
        assertEquals(content2, auditMap.get("copy/from/node"));
        assertEquals("cm:content", auditMap.get("copy/from/type"));
    }

    @Test
    public final void testOnUpdateProperties()
    {
        Map<QName, Serializable> fromProperties = new HashMap<QName, Serializable>();
        fromProperties.put(ContentModel.PROP_CREATED, "created");
        fromProperties.put(ContentModel.PROP_CREATOR, "creator");
        fromProperties.put(ContentModel.PROP_CONTENT, "content");
        fromProperties.put(ContentModel.PROP_LOCATION, "location");
        fromProperties.put(ContentModel.PROP_MOBILE, "mobile");
        fromProperties.put(ContentModel.PROP_HITS, "hits");
        fromProperties.put(ContentModel.PROP_TITLE, "title");
        
        Map<QName, Serializable> toProperties = new HashMap<QName, Serializable>(fromProperties);
        toProperties.put(ContentModel.PROP_AUTHOR, "AUTHOR");
        toProperties.put(ContentModel.PROP_ADDRESSEE, "ADDRESSEE");
        toProperties.remove(ContentModel.PROP_CREATED);
        toProperties.remove(ContentModel.PROP_CREATOR);
        toProperties.remove(ContentModel.PROP_CONTENT);
        toProperties.put(ContentModel.PROP_LOCATION, "LOCATION");
        
        nodeChange.onUpdateProperties(content1, fromProperties, toProperties);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "updateNodeProperties", "updateNodeProperties");
        
        assertEquals(1, ((Map<?,?>)auditMap.get("properties/from")).size());
        assertEquals("location", ((Map<?,?>)auditMap.get("properties/from")).get(ContentModel.PROP_LOCATION));
        assertEquals("location", auditMap.get("properties/from/cm:location"));

        assertEquals(1, ((Map<?,?>)auditMap.get("properties/to")).size());
        assertEquals("LOCATION", ((Map<?,?>)auditMap.get("properties/to")).get(ContentModel.PROP_LOCATION));
        assertEquals("LOCATION", auditMap.get("properties/to/cm:location"));

        assertEquals(2, ((Map<?,?>)auditMap.get("properties/add")).size());
        assertEquals("AUTHOR", ((Map<?,?>)auditMap.get("properties/add")).get(ContentModel.PROP_AUTHOR));
        assertEquals("ADDRESSEE", ((Map<?,?>)auditMap.get("properties/add")).get(ContentModel.PROP_ADDRESSEE));
        assertEquals("AUTHOR", auditMap.get("properties/add/cm:author"));
        assertEquals("ADDRESSEE", auditMap.get("properties/add/cm:addressee"));

        assertEquals(3, ((Map<?,?>)auditMap.get("properties/delete")).size());
        assertEquals("created", ((Map<?,?>)auditMap.get("properties/delete")).get(ContentModel.PROP_CREATED));
        assertEquals("creator", ((Map<?,?>)auditMap.get("properties/delete")).get(ContentModel.PROP_CREATOR));
        assertEquals("content", ((Map<?,?>)auditMap.get("properties/delete")).get(ContentModel.PROP_CONTENT));
        assertEquals("created", auditMap.get("properties/delete/cm:created"));
        assertEquals("creator", auditMap.get("properties/delete/cm:creator"));
        assertEquals("content", auditMap.get("properties/delete/cm:content"));
    }
    @Test
    public final void testReplaceInvalidPathChars()
    {
        Map<QName, Serializable> fromProperties = new HashMap<QName, Serializable>();
        Map<QName, Serializable> toProperties = new HashMap<QName, Serializable>();
        QName qName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "valid/&\u3001");
        fromProperties.put(qName, "/&\u3001");
        
        nodeChange.onUpdateProperties(content1, fromProperties, toProperties);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertEquals("/&\u3001", ((Map<?,?>)auditMap.get("properties/delete")).get(qName));
        assertEquals("/&\u3001", auditMap.get("properties/delete/cm:valid---"));
    }
    
    @Test
    public final void testOnAddAspect()
    {
        //   add           = add
        //   del add       = ---
        //   add del add   = add
        //   add add       = add
        
        nodeChange.onAddAspect(content1, ContentModel.ASPECT_ARCHIVED);

        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_COPIEDFROM);
        nodeChange.onAddAspect(content1, ContentModel.ASPECT_COPIEDFROM);

        nodeChange.onAddAspect(content1, ContentModel.ASPECT_EMAILED);
        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_EMAILED);
        nodeChange.onAddAspect(content1, ContentModel.ASPECT_EMAILED);

        nodeChange.onAddAspect(content1, ContentModel.ASPECT_GEOGRAPHIC);
        nodeChange.onAddAspect(content1, ContentModel.ASPECT_GEOGRAPHIC);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);
    
        assertStandardData(auditMap, "addNodeAspect", "addNodeAspect deleteNodeAspect");

        assertEquals(3, ((Set<?>)auditMap.get("aspects/add")).size());
        assertTrue("Grouped cm:archived aspect missing", ((Set<?>)auditMap.get("aspects/add")).contains(ContentModel.ASPECT_ARCHIVED));
        assertTrue("Grouped cm:emailed aspect missing", ((Set<?>)auditMap.get("aspects/add")).contains(ContentModel.ASPECT_EMAILED));
        assertTrue("Grouped cm:geographic aspect missing", ((Set<?>)auditMap.get("aspects/add")).contains(ContentModel.ASPECT_GEOGRAPHIC));
        assertTrue("Individual cm:archived aspect missing", auditMap.containsKey("aspects/add/cm:archived"));
        assertTrue("Individual cm:emailed aspect missing", auditMap.containsKey("aspects/add/cm:emailed"));
        assertTrue("Individual cm:geographic aspect missing", auditMap.containsKey("aspects/add/cm:geographic"));
    }

    @Test
    public final void testOnRemoveAspect()
    {
        //   del           = del
        //   add del       = ---
        //   del add del   = del
        //   del del       = del

        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_ARCHIVED);

        nodeChange.onAddAspect(content1, ContentModel.ASPECT_COPIEDFROM);
        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_COPIEDFROM);

        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_EMAILED);
        nodeChange.onAddAspect(content1, ContentModel.ASPECT_EMAILED);
        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_EMAILED);

        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_GEOGRAPHIC);
        nodeChange.onRemoveAspect(content1, ContentModel.ASPECT_GEOGRAPHIC);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);
    
        assertStandardData(auditMap, "deleteNodeAspect", "deleteNodeAspect addNodeAspect");

        assertEquals(3, ((Set<?>)auditMap.get("aspects/delete")).size());
        assertTrue("Grouped cm:archived aspect missing", ((Set<?>)auditMap.get("aspects/delete")).contains(ContentModel.ASPECT_ARCHIVED));
        assertTrue("Grouped cm:emailed aspect missing", ((Set<?>)auditMap.get("aspects/delete")).contains(ContentModel.ASPECT_EMAILED));
        assertTrue("Grouped cm:geographic aspect missing", ((Set<?>)auditMap.get("aspects/delete")).contains(ContentModel.ASPECT_GEOGRAPHIC));
        assertTrue("Individual cm:archived aspect missing", auditMap.containsKey("aspects/delete/cm:archived"));
        assertTrue("Individual cm:emailed aspect missing", auditMap.containsKey("aspects/delete/cm:emailed"));
        assertTrue("Individual cm:geographic aspect missing", auditMap.containsKey("aspects/delete/cm:geographic"));
    }

    @Test
    public final void testOnContentUpdateTrue()
    {
        nodeChange.onContentUpdate(content1, true);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "createContent", "createContent");
    }

    @Test
    public final void testOnContentUpdateFalse()
    {
        nodeChange.onContentUpdate(content1, false);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "UPDATE CONTENT", "updateContent");
    }

    @Test
    public final void testOnContentRead()
    {
        nodeChange.onContentRead(content1);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "READ", "readContent");
    }

    @Test
    public final void testOnCreateVersion()
    {
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "This is a test");
        nodeChange.onCreateVersion(null, content1, versionProperties, null);
        
        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "CREATE VERSION", "createVersion");
 
        assertEquals(1, ((Map<?,?>)auditMap.get("version-properties")).size());
        assertEquals("Grouped description version-properties missing", "This is a test", ((Map<?,?>)auditMap.get("version-properties")).get(Version.PROP_DESCRIPTION));
        assertEquals("Individual description version-properties missing", "This is a test", auditMap.get("version-properties/description"));
    }

    @Test
    public final void testOnCheckOut()
    {
        nodeChange.onCheckOut(content1);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "CHECK OUT", "checkOut");
    }

    @Test
    public final void testOnCheckIn()
    {
        nodeChange.onCheckIn(content1);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "CHECK IN", "checkIn");
    }

    @Test
    public final void testOnCancelCheckOut()
    {
        nodeChange.onCancelCheckOut(content1);

        Map<String, Serializable> auditMap = nodeChange.getAuditData(false);

        assertStandardData(auditMap, "CANCEL CHECK OUT", "cancelCheckOut");
    }
}
