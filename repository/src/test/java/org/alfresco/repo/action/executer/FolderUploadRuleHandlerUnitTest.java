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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.ASPECT_RULE_COPY_TRACKING;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.ASPECT_RULE_MOVE_TRACKING;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_COPY_DESTINATION;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_COPY_LAST_ACTIVITY;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_COPY_OWNER;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_ACTION_ID;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_LAST_ACTIVITY;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_OWNER;
import static org.alfresco.repo.action.executer.FolderUploadRuleHandler.PROP_RULE_MOVE_SOURCE_PARENT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Unit tests for {@link FolderUploadRuleHandler}.
 */
public class FolderUploadRuleHandlerUnitTest
{
    private static final String USER = "alice";
    private static final String ACTION_ID = "move-action-id";

    private final NodeService nodeService = mock(NodeService.class);
    private final FileFolderService fileFolderService = mock(FileFolderService.class);
    private final DictionaryService dictionaryService = mock(DictionaryService.class);
    private final BehaviourFilter behaviourFilter = mock(BehaviourFilter.class);
    private final Action ruleAction = mock(Action.class);

    private final NodeRef actionNode = node("action");
    private final NodeRef compositeNode = node("composite");
    private final NodeRef ruleNode = node("rule");
    private final NodeRef src = node("src");
    private final NodeRef dst = node("dst");
    private final NodeRef uploadedFolder = node("src-build");
    private final NodeRef movedFolder = node("dst-build");

    private FolderUploadRuleHandler merge;

