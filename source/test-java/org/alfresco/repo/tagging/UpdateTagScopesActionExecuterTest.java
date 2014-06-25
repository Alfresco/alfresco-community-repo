/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test for {@link UpdateTagScopesActionExecuter}
 * 
 * @author Dmitry Velichkevich
 */
public class UpdateTagScopesActionExecuterTest extends TestCase
{
    private static final int TAGSCOPE_LAYERS = 3;

    private static final int TEST_TAGS_AMOUNT = 3;

    private static final int TEST_DOCUMENTS_AMOUNT = 3;


    private static final String ACTION_TRACKING_SERVICE_BEAN_NAME = "actionTrackingService";

    private static final String UPDATE_TAGSCOPE_ACTION_EXECUTER_BEAN_NAME = "update-tagscope";

    private static final String TEST_TAG_NAME_PATTERN = "testTag%d-%d-%d";

    private static final String TEST_FOLDER_NAME_PATTERN = "TestFolder-%d";

    private static final String TEST_DOCUMENT_NAME_PATTERN = "InFolder-%d-TestDocument-%d.txt";


    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;

    private ActionService actionService;

    private TaggingService taggingService;

    private FileFolderService fileFolderService;

    private TransactionService transactionService;

    private UpdateTagScopesActionExecuter actionExecuter;

    private ActionTrackingService actionTrackingService;

    private UserTransaction transaction;

    private List<NodeRef> expectedTagScopes;

    private List<String> testTags;

    @Before
    @Override
    public void setUp() throws Exception
    {
        final ServiceRegistry registry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);

