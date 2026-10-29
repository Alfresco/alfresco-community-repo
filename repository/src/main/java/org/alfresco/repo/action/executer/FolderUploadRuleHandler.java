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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * Share uploads a dropped folder one file per request, so a Move or Deep copy rule fires on the folder after the first file only.
 * <ul>
 * <li>Move: the folder that comes back with the next file is merged into the folder this rule already moved.</li>
 * <li>Deep copy: files that land in the copied folder afterwards are copied into the copy too.</li>
 * </ul>
 * Only for the same rule, the same user and within a few minutes; anything else behaves as before.
 */
public class FolderUploadRuleHandler implements NodeServicePolicies.OnCreateChildAssociationPolicy, CopyServicePolicies.OnCopyNodePolicy
{
    private static final Log logger = LogFactory.getLog(FolderUploadRuleHandler.class);

    public static final QName ASPECT_RULE_MOVED = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoved");
    public static final QName PROP_RULE_MOVED_FROM = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedFrom");
    public static final QName PROP_RULE_MOVED_ACTION = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedAction");
    public static final QName PROP_RULE_MOVED_USER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedUser");
    public static final QName PROP_RULE_MOVED_AT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedAt");

    public static final QName ASPECT_RULE_COPY_SOURCE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopySource");
    public static final QName PROP_RULE_COPY_TARGET = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyTarget");
    public static final QName PROP_RULE_COPY_USER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyUser");
    public static final QName PROP_RULE_COPY_AT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyAt");

    private static final String KEY_PENDING_COPIES = FolderUploadRuleHandler.class.getName() + ".pendingCopies";

    // How long after the last file the upload still counts as the same folder drop (long enough for one large file)
    private long windowMillis = 30 * 60_000L;

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;

    private final TransactionListenerAdapter copyListener = new TransactionListenerAdapter() {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            copyPendingChildren();
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

    public void setWindowMinutes(long windowMinutes)
    {
        this.windowMillis = windowMinutes * 60_000L;
    }

    public void init()
    {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ASPECT_RULE_COPY_SOURCE,
                ContentModel.ASSOC_CONTAINS, new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_COPY_SOURCE, new JavaBehaviour(this, "getCopyCallback"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_MOVED, new JavaBehaviour(this, "getCopyCallback"));
    }

    // The markers stay on the node they were set on and are never copied
    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    /**
     * Move rule: merge the folder into the same-named folder this rule moved there before.
     *
     * @return true if merged (nothing left to move), false to move as usual
     */
    public boolean mergeMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent, NodeRef destinationParent) throws FileNotFoundException
    {
        if (!isFolder(folder))
        {
            return false;
        }
        String name = (String) nodeService.getProperty(folder, ContentModel.PROP_NAME);
        NodeRef existing = nodeService.getChildByName(destinationParent, ContentModel.ASSOC_CONTAINS, name);
        Map<QName, Serializable> marker = existing == null ? null : readLiveMarker(existing, ASPECT_RULE_MOVED, PROP_RULE_MOVED_AT);
        if (marker == null
                || !sourceParent.equals(marker.get(PROP_RULE_MOVED_FROM))
                || !ruleAction.getId().equals(marker.get(PROP_RULE_MOVED_ACTION))
                || !marker.get(PROP_RULE_MOVED_USER).equals(currentUser())
                || !isEligibleRuleAction(ruleAction))
        {
            return false;
        }
        mergeInto(folder, existing);
        deleteIfEmpty(folder);
        markMovedFolder(ruleAction, existing, sourceParent);
        return true;
    }

