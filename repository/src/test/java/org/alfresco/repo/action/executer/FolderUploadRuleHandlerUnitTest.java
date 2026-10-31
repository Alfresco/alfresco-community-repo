/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.ASPECT_RULE_COPY_TRACKING;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.ASPECT_RULE_MOVE_TRACKING;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_COPY_DESTINATIONS;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_COPY_LAST_ACTIVITY;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_ACTION_ID;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_LAST_ACTIVITY;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_SOURCE_PARENT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class FolderUploadRuleHandlerUnitTest
{
    private static final String ACTION_ID = "move-action-id";
    private static final Date NOW = new Date();
    private static final Date EXPIRED = new Date(System.currentTimeMillis() - 31 * 60_000L);

    private final NodeService nodeService = mock(NodeService.class);
    private final FileFolderService fileFolderService = mock(FileFolderService.class);
    private final DictionaryService dictionaryService = mock(DictionaryService.class);
    private final RuntimeRuleService ruleService = mock(RuntimeRuleService.class);
    private final Action ruleAction = mock(Action.class);

    private final NodeRef ruleNode = node("rule");
    private final NodeRef src = node("src");
    private final NodeRef dst = node("dst");
    private final NodeRef uploadedFolder = node("src-build");
    private final NodeRef movedFolder = node("dst-build");

    private FolderUploadRuleHandler handler;

    @Before
    public void setUp() throws Exception
    {
        new AuthenticationUtil().afterPropertiesSet();
        AuthenticationUtil.setFullyAuthenticatedUser("alice");
        TransactionSynchronizationManager.initSynchronization();

        handler = new FolderUploadRuleHandler();
        handler.setNodeService(nodeService);
        handler.setFileFolderService(fileFolderService);
        handler.setDictionaryService(dictionaryService);
        handler.setPolicyComponent(mock(PolicyComponent.class));
        handler.setBehaviourFilter(mock(BehaviourFilter.class));
        handler.setRuleService(ruleService);

        when(nodeService.exists(any(NodeRef.class))).thenReturn(true);
        when(dictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);

        // Share saves a rule's action inside a composite action.
        NodeRef actionNode = node("action");
        NodeRef compositeNode = node("composite");
        when(ruleAction.getNodeRef()).thenReturn(actionNode);
        when(ruleAction.getId()).thenReturn(ACTION_ID);
        parent(actionNode, compositeNode);
        parent(compositeNode, ruleNode);
        when(nodeService.getType(compositeNode)).thenReturn(ActionModel.TYPE_COMPOSITE_ACTION);
        when(nodeService.getType(ruleNode)).thenReturn(RuleModel.TYPE_RULE);

        folder(uploadedFolder, "build");
        folder(movedFolder, "build");
        parent(uploadedFolder, src);
        when(nodeService.getChildByName(dst, ContentModel.ASSOC_CONTAINS, "build")).thenReturn(movedFolder);
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    public void mergesIntoFolderMovedBySameRule() throws Exception
    {
        movedMark(ACTION_ID, NOW);
        NodeRef file = file(node("file2"), "file2.txt");
        children(uploadedFolder, file);

        assertTrue("The late folder should be merged", handler.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verify(fileFolderService).move(file, movedFolder, null);
        verify(nodeService).deleteNode(uploadedFolder);
        verify(nodeService).addAspect(eq(movedFolder), eq(ASPECT_RULE_MOVE_TRACKING), anyMap());

        // The rule's next action must run on the folder that still exists.
        RuntimeActionService actionService = mock(RuntimeActionService.class);
        CompositeAction composite = mock(CompositeAction.class);
        Action nextAction = mock(Action.class);
        when(composite.getActions()).thenReturn(List.of(nextAction));
        CompositeActionExecuter executer = new CompositeActionExecuter();
        executer.setActionService(actionService);
        executer.executeImpl(composite, uploadedFolder);
        verify(actionService).directActionExecution(nextAction, movedFolder);
    }

    @Test
    public void mergesSubfoldersAndRenamesClashingFiles() throws Exception
    {
        movedMark(ACTION_ID, NOW);
        NodeRef sub = folder(node("src-sub"), "sub");
        NodeRef existingSub = folder(node("dst-sub"), "sub");
        NodeRef clash = file(node("clash"), "a.txt");
        children(uploadedFolder, sub);
        children(sub, clash);
        when(nodeService.getChildByName(movedFolder, ContentModel.ASSOC_CONTAINS, "sub")).thenReturn(existingSub);
        when(nodeService.getChildByName(existingSub, ContentModel.ASSOC_CONTAINS, "a.txt")).thenReturn(node("existing-a"));

        assertTrue("The late folder should be merged", handler.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verify(fileFolderService).move(clash, existingSub, "a-1.txt");
        verify(nodeService).deleteNode(sub);
        verify(nodeService).deleteNode(uploadedFolder);
    }

    @Test
    public void doesNotMergeForAnotherRuleOrAfterWindow() throws Exception
    {
        movedMark("another-action", NOW);
        assertFalse("Another rule's folder must not be reused", handler.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        movedMark(ACTION_ID, EXPIRED);
        assertFalse("An expired mark must be ignored", handler.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verify(fileFolderService, never()).move(any(NodeRef.class), any(NodeRef.class), isNull());
        verify(nodeService, never()).deleteNode(any());
    }

    @Test
    public void ignoresBackgroundRules()
    {
        when(nodeService.getProperty(ruleNode, RuleModel.PROP_EXECUTE_ASYNC)).thenReturn(true);

        handler.markRuleMovedFolder(ruleAction, movedFolder, src);
        handler.trackCopiedFolder(ruleAction, uploadedFolder, node("copy"));

        verify(nodeService, never()).addAspect(any(), any(), anyMap());
    }

    @Test
    public void marksCopiedFolderAndItsSubfoldersWithEveryCopy()
    {
        NodeRef copy = node("copy");
        NodeRef secondCopy = node("second-copy");
        NodeRef sub = folder(node("sub"), "sub");
        children(uploadedFolder, sub);
        when(nodeService.getChildByName(secondCopy, ContentModel.ASSOC_CONTAINS, "sub")).thenReturn(node("sub-copy"));
        copyMark(uploadedFolder, NOW, copy);

        handler.trackCopiedFolder(ruleAction, uploadedFolder, secondCopy);

        verify(nodeService).addAspect(eq(uploadedFolder), eq(ASPECT_RULE_COPY_TRACKING),
                argThat(mark -> List.of(copy, secondCopy).equals(mark.get(PROP_RULE_COPY_DESTINATIONS))));
        verify(nodeService).addAspect(eq(sub), eq(ASPECT_RULE_COPY_TRACKING),
                argThat(mark -> List.of(node("sub-copy")).equals(mark.get(PROP_RULE_COPY_DESTINATIONS))));
    }

    @Test
    public void copiesLateFileIntoEveryCopyBeforeCommit() throws Exception
    {
        NodeRef copy = node("copy");
        NodeRef secondCopy = node("second-copy");
        copyMark(uploadedFolder, NOW, copy, secondCopy);
        NodeRef file = file(node("file2"), "file2.txt");
        ChildAssociationRef association = parent(file, uploadedFolder);
        FileInfo copied = mock(FileInfo.class);
        when(fileFolderService.copy(eq(file), any(NodeRef.class), eq("file2.txt"))).thenAnswer(invocation -> {
            assertEquals("Copies are made as the uploading user", "bob", AuthenticationUtil.getFullyAuthenticatedUser());
            return copied;
        });

        AuthenticationUtil.setFullyAuthenticatedUser("bob");
        handler.onCreateChildAssociation(association, true);
        verify(fileFolderService, never()).copy(any(NodeRef.class), any(NodeRef.class), anyString());
        TransactionSynchronizationUtils.triggerBeforeCommit(false);

        verify(fileFolderService).copy(file, copy, "file2.txt");
        verify(fileFolderService).copy(file, secondCopy, "file2.txt");
        verify(ruleService).executePendingRules();
        verify(nodeService).setProperty(eq(uploadedFolder), eq(PROP_RULE_COPY_LAST_ACTIVITY), any(Date.class));
    }

    @Test
    public void keepsExistingFileInCopy() throws Exception
    {
        NodeRef copy = node("copy");
        copyMark(uploadedFolder, NOW, copy);
        ChildAssociationRef association = parent(file(node("file2"), "file2.txt"), uploadedFolder);
        when(nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, "file2.txt")).thenReturn(node("existing"));

        handler.onCreateChildAssociation(association, true);
        TransactionSynchronizationUtils.triggerBeforeCommit(false);

        verifyNoCopies();
    }

    @Test
    public void stopsCopyingAfterWindow() throws Exception
    {
        copyMark(uploadedFolder, EXPIRED, node("copy"));
        ChildAssociationRef association = parent(file(node("file2"), "file2.txt"), uploadedFolder);

        handler.onCreateChildAssociation(association, true);
        TransactionSynchronizationUtils.triggerBeforeCommit(false);

        verifyNoCopies();
        verify(nodeService).removeAspect(uploadedFolder, ASPECT_RULE_COPY_TRACKING);
    }

    private void verifyNoCopies() throws FileNotFoundException, FileExistsException
    {
        verify(fileFolderService, never()).copy(any(NodeRef.class), any(NodeRef.class), anyString());
    }

    private void movedMark(String actionId, Date lastActivity)
    {
        when(nodeService.hasAspect(movedFolder, ASPECT_RULE_MOVE_TRACKING)).thenReturn(true);
        when(nodeService.getProperty(movedFolder, PROP_RULE_MOVE_SOURCE_PARENT)).thenReturn(src);
        when(nodeService.getProperty(movedFolder, PROP_RULE_MOVE_ACTION_ID)).thenReturn(actionId);
        when(nodeService.getProperty(movedFolder, PROP_RULE_MOVE_LAST_ACTIVITY)).thenReturn(lastActivity);
    }

    private void copyMark(NodeRef folder, Date lastActivity, NodeRef... copies)
    {
        when(nodeService.hasAspect(folder, ASPECT_RULE_COPY_TRACKING)).thenReturn(true);
        when(nodeService.getProperty(folder, PROP_RULE_COPY_DESTINATIONS)).thenAnswer(invocation -> new ArrayList<>(List.of(copies)));
        when(nodeService.getProperty(folder, PROP_RULE_COPY_LAST_ACTIVITY)).thenReturn(lastActivity);
    }

    private NodeRef folder(NodeRef node, String name)
    {
        when(nodeService.getType(node)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getProperty(node, ContentModel.PROP_NAME)).thenReturn(name);
        return node;
    }

    private NodeRef file(NodeRef node, String name)
    {
        when(nodeService.getType(node)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getProperty(node, ContentModel.PROP_NAME)).thenReturn(name);
        return node;
    }

    private void children(NodeRef parent, NodeRef... children)
    {
        List<ChildAssociationRef> associations = new ArrayList<>();
        for (NodeRef child : children)
        {
            associations.add(association(parent, child));
        }
        when(nodeService.getChildAssocs(parent, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL)).thenReturn(associations);
    }

    private ChildAssociationRef parent(NodeRef child, NodeRef parent)
    {
        ChildAssociationRef association = association(parent, child);
        when(nodeService.getPrimaryParent(child)).thenReturn(association);
        return association;
    }

    private static ChildAssociationRef association(NodeRef parent, NodeRef child)
    {
        return new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parent,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, child.getId()), child, true, -1);
    }

    private static NodeRef node(String id)
    {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }
}
