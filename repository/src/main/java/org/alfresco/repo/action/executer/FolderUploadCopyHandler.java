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
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rule.RuleServiceImpl;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionListenerAdapter;

/**
 * Share and ADW upload a folder one file at a time, so a Deep copy rule can copy the folder before all files have arrived. This class copies the late files too:
 * <ol>
 * <li>When the rule copies a folder, the folder remembers its copy.</li>
 * <li>When a file or subfolder is added to that folder, it is scheduled for copying.</li>
 * <li>Just before the upload is saved, it is copied into every copy of the folder.</li>
 * </ol>
 * This stops when no file has arrived for the configured number of minutes.
 */
public class FolderUploadCopyHandler implements NodeServicePolicies.OnCreateChildAssociationPolicy, CopyServicePolicies.OnCopyNodePolicy
{
    public static final QName ASPECT_RULE_COPY_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyTracking");
    public static final QName PROP_RULE_COPY_DESTINATIONS = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyDestinations");
    public static final QName PROP_RULE_COPY_LAST_ACTIVITY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyLastActivity");

    private static final String KEY_ITEMS_TO_COPY = FolderUploadCopyHandler.class.getName() + ".itemsToCopy";

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private PolicyComponent policyComponent;
    private RuleServiceImpl ruleService;
    private FolderUploadRuleSupport support;

