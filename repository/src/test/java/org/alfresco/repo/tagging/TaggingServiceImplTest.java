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
package org.alfresco.repo.tagging;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.AuditServiceImpl;
import org.alfresco.repo.audit.UserAuditFilter;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.node.NodeRefPropertyMethodInterceptor;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Tagging service implementation unit test
 *
 * @author Roy Wetherall
 * @author Nick Burch
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaggingServiceImplTest extends BaseSpringTest
{
    private static final Log logger = LogFactory.getLog(TaggingServiceImplTest.class);

    /** Services */
    private TaggingServiceImpl taggingService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private CopyService copyService;
    private CheckOutCheckInService checkOutCheckInService;
    private ScriptService scriptService;
    private AuditService auditService;
    private ActionService actionService;
    private ActionTrackingService actionTrackingService;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private PermissionService permissionService;
    private MutableAuthenticationService authenticationService;
    private AsyncOccurs asyncOccurs;
    private SiteService siteService;

    private NodeRefPropertyMethodInterceptor nodeRefPropInterceptor;

    private static StoreRef storeRef;
    private static NodeRef rootNode;
    private NodeRef folder;
    private NodeRef subFolder;
    private NodeRef document;
    private NodeRef subDocument;

    private static final String TEST_SITE_PRESET = "testSitePreset";
    private static final String TEST_TITLE = "TitleTest This is my title";
    private static final String TEST_DESCRIPTION = "DescriptionTest This is my description";

    private static final String TAG_1 = "tag one";
    private static final String TAG_2 = "tag two";
    private static final String TAG_3 = "Tag Three";
    private static final String TAG_4 = "tag four";
    private static final String TAG_5 = "tag five";
    private static final String TAG_I18N = "àâæçéèêëîïôœùûüÿñ";

    private static final String BAD_TAG = "bad \n tag";
    private static final String BAD_TAG2 = "Broken|2";

    private static final String UPPER_TAG = "House";
    private static final String LOWER_TAG = "house";

    private static boolean init = false;

    @Before
    public void setUp() throws Exception
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
        this.taggingService = (TaggingServiceImpl)applicationContext.getBean("taggingService");
        this.nodeService = (NodeService) applicationContext.getBean("NodeService");
        this.fileFolderService = (FileFolderService) applicationContext.getBean("FileFolderService");
        this.copyService = (CopyService) applicationContext.getBean("CopyService");
        this.checkOutCheckInService = (CheckOutCheckInService) applicationContext.getBean("CheckoutCheckinService");
        this.actionService = (ActionService)applicationContext.getBean("ActionService");
        this.transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
        this.siteService = applicationContext.getBean("SiteService", SiteService.class);
        //MNT-10807 : Auditing does not take into account audit.filter.alfresco-access.transaction.user
        UserAuditFilter userAuditFilter = new UserAuditFilter();
        userAuditFilter.setUserFilterPattern("System;.*");
        userAuditFilter.afterPropertiesSet();
        AuditComponent auditComponent = (AuditComponent) applicationContext.getBean("auditComponent");
        auditComponent.setUserAuditFilter(userAuditFilter);
        AuditServiceImpl auditServiceImpl = (AuditServiceImpl) applicationContext.getBean("auditService");
        auditServiceImpl.setAuditComponent(auditComponent);
        this.auditService = (AuditService)applicationContext.getBean("auditService");
        this.scriptService = (ScriptService)applicationContext.getBean("scriptService");
        this.actionTrackingService = (ActionTrackingService)applicationContext.getBean("actionTrackingService");
        this.authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");

        this.personService = (PersonService)applicationContext.getBean("PersonService");
        this.permissionService = (PermissionService)applicationContext.getBean("PermissionService");
        this.authenticationService = (MutableAuthenticationService)applicationContext.getBean("authenticationService");
        this.nodeRefPropInterceptor = (NodeRefPropertyMethodInterceptor)applicationContext.getBean("nodeRefPropertyInterceptor");

        if (init == false)
        {
            this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

                @Override
                public Void execute() throws Throwable
                {
                    // Authenticate as the system user
                    authenticationComponent.setSystemUserAsCurrentUser();

                    // Create the store and get the root node
                    TaggingServiceImplTest.storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
                    TaggingServiceImplTest.rootNode = nodeService.getRootNode(TaggingServiceImplTest.storeRef);

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
                    return null;
                }});

        }

        // We want to know when tagging actions have finished running
        asyncOccurs = new AsyncOccurs();
        ((PolicyComponent)applicationContext.getBean("policyComponent")).bindClassBehaviour(
                AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute.QNAME,
                ActionModel.TYPE_ACTION,
                new JavaBehaviour(asyncOccurs, "onAsyncActionExecute", NotificationFrequency.EVERY_EVENT)
        );

        // We do want action tracking whenever the tag scope updater runs
        UpdateTagScopesActionExecuter updateTagsAction =
                (UpdateTagScopesActionExecuter)applicationContext.getBean("update-tagscope");
        updateTagsAction.setTrackStatus(true);

        // Create the folders and documents to be tagged
        createTestDocumentsAndFolders();
    }

    @After
    public void tearDown() throws Exception
    {
        removeTestDocumentsAndFolders();
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            fail("Test is not transaction-safe.  Fix up transaction handling and re-test.");
        }
    }

    private void createTestDocumentsAndFolders() throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {

                // Authenticate as the system user
                authenticationComponent.setSystemUserAsCurrentUser();

                String guid = GUID.generate();

                // Create a folder
                Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
                folderProps.put(ContentModel.PROP_NAME, "testFolder" + guid);
                folder = nodeService.createNode(
                        TaggingServiceImplTest.rootNode,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder" + guid),
                        ContentModel.TYPE_FOLDER,
                        folderProps).getChildRef();

                // Create a node
                Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
                docProps.put(ContentModel.PROP_NAME, "testDocument" + guid + ".txt");
                document = nodeService.createNode(
                        folder,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testDocument" + guid + ".txt"),
                        ContentModel.TYPE_CONTENT,
                        docProps).getChildRef();

                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "subFolder" + guid);
                subFolder = nodeService.createNode(
                        folder,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subFolder" + guid),
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();

                props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "subDocument" + guid + ".txt");
                subDocument = nodeService.createNode(
                        subFolder,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subDocument" + guid + ".txt"),
                        ContentModel.TYPE_CONTENT,
                        props).getChildRef();
                return null;
            }
        });
    }

    private void removeTestDocumentsAndFolders() throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){
            @Override
            public Void execute() throws Throwable
            {
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
                return null;
            }
        });
    }

    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test1TagCRUD()
            throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                // Get the tags
                List<String> tags = taggingService.getTags(TaggingServiceImplTest.storeRef);
                assertNotNull(tags);
                assertEquals(0, tags.size());

                // Create a tag
                taggingService.createTag(TaggingServiceImplTest.storeRef, TAG_1);
                taggingService.createTag(TaggingServiceImplTest.storeRef, UPPER_TAG);

                return null;
            }
        });

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                // Get all the tags
                List<String> tags = taggingService.getTags(TaggingServiceImplTest.storeRef);
                assertNotNull(tags);
                assertEquals(2, tags.size());
                assertTrue(tags.contains(TAG_1));
                assertTrue(tags.contains(LOWER_TAG));

                // Get Paged tags with filter
                Pair<List<String>, Integer> pagedTags = taggingService.getPagedTags(TaggingServiceImplTest.storeRef, "one", 0 ,10);
                assertNotNull(pagedTags);
                List<String> tagPage = pagedTags.getFirst();
                int allFilteredTagsCount = pagedTags.getSecond();
                assertEquals(1, allFilteredTagsCount);
                assertEquals(1, tagPage.size());
                assertTrue(tagPage.get(0).contains("one"));

                tagPage = taggingService.getTags(TaggingServiceImplTest.storeRef, "one");
                assertNotNull(pagedTags);
                tagPage = pagedTags.getFirst();
                allFilteredTagsCount = pagedTags.getSecond();
                assertEquals(1, allFilteredTagsCount);
                assertEquals(1, tagPage.size());
                assertTrue(tagPage.get(0).contains("one"));

                // Check isTag method
                assertFalse(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
                assertTrue(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
                assertTrue(taggingService.isTag(TaggingServiceImplTest.storeRef, UPPER_TAG));
                assertTrue(taggingService.isTag(TaggingServiceImplTest.storeRef, LOWER_TAG));

                // Remove a tag
                taggingService.deleteTag(TaggingServiceImplTest.storeRef, UPPER_TAG);
                return null;
            }
        });

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                // Get all the tags
                List<String> tags = taggingService.getTags(TaggingServiceImplTest.storeRef);
                assertNotNull(tags);
                assertEquals(1, tags.size());
                assertTrue(tags.contains(TAG_1));
                assertFalse(tags.contains(LOWER_TAG));

                // Check isTag method
                assertFalse(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
                assertTrue(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
                assertFalse(taggingService.isTag(TaggingServiceImplTest.storeRef, UPPER_TAG));
                assertFalse(taggingService.isTag(TaggingServiceImplTest.storeRef, LOWER_TAG));
                return null;
            }
        });
    }

    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test2AddRemoveTag()
            throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                List<String> tags = taggingService.getTags(document);
                assertNotNull(tags);
                assertTrue(tags.isEmpty());
                assertFalse(taggingService.hasTag(document, TAG_1));

                assertTrue(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_1));
                taggingService.addTag(document, TAG_1);

                tags = taggingService.getTags(document);
                assertNotNull(tags);
                assertEquals(1, tags.size());
                assertTrue(tags.contains(TAG_1));
                assertTrue(taggingService.hasTag(document, TAG_1));

                assertFalse(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
                taggingService.addTag(document, TAG_2);

                assertTrue(taggingService.isTag(TaggingServiceImplTest.storeRef, TAG_2));
                tags = taggingService.getTags(document);
                assertNotNull(tags);
                assertEquals(2, tags.size());
                assertTrue(tags.contains(TAG_1));
                assertTrue(tags.contains(TAG_2));
                assertTrue(taggingService.hasTag(document, TAG_1));
                assertTrue(taggingService.hasTag(document, TAG_2));

                taggingService.removeTag(document, TAG_1);
                tags = taggingService.getTags(document);
                assertNotNull(tags);
                assertEquals(1, tags.size());
                assertFalse(tags.contains(TAG_1));
                assertFalse(taggingService.hasTag(document, TAG_1));
                assertTrue(tags.contains(TAG_2));
                assertTrue(taggingService.hasTag(document, TAG_2));

                List<String> setTags = new ArrayList<String>(2);
                setTags.add(TAG_3);
                setTags.add(TAG_1);
                taggingService.setTags(document, setTags);
                tags = taggingService.getTags(document);
                assertNotNull(tags);
                assertEquals(2, tags.size());
                assertTrue(tags.contains(TAG_1));
                assertFalse(tags.contains(TAG_2));
                assertTrue(tags.contains(TAG_3.toLowerCase()));

                taggingService.clearTags(document);
                tags = taggingService.getTags(document);
                assertNotNull(tags);
                assertTrue(tags.isEmpty());
                return null;
            }
        });
    }

    @Test
    public void test3TagScopeFindAddRemove()
            throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                // Get scopes for node without
                TagScope tagScope = taggingService.findTagScope(subDocument);
                assertNull(tagScope);
                List<TagScope> tagScopes = taggingService.findAllTagScopes(subDocument);
                assertNotNull(tagScopes);
                assertEquals(0, tagScopes.size());

                // Add scopes
                // TODO should the add return the created scope ??
                taggingService.addTagScope(folder);
                taggingService.addTagScope(subFolder);

                // Get the scopes
                tagScope = taggingService.findTagScope(subDocument);
                assertNotNull(tagScope);
                tagScopes = taggingService.findAllTagScopes(subDocument);
                assertNotNull(tagScopes);
                assertEquals(2, tagScopes.size());
                tagScope = taggingService.findTagScope(subFolder);
                assertNotNull(tagScope);
                tagScopes = taggingService.findAllTagScopes(subFolder);
                assertNotNull(tagScopes);
                assertEquals(2, tagScopes.size());
                tagScope = taggingService.findTagScope(folder);
                assertNotNull(tagScope);
                tagScopes = taggingService.findAllTagScopes(folder);
                assertNotNull(tagScopes);
                assertEquals(1, tagScopes.size());

                // Remove a scope
                taggingService.removeTagScope(folder);
                tagScope = taggingService.findTagScope(subDocument);
                assertNotNull(tagScope);
                tagScopes = taggingService.findAllTagScopes(subDocument);
                assertNotNull(tagScopes);
                assertEquals(1, tagScopes.size());
                tagScope = taggingService.findTagScope(subFolder);
                assertNotNull(tagScope);
                tagScopes = taggingService.findAllTagScopes(subFolder);
                assertNotNull(tagScopes);
                assertEquals(1, tagScopes.size());
                tagScope = taggingService.findTagScope(folder);
                assertNull(tagScope);
                tagScopes = taggingService.findAllTagScopes(folder);
                assertNotNull(tagScopes);
                assertEquals(0, tagScopes.size());
                return null;
            }
        });
    }

    @Test
    public void test4TagScope()
            throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                // TODO add some tags before the scopes are added

                // Add some tag scopes
                taggingService.addTagScope(folder);
                taggingService.addTagScope(subFolder);

                // Add some more tags after the scopes have been added
                taggingService.addTag(subDocument, TAG_1); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_2); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_3); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_1); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_2); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(folder, TAG_2); // folder only
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // re get the tag scopes
                TagScope ts1 = taggingService.findTagScope(subDocument);
                TagScope ts2 = taggingService.findTagScope(folder);

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
                taggingService.removeTag(folder, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.removeTag(subFolder, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.removeTag(subFolder, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.removeTag(subDocument, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // And add one more
                taggingService.addTag(folder, TAG_3);
                return null;
            }
        });

        /*
        The following code from the test was commented out as part of REPO-2028 to remove
        the lucene dependencies in the tests.
        @Category({RedundantTests.class,LuceneTests.class})
         */
//
//        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
//        {
//            @Override
//            public Void execute() throws Throwable
//            {
//                // re get the tag scopes
//                TagScope ts1 = taggingService.findTagScope(subDocument);
//                TagScope ts2 = taggingService.findTagScope(folder);
//
//                // Recheck the tag scopes
//                assertEquals(
//                      "Wrong tags on sub folder: " + ts1.getTags(),
//                      2, ts1.getTags().size()
//                );
//                assertEquals(
//                      "Wrong tags on main folder: " + ts2.getTags(),
//                      2, ts2.getTags().size()
//                );
//
//                // Sub-folder should be ordered by tag name, as all values 1
//                assertEquals(1, ts1.getTags().get(0).getCount());
//                assertEquals(1, ts1.getTags().get(1).getCount());
//                assertEquals(TAG_2, ts1.getTags().get(0).getName());
//                assertEquals(TAG_3.toLowerCase(), ts1.getTags().get(1).getName());
//
//                // Folder should be still sorted by size, as a 2 & a 1
//                assertEquals(2, ts2.getTags().get(0).getCount());
//                assertEquals(1, ts2.getTags().get(1).getCount());
//                assertEquals(TAG_3.toLowerCase(), ts2.getTags().get(0).getName());
//                assertEquals(TAG_2, ts2.getTags().get(1).getName());
//                return null;
//            }
//        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test5TagScopeSummary() throws Exception
    {
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // TODO add some tags before the scopes are added

                // Add some tag scopes
                taggingService.addTagScope(folder);
                taggingService.addTagScope(subFolder);

                // Add some more tags after the scopes have been added
                taggingService.addTag(subDocument, TAG_1); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_2); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_3); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_1); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_2); // folder+subfolder
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(folder, TAG_2); // folder only
                return null;
            }
        });
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TagScopePropertyMethodInterceptor.setEnabled(Boolean.TRUE);

                Serializable summaryObj = nodeService.getProperty(folder, ContentModel.PROP_TAGSCOPE_SUMMARY);
                assertTrue("TagScopeSummary value on main folder is not of correct class: " + summaryObj.getClass().getName(),
                        List.class.isAssignableFrom(summaryObj.getClass()));
                assertEquals(3, ((List)summaryObj).size());

                //Check that the next call for the same summary comes from the cache
                Serializable summaryObj2 = nodeService.getProperty(folder, ContentModel.PROP_TAGSCOPE_SUMMARY);
                assertTrue("TagScopeSummary value on main folder did not come from the cache",
                        summaryObj == summaryObj2);

                Map<QName,Serializable> props = nodeService.getProperties(subFolder);
                assertTrue("Properties of subfolder do not include tagScopeSummary", props.containsKey(ContentModel.PROP_TAGSCOPE_SUMMARY));
                summaryObj = props.get(ContentModel.PROP_TAGSCOPE_SUMMARY);
                assertTrue("TagScopeSummary value on subfolder is not of correct class: " + summaryObj.getClass().getName(),
                        List.class.isAssignableFrom(summaryObj.getClass()));
                assertEquals(3, ((List)summaryObj).size());

                TagScopePropertyMethodInterceptor.setEnabled(Boolean.FALSE);

                summaryObj = nodeService.getProperty(folder, ContentModel.PROP_TAGSCOPE_SUMMARY);
                assertNull("TagScopeSummary value on main folder should be null: " + summaryObj,
                        summaryObj);

                props = nodeService.getProperties(subFolder);
                assertFalse("Properties of subfolder should not contain tagScopeProperty", props.containsKey(ContentModel.PROP_TAGSCOPE_SUMMARY));
                return null;
            }
        });
    }

    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test6TagScopeRefresh()
            throws Exception
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Add some tags to the nodes
                // tag scope on folder should be ....
                // tag2 = 3
                // tag1 = 2
                // tag3 = 1
                taggingService.addTag(subDocument, TAG_1);
                taggingService.addTag(subDocument, TAG_2);
                taggingService.addTag(subDocument, TAG_3);
                taggingService.addTag(subFolder, TAG_1);
                taggingService.addTag(subFolder, TAG_2);
                taggingService.addTag(folder, TAG_2);

                // Commit the changes. No action will fire, as there
                // aren't currently any tag scopes to be updated!
                return null;
            }
        });
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Add the tag scope
                taggingService.addTagScope(folder);

                // The updates in this case are synchronous, not
                // async, so we don't need to wait for any actions
                return null;
            }
        });
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Get the tag scope and check that all the values have been set correctly
                TagScope tagScope = taggingService.findTagScope(folder);
                assertNotNull(tagScope);
                assertEquals(3, tagScope.getTags().size());
                assertEquals(3, tagScope.getTag(TAG_2).getCount());
                assertEquals(2, tagScope.getTag(TAG_1).getCount());
                assertEquals(1, tagScope.getTag(TAG_3.toLowerCase()).getCount());
                return null;
            }
        });
    }

    @Test
    public void test7TagScopeSetUpdate()
            throws Exception
    {
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Set up tag scope
                taggingService.addTagScope(folder);
                ;

                // Add some tags
                taggingService.addTag(folder, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(document, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(document, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_3);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check that tag scope
                TagScope ts1 = taggingService.findTagScope(folder);
                assertEquals("Wrong tags on folder tagscope: " + ts1.getTags(), 3, ts1.getTags().size());
                assertEquals(4, ts1.getTag(TAG_1).getCount());
                assertEquals(2, ts1.getTag(TAG_2).getCount());
                assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());

                // Re-set the tag scopes
                List<String> tags = new ArrayList<String>(3);
                tags.add(TAG_2);
                tags.add(TAG_3);
                tags.add(TAG_4);

                taggingService.setTags(subDocument, tags);
                return null;
            }
        });
        /*
        This part of the code was commented out as part of the REPO-2028 to remove the lucene dependency from tests
        @Category({RedundantTests.class,LuceneTests.class})
         */

