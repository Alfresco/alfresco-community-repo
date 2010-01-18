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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * Tagging service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceImplTest extends BaseAlfrescoSpringTest
                                    implements AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute
{
    /** Services */
    private TaggingService taggingService;
    private ScriptService scriptService;
    private PolicyComponent policyComponent;
    
    private static StoreRef storeRef;
    private static NodeRef rootNode;
    private NodeRef folder;
    private NodeRef subFolder;
    private NodeRef document;
    private NodeRef subDocument;
    
    private static final String TAG_1 = "tag one";
    private static final String TAG_2 = "tag two";
    private static final String TAG_3 = "Tag Three";
    private static final String TAG_4 = "tag four";
    private static final String TAG_5 = "tag five";
    private static final String TAG_I18N = "àâæçéèêëîïôœùûüÿñ";
    
    private static final String UPPER_TAG = "House";
    private static final String LOWER_TAG = "house";
    
    private static boolean init = false;
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        super.onSetUpBeforeTransaction();
        
        // Get services
        this.taggingService = (TaggingService)this.applicationContext.getBean("TaggingService");
        this.nodeService = (NodeService) this.applicationContext.getBean("NodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("ContentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService)this.applicationContext.getBean("ActionService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.scriptService = (ScriptService)this.applicationContext.getBean("scriptService");        
        this.policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");       

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
            
            // Register the policy callback
            this.policyComponent.bindClassBehaviour(
                    AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute.QNAME, 
                    this, 
                    new JavaBehaviour(this, "onAsyncActionExecute", Behaviour.NotificationFrequency.EVERY_EVENT));
            
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
        
        String guid = GUID.generate();
        
        // Create a folder
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "testFolder" + guid);
        folder = this.nodeService.createNode(
                TaggingServiceImplTest.rootNode, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder" + guid),
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();
        
        // Create a node
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        docProps.put(ContentModel.PROP_NAME, "testDocument" + guid + ".txt");
        document = this.nodeService.createNode(
                folder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testDocument" + guid + ".txt"), 
                ContentModel.TYPE_CONTENT,
                docProps).getChildRef();    
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "subFolder" + guid);
        subFolder = this.nodeService.createNode(
                folder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subFolder" + guid), 
                ContentModel.TYPE_FOLDER,
                props).getChildRef();
        
        props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "subDocument" + guid + ".txt");
        subDocument = this.nodeService.createNode(
                subFolder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subDocument" + guid + ".txt"), 
                ContentModel.TYPE_CONTENT,
                props).getChildRef();
        
        //tx.commit();
        setComplete();
        endTransaction();
    }
    
    public void testTagCRUD()
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Get the tags
        List<String> tags = this.taggingService.getTags(TaggingServiceImplTest.storeRef);
        assertNotNull(tags);
        assertEquals(0, tags.size());
        
        // Create a tag
        this.taggingService.createTag(TaggingServiceImplTest.storeRef, TAG_1);
        this.taggingService.createTag(TaggingServiceImplTest.storeRef, UPPER_TAG);
        
        //setComplete();
        //endTransaction();
        tx.commit();
        
        tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Get all the tags
        tags = this.taggingService.getTags(TaggingServiceImplTest.storeRef);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains(TAG_1));
        assertTrue(tags.contains(LOWER_TAG));
        
        // Check isTag method
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, UPPER_TAG));
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, LOWER_TAG));
        
        // Remove a tag
        this.taggingService.deleteTag(TaggingServiceImplTest.storeRef, UPPER_TAG);
        
        tx.commit();   
        tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Get all the tags
        tags = this.taggingService.getTags(TaggingServiceImplTest.storeRef);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertTrue(tags.contains(TAG_1));
        assertFalse(tags.contains(LOWER_TAG));
        
        // Check isTag method
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, UPPER_TAG));
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, LOWER_TAG));
        
        tx.commit();
    }
    
    public void testAddRemoveTag()
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
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
        
        List<String> setTags = new ArrayList<String>(2);
        setTags.add(TAG_3);
        setTags.add(TAG_1);
        this.taggingService.setTags(this.document, setTags);
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains(TAG_1));
        assertFalse(tags.contains(TAG_2));
        assertTrue(tags.contains(TAG_3.toLowerCase()));
        
        this.taggingService.clearTags(this.document);
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        
        tx.commit();
    }
    
    public void testTagScopeFindAddRemove()
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
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
        
        tx.commit();
    }
    
    public void testTagScope()
        throws Exception
    {
        // TODO add some tags before the scopes are added
        
        // Add some tag scopes
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTagScope(this.subFolder);

        // Add some more tags after the scopes have been added
        this.taggingService.addTag(this.subDocument, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_3);
        waitForActionExecution();
        this.taggingService.addTag(this.subFolder, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subFolder, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.folder, TAG_2);
        waitForActionExecution();
        
        // re get the tag scopes
        TagScope ts1 = this.taggingService.findTagScope(this.subDocument);
        TagScope ts2 = this.taggingService.findTagScope(this.folder);
        
        // check the order and count of the tagscopes
        assertEquals(2, ts1.getTags().get(0).getCount());
        assertEquals(2, ts1.getTags().get(1).getCount());
        assertEquals(1, ts1.getTags().get(2).getCount());
        assertEquals(3, ts2.getTags().get(0).getCount());
        assertEquals(TAG_2, ts2.getTags().get(0).getName());
        assertEquals(2, ts2.getTags().get(1).getCount());
        assertEquals(TAG_1, ts2.getTags().get(1).getName());
        assertEquals(1, ts2.getTags().get(2).getCount());
        assertEquals(TAG_3.toLowerCase(), ts2.getTags().get(2).getName());
        
        this.taggingService.removeTag(this.folder, TAG_2);
        waitForActionExecution();
        this.taggingService.removeTag(this.subFolder, TAG_2);
        waitForActionExecution();
        this.taggingService.removeTag(this.subFolder, TAG_1);
        waitForActionExecution();
        this.taggingService.removeTag(this.subDocument, TAG_1);
        waitForActionExecution();
        
        // re get the tag scopes
        ts1 = this.taggingService.findTagScope(this.subDocument);
        ts2 = this.taggingService.findTagScope(this.folder);
        
        // Recheck the tag scopes
        assertEquals(2, ts1.getTags().size());
        assertEquals(2, ts2.getTags().size());     
    }
    
    public void testTagScopeRefresh()
        throws Exception
    {
        // Add some tags to the nodes
        // tag scope on folder should be ....
        //  tag2 = 3
        //  tag1 = 2
        //  tag3 = 1
        this.taggingService.addTag(this.subDocument, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_2);   
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_3);
        waitForActionExecution();
        this.taggingService.addTag(this.subFolder, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subFolder, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.folder, TAG_2);
        waitForActionExecution();
        
        // Add the tag scope 
        this.taggingService.addTagScope(this.folder);
        
        // Get the tag scope and check that all the values have been set correctly
        TagScope tagScope = this.taggingService.findTagScope(this.folder);
        assertNotNull(tagScope);
        assertEquals(3, tagScope.getTags().size());
        assertEquals(3, tagScope.getTag(TAG_2).getCount());
        assertEquals(2, tagScope.getTag(TAG_1).getCount());
        assertEquals(1, tagScope.getTag(TAG_3.toLowerCase()).getCount());
    }
    
    public void testTagScopeSetUpdate()
        throws Exception
    {
        // Set up tag scope 
        this.taggingService.addTagScope(this.folder);;
        
        // Add some tags
        this.taggingService.addTag(this.folder, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.document, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.document, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_3);
        waitForActionExecution();
        this.taggingService.addTag(this.subFolder, TAG_1);
        waitForActionExecution();

        // Check that tag scope
        TagScope ts1 = this.taggingService.findTagScope(this.folder);
        assertEquals(4, ts1.getTag(TAG_1).getCount());
        assertEquals(2, ts1.getTag(TAG_2).getCount());
        assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());
        
        // Re-set the tag scopes
        List<String> tags = new ArrayList<String>(3);
        tags.add(TAG_2);
        tags.add(TAG_3);
        tags.add(TAG_4);
        this.taggingService.setTags(this.subDocument, tags);
        waitForActionExecution();
        
        // Check that the tagscope has been updated correctly
        ts1 = this.taggingService.findTagScope(this.folder);
        assertEquals(3, ts1.getTag(TAG_1).getCount());
        assertEquals(2, ts1.getTag(TAG_2).getCount());
        assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());
        assertEquals(1, ts1.getTag(TAG_4).getCount());               
    }
    
    /* 
     * https://issues.alfresco.com/jira/browse/ETHREEOH-220 
     */
    public void testETHREEOH_220() throws Exception
    {
        this.taggingService.addTagScope(this.folder);;
        this.taggingService.addTag(this.folder, TAG_I18N);
        waitForActionExecution();
        
        // Get the tag from the node
        List<String> tags = this.taggingService.getTags(this.folder);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals(TAG_I18N, tags.get(0));
        
        // Get the tag from the tagscope
        TagScope tagScope = this.taggingService.findTagScope(this.folder);
        assertNotNull(tagScope);
        assertEquals(1, tagScope.getTags().size());
        TagDetails tagDetails = tagScope.getTag(TAG_I18N);
        assertNotNull(tagDetails);
        assertEquals(TAG_I18N, tagDetails.getName());
        assertEquals(1, tagDetails.getCount());
    }
    
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        Map model = new HashMap<String, Object>(0);
        model.put("folder", this.folder);
        model.put("subFolder", this.subFolder);
        model.put("document", this.document);
        model.put("subDocument", this.subDocument);
        model.put("tagScopeTest", false);
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/tagging/script/test_taggingService.js");
        this.scriptService.executeScript(location, model);
        
        tx.commit();
    }
    
    public void testJSTagScope() throws Exception
    {        
        // Add a load of tags to test the global tag methods with
        this.taggingService.createTag(storeRef, "alpha");
        this.taggingService.createTag(storeRef, "alpha double");
        this.taggingService.createTag(storeRef, "beta");
        this.taggingService.createTag(storeRef, "gamma");
        this.taggingService.createTag(storeRef, "delta");
        
        // Add a load of tags and tag scopes to the object and commit before executing the script
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTagScope(this.subFolder);
        
        this.taggingService.addTag(this.subDocument, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subDocument, TAG_2);
        waitForActionExecution(); 
        this.taggingService.addTag(this.subDocument, TAG_3);
        waitForActionExecution();   
        this.taggingService.addTag(this.subFolder, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.subFolder, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.document, TAG_1);
        waitForActionExecution();
        this.taggingService.addTag(this.document, TAG_2);
        waitForActionExecution();
        this.taggingService.addTag(this.folder, TAG_1);
        waitForActionExecution();
        
        Map model = new HashMap<String, Object>(0);
        model.put("folder", this.folder);
        model.put("subFolder", this.subFolder);
        model.put("document", this.document);
        model.put("subDocument", this.subDocument);
        model.put("tagScopeTest", true);
        model.put("store", storeRef.toString());
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/tagging/script/test_taggingService.js");
        this.scriptService.executeScript(location, model);
    }
    
    private static Object mutex = new Object();
    
    private void waitForActionExecution()
        throws Exception
    {
        synchronized (mutex)
        {
            // Wait for a maximum of 10 seconds
            mutex.wait(10000);         
        }
    }

    public void onAsyncActionExecute(Action action, NodeRef actionedUponNodeRef)
    {
        synchronized (mutex)
        {
            // Notify the waiting thread
            mutex.notifyAll();
        }
    }
}
