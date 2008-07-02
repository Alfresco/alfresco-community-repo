/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.tagging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * Tagging service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceImplTest extends BaseAlfrescoSpringTest
{
    /** Services */
    private TaggingService taggingService;
    
    private static StoreRef storeRef;
    private static NodeRef rootNode;
    private NodeRef folder;
    private NodeRef subFolder;
    private NodeRef document;
    private NodeRef subDocument;
    
    private static final String TAG_1 = "tagOne";
    private static final String TAG_2 = "tagTwo";
    private static final String TAG_3 = "tagThree";
    
    private static boolean init = false;
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        super.onSetUpBeforeTransaction();
        
        // Get services
        this.taggingService = (TaggingService)this.applicationContext.getBean("TaggingService");
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (AuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");

        if (init == false)
        {
            UserTransaction tx = this.transactionService.getUserTransaction();
            tx.begin();
            
            // Authenticate as the system user
            AuthenticationComponent authenticationComponent = (AuthenticationComponent) this.applicationContext
                    .getBean("authenticationComponent");
            authenticationComponent.setSystemUserAsCurrentUser();
            
            // Create the store and get the root node
            TaggingServiceImplTest.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
            TaggingServiceImplTest.rootNode = this.nodeService.getRootNode(TaggingServiceImplTest.storeRef);
        
            // Create the required tagging category
            NodeRef catContainer = nodeService.createNode(TaggingServiceImplTest.rootNode, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categoryContainer"), ContentModel.TYPE_CONTAINER).getChildRef();        
            NodeRef catRoot = nodeService.createNode(
                    catContainer, 
                    ContentModel.ASSOC_CHILDREN, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categoryRoot"), 
                    ContentModel.TYPE_CATEGORYROOT).getChildRef();
            nodeService.createNode(
                   catRoot, 
                   ContentModel.ASSOC_CATEGORIES, 
                   ContentModel.ASPECT_TAGGABLE, 
                   ContentModel.TYPE_CATEGORY).getChildRef();
            
            init = true;
            
            tx.commit();            
        }
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        // Authenticate as the system user
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) this.applicationContext
                .getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder");
        folder = this.nodeService.createNode(
                TaggingServiceImplTest.rootNode, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();
        
        // Create a node
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        docProps.put(ContentModel.PROP_NAME, "testDocument.txt");
        document = this.nodeService.createNode(
                folder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testDocument.txt"), 
                ContentModel.TYPE_CONTENT,
                docProps).getChildRef();    
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "subFolder");
        subFolder = this.nodeService.createNode(
                folder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subFolder"), 
                ContentModel.TYPE_FOLDER,
                props).getChildRef();
        
        props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "subDocument.txt");
        subDocument = this.nodeService.createNode(
                subFolder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subDocument.txt"), 
                ContentModel.TYPE_CONTENT,
                props).getChildRef();
    }
    
    public void testTagCRUD()
        throws Exception
    {
        // Get the tags
        List<String> tags = this.taggingService.getTags(TaggingServiceImplTest.storeRef);
        assertNotNull(tags);
        assertEquals(0, tags.size());
        
        // Create a tag
        this.taggingService.createTag(TaggingServiceImplTest.storeRef, TAG_1);
        
        setComplete();
        endTransaction();
        
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Get all the tags
        tags = this.taggingService.getTags(TaggingServiceImplTest.storeRef);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertTrue(tags.contains(TAG_1));
                
        // Check isTag method
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
        
        tx.commit();               
    }
    
    public void testAddRemoveTag()
        throws Exception
    {
        List<String> tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
        this.taggingService.addTag(this.document, TAG_1);
        
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertTrue(tags.contains(TAG_1));
        
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        this.taggingService.addTag(this.document, TAG_2);
        
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains(TAG_1));
        assertTrue(tags.contains(TAG_2));
        
        this.taggingService.removeTag(this.document, TAG_1);
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertFalse(tags.contains(TAG_1));
        assertTrue(tags.contains(TAG_2));       
    }
    
    public void testTagScopeFindAddRemove()
    {
        // Get scopes for node without
        TagScope tagScope = this.taggingService.findTagScope(this.subDocument);
        assertNull(tagScope);
        List<TagScope> tagScopes = this.taggingService.findAllTagScopes(this.subDocument);
        assertNotNull(tagScopes);
        assertEquals(0, tagScopes.size());
        
        // Add scopes 
        // TODO should the add return the created scope ??
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTagScope(this.subFolder);
        
        // Get the scopes
        tagScope = this.taggingService.findTagScope(this.subDocument);
        assertNotNull(tagScope);
        tagScopes = this.taggingService.findAllTagScopes(this.subDocument);
        assertNotNull(tagScopes);
        assertEquals(2, tagScopes.size());
        tagScope = this.taggingService.findTagScope(this.subFolder);
        assertNotNull(tagScope);
        tagScopes = this.taggingService.findAllTagScopes(this.subFolder);
        assertNotNull(tagScopes);
        assertEquals(2, tagScopes.size());
        tagScope = this.taggingService.findTagScope(this.folder);
        assertNotNull(tagScope);
        tagScopes = this.taggingService.findAllTagScopes(this.folder);
        assertNotNull(tagScopes);
        assertEquals(1, tagScopes.size());
        
        // Remove a scope
        this.taggingService.removeTagScope(this.folder);
        tagScope = this.taggingService.findTagScope(this.subDocument);
        assertNotNull(tagScope);
        tagScopes = this.taggingService.findAllTagScopes(this.subDocument);
        assertNotNull(tagScopes);
        assertEquals(1, tagScopes.size());
        tagScope = this.taggingService.findTagScope(this.subFolder);
        assertNotNull(tagScope);
        tagScopes = this.taggingService.findAllTagScopes(this.subFolder);
        assertNotNull(tagScopes);
        assertEquals(1, tagScopes.size());
        tagScope = this.taggingService.findTagScope(this.folder);
        assertNull(tagScope);
        tagScopes = this.taggingService.findAllTagScopes(this.folder);
        assertNotNull(tagScopes);
        assertEquals(0, tagScopes.size());
    }
    
    public void testTagScope()
        throws Exception
    {
        // TODO add some tags before the scopes are added
        
        // Add some tag scopes
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTagScope(this.subFolder);
        
        // Get the tag scope 
        TagScope ts1 = this.taggingService.findTagScope(this.subDocument);
        TagScope ts2 = this.taggingService.findTagScope(this.folder);
   
        setComplete();
        endTransaction();   
        
        addTag(this.subDocument, TAG_1, 1, ts1.getNodeRef());
        addTag(this.subDocument, TAG_2, 1, ts1.getNodeRef());   
        addTag(this.subDocument, TAG_3, 1, ts1.getNodeRef());   
        addTag(this.subFolder, TAG_1, 2, ts1.getNodeRef());
        addTag(this.subFolder, TAG_2, 2, ts1.getNodeRef()); 
        addTag(this.folder, TAG_2, 3, ts2.getNodeRef());
        
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // re get the tag scopes
        ts1 = this.taggingService.findTagScope(this.subDocument);
        ts2 = this.taggingService.findTagScope(this.folder);
        
        // check the order and count of the tagscopes
        assertEquals(2, ts1.getTags().get(0).getTagCount());
        assertEquals(2, ts1.getTags().get(1).getTagCount());
        assertEquals(1, ts1.getTags().get(2).getTagCount());
        assertEquals(3, ts2.getTags().get(0).getTagCount());
        assertEquals(TAG_2, ts2.getTags().get(0).getTagName());
        assertEquals(2, ts2.getTags().get(1).getTagCount());
        assertEquals(TAG_1, ts2.getTags().get(1).getTagName());
        assertEquals(1, ts2.getTags().get(2).getTagCount());
        assertEquals(TAG_3, ts2.getTags().get(2).getTagName());
        
        tx.commit();
        
        removeTag(this.folder, TAG_2, 2, ts2.getNodeRef());
        removeTag(this.subFolder, TAG_2, 1, ts1.getNodeRef());
        removeTag(this.subFolder, TAG_1, 1, ts1.getNodeRef());
        removeTag(this.subDocument, TAG_1, 0, ts1.getNodeRef());

        tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // re get the tag scopes
        ts1 = this.taggingService.findTagScope(this.subDocument);
        ts2 = this.taggingService.findTagScope(this.folder);
        
        assertEquals(2, ts1.getTags().size());
        assertEquals(2, ts2.getTags().size());
        
        tx.commit();
        
    }
    
    private void addTag(NodeRef nodeRef, String tag, int tagCount, NodeRef tagScopeNodeRef)
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Add some tags
        this.taggingService.addTag(nodeRef, tag);        
        
        tx.commit();
        
        // Wait a bit cos we want the background threads to kick in and update the tag scope
        int count = 0;
        boolean bCreated = false;
        while (true) 
        {        
            UserTransaction  tx2 = this.transactionService.getUserTransaction();
            tx2.begin();
            
            try
            {
                // Get the tag scope
                List<TagScope> tagScopes = this.taggingService.findAllTagScopes(nodeRef);
                TagScope checkTagScope = null;
                for (TagScope tagScope : tagScopes)
                {
                    if (tagScope.getNodeRef().equals(tagScopeNodeRef) == true)
                    {
                        checkTagScope = tagScope;
                        break;
                    }
                }    
                assertNotNull(checkTagScope);
                
                // Check that tag scopes are in the correct order
                List<TagDetails> tagDetailsList = checkTagScope.getTags();
                for (TagDetails tagDetails : tagDetailsList)
                {
                    if (tagDetails.getTagName().equals(tag) == true &&
                        tagDetails.getTagCount() == tagCount)
                    {
                        bCreated = true;
                        break;
                    }
                }
                
                if (bCreated == true)
                {
                    break;
                }
                
                // Wait to give the threads a chance to execute
                Thread.sleep(1000);
                
                if (count == 10)
                {
                    fail("The background task to update the tag scope failed");
                }
                count ++;
            }
            finally
            {            
                tx2.commit();
            }
        } 
    }
    
    private void removeTag(NodeRef nodeRef, String tag, int tagCount, NodeRef tagScopeNodeRef)
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Add some tags
        this.taggingService.removeTag(nodeRef, tag);        
        
        tx.commit();
        
        // Wait a bit cos we want the background threads to kick in and update the tag scope
        int count = 0;
        boolean bRemoved = false;
        boolean bMissing = (tagCount == 0);
        while (true) 
        {        
            UserTransaction  tx2 = this.transactionService.getUserTransaction();
            tx2.begin();
            
            try
            {
                // Get the tag scope
                List<TagScope> tagScopes = this.taggingService.findAllTagScopes(nodeRef);
                TagScope checkTagScope = null;
                for (TagScope tagScope : tagScopes)
                {
                    if (tagScope.getNodeRef().equals(tagScopeNodeRef) == true)
                    {
                        checkTagScope = tagScope;
                        break;
                    }
                }    
                assertNotNull(checkTagScope);
                
                // Check that tag scopes are in the correct order
                boolean bFound = false;
                List<TagDetails> tagDetailsList = checkTagScope.getTags();
                for (TagDetails tagDetails : tagDetailsList)
                {
                    if (tagDetails.getTagName().equals(tag) == true )
                    {
                        if (tagDetails.getTagCount() == tagCount)
                        {
                            bRemoved = true;                            
                        }
                        
                        bFound = true;
                        break;
                    }
                }
                
                if (bRemoved == true)
                {
                    break;
                }
                else if (bMissing == true && bFound == false)
                {
                    break;
                }
                
                // Wait to give the threads a chance to execute
                Thread.sleep(1000);
                
                if (count == 10)
                {
                    fail("The background task to update the tag scope failed");
                }
                count ++;
            }
            finally
            {            
                tx2.commit();
            }
        } 
    }
}