//        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
//        {
//            @Override
//            public Void execute() throws Throwable
//            {
//                // Check that the tagscope has been updated correctly
//                TagScope ts1 = taggingService.findTagScope(folder);
//                assertEquals(3, ts1.getTag(TAG_1).getCount());
//                assertEquals(2, ts1.getTag(TAG_2).getCount());
//                assertEquals(1, ts1.getTag(TAG_3.toLowerCase()).getCount());
//                assertEquals(1, ts1.getTag(TAG_4).getCount());
//                return null;
//            }
//        });
    }

    /*
     * https://issues.alfresco.com/jira/browse/ETHREEOH-220
     */
    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test8ETHREEOH_220() throws Exception
    {
        // Add tag scope to a folder, then add a non-ASCII (unicode)
        //  tag onto the folder
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTagScope(folder);
                taggingService.addTag(folder, TAG_I18N);
                return null;
            }
        });

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Get the tag from the node
                List<String> tags = taggingService.getTags(folder);
                assertNotNull(tags);
                assertEquals(1, tags.size());
                assertEquals(TAG_I18N, tags.get(0));

                // Get the tag from the tagscope
                TagScope tagScope = taggingService.findTagScope(folder);
                assertNotNull(tagScope);
                assertEquals(1, tagScope.getTags().size());
                TagDetails tagDetails = tagScope.getTag(TAG_I18N);
                assertNotNull(tagDetails);
                assertEquals(TAG_I18N, tagDetails.getName());
                assertEquals(1, tagDetails.getCount());
                return null;
            }
        });
    }

    /**
     * Ensures that the tag scope is correctly updated
     *  when folders and content are created, updated,
     *  moved, copied and deleted.
     */
    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test9TagScopeUpdateViaNodePolicies() throws Exception {
        class TestData
        {
            public NodeRef tagFoo1;
            public NodeRef tagFoo2;
            public NodeRef tagFoo3;
            public NodeRef tagBar;
            public NodeRef container1;
            public NodeRef container2;
            public NodeRef taggedFolder;
            public NodeRef taggedFolder2;
            public NodeRef taggedDoc;
            public NodeRef taggedDoc2;
            public NodeRef checkedOutDoc;
        }
        final TestData testData = new TestData();

        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // The various tags we'll be using in testing
                testData.tagFoo1 = taggingService.createTag(folder.getStoreRef(), "Foo1");
                testData.tagFoo2 = taggingService.createTag(folder.getStoreRef(), "Foo2");
                testData.tagFoo3 = taggingService.createTag(folder.getStoreRef(), "Foo3");
                testData.tagBar = taggingService.createTag(folder.getStoreRef(), "Bar");

                List<NodeRef> tagsList = new ArrayList<NodeRef>();


                // Create two containers marked as tag scopes
                Map<QName, Serializable> container1Props = new HashMap<QName, Serializable>(1);
                container1Props.put(ContentModel.PROP_NAME, "Container1");
                testData.container1 = nodeService.createNode(
                        folder,
                        ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        container1Props).getChildRef();
                assertEquals(0, nodeService.getChildAssocs(testData.container1).size());

                Map<QName, Serializable> container2Props = new HashMap<QName, Serializable>(1);
                container1Props.put(ContentModel.PROP_NAME, "Container2");
                testData.container2 = nodeService.createNode(
                        folder,
                        ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER,
                        container2Props).getChildRef();
                assertEquals(0, nodeService.getChildAssocs(testData.container2).size());


                // Check that the tag scopes are empty
                taggingService.addTagScope(testData.container1);
                taggingService.addTagScope(testData.container2);
                return null;
            }
        });
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertTrue(taggingService.isTagScope(testData.container1));
                assertTrue(taggingService.isTagScope(testData.container2));
                assertEquals(0, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());

                // Create a folder, without any tags on it
                Map<QName, Serializable> taggedFolderProps = new HashMap<QName, Serializable>(1);
                taggedFolderProps.put(ContentModel.PROP_NAME, "Folder");
                testData.taggedFolder = nodeService.createNode(testData.container1, ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_FOLDER, taggedFolderProps).getChildRef();

                // No tags, so no changes should have occured, and no actions
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(0, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());

                // Now update the folder to add in tags
                Map<QName, Serializable> taggedFolderProps = new HashMap<QName, Serializable>(1);
                List<NodeRef> tagsList = new ArrayList<NodeRef>();

                tagsList.add(testData.tagFoo1);
                tagsList.add(testData.tagFoo3);
                taggedFolderProps.put(ContentModel.ASPECT_TAGGABLE, (Serializable) tagsList);
                nodeService.addProperties(testData.taggedFolder, taggedFolderProps);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(
                        "Unexpected tags " + taggingService.findTagScope(testData.container1).getTags(),
                        2, taggingService.findTagScope(testData.container1).getTags().size()
                );
                assertEquals(
                        "Unexpected tags " + taggingService.findTagScope(testData.container2).getTags(),
                        0, taggingService.findTagScope(testData.container2).getTags().size()
                );

                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());


                // Create a document within it, check that tag scope tags are updated
                List<NodeRef> tagsList = new ArrayList<NodeRef>();
                tagsList.add(testData.tagFoo1);
                tagsList.add(testData.tagFoo2);

                Map<QName, Serializable> taggedDocProps = new HashMap<QName, Serializable>(1);
                taggedDocProps.put(ContentModel.PROP_NAME, "Document");
                taggedDocProps.put(ContentModel.ASPECT_TAGGABLE, (Serializable)tagsList);
                testData.taggedDoc = nodeService.createNode(
                        testData.taggedFolder,
                        ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASPECT_TAGGABLE,
                        ContentModel.TYPE_CONTENT,
                        taggedDocProps).getChildRef();
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());

                assertEquals(2, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());


                // Check that the Document really is a child of the folder,
                //  otherwise later checks will fail for really odd reasons
                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(1, nodeService.getChildAssocs(testData.taggedFolder).size());


                // Check out the node
                // Tags should be doubled up. (We don't care about ContentModel.ASPECT_WORKING_COPY
                //  because it isn't applied at suitable times to take not of)
                testData.checkedOutDoc = checkOutCheckInService.checkout(testData.taggedDoc);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());

                assertEquals(2, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(2, nodeService.getChildAssocs(testData.taggedFolder).size());


                // And check it back in again
                // Tags should go back to how they were
                checkOutCheckInService.checkin(testData.checkedOutDoc, null);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());

                assertEquals(2, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(1, nodeService.getChildAssocs(testData.taggedFolder).size());


                // Do a node->node copy of the document onto the other container
                Map<QName, Serializable> taggedDocProps = new HashMap<QName, Serializable>(1);
                taggedDocProps.put(ContentModel.PROP_NAME, "CopyDoc");
                assertEquals(0, nodeService.getChildAssocs(testData.container2).size());
                testData.taggedDoc2 = nodeService.createNode(
                        testData.container2,
                        ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASPECT_TAGGABLE,
                        ContentModel.TYPE_CONTENT,
                        taggedDocProps).getChildRef();
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(1, nodeService.getChildAssocs(testData.container2).size());
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                copyService.copy(testData.taggedDoc, testData.taggedDoc2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(2, taggingService.findTagScope(testData.container2).getTags().size());

                assertEquals(2, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());

                // Check that things were fine after the copy
                assertEquals(1, nodeService.getChildAssocs(testData.container2).size());
                assertEquals(testData.container2, nodeService.getPrimaryParent(testData.taggedDoc2).getParentRef());
                assertEquals(testData.container2, taggingService.findTagScope(testData.taggedDoc2).getNodeRef());


                // Copy the folder to another container
                // Does a proper, recursing copy
                testData.taggedFolder2 = copyService.copy(
                        testData.taggedFolder, testData.container2,
                        ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASSOC_CHILDREN,
                        true);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(3, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(2, nodeService.getChildAssocs(testData.container2).size());
                assertEquals(1, nodeService.getChildAssocs(testData.taggedFolder2).size());

                assertEquals(2, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(3, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(2, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo3").getCount());


                // Update the document on the original
                List<NodeRef> tagsList = new ArrayList<NodeRef>();
                tagsList.add(testData.tagBar);
                Map<QName, Serializable> taggedDocProps = new HashMap<QName, Serializable>(1);
                taggedDocProps.put(ContentModel.ASPECT_TAGGABLE, (Serializable)tagsList);
                taggedDocProps.put(ContentModel.PROP_NAME, "UpdatedDocument");
                nodeService.addProperties(testData.taggedDoc, taggedDocProps);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(3, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(3, taggingService.findTagScope(testData.container2).getTags().size());

                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("bar").getCount());

                assertEquals(3, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(2, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo3").getCount());


                // Move the document to another container
                testData.taggedDoc = nodeService.moveNode(testData.taggedDoc, testData.container2,
                        ContentModel.ASSOC_CONTAINS,
                        ContentModel.ASPECT_TAGGABLE).getChildRef();
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(2, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(4, taggingService.findTagScope(testData.container2).getTags().size());

                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(3, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(2, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo3").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("bar").getCount());


                // Check the state of the tree
                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(3, nodeService.getChildAssocs(testData.container2).size());

                assertEquals(testData.container2, nodeService.getPrimaryParent(testData.taggedDoc).getParentRef());
                assertEquals(testData.container2, taggingService.findTagScope(testData.taggedDoc).getNodeRef());
                assertEquals(testData.container2, nodeService.getPrimaryParent(testData.taggedDoc2).getParentRef());
                assertEquals(testData.container2, taggingService.findTagScope(testData.taggedDoc2).getNodeRef());


                // Delete the documents and folder one at a time
                nodeService.deleteNode(testData.taggedDoc); // container 2, "bar"
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(2, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(3, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(2, nodeService.getChildAssocs(testData.container2).size());

                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(3, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(2, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo3").getCount());
                assertEquals(null, taggingService.findTagScope(testData.container2).getTag("bar"));


                nodeService.deleteNode(testData.taggedDoc2); // container 2, "foo1", "foo2"
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(2, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(3, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(1, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(1, nodeService.getChildAssocs(testData.container2).size());

                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container1).getTag("foo3").getCount());

                assertEquals(2, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo3").getCount());
                assertEquals(null, taggingService.findTagScope(testData.container2).getTag("bar"));


                nodeService.deleteNode(testData.taggedFolder); // container 1, "foo1", "foo3"
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(0, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(3, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(0, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(1, nodeService.getChildAssocs(testData.container2).size());

                assertEquals(2, taggingService.findTagScope(testData.container2).getTag("foo1").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo2").getCount());
                assertEquals(1, taggingService.findTagScope(testData.container2).getTag("foo3").getCount());


                nodeService.deleteNode(testData.taggedFolder2); // container 2, has a child also
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                assertEquals(0, taggingService.findTagScope(testData.container1).getTags().size());
                assertEquals(0, taggingService.findTagScope(testData.container2).getTags().size());
                assertEquals(0, nodeService.getChildAssocs(testData.container1).size());
                assertEquals(0, nodeService.getChildAssocs(testData.container2).size());
                return null;
            }
        });
    }

    /**
     * Ensures that a user can only tag a node they can write to,
     *  but that the tag scope updates propagate upwards as the system
     *  (even to things the user can't write to)
     * Also checks that policies are disabled during tag scope updates,
     *  so that the auditable flags aren't incorrectly set by the change
     */
    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test91PermissionsAndPolicies() throws Exception
    {
        class TestData
        {
            public NodeRef taggedNode;
            public NodeRef auditableFolder;
            public Date origModified;
        }
        final TestData testData = new TestData();
        final String USER_1 = "User1";
        authenticationComponent.setSystemUserAsCurrentUser();
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Create a user
                if(authenticationService.authenticationExists(USER_1))
                    authenticationService.deleteAuthentication(USER_1);
                if(personService.personExists(USER_1))
                    personService.deletePerson(USER_1);

                authenticationService.createAuthentication(USER_1, "PWD".toCharArray());
                PropertyMap personProperties = new PropertyMap();
                personProperties.put(ContentModel.PROP_USERNAME, USER_1);
                personProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + USER_1);
                personProperties.put(ContentModel.PROP_FIRSTNAME, "firstName");
                personProperties.put(ContentModel.PROP_LASTNAME, "lastName");
                personProperties.put(ContentModel.PROP_EMAIL, USER_1+"@example.com");
                personProperties.put(ContentModel.PROP_JOBTITLE, "jobTitle");
                personService.createPerson(personProperties);


                // Give that user permissions on the tagging category root, so
                //  they're allowed to add new tags
                NodeRef tn = taggingService.createTag(folder.getStoreRef(), "Testing");
                NodeRef tr = nodeService.getPrimaryParent(tn).getParentRef();
                permissionService.setPermission(tr, USER_1, PermissionService.EDITOR, true);
                permissionService.setPermission(tr, USER_1, PermissionService.CONTRIBUTOR, true);


                // Create a folder with a tag scope on it + auditable aspect
                // User can read but not write
                authenticationComponent.setSystemUserAsCurrentUser();
                testData.auditableFolder = nodeService.createNode(
                        folder, ContentModel.ASSOC_CONTAINS,
                        QName.createQName("Folder"), ContentModel.TYPE_FOLDER
                ).getChildRef();
                nodeService.addAspect(testData.auditableFolder, ContentModel.ASPECT_AUDITABLE, null);
                taggingService.addTagScope(testData.auditableFolder);
                permissionService.setPermission(testData.auditableFolder, USER_1, PermissionService.CONSUMER, true);

                // Auditable checks
                assertEquals("System", nodeService.getProperty(testData.auditableFolder, ContentModel.PROP_CREATOR));
                assertEquals("System", nodeService.getProperty(testData.auditableFolder, ContentModel.PROP_MODIFIER));


                // Create a node without tags, which the user
                //  can write to
                testData.taggedNode = nodeService.createNode(
                        testData.auditableFolder, ContentModel.ASSOC_CONTAINS,
                        QName.createQName("Tagged"), ContentModel.TYPE_CONTENT
                ).getChildRef();
                permissionService.setPermission(testData.taggedNode, USER_1, PermissionService.EDITOR, true);


                // Tag the node as the user
                authenticationComponent.setCurrentUser(USER_1);
                assertEquals(0, taggingService.getTags(testData.taggedNode).size());

                nodeService.setProperty(testData.taggedNode, ContentModel.PROP_TITLE, "To ensure we're allowed to write");

                taggingService.addTag(testData.taggedNode, TAG_1);
                taggingService.addTag(testData.taggedNode, TAG_2);
                assertEquals(2, taggingService.getTags(testData.taggedNode).size());


                // Ensure the folder tag scope got the update
                TagScope ts = taggingService.findTagScope(testData.taggedNode);
                assertEquals(testData.auditableFolder, ts.getNodeRef());
                assertEquals(0, ts.getTags().size());

                assertEquals("System", nodeService.getProperty(testData.auditableFolder, ContentModel.PROP_CREATOR));
                assertEquals("System", nodeService.getProperty(ts.getNodeRef(), ContentModel.PROP_MODIFIER));
                return null;
            }
        });
        // Due to timestamp propagation, we need to start a new transaction to get the current folder modified date
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                testData.origModified = (Date)nodeService.getProperty(testData.auditableFolder, ContentModel.PROP_MODIFIED);
                return null;
            }
        });
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TagScope ts = taggingService.findTagScope(testData.taggedNode);
                assertEquals(testData.auditableFolder, ts.getNodeRef());
                assertEquals(2, ts.getTags().size());
                assertEquals(1, ts.getTag(TAG_1).getCount());
                assertEquals(1, ts.getTag(TAG_2).getCount());

                // Ensure the auditable flags on the folder are unchanged
                assertEquals("System", nodeService.getProperty(testData.auditableFolder, ContentModel.PROP_CREATOR));
                assertEquals("System", nodeService.getProperty(ts.getNodeRef(), ContentModel.PROP_MODIFIER));
                assertEquals(testData.origModified.getTime(), ((Date)nodeService.getProperty(testData.auditableFolder, ContentModel.PROP_MODIFIED)).getTime());

                // Tidy up
                authenticationComponent.setSystemUserAsCurrentUser();
                authenticationService.deleteAuthentication(USER_1);
                personService.deletePerson(USER_1);
                return null;
            }
        });
    }

    // == Test the JavaScript API ==
    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test92JSAPI() throws Exception
    {
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<String, Object> model = new HashMap<String, Object>(0);
                model.put("folder", folder);
                model.put("subFolder", subFolder);
                model.put("document", document);
                model.put("subDocument", subDocument);
                model.put("tagScopeTest", false);

                ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/tagging/script/test_taggingService.js");
                scriptService.executeScript(location, model);

                // Let the script run
                return null;
            }
        });
    }

    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void test93JSTagScope() throws Exception
    {
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Add a load of tags to test the global tag methods with
                taggingService.createTag(storeRef, "alpha");
                taggingService.createTag(storeRef, "alpha double");
                taggingService.createTag(storeRef, "beta");
                taggingService.createTag(storeRef, "gamma");
                taggingService.createTag(storeRef, "delta");

                // Add a load of tags and tag scopes to the object and commit before executing the script
                taggingService.addTagScope(folder);
                taggingService.addTagScope(subFolder);

                taggingService.addTag(subDocument, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_3);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subFolder, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(document, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(document, TAG_2);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(folder, TAG_1);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<String, Object> model = new HashMap<String, Object>(0);
                model.put("folder", folder);
                model.put("subFolder", subFolder);
                model.put("document", document);
                model.put("subDocument", subDocument);
                model.put("tagScopeTest", true);
                model.put("store", storeRef.toString());

                ScriptLocation location = new ClasspathScriptLocation(
                        "org/alfresco/repo/tagging/script/test_taggingService.js");
                scriptService.executeScript(location, model);

                // Let the script run
                return null;
            }
        });
    }

    /**
     * Test that the scheduled task will do the right thing
     *  when it runs.
     */
    @Test
    public void test93OnStartupJob() throws Exception
    {
        final UpdateTagScopesActionExecuter updateTagsAction = (UpdateTagScopesActionExecuter) applicationContext
                .getBean("update-tagscope");
        class TestData
        {
            public String lockF;
            public String lockSF;
        }
        final TestData testData = new TestData();
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Nothing is pending to start with
                assertEquals(0, updateTagsAction.searchForTagScopesPendingUpdates().size());


                // Take the tag scope lock, so that no real updates will happen
                testData.lockF = updateTagsAction.lockTagScope(folder);
                testData.lockSF = updateTagsAction.lockTagScope(subFolder);

                // Do some tagging
                taggingService.addTagScope(folder);
                taggingService.addTagScope(subFolder);

                taggingService.addTag(subDocument, TAG_1);
                taggingService.addTag(subDocument, TAG_2);
                taggingService.addTag(subFolder, TAG_1);
                taggingService.addTag(document, TAG_1);
                taggingService.addTag(folder, TAG_1);
                taggingService.addTag(folder, TAG_3);
                return null;
            }
        });

        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Tag scope updates shouldn't have happened yet,
                //  as the scopes are locked
                TagScope ts1 = taggingService.findTagScope(folder);
                TagScope ts2 = taggingService.findTagScope(subFolder);
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
                assertTrue("Not found in " + pendingScopes, pendingScopes.contains(folder));
                assertTrue("Not found in " + pendingScopes, pendingScopes.contains(subFolder));


                // Ensure that we've still got the lock, eg in case
                //  of the async execution taking a while to proceed
                updateTagsAction.updateTagScopeLock(folder, testData.lockF);
                updateTagsAction.updateTagScopeLock(subFolder, testData.lockSF);


                // Have the Quartz bean fire now
                // It won't be able to do anything, as the locks are taken
                UpdateTagScopesQuartzJob job = new UpdateTagScopesQuartzJob();
                job.execute(actionService, updateTagsAction);
                return null;
            }
        });
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Check that things are still pending despite the quartz run
                assertEquals(2, updateTagsAction.searchForTagScopesPendingUpdates().size());
                List<NodeRef> pendingScopes = updateTagsAction.searchForTagScopesPendingUpdates();
                assertTrue("Not found in " + pendingScopes, pendingScopes.contains(folder));
                assertTrue("Not found in " + pendingScopes, pendingScopes.contains(subFolder));
                return null;
            }
        });


        // Give back our locks, so we can proceed
        updateTagsAction.unlockTagScope(folder, testData.lockF);
        updateTagsAction.unlockTagScope(subFolder, testData.lockSF);


        // Fire off the quartz bean, this time it can really work
        UpdateTagScopesQuartzJob job = new UpdateTagScopesQuartzJob();
        job.execute(actionService, updateTagsAction);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Now check again - nothing should be pending
                assertEquals(0, updateTagsAction.searchForTagScopesPendingUpdates().size());

                TagScope ts1 = taggingService.findTagScope(folder);
                TagScope ts2 = taggingService.findTagScope(subFolder);
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
                return null;
            }
        });
    }

    /**
     * Test that when multiple threads do tag updates, the right thing still
     * happens
     */
    @Test
    public void test94MultiThreaded() throws Exception
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTagScope(folder);
                taggingService.addTagScope(subFolder);
                return null;
            }
        });

        // Reset the action count
        asyncOccurs.wantedActionsCount = 0;


        // Prepare a bunch of threads to do tagging
        final List<Thread> threads = new ArrayList<Thread>();
        final String[] tags = new String[] { TAG_1, TAG_2, TAG_3, TAG_4, TAG_5,
                "testTag06", "testTag07", "testTag08", "testTag09", "testTag10",
                "testTag11", "testTag12", "testTag13", "testTag14", "testTag15",
                "testTag16", "testTag17", "testTag18", "testTag19", "testTag20"};
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
                    logger.debug(Thread.currentThread() + " - About to start tagging for " + tag);

                    // Do the updates
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    RetryingTransactionCallback<Void> txnCallback = new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                            taggingService.addTag(folder, tag);
                            taggingService.addTag(subFolder, tag);
                            taggingService.addTag(subDocument, tag);
                            logger.debug(Thread.currentThread() + " - Tagging for " + tag);
                            return null;
                        }
                    };
                    try
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(txnCallback);
                    }
                    catch (Throwable e)
                    {
                        logger.error("Tagging failed: " + e);
                    }
                    logger.debug(Thread.currentThread() + " - Done tagging for " + tag);

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
        logger.info("Releasing tagging threads");
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
        logger.info("All threads should have finished");

        // Have a brief pause, while we wait for their related
        //  async actions to kick off
        Thread.sleep(150);

        // Wait until we've had 20 async tagging actions run (One per Thread)
        // Not all of the actions will do something, but we just need to
        //  wait for all of them!
        // As a backup check, also ensure that the action tracking service
        //  shows none of them running either
        for (int i = 0; i < 600; i++)
        {
            try
            {
                if(asyncOccurs.wantedActionsCount < tags.length)
                {
                    if(i%50 == 0)
                    {
                        logger.info("Done " + asyncOccurs.wantedActionsCount + " of " + tags.length);
                    }
                    Thread.sleep(100);
                    continue;
                }
                if (actionTrackingService.getAllExecutingActions().size() > 0)
                {
                    if(i%50 == 0)
                    {
                        List<ExecutionSummary> actions = actionTrackingService.getAllExecutingActions();
                        logger.info("Waiting on " + actions.size() + " actions: " + actions);
                    }
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
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TagScope ts1 = taggingService.findTagScope(folder);
                TagScope ts2 = taggingService.findTagScope(subFolder);
                assertEquals(
                        "Wrong tags on folder tagscope: " + ts1.getTags(),
                        tags.length, ts1.getTags().size()
                );
                assertEquals(
                        "Wrong tags on subfolder tagscope: " + ts2.getTags(),
                        tags.length, ts2.getTags().size()
                );

                // Each tag should crop up 3 times on the folder
                // and twice for the subfolder
                for (String tag : tags)
                {
                    assertEquals(3, ts1.getTag(tag.toLowerCase()).getCount());
                    assertEquals(2, ts2.getTag(tag.toLowerCase()).getCount());
                }

                // All done
                return null;
            }
        });
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

        public <T> T awaitExecution(RetryingTransactionCallback<T> callback) throws Exception
        {
            T returnVal = transactionService.getRetryingTransactionHelper().doInTransaction(callback);

            synchronized (waitForExecutionLock) {

                // Always wait 100ms
                waitForExecutionLock.wait(100);

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
            for(int i=0; i<100; i++)
            {
                if( actionTrackingService.getExecutingActions(ACTION_TYPE).size() == 0 )
                {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {}
            }
            if (actionTrackingService.getExecutingActions(ACTION_TYPE).size() > 0)
            {
                System.err.println("Warning - not all actions finished");
            }

            return returnVal;
        }
    }

    /**
     * Test for https://https://issues.alfresco.com/jira/browse/ALF-17260.
     *
     * When the audit queue for the tagging service contains more than 100 entries which aren't update,
     * {@link UpdateTagScopesActionExecuter} was failing to update the tag scope of containers correctly.
     *
     * @throws Exception
     */
    @Test
    public void testALF_17260() throws Exception
    {
        // Add tag scope to our container
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                // Add some tag scopes
                taggingService.addTagScope(folder);
                return null;
            }
        });

        // Generate ~1800 audit entries, none of which are updates, as determined by UpdateTagScopesActionExecuter
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (int j = 0; j < 10; j++)
                {
                    final int k = j;
                    RetryingTransactionCallback<NodeRef> createFoldersAndTags = new RetryingTransactionCallback<NodeRef>()
                    {
                        @Override
                        public NodeRef execute() throws Throwable
                        {
                            NodeRef folder1 = fileFolderService.create(folder, "Test1", ContentModel.TYPE_FOLDER).getNodeRef();
                            NodeRef folder2 = fileFolderService.create(folder, "Test2", ContentModel.TYPE_FOLDER).getNodeRef();

                            for (int i = 0; i < 102; i++)
                            {
                                NodeRef file = fileFolderService.create(folder1, k + "_test_" + i + ".text", ContentModel.TYPE_CONTENT).getNodeRef();
                                taggingService.addTag(file, k + "testTag" + i);
                            }

                            List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(folder1);
                            for (ChildAssociationRef childRef : childRefs)
                            {
                                taggingService.clearTags(childRef.getChildRef());
                            }
                            QName assocQName = nodeService.getPrimaryParent(folder1).getTypeQName();
                            nodeService.moveNode(folder1, folder2, ContentModel.ASSOC_CONTAINS, assocQName);
                            return folder2;
                        }
                    };

                    NodeRef folder2 = transactionService.getRetryingTransactionHelper().doInTransaction(createFoldersAndTags);
                    nodeService.deleteNode(folder2);
                }

                return null;
            }
        });

        // The tag scope of our container should be empty at this stage.
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // re get the tag scopes
                TagScope ts1 = taggingService.findTagScope(folder);

                // Check that the tag scopes got populated
                assertEquals("Wrong tags on sub folder: " + ts1.getTags(), 0, ts1.getTags().size());
                return null;
            }
        });

        // Add some tags which should update the tag scope.
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                taggingService.addTag(subDocument, TAG_1); // folder+subfolder
                return null;
            }
        });

        // Prior to fixing ALF-17260, the tag scope of our container wasn't being updated correctly. These
        // assertions were failing.
        asyncOccurs.awaitExecution(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // re get the tag scopes
                TagScope ts1 = taggingService.findTagScope(folder);

                // Check that the tag scopes got populated
                assertEquals("Wrong tags on folder: " + ts1.getTags(), 1, ts1.getTags().size());
                assertEquals("Wrong tag name", TAG_1, ts1.getTags().get(0).getName());
                assertEquals("Wrong number of documents", 1, ts1.getTags().get(0).getCount());
                return null;
            }
        });
    }

    @Test
    public void testTagFileRead() throws UnsupportedEncodingException
    {
        List<TagDetails> tags = TaggingServiceImpl.readTagDetails(new ByteArrayInputStream("Tag1|10\nTag2|20\nInvalid\nInvalid2|\nInvalid3|One\nTooMany|1|2\n\n".getBytes("UTF-8")));
        assertEquals(3, tags.size());
        assertEquals(tags.get(0).getName(), "Tag1");
        assertEquals(tags.get(1).getName(), "Tag2");
        assertEquals(tags.get(2).getName(), "TooMany");
        assertEquals(tags.get(0).getCount(), 10);
        assertEquals(tags.get(1).getCount(), 20);
        assertEquals(tags.get(2).getCount(), 1);
    }

    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void testPagedTags() throws UnsupportedEncodingException
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        Pair<List<String>, Integer> tags = taggingService.getPagedTags(storeRef, 1, 10);
        assertNotNull(tags);
        PagingResults<Pair<NodeRef, String>> res = taggingService.getTags(storeRef, new PagingRequest(10));
        assertNotNull(res);


        String guid = GUID.generate();
        // Create a node
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        String docName = "testDocument" + guid + ".txt";
        docProps.put(ContentModel.PROP_NAME, docName);
        NodeRef myDoc = nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, docName),
                ContentModel.TYPE_CONTENT,
                docProps).getChildRef();
        taggingService.addTag(myDoc,"dog");

        res = taggingService.getTags(myDoc, new PagingRequest(10));
        assertNotNull(res);
        assertTrue(res.getTotalResultCount().getFirst() == 1);
    }

    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void testChangeTags() throws UnsupportedEncodingException
    {
        try
        {
            taggingService.changeTag(storeRef, null, null);
            fail("Should throw exception");
        }
        catch (TaggingException tae)
        {
            tae.getMessage().contains("Existing tag cannot be null");
        }

        try
        {
            taggingService.changeTag(storeRef, "bob", null);
            fail("Should throw exception");
        }
        catch (TaggingException tae)
        {
            tae.getMessage().contains("New tag cannot be null");
        }

        try
        {
            taggingService.changeTag(storeRef, "bob", "bob");
            fail("Should throw exception");
        }
        catch (TaggingException tae)
        {
            tae.getMessage().contains("New and existing tags are the same");
        }

        try
        {
            taggingService.changeTag(storeRef, "bob", "hope");
            fail("Should throw exception");
        }
        catch (NonExistentTagException net)
        {
            net.getMessage().contains("not found");
        }

        try
        {
            List<String> storeTags = taggingService.getTags(storeRef);
            assertNotNull(storeTags);
            assertTrue(storeTags.size()>1);
            taggingService.changeTag(storeRef, storeTags.get(0), storeTags.get(1));
            fail("Should throw exception");
        }
        catch (TagExistsException tee)
        {
            tee.getMessage().contains("already exists");
        }
    }


    /* Test adding tags containing \n and | chars. Test all ways to create tag (e.g. createTag, addTag, setTags) */
    @Test
    public void testBadTags()
    {
        testTag(BAD_TAG);
        testTag(BAD_TAG2);
    }

    private void testTag(final String tag)
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    taggingService.createTag(storeRef, tag);
                    fail();
                }
                catch(IllegalArgumentException iae)
                {
                    //
                }

                try
                {
                    taggingService.addTag(document, tag);
                    fail();
                }
                catch(IllegalArgumentException iae)
                {
                    //
                }

                try
                {
                    List<String> setTags = new ArrayList<String>(2);
                    setTags.add(tag);
                    taggingService.setTags(document, setTags);

                    fail();
                }
                catch(IllegalArgumentException iae)
                {
                    //
                }

                return null;
            }
        });
    }

    /**
     * Tests that when the deleteTag() method runs, it will remove invalid references to the deleted tag.
     */
    @Test
    @Category({RedundantTests.class,LuceneTests.class})
    public void testDeleteTag() throws Exception{

        try{
            // We instruct the 'nodeRefPropInterceptor' to skip processing on the 'get' methods.
            // This is needed because this interceptor removes any properties which are invalid. e.g. have been deleted.
            // We need to make sure that the 'taggable' property stays put.
            nodeRefPropInterceptor.setFilterOnGet(false);

            this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

                @SuppressWarnings("unchecked")
                @Override
                public Void execute() throws Throwable
                {
                    taggingService.clearTags(folder);
                    // addTag uses lucene to get a reference to the existing TAG_1 tag node.
                    // this fails without lucene
                    taggingService.addTag(folder, TAG_1);

                    // The deleteTag() should remove any reference to the deleted tag
                    List<NodeRef> taggableProperty = (List<NodeRef>) nodeService.getProperty(folder, ContentModel.PROP_TAGS);
                    assertTrue("Our folder should have a reference on one tag.", taggableProperty.size() == 1);

                    taggingService.deleteTag(TaggingServiceImplTest.storeRef, TAG_1);

                    // The deleteTag() should remove any reference to the deleted tag
                    taggableProperty = (List<NodeRef>) nodeService.getProperty(folder, ContentModel.PROP_TAGS);
                    assertTrue("Our folder shouldn't have any references left to deleted tags.", taggableProperty.size() == 0);

                    return null;
                }
            });
        } finally{
            nodeRefPropInterceptor.setFilterOnGet(true);
        }
    }

    /**
     * ALF-21875
     */
    @Test
    public void testTagScopeIfUrlNull()
    {

        TagScopePropertyMethodInterceptor.setEnabled(Boolean.TRUE);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        String siteName = GUID.generate();

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public Void execute() throws Throwable
            {

                // STEP1 CreateSite
                SiteInfo siteInfo = createSite(siteName, "doclib", SiteVisibility.PUBLIC);

                // STEP2 upload a document in documentLibrary
                NodeRef documentLibraryOfSite = siteService.getContainer(siteInfo.getShortName(), "doclib");

                NodeRef containerTagScope = fileFolderService
                        .create(documentLibraryOfSite, "containerTagScope" + GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();

                // STEP4 create tag
                String tagName = GUID.generate();
                taggingService.createTag(storeRef, tagName);

                // Add some tag scopes
                taggingService.addTagScope(containerTagScope);

                NodeRef file = fileFolderService.create(containerTagScope, "_test_" + GUID.generate() + ".text", ContentModel.TYPE_CONTENT)
                        .getNodeRef();
                taggingService.addTag(file, tagName);
                fileFolderService.delete(file);

                return null;
            }
        });

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {

            @SuppressWarnings("unchecked")
            @Override
            public Void execute() throws Throwable
            {

                // STEP5 start job taggingStartupJobDetail
                // Fire off the quartz bean, this time it can really work
                final UpdateTagScopesActionExecuter updateTagsAction = (UpdateTagScopesActionExecuter) applicationContext.getBean("update-tagscope");
                UpdateTagScopesQuartzJob job = new UpdateTagScopesQuartzJob();
                job.execute(actionService, updateTagsAction);

                return null;
            }
        });

        // STEP6 execute script
        // Create a model to pass to the unit test scripts
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("customSiteName", siteName);

        // Execute the unit test script
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/site/script/test_tagScopeALF_21875.js");
        this.scriptService.executeScript(location, model);

        TagScopePropertyMethodInterceptor.setEnabled(Boolean.FALSE);

    }

    private SiteInfo createSite(String siteShortName, String componentId, SiteVisibility visibility)
    {
        // Create a public site
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, visibility);
        this.siteService.createContainer(siteShortName, componentId, ContentModel.TYPE_FOLDER, null);
        return siteInfo;
    }

}