    // Move rule: remember which rule moved this folder, from where, for whom and when
    public void markMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent)
    {
        if (!isFolder(folder) || !isEligibleRuleAction(ruleAction))
        {
            return;
        }
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_RULE_MOVED_FROM, sourceParent);
        props.put(PROP_RULE_MOVED_ACTION, ruleAction.getId());
        props.put(PROP_RULE_MOVED_USER, currentUser());
        props.put(PROP_RULE_MOVED_AT, new Date());
        writeQuietly(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_MOVED, props));
    }

    // Deep copy rule: remember the copy of the folder and of each subfolder so later files can follow
    public void markCopiedFolder(Action ruleAction, NodeRef folder, NodeRef copy)
    {
        if (!isFolder(folder) || !isEligibleRuleAction(ruleAction))
        {
            return;
        }
        markCopiedTree(folder, copy, currentUser());
    }

    // A new file or folder in a copied folder: copy it too, at commit when its content is written
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        if (!isNewNode || !childAssocRef.isPrimary() || activeCopyTarget(childAssocRef.getParentRef()) == null)
        {
            return;
        }
        Set<ChildAssociationRef> pending = TransactionalResourceHelper.getSet(KEY_PENDING_COPIES);
        pending.add(childAssocRef);
        AlfrescoTransactionSupport.bindListener(copyListener);
    }

    private void copyPendingChildren()
    {
        Set<ChildAssociationRef> pending = TransactionalResourceHelper.getSet(KEY_PENDING_COPIES);
        List<ChildAssociationRef> toCopy = new ArrayList<>(pending);
        pending.clear();
        for (ChildAssociationRef assoc : toCopy)
        {
            NodeRef child = assoc.getChildRef();
            NodeRef parent = assoc.getParentRef();
            NodeRef target = activeCopyTarget(parent);
            if (target == null || !nodeService.exists(child) || !parent.equals(nodeService.getPrimaryParent(child).getParentRef()))
            {
                continue;
            }
            String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
            if (nodeService.getChildByName(target, ContentModel.ASSOC_CONTAINS, name) != null)
            {
                logger.info("Not copying " + child + " into " + target + ": '" + name + "' already exists there");
                continue;
            }
            NodeRef copy;
            try
            {
                // FileFolderService keeps the name; CopyService alone would not
                copy = fileFolderService.copy(child, target, name).getNodeRef();
            }
            catch (FileNotFoundException e)
            {
                continue;
            }
            if (isFolder(child))
            {
                markCopiedTree(child, copy, currentUser());
            }
            // Each copied file keeps the upload window open
            NodeRef root = copyRoot(parent);
            writeQuietly(root, () -> nodeService.setProperty(root, PROP_RULE_COPY_AT, new Date()));
        }
    }

    // The copy of this folder, if a rule copied it for the current user and the upload is still going on
    private NodeRef activeCopyTarget(NodeRef folder)
    {
        String user = currentUser();
        Map<QName, Serializable> marker = readMarker(folder, ASPECT_RULE_COPY_SOURCE);
        if (user == null || marker == null || !user.equals(marker.get(PROP_RULE_COPY_USER)))
        {
            return null;
        }
        if (readLiveMarker(copyRoot(folder), ASPECT_RULE_COPY_SOURCE, PROP_RULE_COPY_AT) == null)
        {
            return null;
        }
        NodeRef target = (NodeRef) marker.get(PROP_RULE_COPY_TARGET);
        return target != null && nodeService.exists(target) ? target : null;
    }

    // The top-most copied folder of the drop; its time is the one that counts
    private NodeRef copyRoot(NodeRef folder)
    {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef root = folder;
            NodeRef parent = nodeService.getPrimaryParent(root).getParentRef();
            while (parent != null && nodeService.hasAspect(parent, ASPECT_RULE_COPY_SOURCE))
            {
                root = parent;
                parent = nodeService.getPrimaryParent(root).getParentRef();
            }
            return root;
        });
    }

    private void markCopiedTree(NodeRef folder, NodeRef copy, String user)
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_RULE_COPY_TARGET, copy);
        props.put(PROP_RULE_COPY_USER, user);
        props.put(PROP_RULE_COPY_AT, new Date());
        writeQuietly(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_COPY_SOURCE, props));

        for (ChildAssociationRef assoc : nodeService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
        {
            NodeRef child = assoc.getChildRef();
            if (assoc.isPrimary() && isFolder(child))
            {
                String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
                NodeRef childCopy = nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, name);
                if (childCopy != null)
                {
                    markCopiedTree(child, childCopy, user);
                }
            }
        }
    }

    // Move the children across: same-named folders are merged, same-named files get a new name (never overwritten)
    private void mergeInto(NodeRef from, NodeRef into) throws FileNotFoundException
    {
        for (ChildAssociationRef assoc : nodeService.getChildAssocs(from, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
        {
            if (!assoc.isPrimary())
            {
                continue;
            }
            NodeRef child = assoc.getChildRef();
            String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
            NodeRef existing = nodeService.getChildByName(into, ContentModel.ASSOC_CONTAINS, name);
            if (existing == null)
            {
                fileFolderService.move(child, into, null);
            }
            else if (isFolder(child) && isFolder(existing))
            {
                mergeInto(child, existing);
                deleteIfEmpty(child);
            }
            else
            {
                String newName = freeName(into, name);
                logger.warn("'" + name + "' already exists in " + into + ", moving " + child + " as '" + newName + "'");
                fileFolderService.move(child, into, newName);
            }
        }
    }

    private void deleteIfEmpty(NodeRef folder)
    {
        if (nodeService.getChildAssocs(folder).isEmpty())
        {
            // The emptied leftover of the upload is not worth keeping in the trashcan
            nodeService.addAspect(folder, ContentModel.ASPECT_TEMPORARY, null);
            nodeService.deleteNode(folder);
        }
    }

    // name.txt -> name-1.txt, name-2.txt, ... whichever is free
    private String freeName(NodeRef folder, String name)
    {
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String extension = dot > 0 ? name.substring(dot) : "";
        String candidate;
        int i = 1;
        do
        {
            candidate = base + "-" + i++ + extension;
        } while (nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, candidate) != null);
        return candidate;
    }

    // Only rules that run right away and only on their own folder (not "run in background" or "apply to subfolders")
    private boolean isEligibleRuleAction(Action action)
    {
        NodeRef actionNodeRef = action.getNodeRef();
        if (actionNodeRef == null || currentUser() == null)
        {
            return false;
        }
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef node = actionNodeRef;
            while (nodeService.exists(node))
            {
                NodeRef parent = nodeService.getPrimaryParent(node).getParentRef();
                if (parent == null)
                {
                    return false;
                }
                QName parentType = nodeService.getType(parent);
                if (ActionModel.TYPE_COMPOSITE_ACTION.equals(parentType))
                {
                    node = parent;
                }
                else
                {
                    return RuleModel.TYPE_RULE.equals(parentType)
                            && !Boolean.TRUE.equals(nodeService.getProperty(parent, RuleModel.PROP_APPLY_TO_CHILDREN))
                            && !Boolean.TRUE.equals(nodeService.getProperty(parent, RuleModel.PROP_EXECUTE_ASYNC));
                }
            }
            return false;
        });
    }

    private Map<QName, Serializable> readMarker(NodeRef node, QName aspect)
    {
        return AuthenticationUtil.runAsSystem(() -> nodeService.exists(node) && nodeService.hasAspect(node, aspect) ? nodeService.getProperties(node) : null);
    }

    // An expired marker is of no use to anyone: remove it so the folder ends up clean
    private Map<QName, Serializable> readLiveMarker(NodeRef node, QName aspect, QName atProperty)
    {
        Map<QName, Serializable> marker = readMarker(node, aspect);
        if (marker != null && !withinWindow((Date) marker.get(atProperty)))
        {
            writeQuietly(node, () -> nodeService.removeAspect(node, aspect));
            return null;
        }
        return marker;
    }

    // Markers are bookkeeping: write them as system without touching cm:modified or firing update rules
    private void writeQuietly(NodeRef node, Runnable write)
    {
        AuthenticationUtil.runAsSystem(() -> {
            behaviourFilter.disableBehaviour(node);
            try
            {
                write.run();
            }
            finally
            {
                behaviourFilter.enableBehaviour(node);
            }
            return null;
        });
    }

    private boolean withinWindow(Date at)
    {
        return at != null && System.currentTimeMillis() - at.getTime() <= windowMillis;
    }

    private boolean isFolder(NodeRef node)
    {
        return dictionaryService.isSubClass(nodeService.getType(node), ContentModel.TYPE_FOLDER);
    }

    private String currentUser()
    {
        return AuthenticationUtil.getFullyAuthenticatedUser();
    }
}
