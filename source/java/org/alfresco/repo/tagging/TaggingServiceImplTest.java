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
package org.alfresco.repo.tagging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tagging service implementation unit test
 * 
 * @author Roy Wetherall
 * @author Nick Burch
 */
public class TaggingServiceImplTest extends TestCase
{
   private static ConfigurableApplicationContext ctx = 
      (ConfigurableApplicationContext)ApplicationContextHelper.getApplicationContext();
   
    /** Services */
    private TaggingService taggingService;
    private NodeService nodeService;
    private CopyService copyService;
    private CheckOutCheckInService checkOutCheckInService;
    private ScriptService scriptService;
    private AuditService auditService;
    private ActionService actionService;
    private ActionTrackingService actionTrackingService;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private AsyncOccurs asyncOccurs;
    
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
    protected void setUp() throws Exception
    {
        // Detect any dangling transactions as there is a lot of direct UserTransaction manipulation
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new IllegalStateException(
                    "There should not be any transactions when starting test: " +
                    AlfrescoTransactionSupport.getTransactionId() + " started at " +
                    new Date(AlfrescoTransactionSupport.getTransactionStartTime()));
        }
        
        // Get services
        this.taggingService = (TaggingService)ctx.getBean("TaggingService");
        this.nodeService = (NodeService) ctx.getBean("NodeService");
        this.copyService = (CopyService) ctx.getBean("CopyService");
        this.checkOutCheckInService = (CheckOutCheckInService) ctx.getBean("CheckoutCheckinService");
        this.actionService = (ActionService)ctx.getBean("ActionService");
        this.transactionService = (TransactionService)ctx.getBean("transactionComponent");
        this.auditService = (AuditService)ctx.getBean("auditService");
        this.scriptService = (ScriptService)ctx.getBean("scriptService");        
        this.actionTrackingService = (ActionTrackingService)ctx.getBean("actionTrackingService");
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");

