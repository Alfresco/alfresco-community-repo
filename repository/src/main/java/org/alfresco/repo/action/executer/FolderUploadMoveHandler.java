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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Share and ADW upload a folder one file at a time, so a Move rule can move the folder before all files have arrived. Late files go into the folder that was already moved.
 */
public class FolderUploadMoveHandler implements CopyServicePolicies.OnCopyNodePolicy
{
    public static final QName ASPECT_RULE_MOVE_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveTracking");
    public static final QName PROP_RULE_MOVE_SOURCE_PARENT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveSourceParent");
    public static final QName PROP_RULE_MOVE_ACTION_ID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveActionId");
    public static final QName PROP_RULE_MOVE_LAST_ACTIVITY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveLastActivity");

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private PolicyComponent policyComponent;
    private FolderUploadRuleSupport support;
    private FolderUploadCopyHandler copyHandler;

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

    public void setSupport(FolderUploadRuleSupport support)
    {
        this.support = support;
    }

    public void setCopyHandler(FolderUploadCopyHandler copyHandler)
    {
        this.copyHandler = copyHandler;
    }

    public void init()
    {
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_MOVE_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * A copy of a moved folder does not get the folder's tracking.
     */
    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    /**
     * Marks a folder that a rule has just moved.
     */
    public void markRuleMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent)
    {
        if (!support.isFolder(folder) || !support.isSupportedRule(ruleAction))
        {
            return;
        }
        Map<QName, Serializable> mark = new HashMap<>();
        mark.put(PROP_RULE_MOVE_SOURCE_PARENT, sourceParent);
        mark.put(PROP_RULE_MOVE_ACTION_ID, ruleAction.getId());
        mark.put(PROP_RULE_MOVE_LAST_ACTIVITY, new Date());
        support.writeMark(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_MOVE_TRACKING, mark));
    }

    /**
     * If this rule recently moved a folder with the same name, moves this folder's files into it.
     *
     * @return true if the files were moved, false if the folder should be moved as usual
     */
    public boolean mergeIntoPreviouslyMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent, NodeRef destinationParent) throws FileNotFoundException
    {
        if (!support.isFolder(folder) || !support.isSupportedRule(ruleAction))
        {
            return false;
        }
        NodeRef movedFolder = nodeService.getChildByName(destinationParent, ContentModel.ASSOC_CONTAINS, support.getName(folder));
        boolean movedBySameRule = movedFolder != null
                && !movedFolder.equals(folder)
                && nodeService.hasAspect(movedFolder, ASPECT_RULE_MOVE_TRACKING)
                && support.isRecent((Date) nodeService.getProperty(movedFolder, PROP_RULE_MOVE_LAST_ACTIVITY))
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

    // Same-name folders are merged. Same-name files are renamed, for example a.txt to a-1.txt.
    private void moveContents(NodeRef fromFolder, NodeRef toFolder) throws FileNotFoundException
    {
        for (NodeRef child : support.getChildren(fromFolder))
        {
            String name = support.getName(child);
            NodeRef existing = nodeService.getChildByName(toFolder, ContentModel.ASSOC_CONTAINS, name);
            if (support.isFolder(child) && existing != null && support.isFolder(existing))
            {
                moveContents(child, existing);
                deleteIfEmpty(child);
                continue;
            }
            fileFolderService.move(child, toFolder, existing == null ? null : getFreeName(toFolder, name));
            copyHandler.copyIfFolderWasCopied(toFolder, child);
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
        // The empty folder is not needed, so do not keep it in the trashcan.
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
}
