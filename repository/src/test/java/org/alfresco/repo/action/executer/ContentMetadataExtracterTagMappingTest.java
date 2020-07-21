/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.metadata.TikaPoweredMetadataExtracter;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tagging.TaggingServiceImplTest;
import org.alfresco.repo.tagging.TaggingServiceImplTest.AsyncOccurs;
import org.alfresco.repo.tagging.UpdateTagScopesActionExecuter;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.junit.experimental.categories.Category;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.common.collect.Sets;

/**
 * Test of the ActionExecuter for extracting metadata, specifically for
 * the mapping to cm:taggable tags which requires different transaction
 * mechanisms than the existing {@link ContentMetadataExtracterTest}.
 * 
 * @author Roy Wetherall
 * @author Nick Burch
 * @author Ray Gauss II
 */
@Category({OwnJVMTestsCategory.class, LuceneTests.class, RedundantTests.class})
public class ContentMetadataExtracterTagMappingTest extends TestCase
{
   private static ConfigurableApplicationContext ctx = 
      (ConfigurableApplicationContext)ApplicationContextHelper.getApplicationContext();
   
   protected static final String TAGGING_AUDIT_APPLICATION_NAME = "Alfresco Tagging Service";

   protected static final String QUICK_FILENAME = "quickIPTC.jpg"; // Keywords separated with comma (,)
   protected static final String QUICK_FILENAME2 = "quickIPTC2.jpg"; // Keywords separated with pipe (|)
    protected static final String QUICK_FILENAME3 = "quickIPTC3.jpg"; // Keywords separated with semi-colon (;)

   protected static final String QUICK_KEYWORD = "fox";
   protected static final String TAG_1 = "tag one";
   protected static final String TAG_2 = "tag two";
   protected static final String TAG_3 = "Tag Three";
   protected static final String TAG_NONEXISTENT_NODEREF = "workspace://SpacesStore/cb725c1f-4f7a-4232-8870-6c95b65407e1";
   
    /** Services */
    private TaggingService taggingService;
    private NodeService nodeService;
    private ContentService contentService;
    private AuditService auditService;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private AsyncOccurs asyncOccurs;
    
    private static StoreRef storeRef;
    private static NodeRef rootNode;
    
    private ContentMetadataExtracter executer;
    private TagMappingMetadataExtracter extractor;
    
    private static boolean init = false;
    
    private final static String ID = GUID.generate();
    
    
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
        this.contentService = (ContentService) ctx.getBean("ContentService");
        
        this.transactionService = (TransactionService)ctx.getBean("transactionComponent");
        this.auditService = (AuditService)ctx.getBean("auditService");
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        
        this.executer = (ContentMetadataExtracter) ctx.getBean("extract-metadata");
        executer.setEnableStringTagging(true);
        executer.setTaggingService(taggingService);
        