        nodeService = registry.getNodeService();
        actionService = registry.getActionService();
        actionExecuter = (UpdateTagScopesActionExecuter) applicationContext.getBean(UPDATE_TAGSCOPE_ACTION_EXECUTER_BEAN_NAME);
        taggingService = registry.getTaggingService();
        fileFolderService = registry.getFileFolderService();
        transactionService = registry.getTransactionService();
        actionTrackingService = (ActionTrackingService) applicationContext.getBean(ACTION_TRACKING_SERVICE_BEAN_NAME);

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        expectedTagScopes = new LinkedList<NodeRef>();
        testTags = new LinkedList<String>();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                createTestContent(registry, expectedTagScopes);
                return null;
            }
        }, false, true);

        waitForTagScopeUpdate();

        transaction = transactionService.getUserTransaction();
        transaction.begin();
    }

    /**
     * Creates simple hierarchy with documents tagged on the first layer only
     * 
     * @param registry - {@link ServiceRegistry} instance
     * @param createdTagScopes - {@link List}&lt;{@link NodeRef}&gt; instance which contains all tag scope folders
     */
    private void createTestContent(ServiceRegistry registry, List<NodeRef> createdTagScopes)
    {
        NodeRef rootNode = registry.getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);

        NodeRef currentParent = rootNode;
        for (int i = 0; i < TAGSCOPE_LAYERS; i++)
        {
            FileInfo newFolder = fileFolderService.create(currentParent, String.format(TEST_FOLDER_NAME_PATTERN, i), ContentModel.TYPE_FOLDER);
            currentParent = newFolder.getNodeRef();

            if (null != createdTagScopes)
            {
                createdTagScopes.add(currentParent);
            }

            nodeService.addAspect(currentParent, ContentModel.ASPECT_TAGSCOPE, null);

            for (int j = 0; j < TEST_DOCUMENTS_AMOUNT; j++)
            {
                FileInfo newDocument = fileFolderService.create(currentParent, String.format(TEST_DOCUMENT_NAME_PATTERN, i, j), ContentModel.TYPE_CONTENT);
                nodeService.addAspect(newDocument.getNodeRef(), ContentModel.ASPECT_TAGGABLE, null);

                if (0 == i)
                {
                    for (int k = 0; k < TEST_TAGS_AMOUNT; k++)
                    {
                        String tagName = String.format(TEST_TAG_NAME_PATTERN, k, j, i);
                        testTags.add(tagName);
                        taggingService.addTag(newDocument.getNodeRef(), tagName);
                    }
                }
            }
        }
    }

    private void waitForTagScopeUpdate() throws Exception
    {
        List<ExecutionSummary> executingActions = null;

        do
        {
            synchronized (this)
            {
                wait(1000);
            }

            executingActions = actionTrackingService.getExecutingActions(UpdateTagScopesActionExecuter.NAME);
        } while (!executingActions.isEmpty());
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        final NodeRef rootTestFolder = expectedTagScopes.iterator().next();

        for (String tagName : testTags)
        {
            taggingService.deleteTag(rootTestFolder.getStoreRef(), tagName);
        }

        testTags.clear();
        testTags = null;

        if (Status.STATUS_ROLLEDBACK != transaction.getStatus())
        {
            transaction.rollback();
        }

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(rootTestFolder);
                return null;
            }
        }, false, true);

        AuthenticationUtil.clearCurrentSecurityContext();

        expectedTagScopes.clear();
        expectedTagScopes = null;
    }

    /**
     * Tests that tag scopes are properly updated. Cache on the first layer MUST NOT be empty. All other tag scopes MUST BE null
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleTagScopesUpsdate() throws Exception
    {
        Action tagScopeUpdateAction = actionService.createAction(UpdateTagScopesActionExecuter.NAME);
        tagScopeUpdateAction.setParameterValue(UpdateTagScopesActionExecuter.PARAM_TAG_SCOPES, (Serializable) expectedTagScopes);
        actionExecuter.execute(tagScopeUpdateAction, null);

        Iterator<NodeRef> iterator = expectedTagScopes.iterator();
        assertTrue(iterator.hasNext());

        NodeRef taggedTagScope = iterator.next();
        assertNotNull(taggedTagScope);

        ContentData contentData = getTagScopeCacheContentDataProperty(taggedTagScope);
        assertNotNull(contentData);
        assertTrue(contentData.getSize() > 0L);

        assertTrue(iterator.hasNext());

        for (NodeRef tagScopeFolder = iterator.next(); iterator.hasNext(); tagScopeFolder = iterator.next())
        {
            assertNotNull(tagScopeFolder);
            contentData = getTagScopeCacheContentDataProperty(tagScopeFolder);
            assertNull(contentData);
        }
    }

    /**
     * <a href="https://issues.alfresco.com/jira/browse/ACE-1979">ACE-1979</a>: tag scope cache must be emptied when tag scope doesn't contain tags anymore. The fix nullifies
     * content data property for the tag scope cache. This approach allows avoiding immediate update in content store and postponing it untill content store cleaner job is executed
     * 
     * @throws Exception
     */
    @Test
    public void testTagScopesUpdateWhenTagsAreRemoved() throws Exception
    {
        Action tagScopeUpdateAction = actionService.createAction(UpdateTagScopesActionExecuter.NAME);
        tagScopeUpdateAction.setParameterValue(UpdateTagScopesActionExecuter.PARAM_TAG_SCOPES, (Serializable) expectedTagScopes);
        actionExecuter.execute(tagScopeUpdateAction, null);

        waitForTagScopeUpdate();

        final NodeRef taggedTagScope = expectedTagScopes.iterator().next();
        assertNotNull(taggedTagScope);

        ContentData contentData = getTagScopeCacheContentDataProperty(taggedTagScope);
        assertNotNull(contentData);
        actionTrackingService.getExecutingActions(UpdateTagScopesActionExecuter.NAME);
        assertTrue(contentData.getSize() > 0L);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (ChildAssociationRef child : nodeService.getChildAssocs(taggedTagScope, Collections.singleton(ContentModel.TYPE_CONTENT)))
                {
                    taggingService.removeTags(child.getChildRef(), testTags);
                }

                return null;
            }
        }, false, true);

        waitForTagScopeUpdate();

        actionExecuter.execute(tagScopeUpdateAction, null);

        for (NodeRef tagScopeFolder : expectedTagScopes)
        {
            assertNotNull(tagScopeFolder);
            contentData = getTagScopeCacheContentDataProperty(tagScopeFolder);
            assertNull(contentData);
        }
    }

    /**
     * @param nodeRef - {@link NodeRef} instance which represents tag scope folder
     * @return {@link ContentModel#PROP_TAGSCOPE_CACHE} {@link ContentData} property instance for the given <code>nodeRef</code> or
     */
    private ContentData getTagScopeCacheContentDataProperty(final NodeRef nodeRef)
    {
        ContentData result = null;

        Serializable contentProperty = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Serializable>()
        {
            @Override
            public Serializable execute() throws Throwable
            {
                return nodeService.getProperty(nodeRef, ContentModel.PROP_TAGSCOPE_CACHE);
            }
        }, false, true);

        if (contentProperty instanceof ContentData)
        {
            result = (ContentData) contentProperty;
        }

        return result;
    }
}
