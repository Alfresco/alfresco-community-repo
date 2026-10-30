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
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * Folder uploads arrive as separate file requests, so a rule can run before the whole folder is uploaded.
 * <ul>
 * <li>Move: add later files to the folder that the rule already moved.</li>
 * <li>Deep copy: also copy files added to the original folder later.</li>
 * </ul>
 * Tracking aspects remember the related folder, user and last successful activity. Later requests must match the user and time window. Move also checks the source parent and rule action.
 * <p>
 * These checks do not identify a client upload batch. Separate same-name drops can match, and other files added by the same user can follow an active copy marker. Move renames conflicts; catch-up copies preserve existing destination items. Only successful merges or copies restart the time window.
 */
public class FolderUploadRuleHandler implements NodeServicePolicies.OnCreateChildAssociationPolicy, CopyServicePolicies.OnCopyNodePolicy
{
    private static final Log logger = LogFactory.getLog(FolderUploadRuleHandler.class);

    // These names are stored in the repository and must match systemModel.xml.
    public static final QName ASPECT_RULE_MOVE_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveTracking");
    public static final QName PROP_RULE_MOVE_SOURCE_PARENT = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveSourceParent");
    public static final QName PROP_RULE_MOVE_ACTION_ID = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveActionId");
    public static final QName PROP_RULE_MOVE_OWNER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveOwner");
    public static final QName PROP_RULE_MOVE_LAST_ACTIVITY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoveLastActivity");

    public static final QName ASPECT_RULE_COPY_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyTracking");
    public static final QName PROP_RULE_COPY_DESTINATION = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyDestination");
    public static final QName PROP_RULE_COPY_OWNER = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyOwner");
    public static final QName PROP_RULE_COPY_LAST_ACTIVITY = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyLastActivity");

