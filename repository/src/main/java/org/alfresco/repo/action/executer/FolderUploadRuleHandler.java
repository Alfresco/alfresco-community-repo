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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuleServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.transaction.TransactionListenerAdapter;

/**
 * Share and ADW upload a folder one file at a time, so a folder rule can run before all of the files have arrived. This class makes the late files follow the folder:
 * <ul>
 * <li>Move rule: a late file is moved into the folder that the rule already moved.</li>
 * <li>Deep copy rule: a late file is also copied into the folder's copy.</li>
 * </ul>
 * A moved or copied folder is marked with an aspect. The mark is ignored once no file has arrived for the configured number of minutes.
 */
@SuppressWarnings("PMD.GodClass")
public class FolderUploadRuleHandler implements NodeServicePolicies.OnCreateChildAssociationPolicy, CopyServicePolicies.OnCopyNodePolicy
{
    public static final QName ASPECT_RULE_MOVE_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveTracking");
    public static final QName PROP_RULE_MOVE_SOURCE_PARENT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveSourceParent");
    public static final QName PROP_RULE_MOVE_ACTION_ID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveActionId");
    public static final QName PROP_RULE_MOVE_LAST_ACTIVITY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveLastActivity");

    public static final QName ASPECT_RULE_COPY_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyTracking");
    public static final QName PROP_RULE_COPY_DESTINATIONS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyDestinations");
    public static final QName PROP_RULE_COPY_LAST_ACTIVITY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyLastActivity");

    private static final String KEY_NEW_CHILDREN = FolderUploadRuleHandler.class.getName() + ".newChildren";

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private RuleServiceImpl ruleService;
    private long windowMillis = 30 * 60_000L;