        if (init == false)
        {
            UserTransaction tx = this.transactionService.getNonPropagatingUserTransaction();
            tx.begin();
            
            // Authenticate as the system user
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
        
        // We want to know when tagging actions have finished running
        asyncOccurs = new AsyncOccurs();
        ((PolicyComponent)ctx.getBean("policyComponent")).bindClassBehaviour(
              AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute.QNAME,
              ActionModel.TYPE_ACTION,
              new JavaBehaviour(asyncOccurs, "onAsyncActionExecute", NotificationFrequency.EVERY_EVENT)
        );
    
        // Create the folders and documents to be tagged
        createTestDocumentsAndFolders();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        removeTestDocumentsAndFolders();
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            fail("Test is not transaction-safe.  Fix up transaction handling and re-test.");
        }
    }

    private void createTestDocumentsAndFolders() throws Exception
    {
        UserTransaction tx = this.transactionService.getNonPropagatingUserTransaction();
        tx.begin();
       
        // Authenticate as the system user
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
        
        tx.commit();
    }
    private void removeTestDocumentsAndFolders() throws Exception
    {
        UserTransaction tx = this.transactionService.getNonPropagatingUserTransaction();
        tx.begin();
       
        // Authenticate as the system user
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // If anything is a tag scope, stop it being
        NodeRef[] nodes = new NodeRef[] { subDocument, subFolder, document, folder };
        for(NodeRef nodeRef : nodes)
        {
           if(taggingService.isTagScope(nodeRef))
           {
              taggingService.removeTagScope(nodeRef);
           }
        }
        
        // Remove the sample nodes
        for(NodeRef nodeRef : nodes)
        {
           nodeService.deleteNode(nodeRef);
        }
        
        // Tidy up the audit component, now all the nodes have gone
        auditService.clearAudit(
              TaggingServiceImpl.TAGGING_AUDIT_APPLICATION_NAME, 
              0l, System.currentTimeMillis()+1
        );
        
        // All done
        tx.commit();
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
        assertFalse(taggingService.hasTag(document, TAG_1));
        
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
        this.taggingService.addTag(this.document, TAG_1);
        
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertTrue(tags.contains(TAG_1));
        assertTrue(taggingService.hasTag(document, TAG_1));
        
        assertFalse(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        this.taggingService.addTag(this.document, TAG_2);
        
        assertTrue(this.taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains(TAG_1));
        assertTrue(tags.contains(TAG_2));    
        assertTrue(taggingService.hasTag(document, TAG_1));
        assertTrue(taggingService.hasTag(document, TAG_2));
        
        this.taggingService.removeTag(this.document, TAG_1);
        tags = this.taggingService.getTags(this.document);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertFalse(tags.contains(TAG_1));
        assertFalse(taggingService.hasTag(document, TAG_1));
        assertTrue(tags.contains(TAG_2));
        assertTrue(taggingService.hasTag(document, TAG_2));
        
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
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
       
        // TODO add some tags before the scopes are added
        
        // Add some tag scopes
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTagScope(this.subFolder);

        // Add some more tags after the scopes have been added
        this.taggingService.addTag(this.subDocument, TAG_1); // folder+subfolder
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_2); // folder+subfolder
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_3); // folder+subfolder
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subFolder, TAG_1); // folder+subfolder
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subFolder, TAG_2); // folder+subfolder
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.folder, TAG_2); // folder only
        
        tx = asyncOccurs.awaitExecution(tx);
        
        // re get the tag scopes
        TagScope ts1 = this.taggingService.findTagScope(this.subDocument);
        TagScope ts2 = this.taggingService.findTagScope(this.folder);
        
        // Check that the tag scopes got populated
        assertEquals(
              "Wrong tags on sub folder: " + ts1.getTags(),
              3, ts1.getTags().size()
        );
        assertEquals(
              "Wrong tags on main folder: " + ts2.getTags(),
              3, ts2.getTags().size()
        );
        
        // check the order and count of the tagscopes
        assertEquals(2, ts1.getTags().get(0).getCount());
        assertEquals(2, ts1.getTags().get(1).getCount());
        assertEquals(1, ts1.getTags().get(2).getCount());
        assertEquals(TAG_1, ts1.getTags().get(0).getName());
        assertEquals(TAG_2, ts1.getTags().get(1).getName());
        assertEquals(TAG_3.toLowerCase(), ts1.getTags().get(2).getName());
        
        assertEquals(3, ts2.getTags().get(0).getCount());
        assertEquals(2, ts2.getTags().get(1).getCount());
        assertEquals(1, ts2.getTags().get(2).getCount());
        assertEquals(TAG_2, ts2.getTags().get(0).getName());
        assertEquals(TAG_1, ts2.getTags().get(1).getName());
        assertEquals(TAG_3.toLowerCase(), ts2.getTags().get(2).getName());
        
        
        // Take some off again
        this.taggingService.removeTag(this.folder, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.removeTag(this.subFolder, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.removeTag(this.subFolder, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.removeTag(this.subDocument, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        // And add one more
        this.taggingService.addTag(this.folder, TAG_3);
        
        tx = asyncOccurs.awaitExecution(tx);
        
        // re get the tag scopes
        ts1 = this.taggingService.findTagScope(this.subDocument);
        ts2 = this.taggingService.findTagScope(this.folder);
        
        // Recheck the tag scopes
        assertEquals(
              "Wrong tags on sub folder: " + ts1.getTags(),
              2, ts1.getTags().size()
        );
        assertEquals(
              "Wrong tags on main folder: " + ts2.getTags(),
              2, ts2.getTags().size()
        );
        
        // Sub-folder should be ordered by tag name, as all values 1
        assertEquals(1, ts1.getTags().get(0).getCount());
        assertEquals(1, ts1.getTags().get(1).getCount());
        assertEquals(TAG_2, ts1.getTags().get(0).getName());
        assertEquals(TAG_3.toLowerCase(), ts1.getTags().get(1).getName());
        
        // Folder should be still sorted by size, as a 2 & a 1
        assertEquals(2, ts2.getTags().get(0).getCount());
        assertEquals(1, ts2.getTags().get(1).getCount());
        assertEquals(TAG_3.toLowerCase(), ts2.getTags().get(0).getName());
        assertEquals(TAG_2, ts2.getTags().get(1).getName());
        
        // Finish
        tx.commit();
    }
    
    public void testTagScopeRefresh()
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
      
        // Add some tags to the nodes
        // tag scope on folder should be ....
        //  tag2 = 3
        //  tag1 = 2
        //  tag3 = 1
        this.taggingService.addTag(this.subDocument, TAG_1);
        this.taggingService.addTag(this.subDocument, TAG_2);   
        this.taggingService.addTag(this.subDocument, TAG_3);
        this.taggingService.addTag(this.subFolder, TAG_1);
        this.taggingService.addTag(this.subFolder, TAG_2);
        this.taggingService.addTag(this.folder, TAG_2);
        
        // Commit the changes. No action will fire, as there
        //  aren't currently any tag scopes to be updated!
        tx.commit();
        tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Add the tag scope 
        this.taggingService.addTagScope(this.folder);
        
        // The updates in this case are synchronous, not
        //  async, so we don't need to wait for any actions
        tx.commit();
        tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        // Get the tag scope and check that all the values have been set correctly
        TagScope tagScope = this.taggingService.findTagScope(this.folder);
        assertNotNull(tagScope);
        assertEquals(3, tagScope.getTags().size());
        assertEquals(3, tagScope.getTag(TAG_2).getCount());
        assertEquals(2, tagScope.getTag(TAG_1).getCount());
        assertEquals(1, tagScope.getTag(TAG_3.toLowerCase()).getCount());
        
        // Finish
        tx.commit();
    }
    
    public void testTagScopeSetUpdate()
        throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
       
        // Set up tag scope 
        this.taggingService.addTagScope(this.folder);;
        
        // Add some tags
        this.taggingService.addTag(this.folder, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.document, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.document, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_3);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subFolder, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);

        // Check that tag scope
        TagScope ts1 = this.taggingService.findTagScope(this.folder);
        assertEquals(
              "Wrong tags on folder tagscope: " + ts1.getTags(),
              3, ts1.getTags().size()
        );
        assertEquals(4, ts1.getTag(TAG_1).getCount());
        assertEquals(2, ts1.getTag(TAG_2).getCount());
        assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());
        
        // Re-set the tag scopes
        List<String> tags = new ArrayList<String>(3);
        tags.add(TAG_2);
        tags.add(TAG_3);
        tags.add(TAG_4);
        
        this.taggingService.setTags(this.subDocument, tags);
        tx = asyncOccurs.awaitExecution(tx);
        
        // Check that the tagscope has been updated correctly
        ts1 = this.taggingService.findTagScope(this.folder);
        assertEquals(3, ts1.getTag(TAG_1).getCount());
        assertEquals(2, ts1.getTag(TAG_2).getCount());
        assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());
        assertEquals(1, ts1.getTag(TAG_4).getCount());

        // Finish
        tx.commit();
    }
    
    /* 
     * https://issues.alfresco.com/jira/browse/ETHREEOH-220 
     */
    public void testETHREEOH_220() throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
      
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTag(this.folder, TAG_I18N);
        tx = asyncOccurs.awaitExecution(tx);
        
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
        
        // Finish
        tx.commit();
    }
    
    /**
     * Ensures that the tag scope is correctly updated
     *  when folders and content are created, updated,
     *  moved, copied and deleted.
     */
    public void testTagScopeUpdateViaNodePolicies() throws Exception {
       UserTransaction tx = this.transactionService.getUserTransaction();
       tx.begin();
     
       // The various tags we'll be using in testing
       NodeRef tagFoo1 = taggingService.createTag(folder.getStoreRef(), "Foo1");
       NodeRef tagFoo2 = taggingService.createTag(folder.getStoreRef(), "Foo2");
       NodeRef tagFoo3 = taggingService.createTag(folder.getStoreRef(), "Foo3");
       NodeRef tagBar = taggingService.createTag(folder.getStoreRef(), "Bar");
       
       List<NodeRef> tagsList = new ArrayList<NodeRef>();
       
       
       // Create two containers marked as tag scopes
       Map<QName, Serializable> container1Props = new HashMap<QName, Serializable>(1);
       container1Props.put(ContentModel.PROP_NAME, "Container1");
       NodeRef container1 = this.nodeService.createNode(
               folder, 
               ContentModel.ASSOC_CONTAINS, 
               ContentModel.ASSOC_CHILDREN,
               ContentModel.TYPE_FOLDER,
               container1Props).getChildRef();
       assertEquals(0, nodeService.getChildAssocs(container1).size());
       
       Map<QName, Serializable> container2Props = new HashMap<QName, Serializable>(1);
       container1Props.put(ContentModel.PROP_NAME, "Container2");
       NodeRef container2 = this.nodeService.createNode(
               folder, 
               ContentModel.ASSOC_CONTAINS, 
               ContentModel.ASSOC_CHILDREN,
               ContentModel.TYPE_FOLDER,
               container2Props).getChildRef();
       assertEquals(0, nodeService.getChildAssocs(container2).size());
       
       
       // Check that the tag scopes are empty
       taggingService.addTagScope(container1);
       taggingService.addTagScope(container2);
       tx = asyncOccurs.awaitExecution(tx);
       assertTrue( taggingService.isTagScope(container1) );
       assertTrue( taggingService.isTagScope(container2) );
       assertEquals(0, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       
       
       // Create a folder, without any tags on it
       Map<QName, Serializable> taggedFolderProps = new HashMap<QName, Serializable>(1);
       taggedFolderProps.put(ContentModel.PROP_NAME, "Folder");
       NodeRef taggedFolder = this.nodeService.createNode(
               container1, 
               ContentModel.ASSOC_CONTAINS, 
               ContentModel.ASSOC_CHILDREN,
               ContentModel.TYPE_FOLDER,
               taggedFolderProps).getChildRef();
       
       // No tags, so no changes should have occured, and no actions
       tx.commit();
       tx = transactionService.getUserTransaction();
       tx.begin();
       assertEquals(0, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       
       
       // Now update the folder to add in tags
       taggedFolderProps.clear();
       tagsList.clear();
       
       tagsList.add(tagFoo1);
       tagsList.add(tagFoo3);
       taggedFolderProps.put(ContentModel.ASPECT_TAGGABLE, (Serializable)tagsList);
       this.nodeService.addProperties(taggedFolder, taggedFolderProps);
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(
             "Unexpected tags " + taggingService.findTagScope(container1).getTags(),
             2, taggingService.findTagScope(container1).getTags().size()
       );
       assertEquals(
             "Unexpected tags " + taggingService.findTagScope(container2).getTags(),
             0, taggingService.findTagScope(container2).getTags().size()
       );
       
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       
       // Create a document within it, check that tag scope tags are updated
       tagsList.clear();
       tagsList.add(tagFoo1);
       tagsList.add(tagFoo2);
       
       Map<QName, Serializable> taggedDocProps = new HashMap<QName, Serializable>(1);
       taggedDocProps.put(ContentModel.PROP_NAME, "Document");
       taggedDocProps.put(ContentModel.ASPECT_TAGGABLE, (Serializable)tagsList);
       NodeRef taggedDoc = this.nodeService.createNode(
               taggedFolder, 
               ContentModel.ASSOC_CONTAINS, 
               ContentModel.ASPECT_TAGGABLE,
               ContentModel.TYPE_CONTENT,
               taggedDocProps).getChildRef();
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       
       assertEquals(2, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       
       // Check that the Document really is a child of the folder,
       //  otherwise later checks will fail for really odd reasons
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       assertEquals(1, nodeService.getChildAssocs(taggedFolder).size());
       
       
       // Check out the node
       // Tags should be doubled up. (We don't care about ContentModel.ASPECT_WORKING_COPY
       //  because it isn't applied at suitable times to take not of)
       NodeRef checkedOutDoc = checkOutCheckInService.checkout(taggedDoc);
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       
       assertEquals(3, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(2, taggingService.findTagScope(container1).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       assertEquals(2, nodeService.getChildAssocs(taggedFolder).size());
       
       
       // And check it back in again
       // Tags should go back to how they were
       checkOutCheckInService.checkin(checkedOutDoc, null);
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       
       assertEquals(2, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       assertEquals(1, nodeService.getChildAssocs(taggedFolder).size());

       
       // Do a node->node copy of the document onto the other container
       taggedDocProps.clear();
       taggedDocProps.put(ContentModel.PROP_NAME, "CopyDoc");
       assertEquals(0, nodeService.getChildAssocs(container2).size());
       NodeRef taggedDoc2 = this.nodeService.createNode(
             container2, 
             ContentModel.ASSOC_CONTAINS, 
             ContentModel.ASPECT_TAGGABLE,
             ContentModel.TYPE_CONTENT,
             taggedDocProps).getChildRef();
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       assertEquals(1, nodeService.getChildAssocs(container2).size());
       
       copyService.copy(taggedDoc, taggedDoc2);
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(2, taggingService.findTagScope(container2).getTags().size());
       
       assertEquals(2, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo2").getCount());
       
       // Check that things were fine after the copy
       assertEquals(1, nodeService.getChildAssocs(container2).size());
       assertEquals(container2, nodeService.getPrimaryParent(taggedDoc2).getParentRef());
       assertEquals(container2, taggingService.findTagScope(taggedDoc2).getNodeRef());
       
       
       // Copy the folder to another container
       // Does a proper, recursing copy
       NodeRef taggedFolder2 = copyService.copy(
             taggedFolder, container2,
             ContentModel.ASSOC_CONTAINS,
             ContentModel.ASSOC_CHILDREN,
             true);
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(3, taggingService.findTagScope(container2).getTags().size());
       assertEquals(2, nodeService.getChildAssocs(container2).size());
       assertEquals(1, nodeService.getChildAssocs(taggedFolder2).size());
       
       assertEquals(2, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(3, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(2, taggingService.findTagScope(container2).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo3").getCount());
       
       
       // Update the document on the original
       tagsList.clear();
       tagsList.add(tagBar);
       taggedDocProps.clear();
       taggedDocProps.put(ContentModel.ASPECT_TAGGABLE, (Serializable)tagsList);
       taggedDocProps.put(ContentModel.PROP_NAME, "UpdatedDocument");
       this.nodeService.addProperties(taggedDoc, taggedDocProps);
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(3, taggingService.findTagScope(container1).getTags().size());
       assertEquals(3, taggingService.findTagScope(container2).getTags().size());
       
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("bar").getCount());
       
       assertEquals(3, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(2, taggingService.findTagScope(container2).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo3").getCount());

       
       // Move the document to another container
       taggedDoc = nodeService.moveNode(taggedDoc, container2, 
             ContentModel.ASSOC_CONTAINS, 
             ContentModel.ASPECT_TAGGABLE).getChildRef();
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(2, taggingService.findTagScope(container1).getTags().size());
       assertEquals(4, taggingService.findTagScope(container2).getTags().size());
       
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(3, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(2, taggingService.findTagScope(container2).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo3").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("bar").getCount());

       
       // Check the state of the tree
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       assertEquals(3, nodeService.getChildAssocs(container2).size());

       assertEquals(container2, nodeService.getPrimaryParent(taggedDoc).getParentRef());
       assertEquals(container2, taggingService.findTagScope(taggedDoc).getNodeRef());
       assertEquals(container2, nodeService.getPrimaryParent(taggedDoc2).getParentRef());
       assertEquals(container2, taggingService.findTagScope(taggedDoc2).getNodeRef());
       
       
       // Delete the documents and folder one at a time
       nodeService.deleteNode(taggedDoc); // container 2, "bar"
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(2, taggingService.findTagScope(container1).getTags().size());
       assertEquals(3, taggingService.findTagScope(container2).getTags().size());
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       assertEquals(2, nodeService.getChildAssocs(container2).size());
       
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(3, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(2, taggingService.findTagScope(container2).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo3").getCount());
       assertEquals(null, taggingService.findTagScope(container2).getTag("bar"));

       
       nodeService.deleteNode(taggedDoc2); // container 2, "foo1", "foo2"
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(2, taggingService.findTagScope(container1).getTags().size());
       assertEquals(3, taggingService.findTagScope(container2).getTags().size());
       assertEquals(1, nodeService.getChildAssocs(container1).size());
       assertEquals(1, nodeService.getChildAssocs(container2).size());
       
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container1).getTag("foo3").getCount());
       
       assertEquals(2, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo3").getCount());
       assertEquals(null, taggingService.findTagScope(container2).getTag("bar"));
       
       
       nodeService.deleteNode(taggedFolder); // container 1, "foo1", "foo3"
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(0, taggingService.findTagScope(container1).getTags().size());
       assertEquals(3, taggingService.findTagScope(container2).getTags().size());
       assertEquals(0, nodeService.getChildAssocs(container1).size());
       assertEquals(1, nodeService.getChildAssocs(container2).size());
       
       assertEquals(2, taggingService.findTagScope(container2).getTag("foo1").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo2").getCount());
       assertEquals(1, taggingService.findTagScope(container2).getTag("foo3").getCount());
       
       
       nodeService.deleteNode(taggedFolder2); // container 2, has a child also
       
       tx = asyncOccurs.awaitExecution(tx);
       assertEquals(0, taggingService.findTagScope(container1).getTags().size());
       assertEquals(0, taggingService.findTagScope(container2).getTags().size());
       assertEquals(0, nodeService.getChildAssocs(container1).size());
       assertEquals(0, nodeService.getChildAssocs(container2).size());
       
       // Finish
       tx.commit();
    }
    
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
        
        Map<String, Object> model = new HashMap<String, Object>(0);
        model.put("folder", this.folder);
        model.put("subFolder", this.subFolder);
        model.put("document", this.document);
        model.put("subDocument", this.subDocument);
        model.put("tagScopeTest", false);
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/tagging/script/test_taggingService.js");
        this.scriptService.executeScript(location, model);
        
        // Let the script run
        tx = asyncOccurs.awaitExecution(tx);
        tx.commit();
    }
    
    public void testJSTagScope() throws Exception
    {        
        UserTransaction tx = this.transactionService.getUserTransaction();
        tx.begin();
      
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
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subDocument, TAG_3);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subFolder, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.subFolder, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.document, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.document, TAG_2);
        tx = asyncOccurs.awaitExecution(tx);
        this.taggingService.addTag(this.folder, TAG_1);
        tx = asyncOccurs.awaitExecution(tx);
        
        Map<String, Object> model = new HashMap<String, Object>(0);
        model.put("folder", this.folder);
        model.put("subFolder", this.subFolder);
        model.put("document", this.document);
        model.put("subDocument", this.subDocument);
        model.put("tagScopeTest", true);
        model.put("store", storeRef.toString());
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/tagging/script/test_taggingService.js");
        this.scriptService.executeScript(location, model);
        
        // Let the script run
        tx = asyncOccurs.awaitExecution(tx);
        tx.commit();
    }
    
    /**
     * Test that the scheduled task will do the right thing
     *  when it runs.
     */
    public void testOnStartupJob() throws Exception
    {
       UserTransaction tx = this.transactionService.getUserTransaction();
       tx.begin();
       
       // Nothing is pending to start with
       UpdateTagScopesActionExecuter updateTagsAction = (UpdateTagScopesActionExecuter)
          ctx.getBean("update-tagscope");
       assertEquals(0, updateTagsAction.searchForTagScopesPendingUpdates().size());
       
       
       // Take the tag scope lock, so that no real updates will happen
       String lockF = updateTagsAction.lockTagScope(this.folder);
       String lockSF = updateTagsAction.lockTagScope(this.subFolder);
       
       // Do some tagging
       this.taggingService.addTagScope(this.folder);
       this.taggingService.addTagScope(this.subFolder);
       
       this.taggingService.addTag(this.subDocument, TAG_1);
       this.taggingService.addTag(this.subDocument, TAG_2);
       this.taggingService.addTag(this.subFolder, TAG_1);
       this.taggingService.addTag(this.document, TAG_1);
       this.taggingService.addTag(this.folder, TAG_1);
       this.taggingService.addTag(this.folder, TAG_3);
       tx = asyncOccurs.awaitExecution(tx);
       
       
       // Tag scope updates shouldn't have happened yet,
       //  as the scopes are locked
       TagScope ts1 = this.taggingService.findTagScope(this.folder);
       TagScope ts2 = this.taggingService.findTagScope(this.subFolder);
       assertEquals(
             "Wrong tags on folder tagscope: " + ts1.getTags(),
             0, ts1.getTags().size()
       );
       assertEquals(
             "Wrong tags on folder tagscope: " + ts1.getTags(),
             0, ts2.getTags().size()
       );
       
       
       // Check the pending list now
       assertEquals(2, updateTagsAction.searchForTagScopesPendingUpdates().size());
       List<NodeRef> pendingScopes = updateTagsAction.searchForTagScopesPendingUpdates();
       assertTrue("Not found in " + pendingScopes, pendingScopes.contains(this.folder));
       assertTrue("Not found in " + pendingScopes, pendingScopes.contains(this.subFolder));
       
       
       // Have the Quartz bean fire now
       // It won't be able to do anything, as the locks are taken
       UpdateTagScopesQuartzJob job = new UpdateTagScopesQuartzJob();
       job.execute(actionService, updateTagsAction);
       tx = asyncOccurs.awaitExecution(tx);
       
       // Check that things are still pending despite the quartz run
       assertEquals(2, updateTagsAction.searchForTagScopesPendingUpdates().size());
       pendingScopes = updateTagsAction.searchForTagScopesPendingUpdates();
       assertTrue("Not found in " + pendingScopes, pendingScopes.contains(this.folder));
       assertTrue("Not found in " + pendingScopes, pendingScopes.contains(this.subFolder));
       
       
       // Give back our locks, so we can proceed
       updateTagsAction.unlockTagScope(this.folder, lockF);
       updateTagsAction.unlockTagScope(this.subFolder, lockSF);
       
       
       // Fire off the quartz bean, this time it can really work
       job = new UpdateTagScopesQuartzJob();
       job.execute(actionService, updateTagsAction);
       tx = asyncOccurs.awaitExecution(tx);
       
       
       // Now check again - nothing should be pending
       assertEquals(0, updateTagsAction.searchForTagScopesPendingUpdates().size());
       
       ts1 = this.taggingService.findTagScope(this.folder);
       ts2 = this.taggingService.findTagScope(this.subFolder);
       assertEquals(
             "Wrong tags on folder tagscope: " + ts1.getTags(),
             3, ts1.getTags().size()
       );
       assertEquals(
             "Wrong tags on folder tagscope: " + ts1.getTags(),
             2, ts2.getTags().size()
       );
       
       assertEquals(4, ts1.getTag(TAG_1).getCount());
       assertEquals(1, ts1.getTag(TAG_2).getCount());
       assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());
       
       assertEquals(2, ts2.getTag(TAG_1).getCount());
       assertEquals(1, ts2.getTag(TAG_2).getCount());
       
       // Force txn commit to prevent test leaks
       tx.commit();
    }
    
    /**
     * Test that when multiple threads do tag updates, the right thing still
     * happens
     */
    public void testMultiThreaded() throws Exception
    {
        UserTransaction tx = this.transactionService.getNonPropagatingUserTransaction();
        tx.begin();
        this.taggingService.addTagScope(this.folder);
        this.taggingService.addTagScope(this.subFolder);
        tx.commit();
        
        // Reset the action count
        asyncOccurs.wantedActionsCount = 0;

        // Prepare a bunch of threads to do tagging
        final List<Thread> threads = new ArrayList<Thread>();
        String[] tags = new String[] { TAG_1, TAG_2, TAG_3, TAG_4, TAG_5 };
        for (String tmpTag : tags)
        {
            final String tag = tmpTag;
            Thread t = new Thread(new Runnable()
            {
                @Override
                public synchronized void run()
                {
                    // Let everything catch up
                    try
                    {
                        wait();
                    }
                    catch (InterruptedException e)
                    {
                    }
                    System.out.println(Thread.currentThread() + " - About to start tagging for " + tag);

                    // Do the updates
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    transactionService.getRetryingTransactionHelper().doInTransaction(
                            new RetryingTransactionCallback<Void>()
                            {
                                public Void execute() throws Throwable
                                {
                                    taggingService.addTag(folder, tag);
                                    taggingService.addTag(subFolder, tag);
                                    taggingService.addTag(subDocument, tag);
                                    System.out.println(Thread.currentThread() + " - Tagging for " + tag);
                                    return null;
                                }
                            }, false, true
                    );
                    System.out.println(Thread.currentThread() + " - Done tagging for " + tag);
                    
                    // Wait briefly for thing to catch up, before we
                    //  declare ourselves to be done
                    try {
                       Thread.sleep(150);
                    } catch (InterruptedException e) {}
                }
            });
            threads.add(t);
            t.start();
        }

        // Release the threads
        System.out.println("Releasing tagging threads");
        for (Thread t : threads)
        {
            t.interrupt();
        }
        
        // Wait for the threads to finish (and they will finish)
        // The threads will generate further asynchronous actions
        for (Thread t : threads)
        {
            t.join();
        }
        System.out.println("All threads should have finished");
        
        // Have a brief pause, while we wait for their related
        //  async actions to kick off
        Thread.sleep(150);
        
        // Wait until we've had 5 async tagging actions run (One per Thread)
        // Not all of the actions will do something, but we just need to
        //  wait for all of them!
        // As a backup check, also ensure that the action tracking service
        //  shows none of them running either
        for (int i = 0; i < 600; i++)
        {
           try
           {
              if(asyncOccurs.wantedActionsCount < 5)
              {
                 Thread.sleep(100);
                 continue;
              }
              if (actionTrackingService.getAllExecutingActions().size() > 0)
              {
                 Thread.sleep(100);
                 continue;
              }
              break;
           }
           catch (InterruptedException e)
           {
           }
        }
        
        // Extra sleep just to be sure things are quiet before we continue
        // (Allows anything that runs after the async actions commit to
        //  finish up for example)
        Thread.sleep(175);
        System.out.println("Done waiting for tagging, now checking");
        
        // Now check that things ended up as planned
        tx = this.transactionService.getUserTransaction();
        tx.begin();

        TagScope ts1 = this.taggingService.findTagScope(this.folder);
        TagScope ts2 = this.taggingService.findTagScope(this.subFolder);
        assertEquals(
              "Wrong tags on folder tagscope: " + ts1.getTags(), 
              5, ts1.getTags().size()
        );
        assertEquals(
              "Wrong tags on subfolder tagscope: " + ts2.getTags(), 
              5, ts2.getTags().size()
        );

        // Each tag should crop up 3 times on the folder
        // and twice for the subfolder
        for (String tag : tags)
        {
            assertEquals(3, ts1.getTag(tag.toLowerCase()).getCount());
            assertEquals(2, ts2.getTag(tag.toLowerCase()).getCount());
        }

        // All done
        tx.commit();
    }
    
    
    public class AsyncOccurs implements OnAsyncActionExecute {
       private Object waitForExecutionLock = new Object();
       private static final long waitTime = 3500;
       private static final String ACTION_TYPE = UpdateTagScopesActionExecuter.NAME;
       
       private int wantedActionsCount = 0;
       
       @Override
       public void onAsyncActionExecute(Action action, NodeRef actionedUponNodeRef) 
       {
          if(action.getActionDefinitionName().equals(ACTION_TYPE))
          {
             wantedActionsCount++;
             synchronized (waitForExecutionLock) {
                try {
                   waitForExecutionLock.notify();
                } catch(IllegalMonitorStateException e) {}
             }
          }
          else
          {
             System.out.println("Ignoring unexpected async action:" + action);
          }
       }
       
       public UserTransaction awaitExecution(UserTransaction tx) throws Exception
       {
          synchronized (waitForExecutionLock) {
            // Have things begin working
            tx.commit();
            
            // Always wait 25ms
            waitForExecutionLock.wait(25);
            
            // If there are actions if the required type
            //  currently running, keep waiting longer for
            //  them to finish
            if(actionTrackingService.getExecutingActions(ACTION_TYPE).size() > 0)
            {
               long now = System.currentTimeMillis();
               waitForExecutionLock.wait(waitTime);
               
               if(System.currentTimeMillis() - now >= waitTime)
               {
                  System.err.println("Warning - trigger wasn't received");
               }
            }
          }
          
          // If there are any more actions of the same type,
          //  then wait for them to finish too
          for(int i=0; i<50; i++)
          {
             if( actionTrackingService.getExecutingActions(ACTION_TYPE).size() == 0 )
             {
                break;
             }
             try {
                Thread.sleep(10);
             } catch(InterruptedException e) {}
          }
          
          // Now create a new transaction for them
          tx = transactionService.getUserTransaction();
          tx.begin();
          return tx;
       }
    }
}