    private final TransactionListenerAdapter copyScheduledItemsBeforeCommit = new TransactionListenerAdapter() {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            copyScheduledItems();
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

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setRuleService(RuleServiceImpl ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setSupport(FolderUploadRuleSupport support)
    {
        this.support = support;
    }

    public void init()
    {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ASPECT_RULE_COPY_TRACKING,
                ContentModel.ASSOC_CONTAINS, new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_COPY_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * A copy of a tracked folder is not tracked itself.
     */
    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    // ----- Step 1: the rule copies a folder -----

    /**
     * Called by the Copy action after it deep copied a folder.
     */
    public void trackCopiedFolder(Action ruleAction, NodeRef folder, NodeRef copy)
    {
        if (support.isFolder(folder) && support.isSupportedRule(ruleAction))
        {
            rememberCopy(folder, copy);
        }
    }

    // The folder remembers its copy, and each subfolder remembers its own copy.
    private void rememberCopy(NodeRef folder, NodeRef copy)
    {
        // Several rules can copy the same folder, so keep the copies made earlier in this upload.
        List<NodeRef> copies = isUploadInProgress(folder) ? getCopies(folder) : new ArrayList<>();
        if (!copies.contains(copy))
        {
            copies.add(copy);
        }
        Map<QName, Serializable> mark = new HashMap<>();
        mark.put(PROP_RULE_COPY_DESTINATIONS, (Serializable) copies);
        mark.put(PROP_RULE_COPY_LAST_ACTIVITY, new Date());
        support.writeMark(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_COPY_TRACKING, mark));

        for (NodeRef child : support.getChildren(folder))
        {
            NodeRef childCopy = nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, support.getName(child));
            if (support.isFolder(child) && childCopy != null)
            {
                rememberCopy(child, childCopy);
            }
        }
    }

    // ----- Step 2: a file or subfolder is added to a tracked folder -----

    /**
     * Called by Alfresco when something is created in a tracked folder.
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef newItem, boolean isNewNode)
    {
        if (isNewNode && newItem.isPrimary())
        {
            scheduleCopy(newItem);
        }
    }

    /**
     * Called by the Move handler when it merges a file into a folder, because that folder may be tracked too.
     */
    public void copyIfFolderWasCopied(NodeRef folder, NodeRef mergedItem)
    {
        if (nodeService.hasAspect(folder, ASPECT_RULE_COPY_TRACKING))
        {
            scheduleCopy(nodeService.getPrimaryParent(mergedItem));
        }
    }

    private void scheduleCopy(ChildAssociationRef item)
    {
        // Do nothing while rules are disabled, for example during a bulk import.
        if (!ruleService.isEnabled())
        {
            return;
        }
        NodeRef folder = item.getParentRef();
        if (!isUploadInProgress(folder))
        {
            support.writeMark(folder, () -> nodeService.removeAspect(folder, ASPECT_RULE_COPY_TRACKING));
            return;
        }
        // Copy just before the transaction is saved, because the uploaded content is not written yet.
        Set<ChildAssociationRef> itemsToCopy = TransactionalResourceHelper.getSet(KEY_ITEMS_TO_COPY);
        itemsToCopy.add(item);
        AlfrescoTransactionSupport.bindListener(copyScheduledItemsBeforeCommit);
    }

    // ----- Step 3: just before the upload is saved -----

    private void copyScheduledItems()
    {
        Set<ChildAssociationRef> itemsToCopy = TransactionalResourceHelper.getSet(KEY_ITEMS_TO_COPY);
        // A copy can land in another tracked folder, for example with chained Copy rules, so repeat until nothing is left.
        while (!itemsToCopy.isEmpty())
        {
            List<ChildAssociationRef> batch = new ArrayList<>(itemsToCopy);
            itemsToCopy.clear();
            for (ChildAssociationRef item : batch)
            {
                copyIntoEveryCopy(item.getChildRef(), item.getParentRef());
            }
            // Run the rules that the new copies triggered.
            ruleService.executePendingRules();
        }
    }

    private void copyIntoEveryCopy(NodeRef item, NodeRef folder)
    {
        // Skip an item that was deleted or moved away after it was scheduled.
        if (!nodeService.exists(item) || !folder.equals(support.getParent(item)))
        {
            return;
        }
        String name = support.getName(item);
        boolean copiedAny = false;
        for (NodeRef folderCopy : getCopies(folder))
        {
            // Skip a copy that was deleted, and never replace something that is already there.
            if (!nodeService.exists(folderCopy) || nodeService.getChildByName(folderCopy, ContentModel.ASSOC_CONTAINS, name) != null)
            {
                continue;
            }
            NodeRef itemCopy = copy(item, folderCopy, name);
            if (support.isFolder(item))
            {
                rememberCopy(item, itemCopy);
            }
            copiedAny = true;
        }
        if (copiedAny)
        {
            recordUploadActivity(folder);
        }
    }

    private NodeRef copy(NodeRef item, NodeRef folderCopy, String name)
    {
        try
        {
            return fileFolderService.copy(item, folderCopy, name).getNodeRef();
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Could not copy " + item + " into " + folderCopy, e);
        }
    }

    // ----- Is the upload still in progress? -----

    // The upload is in progress while files keep arriving in this folder or in a tracked parent folder.
    private boolean isUploadInProgress(NodeRef folder)
    {
        for (NodeRef trackedFolder : getTrackedFolderAndParents(folder))
        {
            if (support.isRecent((Date) nodeService.getProperty(trackedFolder, PROP_RULE_COPY_LAST_ACTIVITY)))
            {
                return true;
            }
        }
        return false;
    }

    private void recordUploadActivity(NodeRef folder)
    {
        Date now = new Date();
        for (NodeRef trackedFolder : getTrackedFolderAndParents(folder))
        {
            support.writeMark(trackedFolder, () -> nodeService.setProperty(trackedFolder, PROP_RULE_COPY_LAST_ACTIVITY, now));
        }
    }

    // The folder and its parents, up to the first parent that is not tracked.
    private List<NodeRef> getTrackedFolderAndParents(NodeRef folder)
    {
        List<NodeRef> trackedFolders = new ArrayList<>();
        for (NodeRef node = folder; node != null && nodeService.hasAspect(node, ASPECT_RULE_COPY_TRACKING); node = support.getParent(node))
        {
            trackedFolders.add(node);
        }
        return trackedFolders;
    }

    @SuppressWarnings("unchecked")
    private List<NodeRef> getCopies(NodeRef folder)
    {
        List<NodeRef> copies = (List<NodeRef>) nodeService.getProperty(folder, PROP_RULE_COPY_DESTINATIONS);
        return copies == null ? new ArrayList<>() : new ArrayList<>(copies);
    }
}