    private final TransactionListenerAdapter copyBeforeCommit = new TransactionListenerAdapter() {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            copyNewChildren();
        }
    };

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setRuleService(RuleServiceImpl ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setWindowMinutes(long windowMinutes)
    {
        this.windowMillis = windowMinutes * 60_000L;
    }

    public void init()
    {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ASPECT_RULE_COPY_TRACKING,
                ContentModel.ASSOC_CONTAINS, new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_COPY_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_MOVE_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * The marks belong to the original folder, so they are never copied.
     */
    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    // ----- Move rule -----

    /**
     * Marks a folder that a rule has just moved.
     */
    public void markRuleMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent)
    {
        if (!isFolder(folder) || !isSupportedRule(ruleAction))
        {
            return;
        }
        Map<QName, Serializable> mark = new HashMap<>();
        mark.put(PROP_RULE_MOVE_SOURCE_PARENT, sourceParent);
        mark.put(PROP_RULE_MOVE_ACTION_ID, ruleAction.getId());
        mark.put(PROP_RULE_MOVE_LAST_ACTIVITY, new Date());
        writeMark(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_MOVE_TRACKING, mark));
    }

    /**
     * If the same rule recently moved a folder with this name from the same place, moves this folder's contents into it.
     *
     * @return true if the contents were merged, false if the folder should be moved as usual
     */
    public boolean mergeIntoPreviouslyMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent, NodeRef destinationParent) throws FileNotFoundException
    {
        if (!isFolder(folder) || !isSupportedRule(ruleAction))
        {
            return false;
        }
        NodeRef movedFolder = nodeService.getChildByName(destinationParent, ContentModel.ASSOC_CONTAINS, getName(folder));
        boolean movedBySameRule = movedFolder != null
                && !movedFolder.equals(folder)
                && nodeService.hasAspect(movedFolder, ASPECT_RULE_MOVE_TRACKING)
                && isRecent((Date) nodeService.getProperty(movedFolder, PROP_RULE_MOVE_LAST_ACTIVITY))
                && sourceParent.equals(nodeService.getProperty(movedFolder, PROP_RULE_MOVE_SOURCE_PARENT))
                && ruleAction.getId().equals(nodeService.getProperty(movedFolder, PROP_RULE_MOVE_ACTION_ID));
        if (!movedBySameRule)
        {
            return false;
        }

        moveContents(folder, movedFolder);
        deleteIfEmpty(folder);
        markRuleMovedFolder(ruleAction, movedFolder, sourceParent);
        return true;
    }

    // Folders with the same name are merged; a file whose name is taken is renamed (a.txt becomes a-1.txt).
    private void moveContents(NodeRef fromFolder, NodeRef toFolder) throws FileNotFoundException
    {
        for (NodeRef child : getChildren(fromFolder))
        {
            String name = getName(child);
            NodeRef existing = nodeService.getChildByName(toFolder, ContentModel.ASSOC_CONTAINS, name);
            if (isFolder(child) && existing != null && isFolder(existing))
            {
                moveContents(child, existing);
                deleteIfEmpty(child);
                continue;
            }
            fileFolderService.move(child, toFolder, existing == null ? null : getFreeName(toFolder, name));
            // A merged file is new to the moved folder, so it must also reach the folder's copies.
            if (nodeService.hasAspect(toFolder, ASPECT_RULE_COPY_TRACKING))
            {
                queueForCopy(nodeService.getPrimaryParent(child));
            }
        }
    }

    private void deleteIfEmpty(NodeRef folder)
    {
        for (ChildAssociationRef child : nodeService.getChildAssocs(folder))
        {
            if (child.isPrimary())
            {
                return;
            }
        }
        // The empty folder is only a leftover of the upload, so skip the trashcan.
        nodeService.addAspect(folder, ContentModel.ASPECT_TEMPORARY, null);
        nodeService.deleteNode(folder);
    }

    private String getFreeName(NodeRef folder, String name)
    {
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String extension = dot > 0 ? name.substring(dot) : "";
        int number = 1;
        while (nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, base + "-" + number + extension) != null)
        {
            number++;
        }
        return base + "-" + number + extension;
    }

    // ----- Deep copy rule -----

    /**
     * Marks a folder that a rule has just deep copied, and each of its subfolders, with their copy.
     */
    public void trackCopiedFolder(Action ruleAction, NodeRef folder, NodeRef copy)
    {
        if (isFolder(folder) && isSupportedRule(ruleAction))
        {
            markCopiedFolder(folder, copy);
        }
    }

    private void markCopiedFolder(NodeRef folder, NodeRef copy)
    {
        // A folder copied by more than one rule keeps all of its copies.
        List<NodeRef> copies = isCopyActive(folder) ? getCopies(folder) : new ArrayList<>();
        if (!copies.contains(copy))
        {
            copies.add(copy);
        }
        Map<QName, Serializable> mark = new HashMap<>();
        mark.put(PROP_RULE_COPY_DESTINATIONS, (Serializable) copies);
        mark.put(PROP_RULE_COPY_LAST_ACTIVITY, new Date());
        writeMark(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_COPY_TRACKING, mark));

        for (NodeRef child : getChildren(folder))
        {
            NodeRef childCopy = nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, getName(child));
            if (isFolder(child) && childCopy != null)
            {
                markCopiedFolder(child, childCopy);
            }
        }
    }

    /**
     * Called when a file or folder is added to a copied folder.
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        if (isNewNode && childAssocRef.isPrimary())
        {
            queueForCopy(childAssocRef);
        }
    }

    private void queueForCopy(ChildAssociationRef childAssocRef)
    {
        // Keep the mark while rules are disabled, for example during a bulk import.
        if (!ruleService.isEnabled())
        {
            return;
        }
        NodeRef folder = childAssocRef.getParentRef();
        if (!isCopyActive(folder))
        {
            writeMark(folder, () -> nodeService.removeAspect(folder, ASPECT_RULE_COPY_TRACKING));
            return;
        }
        // Copy just before commit, once the upload has written the file content.
        Set<ChildAssociationRef> newChildren = TransactionalResourceHelper.getSet(KEY_NEW_CHILDREN);
        newChildren.add(childAssocRef);
        AlfrescoTransactionSupport.bindListener(copyBeforeCommit);
    }

    private void copyNewChildren()
    {
        Set<ChildAssociationRef> newChildren = TransactionalResourceHelper.getSet(KEY_NEW_CHILDREN);
        // A copy can land in another copied folder, so repeat until nothing new is left.
        while (!newChildren.isEmpty())
        {
            List<ChildAssociationRef> toCopy = new ArrayList<>(newChildren);
            newChildren.clear();
            for (ChildAssociationRef child : toCopy)
            {
                copyNewChild(child.getChildRef(), child.getParentRef());
            }
            // Rules triggered by the copies must also run before this transaction commits.
            ruleService.executePendingRules();
        }
    }

    private void copyNewChild(NodeRef child, NodeRef folder)
    {
        // Skip a child that was deleted or moved away before commit.
        if (!nodeService.exists(child) || !folder.equals(nodeService.getPrimaryParent(child).getParentRef()))
        {
            return;
        }
        String name = getName(child);
        boolean copied = false;
        for (NodeRef copyFolder : getCopies(folder))
        {
            // Never replace something that is already in the copy.
            if (!nodeService.exists(copyFolder) || nodeService.getChildByName(copyFolder, ContentModel.ASSOC_CONTAINS, name) != null)
            {
                continue;
            }
            NodeRef copy = copy(child, copyFolder, name);
            if (isFolder(child))
            {
                markCopiedFolder(child, copy);
            }
            copied = true;
        }
        if (copied)
        {
            refreshCopyActivity(folder);
        }
    }

    private NodeRef copy(NodeRef child, NodeRef copyFolder, String name)
    {
        try
        {
            return fileFolderService.copy(child, copyFolder, name).getNodeRef();
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Could not copy " + child + " into " + copyFolder, e);
        }
    }

    // A copied folder stays active while files keep arriving in it or in a copied folder above it.
    private boolean isCopyActive(NodeRef folder)
    {
        for (NodeRef node = folder; node != null && nodeService.hasAspect(node, ASPECT_RULE_COPY_TRACKING); node = getParent(node))
        {
            if (isRecent((Date) nodeService.getProperty(node, PROP_RULE_COPY_LAST_ACTIVITY)))
            {
                return true;
            }
        }
        return false;
    }

    private void refreshCopyActivity(NodeRef folder)
    {
        Date now = new Date();
        for (NodeRef node = folder; node != null && nodeService.hasAspect(node, ASPECT_RULE_COPY_TRACKING); node = getParent(node))
        {
            NodeRef markedFolder = node;
            writeMark(markedFolder, () -> nodeService.setProperty(markedFolder, PROP_RULE_COPY_LAST_ACTIVITY, now));
        }
    }

    @SuppressWarnings("unchecked")
    private List<NodeRef> getCopies(NodeRef folder)
    {
        List<NodeRef> copies = (List<NodeRef>) nodeService.getProperty(folder, PROP_RULE_COPY_DESTINATIONS);
        return copies == null ? new ArrayList<>() : new ArrayList<>(copies);
    }

    // ----- Shared helpers -----

    // Only rules that run in the foreground and do not apply to subfolders. The action can sit inside the rule's composite action.
    private boolean isSupportedRule(Action action)
    {
        NodeRef actionNode = action.getNodeRef();
        return actionNode != null && AuthenticationUtil.runAsSystem(() -> {
            NodeRef parent = getParent(actionNode);
            while (ActionModel.TYPE_COMPOSITE_ACTION.equals(nodeService.getType(parent)))
            {
                parent = getParent(parent);
            }
            return RuleModel.TYPE_RULE.equals(nodeService.getType(parent))
                    && !Boolean.TRUE.equals(nodeService.getProperty(parent, RuleModel.PROP_APPLY_TO_CHILDREN))
                    && !Boolean.TRUE.equals(nodeService.getProperty(parent, RuleModel.PROP_EXECUTE_ASYNC));
        });
    }

    // Any user uploading to the folder may update the mark, and updating it must not change the folder's modified date.
    private void writeMark(NodeRef folder, Runnable write)
    {
        AuthenticationUtil.runAsSystem(() -> {
            behaviourFilter.disableBehaviour(folder);
            try
            {
                write.run();
            }
            finally
            {
                behaviourFilter.enableBehaviour(folder);
            }
            return null;
        });
    }

    private boolean isRecent(Date lastActivity)
    {
        return lastActivity != null && System.currentTimeMillis() - lastActivity.getTime() <= windowMillis;
    }

    private List<NodeRef> getChildren(NodeRef folder)
    {
        List<NodeRef> children = new ArrayList<>();
        for (ChildAssociationRef child : nodeService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
        {
            if (child.isPrimary())
            {
                children.add(child.getChildRef());
            }
        }
        return children;
    }

    private NodeRef getParent(NodeRef node)
    {
        return nodeService.getPrimaryParent(node).getParentRef();
    }

    private String getName(NodeRef node)
    {
        return (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
    }

    private boolean isFolder(NodeRef node)
    {
        return dictionaryService.isSubClass(nodeService.getType(node), ContentModel.TYPE_FOLDER);
    }
}