        if (init == false)
        {
            this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

                @Override
                public Void execute() throws Throwable
                {
                    // Authenticate as the system user
                    authenticationComponent.setSystemUserAsCurrentUser();
                    
                    // Create the store and get the root node
                    ContentMetadataExtracterTagMappingTest.storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
                    ContentMetadataExtracterTagMappingTest.rootNode = nodeService.getRootNode(ContentMetadataExtracterTagMappingTest.storeRef);
                
                    // Create the required tagging category
                    NodeRef catContainer = nodeService.createNode(ContentMetadataExtracterTagMappingTest.rootNode, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categoryContainer"), ContentModel.TYPE_CONTAINER).getChildRef();        
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
                    
                    MetadataExtracterRegistry registry = (MetadataExtracterRegistry) ctx.getBean("metadataExtracterRegistry");
                    extractor = new TagMappingMetadataExtracter();
                    extractor.setRegistry(registry);
                    extractor.register();
                    
                    init = true;
                    return null;
                }});
            
        }
        
        // We want to know when tagging actions have finished running
        asyncOccurs = (new TaggingServiceImplTest()).new AsyncOccurs();
        ((PolicyComponent)ctx.getBean("policyComponent")).bindClassBehaviour(
              AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute.QNAME,
              ActionModel.TYPE_ACTION,
              new JavaBehaviour(asyncOccurs, "onAsyncActionExecute", NotificationFrequency.EVERY_EVENT)
        );
    
        // We do want action tracking whenever the tag scope updater runs
        UpdateTagScopesActionExecuter updateTagsAction = 
            (UpdateTagScopesActionExecuter)ctx.getBean("update-tagscope");
        updateTagsAction.setTrackStatus(true);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            fail("Test is not transaction-safe.  Fix up transaction handling and re-test.");
        }
    }

    private NodeRef[] createTestFolderAndDocument(String filename) throws Exception
    {
        return this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef[]>(){

            @Override
            public NodeRef[] execute() throws Throwable
            {
               
                // Authenticate as the system user
                authenticationComponent.setSystemUserAsCurrentUser();
                
                String guid = GUID.generate();
                
                // Create a folder
                Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
                folderProps.put(ContentModel.PROP_NAME, "testFolder" + guid);
                NodeRef folder = nodeService.createNode(
                        ContentMetadataExtracterTagMappingTest.rootNode, 
                        ContentModel.ASSOC_CHILDREN, 
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder" + guid),
                        ContentModel.TYPE_FOLDER,
                        folderProps).getChildRef();
                
                // Create a node
                Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
                docProps.put(ContentModel.PROP_NAME, "testDocument" + guid + ".jpg");
                NodeRef document = nodeService.createNode(
                        folder, 
                        ContentModel.ASSOC_CONTAINS, 
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testDocument" + guid + ".jpg"), 
                        ContentModel.TYPE_CONTENT,
                        docProps).getChildRef();
                
                try
                {
                    ContentWriter cw = contentService.getWriter(document, ContentModel.PROP_CONTENT, true);
                    cw.setMimetype(MimetypeMap.MIMETYPE_IMAGE_JPEG);
                    cw.putContent(AbstractContentTransformerTest.loadNamedQuickTestFile(filename));
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }

                return new NodeRef[] { document, folder };
            }
        });
    }

    private void removeTestFolderAndDocument(NodeRef[] nodes) throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){
            @Override
            public Void execute() throws Throwable
            {
                // Authenticate as the system user
                authenticationComponent.setSystemUserAsCurrentUser();
                
                // If anything is a tag scope, stop it being
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
                      TAGGING_AUDIT_APPLICATION_NAME, 
                      0l, System.currentTimeMillis()+1
                );
                return null;
            }
        });
    }

    private static class TagMappingMetadataExtracter extends TikaPoweredMetadataExtracter
    {
        
        private String existingTagNodeRef;
        
        public TagMappingMetadataExtracter()
        {
            super(Sets.newHashSet(MimetypeMap.MIMETYPE_IMAGE_JPEG));
            Properties mappingProperties = new Properties();
            // TODO move to new keyword once tika is upgraded
            mappingProperties.put(Metadata.KEYWORDS, ContentModel.PROP_TAGS.toString());
            mappingProperties.put(Metadata.DESCRIPTION, ContentModel.PROP_DESCRIPTION.toString());
            setMappingProperties(mappingProperties);
        }
        
        public void setExistingTagNodeRef(String existingTagNodeRef)
        {
            this.existingTagNodeRef = existingTagNodeRef;
        }
        
        @Override
        protected Map<String, Set<QName>> getDefaultMapping()
        {
            // No need to give anything back as we have explicitly set the mapping already
            return new HashMap<String, Set<QName>>(0);
        }
        @Override
        public boolean isSupported(String sourceMimetype)
        {
            return sourceMimetype.equals(MimetypeMap.MIMETYPE_IMAGE_JPEG);
        }
        
        @Override
        protected Parser getParser()
        {
            return new JpegParser();
        }
        
        @SuppressWarnings("unchecked")
        public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            Map<String, Serializable> rawMap = super.extractRaw(reader);
            
            // Add some test keywords to those actually extracted from the file including a nodeRef
            List<String> keywords = new ArrayList<String>(Arrays.asList(
                    new String[] { existingTagNodeRef, TAG_2, TAG_3, TAG_NONEXISTENT_NODEREF }));
            Serializable extractedKeywords = rawMap.get(Metadata.KEYWORDS);
            if (extractedKeywords != null && extractedKeywords instanceof String)
            {
                keywords.add((String) extractedKeywords);
            }
            else if (extractedKeywords != null && extractedKeywords instanceof Collection<?>)
            {
                keywords.addAll((Collection<? extends String>) extractedKeywords);
            }
            putRawValue(Metadata.KEYWORDS, (Serializable) keywords, rawMap);
            return rawMap;
        }
    }
    
    /**
     * Test execution of mapping strings to tags
     */
    public void testTagMapping() throws Exception
    {
        // explicitly set here (rather than rely on defaults) in case another test method nullified
        this.executer = (ContentMetadataExtracter) ctx.getBean("extract-metadata");
        executer.setStringTaggingSeparators(Arrays.asList(",", ";", "\\|"));
        
        // Create the folders and documents to be tagged
        NodeRef[] nodes = createTestFolderAndDocument(QUICK_FILENAME);
        NodeRef document = nodes[0];
        
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){
            
            @Override
            public Void execute() throws Throwable
            {
                NodeRef existingTagNodeRef = taggingService.createTag(storeRef, TAG_1);
                extractor.setExistingTagNodeRef(existingTagNodeRef.toString());
                
                ActionImpl action = new ActionImpl(document, ID, ContentMetadataExtracter.EXECUTOR_NAME, null);
                executer.execute(action, document);
                
                // Test extracted properties

                assertEquals(ContentMetadataExtracterTest.QUICK_DESCRIPTION, 
                        nodeService.getProperty(document, ContentModel.PROP_DESCRIPTION));
                
                assertTrue("storeRef tags should contain '" + QUICK_KEYWORD + "'", 
                        taggingService.getTags(storeRef).contains(QUICK_KEYWORD));
                
                List<String> tags = taggingService.getTags(document);
                assertTrue("doc tags '"+tags+"' should contain '" + QUICK_KEYWORD + "'", 
                        tags.contains(QUICK_KEYWORD));
                
                // Test manually added keyword
                assertTrue("doc tags '"+tags+"' should contain '" + TAG_2 + "'", 
                        tags.contains(TAG_2));

                // Test manually added keyword - note: lower-case tag name
                assertTrue("doc tags '"+tags+"' should contain '" + TAG_3.toLowerCase() + "'",
                        tags.contains(TAG_3.toLowerCase()));
                
                // Test manually added nodeRef keyword
                assertTrue("doc tags '"+tags+"' should contain '" + TAG_1 + "'", 
                        tags.contains(TAG_1));
                
                // Test that there are no extra tags created by the non-existent nodeRef
                assertEquals("Unexpected number of doc tags '"+tags+"'", 7,
                        tags.size());
                
                return null;
            }
        });

        removeTestFolderAndDocument(nodes);
    }

    /**
     * Test to validate that we ignore invalid tag names when running with "enableStringTagging" option (aka "addTags")
     * 
     * eg. "java.lang.IllegalArgumentException: Tag name must not contain | char sequence"
     */
    public void testIgnoreInvalidTag() throws Exception
    {
        this.executer = (ContentMetadataExtracter) ctx.getBean("extract-metadata");
        executer.setStringTaggingSeparators(null);

        // Create the folders and documents to be tagged
        NodeRef[] nodes = createTestFolderAndDocument(QUICK_FILENAME2);
        NodeRef document = nodes[0];

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                ActionImpl action = new ActionImpl(document, ID, ContentMetadataExtracter.EXECUTOR_NAME, null);
                action.setExecuteAsynchronously(false);

                executer.execute(action, document);

                return null;
            }
        });

        removeTestFolderAndDocument(nodes);
    }

    public void testTagMappingSeparators() throws Exception
    {
        // explicitly set here (rather than rely on defaults) in case another test method nullified
        this.executer = (ContentMetadataExtracter) ctx.getBean("extract-metadata");
        executer.setStringTaggingSeparators(Arrays.asList(",", ";", "\\|"));

        // IPTC Keywords with comma
        NodeRef[] nodes = createTestFolderAndDocument(QUICK_FILENAME);
        extractAndCheckTags(nodes[0], Arrays.asList("fox", "dog", "lazy", "jumping"));
        removeTestFolderAndDocument(nodes);

        // IPTC Keywords with vertical bar (pipe)
        nodes = createTestFolderAndDocument(QUICK_FILENAME2);
        extractAndCheckTags(nodes[0], Arrays.asList("k1", "k2", "k3"));
        removeTestFolderAndDocument(nodes);

        // IPTC Keywords with semi-colon
        nodes = createTestFolderAndDocument(QUICK_FILENAME3);
        extractAndCheckTags(nodes[0], Arrays.asList("keyword1", "keyword2", "keyword3", "keyword4"));
        removeTestFolderAndDocument(nodes);
    }

    private void extractAndCheckTags(NodeRef document, List<String> expectedTags)
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                ActionImpl action = new ActionImpl(document, ID, ContentMetadataExtracter.EXECUTOR_NAME, null);
                executer.execute(action, document);

                List<String> tags = taggingService.getTags(document);

                for (String expectedTag : expectedTags)
                {
                    assertTrue("Expected tag '"+expectedTag+"' not in "+tags, tags.contains(expectedTag));
                }

                return null;
            }
        });
    }
}