    // Read earlier tracking names so in-progress uploads survive the rename.
    private static final QName LEGACY_MOVE_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMoved");
    private static final QName LEGACY_COPY_TRACKING = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopySource");
    private static final Map<QName, QName> LEGACY_MOVE_PROPERTIES = Map.of(
            PROP_RULE_MOVE_SOURCE_PARENT, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedFrom"),
            PROP_RULE_MOVE_ACTION_ID, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedAction"),
            PROP_RULE_MOVE_OWNER, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedUser"),
            PROP_RULE_MOVE_LAST_ACTIVITY, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleMovedAt"));
    private static final Map<QName, QName> LEGACY_COPY_PROPERTIES = Map.of(
            PROP_RULE_COPY_DESTINATION, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyTarget"),
            PROP_RULE_COPY_OWNER, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyUser"),
            PROP_RULE_COPY_LAST_ACTIVITY, QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ruleCopyAt"));

    private static final String KEY_PENDING_COPY_ASSOCIATIONS = FolderUploadRuleHandler.class.getName() + ".pendingCopyAssociations";

    // Allow up to 30 minutes between files unless a different limit is configured.
    private long trackingWindowMillis = 30 * 60_000L;

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;

    // Wait until content has been written, but keep the copy in the upload transaction.
    private final TransactionListenerAdapter copyListener = new TransactionListenerAdapter() {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            copyPendingChildrenBeforeCommit();
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

    /**
     * Sets how long later uploads can be added to a folder already moved or copied by a rule.
     *
     * @param windowMinutes
     *            maximum gap between uploads, in minutes
     */
    public void setWindowMinutes(long windowMinutes)
    {
        this.trackingWindowMillis = windowMinutes * 60_000L;
    }

    /**
     * Registers the callbacks for copying later uploads and keeping upload details out of copies.
     */
    public void init()
    {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, ASPECT_RULE_COPY_TRACKING,
                ContentModel.ASSOC_CONTAINS, new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, LEGACY_COPY_TRACKING,
                ContentModel.ASSOC_CONTAINS, new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_COPY_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ASPECT_RULE_MOVE_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, LEGACY_COPY_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, LEGACY_MOVE_TRACKING, new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * Prevents the stored upload details from being copied to another folder.
     *
     * @param classRef
     *            the aspect being copied
     * @param copyDetails
     *            details of the copy operation
     * @return a callback that leaves this aspect out of the copy
     */
    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    // Move tracking and folder merging.

    /**
     * Adds this folder's contents to the folder with the same name that this rule already moved.
     *
     * @param ruleAction
     *            the move action being run by the rule
     * @param folder
     *            the newly uploaded folder
     * @param sourceParent
     *            the parent of the uploaded folder
     * @param destinationParent
     *            the folder selected as the move destination
     * @return true if merged, or false if the caller should move the folder normally
     * @throws FileNotFoundException
     *             if a folder needed for the merge cannot be found
     */
    public boolean mergeIntoPreviouslyMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent, NodeRef destinationParent) throws FileNotFoundException
    {
        if (!isFolder(folder))
        {
            return false;
        }
        NodeRef existingFolder = findDestinationFolder(folder, destinationParent);
        Map<QName, Serializable> marker = existingFolder == null ? null : getUnexpiredMarker(existingFolder, ASPECT_RULE_MOVE_TRACKING, PROP_RULE_MOVE_LAST_ACTIVITY);
        // A matching name alone is not enough; the stored source, action and user must also match.
        if (marker == null
                || !sourceParent.equals(marker.get(PROP_RULE_MOVE_SOURCE_PARENT))
                || !ruleAction.getId().equals(marker.get(PROP_RULE_MOVE_ACTION_ID))
                || !Objects.equals(marker.get(PROP_RULE_MOVE_OWNER), currentUser())
                || !isSupportedRuleAction(ruleAction))
        {
            return false;
        }
        mergeFolderContents(folder, existingFolder);
        deleteIfEmpty(folder);
        markRuleMovedFolder(ruleAction, existingFolder, sourceParent);
        return true;
    }

    private NodeRef findDestinationFolder(NodeRef sourceFolder, NodeRef destinationParent)
    {
        String folderName = (String) nodeService.getProperty(sourceFolder, ContentModel.PROP_NAME);
        return nodeService.getChildByName(destinationParent, ContentModel.ASSOC_CONTAINS, folderName);
    }

    /**
     * Stores move details so later uploads can be added to this folder. Does nothing for files or rules that run in the background or apply to subfolders.
     *
     * @param ruleAction
     *            the move action being run by the rule
     * @param folder
     *            the folder that was moved
     * @param sourceParent
     *            the folder's parent before the move
     */
    public void markRuleMovedFolder(Action ruleAction, NodeRef folder, NodeRef sourceParent)
    {
        if (!isFolder(folder) || !isSupportedRuleAction(ruleAction))
        {
            return;
        }
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_RULE_MOVE_SOURCE_PARENT, sourceParent);
        props.put(PROP_RULE_MOVE_ACTION_ID, ruleAction.getId());
        props.put(PROP_RULE_MOVE_OWNER, currentUser());
        props.put(PROP_RULE_MOVE_LAST_ACTIVITY, new Date());
        updateMarkerWithoutBehaviours(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_MOVE_TRACKING, props));
    }

    // Deep Copy tracking and catch-up before commit.

    /**
     * Stores the copy location on the original folder and its subfolders for later uploads. Does nothing for files or rules that run in the background or apply to subfolders.
     *
     * @param ruleAction
     *            the copy action being run by the rule
     * @param folder
     *            the original folder
     * @param copy
     *            the new copy of the folder
     */
    public void trackCopiedFolder(Action ruleAction, NodeRef folder, NodeRef copy)
    {
        if (!isFolder(folder) || !isSupportedRuleAction(ruleAction))
        {
            return;
        }
        // Each original subfolder needs its own destination so later children can follow it.
        trackCopiedTree(folder, copy, currentUser());
    }

    /**
     * Schedules a newly uploaded file or folder to be copied just before the transaction commits. Waiting until then allows its content to be written before it is copied.
     *
     * @param childAssocRef
     *            the link between the parent folder and the new item
     * @param isNewNode
     *            whether the item was newly created
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        if (!isNewNode || !childAssocRef.isPrimary() || getActiveCopyTarget(childAssocRef.getParentRef(), false) == null)
        {
            return;
        }
        // Keep the original parent association so a later move does not copy the child to the wrong place.
        // The transaction-scoped set also avoids queuing the same association twice.
        Set<ChildAssociationRef> pending = TransactionalResourceHelper.getSet(KEY_PENDING_COPY_ASSOCIATIONS);
        pending.add(childAssocRef);
        AlfrescoTransactionSupport.bindListener(copyListener);
    }

    private void copyPendingChildrenBeforeCommit()
    {
        Set<ChildAssociationRef> pending = TransactionalResourceHelper.getSet(KEY_PENDING_COPY_ASSOCIATIONS);
        List<ChildAssociationRef> toCopy = new ArrayList<>(pending);
        // Copying may trigger more callbacks process this snapshot only once.
        pending.clear();
        for (ChildAssociationRef association : toCopy)
        {
            copyPendingChild(association);
        }
    }

    /**
     * Rechecks a queued child because it may have moved or been deleted before commit.
     */
    private void copyPendingChild(ChildAssociationRef association)
    {
        NodeRef child = association.getChildRef();
        NodeRef sourceParent = association.getParentRef();
        // The marker or destination may have changed since this association was queued.
        NodeRef destinationParent = getActiveCopyTarget(sourceParent, true);
        if (destinationParent == null || !isCurrentChild(child, sourceParent))
        {
            return;
        }

        String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
        // Preserve the first destination item; skipped copies must not extend the tracking window.
        if (destinationChildExists(destinationParent, name, child))
        {
            return;
        }

        NodeRef copy = copyUploadedChild(child, destinationParent, name);
        if (copy == null)
        {
            // The source was deleted during the copy attempt, so there is no successful copy to record.
            return;
        }
        if (isFolder(child))
        {
            trackCopiedTree(child, copy, currentUser());
        }
        refreshCopyTrackingActivity(sourceParent);
    }

    private boolean isCurrentChild(NodeRef child, NodeRef expectedParent)
    {
        return nodeService.exists(child) && expectedParent.equals(nodeService.getPrimaryParent(child).getParentRef());
    }

    // Unlike Move, Deep Copy skips a taken name instead of generating a new one.
    private boolean destinationChildExists(NodeRef destinationParent, String name, NodeRef sourceChild)
    {
        if (nodeService.getChildByName(destinationParent, ContentModel.ASSOC_CONTAINS, name) == null)
        {
            return false;
        }
        logger.info("Not copying " + sourceChild + " into " + destinationParent + ": '" + name + "' already exists there");
        return true;
    }

    private NodeRef copyUploadedChild(NodeRef child, NodeRef destinationParent, String name)
    {
        try
        {
            // Keep the uploaded name instead of creating an automatically renamed copy.
            return fileFolderService.copy(child, destinationParent, name).getNodeRef();
        }
        catch (FileNotFoundException e)
        {
            // The source may have been deleted after the earlier check but before the copy started.
            if (!nodeService.exists(child))
            {
                logger.info("Not copying " + child + ": the uploaded item was deleted");
                return null;
            }
            // A real copy failure must fail the upload, not leave an incomplete destination silently.
            throw new AlfrescoRuntimeException("Unable to copy uploaded item " + child + " into " + destinationParent, e);
        }
    }

    private void refreshCopyTrackingActivity(NodeRef sourceFolder)
    {
        // One timestamp controls the tracked tree, even when the new file is several folders deep.
        NodeRef trackingRoot = getCopyTrackingRoot(sourceFolder);
        updateMarkerWithoutBehaviours(trackingRoot, () -> nodeService.setProperty(trackingRoot, PROP_RULE_COPY_LAST_ACTIVITY, new Date()));
    }

    /**
     * Finds this folder's copy when the user and root timer still match.
     *
     * @param folder
     *            the original folder receiving a new child
     * @param failOnMissingDestination
     *            false when deciding whether to queue true when completing an already queued copy
     * @return the destination folder, or null when tracking no longer applies
     */
    private NodeRef getActiveCopyTarget(NodeRef folder, boolean failOnMissingDestination)
    {
        String user = currentUser();
        Map<QName, Serializable> marker = getOrMigrateMarker(folder, ASPECT_RULE_COPY_TRACKING);
        if (user == null || marker == null || !user.equals(marker.get(PROP_RULE_COPY_OWNER)))
        {
            return null;
        }
        // Use the top folder's activity, not an older timestamp on an untouched subfolder.
        if (getUnexpiredMarker(getCopyTrackingRoot(folder), ASPECT_RULE_COPY_TRACKING, PROP_RULE_COPY_LAST_ACTIVITY) == null)
        {
            return null;
        }
        NodeRef target = (NodeRef) marker.get(PROP_RULE_COPY_DESTINATION);
        if (target != null && nodeService.exists(target))
        {
            return target;
        }
        if (failOnMissingDestination)
        {
            // Losing a destination after queuing must fail the upload rather than silently omit its copy.
            throw new AlfrescoRuntimeException("Copy destination disappeared while uploading into " + folder + ": " + target);
        }
        return null;
    }

    // Walk up to the top folder with copy details and use its last copy time for the time limit.
    private NodeRef getCopyTrackingRoot(NodeRef folder)
    {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef root = folder;
            NodeRef parent = nodeService.getPrimaryParent(root).getParentRef();
            // Include legacy aspects so an older upload still shares its original root timer.
            while (parent != null && (nodeService.hasAspect(parent, ASPECT_RULE_COPY_TRACKING) || nodeService.hasAspect(parent, LEGACY_COPY_TRACKING)))
            {
                root = parent;
                parent = nodeService.getPrimaryParent(root).getParentRef();
            }
            return root;
        });
    }

    private void trackCopiedTree(NodeRef folder, NodeRef copy, String user)
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(PROP_RULE_COPY_DESTINATION, copy);
        props.put(PROP_RULE_COPY_OWNER, user);
        props.put(PROP_RULE_COPY_LAST_ACTIVITY, new Date());
        updateMarkerWithoutBehaviours(folder, () -> nodeService.addAspect(folder, ASPECT_RULE_COPY_TRACKING, props));

        // Files use their parent's marker. Only folders need their own mapping for future children.
        for (ChildAssociationRef assoc : nodeService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
        {
            NodeRef child = assoc.getChildRef();
            if (assoc.isPrimary() && isFolder(child))
            {
                String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
                NodeRef childCopy = nodeService.getChildByName(copy, ContentModel.ASSOC_CONTAINS, name);
                if (childCopy != null)
                {
                    trackCopiedTree(child, childCopy, user);
                }
            }
        }
    }

    // Move the contents, combine folders with matching names, and rename files instead of replacing them.
    private void mergeFolderContents(NodeRef from, NodeRef into) throws FileNotFoundException
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
                // Combine matching folders recursively instead of replacing either folder's contents.
                mergeFolderContents(child, existing);
                deleteIfEmpty(child);
            }
            else
            {
                String newName = generateAvailableName(into, name);
                logger.warn("'" + name + "' already exists in " + into + ", moving " + child + " as '" + newName + "'");
                fileFolderService.move(child, into, newName);
            }
        }
    }

    private void deleteIfEmpty(NodeRef folder)
    {
        if (nodeService.getChildAssocs(folder).isEmpty())
        {
            // Delete the empty upload folder without putting it in the trashcan.
            nodeService.addAspect(folder, ContentModel.ASPECT_TEMPORARY, null);
            nodeService.deleteNode(folder);
        }
    }

    // Try name-1.txt, name-2.txt and so on until the name is unused.
    private String generateAvailableName(NodeRef folder, String name)
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

    // Shared tracking and rule checks.

    // Skip actions outside rules, background rules, and rules set to apply to subfolders.
    private boolean isSupportedRuleAction(Action action)
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
                    // A rule action can sit inside nested action groups; keep walking to the owning rule.
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

    /**
     * Reads current tracking details, upgrading an earlier aspect without restarting its timer.
     */
    private Map<QName, Serializable> getOrMigrateMarker(NodeRef node, QName aspect)
    {
        return AuthenticationUtil.runAsSystem(() -> {
            if (!nodeService.exists(node))
            {
                return null;
            }
            if (nodeService.hasAspect(node, aspect))
            {
                return nodeService.getProperties(node);
            }
            boolean moveTracking = ASPECT_RULE_MOVE_TRACKING.equals(aspect);
            QName legacyAspect = moveTracking ? LEGACY_MOVE_TRACKING : LEGACY_COPY_TRACKING;
            if (!nodeService.hasAspect(node, legacyAspect))
            {
                return null;
            }
            Map<QName, Serializable> properties = nodeService.getProperties(node);
            Map<QName, Serializable> migrated = new HashMap<>();
            Map<QName, QName> aliases = moveTracking ? LEGACY_MOVE_PROPERTIES : LEGACY_COPY_PROPERTIES;
            // Carry over the old activity time renaming metadata must not reopen an expired window.
            aliases.forEach((current, legacy) -> migrated.put(current, properties.get(legacy)));
            updateMarkerWithoutBehaviours(node, () -> {
                // Install the replacement before removing the legacy aspect in the same transaction.
                nodeService.addAspect(node, aspect, migrated);
                nodeService.removeAspect(node, legacyAspect);
            });
            Map<QName, Serializable> marker = new HashMap<>(properties);
            // Return the renamed values immediately without another repository lookup.
            marker.putAll(migrated);
            return marker;
        });
    }

    // Remove the stored upload details if their time limit has passed.
    private Map<QName, Serializable> getUnexpiredMarker(NodeRef node, QName aspect, QName atProperty)
    {
        Map<QName, Serializable> marker = getOrMigrateMarker(node, aspect);
        // Clean up on access rather than running a separate background scan.
        if (marker != null && !withinWindow((Date) marker.get(atProperty)))
        {
            if (ASPECT_RULE_COPY_TRACKING.equals(aspect))
            {
                // Expiring only the root could let leftover child markers become active roots later.
                expireCopyTracking(node, marker);
            }
            else
            {
                updateMarkerWithoutBehaviours(node, () -> nodeService.removeAspect(node, aspect));
            }
            return null;
        }
        return marker;
    }

    /**
     * Removes matching nested markers, leaving separately tracked destinations alone.
     */
    private void expireCopyTracking(NodeRef folder, Map<QName, Serializable> marker)
    {
        NodeRef destination = (NodeRef) marker.get(PROP_RULE_COPY_DESTINATION);
        Serializable owner = marker.get(PROP_RULE_COPY_OWNER);
        for (ChildAssociationRef association : nodeService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
        {
            NodeRef child = association.getChildRef();
            if (!association.isPrimary() || !isFolder(child))
            {
                continue;
            }
            Map<QName, Serializable> childMarker = getOrMigrateMarker(child, ASPECT_RULE_COPY_TRACKING);
            if (childMarker == null || !Objects.equals(owner, childMarker.get(PROP_RULE_COPY_OWNER)))
            {
                continue;
            }
            NodeRef childDestination = (NodeRef) childMarker.get(PROP_RULE_COPY_DESTINATION);
            // Follow only the same destination tree a different mapping may belong to another copy.
            if (destination != null && childDestination != null && nodeService.exists(childDestination)
                    && destination.equals(nodeService.getPrimaryParent(childDestination).getParentRef()))
            {
                expireCopyTracking(child, childMarker);
            }
        }
        // Remove the root last, after visiting its matching descendants.
        updateMarkerWithoutBehaviours(folder, () -> nodeService.removeAspect(folder, ASPECT_RULE_COPY_TRACKING));
    }

    // BehaviourFilter uses reference counts, so this also preserves an outer caller's disable.
    private void updateMarkerWithoutBehaviours(NodeRef node, Runnable write)
    {
        // Elevate only the tracking update the file move or copy still runs as the uploading user.
        AuthenticationUtil.runAsSystem(() -> {
            behaviourFilter.disableBehaviour(node);
            try
            {
                write.run();
            }
            finally
            {
                // Release this method's disable even if the update fails. Any outer disable remains in effect.
                behaviourFilter.enableBehaviour(node);
            }
            return null;
        });
    }

    private boolean withinWindow(Date at)
    {
        // An incomplete marker with no timestamp is treated as expired.
        return at != null && System.currentTimeMillis() - at.getTime() <= trackingWindowMillis;
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