    @Before
    public void setUp() throws Exception
    {
        new AuthenticationUtil().afterPropertiesSet();
        AuthenticationUtil.setFullyAuthenticatedUser(USER);

        merge = new FolderUploadRuleHandler();
        merge.setNodeService(nodeService);
        merge.setFileFolderService(fileFolderService);
        merge.setDictionaryService(dictionaryService);
        merge.setPolicyComponent(mock(PolicyComponent.class));
        merge.setBehaviourFilter(behaviourFilter);

        when(nodeService.exists(any(NodeRef.class))).thenReturn(true);
        when(dictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(dictionaryService.isSubClass(ContentModel.TYPE_CONTENT, ContentModel.TYPE_FOLDER)).thenReturn(false);

        // Share stores the move action inside a group of actions belonging to the rule.
        when(ruleAction.getNodeRef()).thenReturn(actionNode);
        when(ruleAction.getId()).thenReturn(ACTION_ID);
        parent(actionNode, compositeNode);
        parent(compositeNode, ruleNode);
        when(nodeService.getType(compositeNode)).thenReturn(ActionModel.TYPE_COMPOSITE_ACTION);
        when(nodeService.getType(ruleNode)).thenReturn(RuleModel.TYPE_RULE);

        folder(uploadedFolder, "build");
        folder(movedFolder, "build");
        when(nodeService.getChildByName(dst, ContentModel.ASSOC_CONTAINS, "build")).thenReturn(movedFolder);
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            // Clear the transaction data so the next test starts fresh.
            TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // Move the next uploaded file into the folder that this rule already moved.
    @Test
    public void mergesIntoFolderMovedBySameRule() throws Exception
    {
        movedMarker(src, ACTION_ID, USER, new Date());
        NodeRef file = file(node("file2"), "file2.txt");
        children(uploadedFolder, file);
        when(nodeService.getChildAssocs(uploadedFolder)).thenReturn(new ArrayList<>());

        assertTrue(merge.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verify(fileFolderService).move(file, movedFolder, null);
        verify(nodeService).deleteNode(uploadedFolder);
        verify(nodeService).addAspect(eq(movedFolder), eq(ASPECT_RULE_MOVE_TRACKING), anyMap());
    }

    // Combine matching subfolders and rename a.txt to a-1.txt when a.txt already exists.
    @Test
    public void mergesSubfoldersAndRenamesClashingFiles() throws Exception
    {
        movedMarker(src, ACTION_ID, USER, new Date());
        NodeRef sub = node("src-sub");
        NodeRef existingSub = node("dst-sub");
        folder(sub, "sub");
        folder(existingSub, "sub");
        NodeRef clash = file(node("clash"), "a.txt");
        children(uploadedFolder, sub);
        children(sub, clash);
        when(nodeService.getChildByName(movedFolder, ContentModel.ASSOC_CONTAINS, "sub")).thenReturn(existingSub);
        when(nodeService.getChildByName(existingSub, ContentModel.ASSOC_CONTAINS, "a.txt")).thenReturn(node("existing-a"));
        when(nodeService.getChildAssocs(sub)).thenReturn(new ArrayList<>());
        when(nodeService.getChildAssocs(uploadedFolder)).thenReturn(new ArrayList<>());

        assertTrue(merge.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verify(fileFolderService).move(clash, existingSub, "a-1.txt");
        verify(nodeService).deleteNode(sub);
        verify(nodeService).deleteNode(uploadedFolder);
    }

    // Do not merge into a folder moved for a different user or rule action.
    @Test
    public void doesNotMergeForAnotherUserOrRule() throws Exception
    {
        when(nodeService.hasAspect(movedFolder, ASPECT_RULE_MOVE_TRACKING)).thenReturn(true);
        when(nodeService.getProperties(movedFolder)).thenReturn(
                movedProperties(src, ACTION_ID, "bob", new Date()),
                movedProperties(src, "another-action", USER, new Date()));

        assertFalse(merge.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));
        assertFalse(merge.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verifyNoMoves();
    }

    // After 31 minutes, skip the merge and remove the expired move details.
    @Test
    public void doesNotMergeAfterWindow() throws Exception
    {
        movedMarker(src, ACTION_ID, USER, new Date(System.currentTimeMillis() - 31 * 60_000L));
        assertFalse(merge.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));
        verifyNoMoves();
        verify(nodeService).removeAspect(movedFolder, ASPECT_RULE_MOVE_TRACKING);
    }

    @Test
    public void upgradesLegacyMoveTracking() throws Exception
    {
        QName legacyAspect = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoved");
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedFrom"), src);
        properties.put(QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedAction"), ACTION_ID);
        properties.put(QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedUser"), USER);
        properties.put(QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedAt"), new Date());
        when(nodeService.hasAspect(movedFolder, legacyAspect)).thenReturn(true);
        when(nodeService.getProperties(movedFolder)).thenReturn(properties);
        children(uploadedFolder);

        assertTrue(merge.mergeIntoPreviouslyMovedFolder(ruleAction, uploadedFolder, src, dst));

        verify(nodeService).removeAspect(movedFolder, legacyAspect);
    }

    // Store the copy location on the original folder and its subfolders.
    @Test
    public void marksCopiedFolderAndItsSubfolders()
    {
        NodeRef copy = node("copy");
        NodeRef sub = node("sub");
        NodeRef subCopy = node("sub-copy");
        folder(sub, "sub");
        children(uploadedFolder, sub, file(node("f"), "f.txt"));
        children(sub);
        when(nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, "sub")).thenReturn(subCopy);

        merge.trackCopiedFolder(ruleAction, uploadedFolder, copy);

        verify(nodeService).addAspect(eq(uploadedFolder), eq(ASPECT_RULE_COPY_TRACKING), anyMap());
        verify(nodeService).addAspect(eq(sub), eq(ASPECT_RULE_COPY_TRACKING), anyMap());
    }

    // Copy a later file at the end of the transaction, not when it is first created.
    @Test
    public void copiesNewChildIntoCopyAtCommit() throws Exception
    {
        NodeRef copy = node("copy");
        copyMarker(uploadedFolder, copy, USER, new Date());
        NodeRef file = file(node("file2"), "file2.txt");
        ChildAssociationRef assoc = parent(file, uploadedFolder);
        parent(uploadedFolder, src);
        FileInfo copied = mock(FileInfo.class);
        when(copied.getNodeRef()).thenReturn(node("file2-copy"));
        when(fileFolderService.copy(file, copy, "file2.txt")).thenReturn(copied);

        TransactionSynchronizationManager.initSynchronization();
        merge.onCreateChildAssociation(assoc, true);
        verifyNoCopies();
        TransactionSynchronizationUtils.triggerBeforeCommit(false);

        verify(fileFolderService).copy(file, copy, "file2.txt");
        verify(nodeService).setProperty(eq(uploadedFolder), eq(PROP_RULE_COPY_LAST_ACTIVITY), any(Date.class));
    }

    // Skip copying when a file with the same name already exists at the destination.
    @Test
    public void preservesExistingDestinationChildDuringDeepCopy() throws Exception
    {
        NodeRef copy = node("copy");
        copyMarker(uploadedFolder, copy, USER, new Date());
        NodeRef file = file(node("file2"), "file2.txt");
        ChildAssociationRef assoc = parent(file, uploadedFolder);
        parent(uploadedFolder, src);
        when(nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, "file2.txt")).thenReturn(node("existing"));

        TransactionSynchronizationManager.initSynchronization();
        merge.onCreateChildAssociation(assoc, true);
        TransactionSynchronizationUtils.triggerBeforeCommit(false);

        verify(nodeService).getChildByName(copy, ContentModel.ASSOC_CONTAINS, "file2.txt");
        verifyNoCopies();
    }

    private void verifyNoCopies() throws Exception
    {
        verify(fileFolderService, never()).copy(any(NodeRef.class), any(NodeRef.class), anyString());
    }

    private void verifyNoMoves() throws Exception
    {
        verify(fileFolderService, never()).move(any(NodeRef.class), any(NodeRef.class), isNull());
        verify(fileFolderService, never()).move(any(NodeRef.class), any(NodeRef.class), anyString());
        verify(nodeService, never()).deleteNode(any());
    }

    private void movedMarker(NodeRef from, String actionId, String user, Date at)
    {
        when(nodeService.hasAspect(movedFolder, ASPECT_RULE_MOVE_TRACKING)).thenReturn(true);
        when(nodeService.getProperties(movedFolder)).thenReturn(movedProperties(from, actionId, user, at));
    }

    private Map<QName, Serializable> movedProperties(NodeRef from, String actionId, String user, Date at)
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_RULE_MOVE_SOURCE_PARENT, from);
        props.put(PROP_RULE_MOVE_ACTION_ID, actionId);
        props.put(PROP_RULE_MOVE_OWNER, user);
        props.put(PROP_RULE_MOVE_LAST_ACTIVITY, at);
        return props;
    }

    private void copyMarker(NodeRef folder, NodeRef target, String user, Date at)
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_RULE_COPY_DESTINATION, target);
        props.put(PROP_RULE_COPY_OWNER, user);
        props.put(PROP_RULE_COPY_LAST_ACTIVITY, at);
        when(nodeService.hasAspect(folder, ASPECT_RULE_COPY_TRACKING)).thenReturn(true);
        when(nodeService.getProperties(folder)).thenReturn(props);
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
        List<ChildAssociationRef> assocs = new ArrayList<>();
        for (NodeRef child : children)
        {
            assocs.add(assoc(parent, child));
        }
        when(nodeService.getChildAssocs(parent, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL)).thenReturn(assocs);
    }

    private ChildAssociationRef parent(NodeRef child, NodeRef parent)
    {
        ChildAssociationRef assoc = assoc(parent, child);
        when(nodeService.getPrimaryParent(child)).thenReturn(assoc);
        return assoc;
    }

    private static ChildAssociationRef assoc(NodeRef parent, NodeRef child)
    {
        return new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parent,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, child.getId()), child, true, -1);
    }

    private static NodeRef node(String id)
    {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }
}
