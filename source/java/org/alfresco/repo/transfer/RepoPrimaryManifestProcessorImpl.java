/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.transfer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.CorrespondingNodeResolver.ResolvedParentChildPair;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 */
public class RepoPrimaryManifestProcessorImpl extends AbstractManifestProcessorBase
{
    private static final Log log = LogFactory.getLog(RepoPrimaryManifestProcessorImpl.class);

    private static final String MSG_NO_PRIMARY_PARENT_SUPPLIED = "transfer_service.receiver.no_primary_parent_supplied";
    private static final String MSG_ORPHANS_EXIST = "transfer_service.receiver.orphans_exist";
    private static final String MSG_REFERENCED_CONTENT_FILE_MISSING = "transfer_service.receiver.content_file_missing";

    protected static final Set<QName> DEFAULT_LOCAL_PROPERTIES = new HashSet<QName>();

    static
    {
        DEFAULT_LOCAL_PROPERTIES.add(ContentModel.PROP_STORE_IDENTIFIER);
        DEFAULT_LOCAL_PROPERTIES.add(ContentModel.PROP_STORE_NAME);
        DEFAULT_LOCAL_PROPERTIES.add(ContentModel.PROP_STORE_PROTOCOL);
        DEFAULT_LOCAL_PROPERTIES.add(ContentModel.PROP_NODE_DBID);
        DEFAULT_LOCAL_PROPERTIES.add(ContentModel.PROP_NODE_REF);
        DEFAULT_LOCAL_PROPERTIES.add(ContentModel.PROP_NODE_UUID);
    }

    private NodeService nodeService;
    private ContentService contentService;
    private CorrespondingNodeResolver nodeResolver;

    private Map<NodeRef, List<ChildAssociationRef>> orphans = new HashMap<NodeRef, List<ChildAssociationRef>>(89);

    /**
     * @param transferId
     */
    public RepoPrimaryManifestProcessorImpl(TransferReceiver receiver, String transferId)
    {
        super(receiver, transferId);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.transfer.manifest.TransferManifestProcessor# endTransferManifest()
     */
    protected void endManifest()
    {
        if (!orphans.isEmpty())
        {
            error(MSG_ORPHANS_EXIST);
        }
    }

    /**
     * 
     */
    protected void processNode(TransferManifestDeletedNode node)
    {
        // This is a deleted node. First we need to check whether it has already been deleted in this repo
        // too by looking in the local archive store. If we find it then we need not do anything.
        // If we can't find it in our archive store then we'll see if we can find a corresponding node in the
        // store in which its old parent lives.
        // If we can find a corresponding node then we'll delete it.
        // If we can't find a corresponding node then we'll do nothing.
        logProgress("Processing incoming deleted node: " + node.getNodeRef());
        if (!nodeService.exists(node.getNodeRef()))
        {
            // It's not in our archive store. Check to see if we can find it in
            // its original store...
            ChildAssociationRef origPrimaryParent = node.getPrimaryParentAssoc();
            NodeRef origNodeRef = new NodeRef(origPrimaryParent.getParentRef().getStoreRef(), node.getNodeRef().getId());

            CorrespondingNodeResolver.ResolvedParentChildPair resolvedNodes = nodeResolver.resolveCorrespondingNode(
                    origNodeRef, origPrimaryParent, node.getParentPath());

            // Does a corresponding node exist in this repo?
            if (resolvedNodes.resolvedChild != null)
            {
                // Yes, it does. Delete it.
                if (log.isDebugEnabled())
                {
                    log.debug("Incoming deleted noderef " + node.getNodeRef()
                            + " has been resolved to existing local noderef " + resolvedNodes.resolvedChild
                            + "  - deleting");
                }
                logProgress("Deleting local node: " + resolvedNodes.resolvedChild);
                nodeService.deleteNode(resolvedNodes.resolvedChild);
            }
            else
            {
                logProgress("Unable to find corresponding node for incoming deleted node: " + node.getNodeRef());
                if (log.isDebugEnabled())
                {
                    log.debug("Incoming deleted noderef has no corresponding local noderef: " + node.getNodeRef()
                            + "  - ignoring");
                }
            }
        }
        else
        {
            logProgress("Incoming deleted node is already in the local archive store - ignoring: " + node.getNodeRef());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.transfer.manifest.TransferManifestProcessor#
     * processTransferManifestNode(org.alfresco.repo.transfer .manifest.TransferManifestNode)
     */
    protected void processNode(TransferManifestNormalNode node)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Processing node with incoming noderef of " + node.getNodeRef());
        }
        logProgress("Processing incoming node: " + node.getNodeRef() + " --  Source path = " + node.getParentPath() + "/" + node.getPrimaryParentAssoc().getQName());

        ChildAssociationRef primaryParentAssoc = node.getPrimaryParentAssoc();
        if (primaryParentAssoc == null)
        {
            error(node, MSG_NO_PRIMARY_PARENT_SUPPLIED);
        }

        CorrespondingNodeResolver.ResolvedParentChildPair resolvedNodes = nodeResolver.resolveCorrespondingNode(node
                .getNodeRef(), primaryParentAssoc, node.getParentPath());

        // Does a corresponding node exist in this repo?
        if (resolvedNodes.resolvedChild != null)
        {
            // Yes, it does. Update it.
            if (log.isDebugEnabled())
            {
                log.debug("Incoming noderef " + node.getNodeRef() + " has been resolved to existing local noderef "
                        + resolvedNodes.resolvedChild);
            }
            update(node, resolvedNodes, primaryParentAssoc);
        }
        else
        {
            // No, there is no corresponding node. Worth just quickly checking
            // the archive store...
            NodeRef archiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, node.getNodeRef().getId());
            if (nodeService.exists(archiveNodeRef))
            {
                // We have found a node in the archive store that has the same
                // UUID as the one that we've
                // been sent. We'll restore that archived node to a temporary
                // location and then try
                // processing this node again
                if (log.isInfoEnabled())
                {
                    log.info("Located an archived node with UUID matching transferred node: " + archiveNodeRef);
                    log.info("Attempting to restore " + archiveNodeRef);
                }
                logProgress("Restoring node from archive: " + archiveNodeRef);

                ChildAssociationRef tempLocation = getTemporaryLocation(node.getNodeRef());
                NodeRef restoredNodeRef = nodeService.restoreNode(archiveNodeRef, tempLocation.getParentRef(),
                        tempLocation.getTypeQName(), tempLocation.getQName());
                if (log.isInfoEnabled())
                {
                    log.info("Successfully restored node as " + restoredNodeRef + "  - retrying transferred node");
                }
                processTransferManifestNode(node);
                return;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Incoming noderef has no corresponding local noderef: " + node.getNodeRef());
            }
            create(node, resolvedNodes, primaryParentAssoc);
        }
    }

    /**
     * 
     * @param node
     * @param resolvedNodes
     * @param primaryParentAssoc
     */
    private void create(TransferManifestNormalNode node, ResolvedParentChildPair resolvedNodes,
            ChildAssociationRef primaryParentAssoc)
    {
        log.info("Creating new node with noderef " + node.getNodeRef());
        logProgress("Creating new node to correspond to incoming node: " + node.getNodeRef());
        
        QName parentAssocType = primaryParentAssoc.getTypeQName();
        QName parentAssocName = primaryParentAssoc.getQName();
        NodeRef parentNodeRef = resolvedNodes.resolvedParent;
        if (parentNodeRef == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to resolve parent for inbound noderef " + node.getNodeRef()
                        + ".\n  Supplied parent noderef is " + primaryParentAssoc.getParentRef()
                        + ".\n  Supplied parent path is " + node.getParentPath().toString());
            }
            // We can't find the node's parent.
            // We'll store the node in a temporary location and record it for
            // later processing
            ChildAssociationRef tempLocation = getTemporaryLocation(node.getNodeRef());
            parentNodeRef = tempLocation.getParentRef();
            parentAssocType = tempLocation.getTypeQName();
            parentAssocName = tempLocation.getQName();
            log.info("Recording orphaned transfer node: " + node.getNodeRef());
            logProgress("Unable to resolve parent for new incoming node. Storing it in temp folder: " + node.getNodeRef());
            storeOrphanNode(primaryParentAssoc);
        }
        // We now know that this is a new node, and we have found the
        // appropriate parent node in the
        // local repository.
        log.info("Resolved parent node to " + parentNodeRef);

        // We need to process content properties separately.
        // First, create a shallow copy of the supplied property map...
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(node.getProperties());

        // Split out the content properties and sanitise the others
        Map<QName, Serializable> contentProps = processProperties(null, props, true);

        // Create the corresponding node...
        ChildAssociationRef newNode = nodeService.createNode(parentNodeRef, parentAssocType, parentAssocName, node
                .getType(), props);

        if (log.isDebugEnabled())
        {
            log.debug("Created new node (" + newNode.getChildRef() + ") parented by node " + newNode.getParentRef());
        }

        // Deal with the content properties
        writeContent(newNode.getChildRef(), contentProps);

        // Apply any aspects that are needed but haven't automatically been
        // applied
        Set<QName> aspects = new HashSet<QName>(node.getAspects());
        aspects.removeAll(nodeService.getAspects(newNode.getChildRef()));
        for (QName aspect : aspects)
        {
            nodeService.addAspect(newNode.getChildRef(), aspect, null);
        }

        // Is the node that we've just added the parent of any orphans that
        // we've found earlier?
        List<ChildAssociationRef> orphansToClaim = orphans.get(newNode.getChildRef());
        if (orphansToClaim != null)
        {
            // Yes, it is...
            for (ChildAssociationRef orphan : orphansToClaim)
            {
                logProgress("Re-parenting previously orphaned node (" + orphan.getChildRef() + ") with found parent " + orphan.getParentRef());
                nodeService.moveNode(orphan.getChildRef(), orphan.getParentRef(), orphan.getTypeQName(), orphan
                        .getQName());
            }
            // We can now remove the record of these orphans, as their parent
            // has been found
            orphans.remove(newNode.getChildRef());
        }
    }

    /**
     * 
     * @param node
     * @param resolvedNodes
     * @param primaryParentAssoc
     */
    private void update(TransferManifestNormalNode node, ResolvedParentChildPair resolvedNodes,
            ChildAssociationRef primaryParentAssoc)
    {
        NodeRef nodeToUpdate = resolvedNodes.resolvedChild;

        logProgress("Updating local node: " + node.getNodeRef());
        QName parentAssocType = primaryParentAssoc.getTypeQName();
        QName parentAssocName = primaryParentAssoc.getQName();
        NodeRef parentNodeRef = resolvedNodes.resolvedParent;
        if (parentNodeRef == null)
        {
            // We can't find the node's parent.
            // We'll store the node in a temporary location and record it for
            // later processing
            ChildAssociationRef tempLocation = getTemporaryLocation(node.getNodeRef());
            parentNodeRef = tempLocation.getParentRef();
            parentAssocType = tempLocation.getTypeQName();
            parentAssocName = tempLocation.getQName();
            storeOrphanNode(primaryParentAssoc);
        }
        // First of all, do we need to move the node? If any aspect of the
        // primary parent association has changed
        // then the answer is "yes"
        ChildAssociationRef currentParent = nodeService.getPrimaryParent(nodeToUpdate);
        if (!currentParent.getParentRef().equals(parentNodeRef)
                || !currentParent.getTypeQName().equals(parentAssocType)
                || !currentParent.getQName().equals(parentAssocName))
        {
            // Yes, we need to move the node
            nodeService.moveNode(nodeToUpdate, parentNodeRef, parentAssocType, parentAssocName);
            logProgress("Moved node " + nodeToUpdate + " to be under parent node " + parentNodeRef);
        }

        log.info("Resolved parent node to " + parentNodeRef);

        if (updateNeeded(node, nodeToUpdate))
        {

            // We need to process content properties separately.
            // First, create a shallow copy of the supplied property map...
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(node.getProperties());

            // Split out the content properties and sanitise the others
            Map<QName, Serializable> contentProps = processProperties(nodeToUpdate, props, false);

            // Update the non-content properties
            nodeService.setProperties(nodeToUpdate, props);

            // Deal with the content properties
            writeContent(nodeToUpdate, contentProps);

            // Blend the aspects together
            Set<QName> suppliedAspects = new HashSet<QName>(node.getAspects());
            Set<QName> existingAspects = nodeService.getAspects(nodeToUpdate);
            Set<QName> aspectsToRemove = new HashSet<QName>(existingAspects);

            aspectsToRemove.removeAll(suppliedAspects);
            suppliedAspects.removeAll(existingAspects);

            // Now aspectsToRemove contains the set of aspects to remove
            // and suppliedAspects contains the set of aspects to add
            for (QName aspect : suppliedAspects)
            {
                nodeService.addAspect(nodeToUpdate, aspect, null);
            }

            for (QName aspect : aspectsToRemove)
            {
                nodeService.removeAspect(nodeToUpdate, aspect);
            }
        }
    }

    /**
     * This method takes all the received properties and separates them into two parts. The content properties are
     * removed from the non-content properties such that the non-content properties remain in the "props" map and the
     * content properties are returned from this method Subsequently, any properties that are to be retained from the
     * local repository are copied over into the "props" map. The result of all this is that, upon return, "props"
     * contains all the non-content properties that are to be written to the local repo, and "contentProps" contains all
     * the content properties that are to be written to the local repo.
     * 
     * @param nodeToUpdate
     *            The noderef of the existing node in the local repo that is to be updated with these properties. May be
     *            null, indicating that these properties are destined for a brand new local node.
     * @param props
     * @return A map containing the content properties from the supplied "props" map
     */
    private Map<QName, Serializable> processProperties(NodeRef nodeToUpdate, Map<QName, Serializable> props,
            boolean isNew)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        // ...and copy any supplied content properties into this new map...
        for (Map.Entry<QName, Serializable> propEntry : props.entrySet())
        {
            Serializable value = propEntry.getValue();
            if (log.isDebugEnabled())
            {
                if (value == null)
                {
                    log.debug("Received a null value for property " + propEntry.getKey());
                }
            }
            if ((value != null) && ContentData.class.isAssignableFrom(value.getClass()))
            {
                contentProps.put(propEntry.getKey(), propEntry.getValue());
            }
        }

        // Now we can remove the content properties from amongst the other kinds
        // of properties
        // (no removeAll on a Map...)
        for (QName contentPropertyName : contentProps.keySet())
        {
            props.remove(contentPropertyName);
        }

        if (!isNew)
        {
            // Finally, overlay the repo-specific properties from the existing
            // node (if there is one)
            Map<QName, Serializable> existingProps = (nodeToUpdate == null) ? new HashMap<QName, Serializable>()
                    : nodeService.getProperties(nodeToUpdate);

            for (QName localProperty : getLocalProperties())
            {
                Serializable existingValue = existingProps.get(localProperty);
                if (existingValue != null)
                {
                    props.put(localProperty, existingValue);
                }
                else
                {
                    props.remove(localProperty);
                }
            }
        }
        return contentProps;
    }

    /**
     * @param node
     * @param nodeToUpdate
     * @param contentProps
     */
    private void writeContent(NodeRef nodeToUpdate, Map<QName, Serializable> contentProps)
    {
        File stagingDir = getStagingFolder();
        for (Map.Entry<QName, Serializable> contentEntry : contentProps.entrySet())
        {
            ContentData contentData = (ContentData) contentEntry.getValue();
            String contentUrl = contentData.getContentUrl();
            String fileName = contentUrl.substring(contentUrl.lastIndexOf('/') + 1);
            File stagedFile = new File(stagingDir, fileName);
            if (!stagedFile.exists())
            {
                error(MSG_REFERENCED_CONTENT_FILE_MISSING);
            }
            ContentWriter writer = contentService.getWriter(nodeToUpdate, contentEntry.getKey(), true);
            writer.setEncoding(contentData.getEncoding());
            writer.setMimetype(contentData.getMimetype());
            writer.setLocale(contentData.getLocale());
            writer.putContent(stagedFile);
        }
    }

    protected boolean updateNeeded(TransferManifestNormalNode node, NodeRef nodeToUpdate)
    {
        boolean updateNeeded = true;
        // Assumption: if the modified and modifier properties haven't changed, and the cm:content property
        // (if it exists) hasn't changed size then we can assume that properties don't need to be updated...
//        Map<QName, Serializable> suppliedProps = node.getProperties();
//        Date suppliedModifiedDate = (Date) suppliedProps.get(ContentModel.PROP_MODIFIED);
//        String suppliedModifier = (String) suppliedProps.get(ContentModel.PROP_MODIFIER);
//        ContentData suppliedContent = (ContentData) suppliedProps.get(ContentModel.PROP_CONTENT);
//
//        Map<QName, Serializable> existingProps = nodeService.getProperties(nodeToUpdate);
//        Date existingModifiedDate = (Date) existingProps.get(ContentModel.PROP_MODIFIED);
//        String existingModifier = (String) existingProps.get(ContentModel.PROP_MODIFIER);
//        ContentData existingContent = (ContentData) existingProps.get(ContentModel.PROP_CONTENT);
//
//        updateNeeded = false;
//        updateNeeded |= ((suppliedModifiedDate != null && !suppliedModifiedDate.equals(existingModifiedDate)) || 
//                (existingModifiedDate != null && !existingModifiedDate.equals(suppliedModifiedDate)));
//        updateNeeded |= ((suppliedContent != null && existingContent == null)
//                || (suppliedContent == null && existingContent != null) || (suppliedContent != null
//                && existingContent != null && suppliedContent.getSize() != existingContent.getSize()));
//        updateNeeded |= ((suppliedModifier != null && !suppliedModifier.equals(existingModifier)) || 
//                (existingModifier != null && !existingModifier.equals(suppliedModifier)));
        return updateNeeded;
    }

    /**
     * @return
     */
    protected Set<QName> getLocalProperties()
    {
        return DEFAULT_LOCAL_PROPERTIES;
    }

    /**
     * @param primaryParentAssoc
     */
    private void storeOrphanNode(ChildAssociationRef primaryParentAssoc)
    {
        List<ChildAssociationRef> orphansOfParent = orphans.get(primaryParentAssoc.getParentRef());
        if (orphansOfParent == null)
        {
            orphansOfParent = new ArrayList<ChildAssociationRef>();
            orphans.put(primaryParentAssoc.getParentRef(), orphansOfParent);
        }
        orphansOfParent.add(primaryParentAssoc);
    }

    /**
     * @param node
     * @param msgId
     */
    private void error(TransferManifestNode node, String msgId)
    {
        TransferProcessingException ex = new TransferProcessingException(msgId);
        log.error(ex.getMessage(), ex);
        throw ex;
    }

    /**
     * @param msgId
     */
    private void error(String msgId)
    {
        TransferProcessingException ex = new TransferProcessingException(msgId);
        log.error(ex.getMessage(), ex);
        throw ex;
    }

    protected void processHeader(TransferManifestHeader header)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.transfer.manifest.TransferManifestProcessor# startTransferManifest()
     */
    protected void startManifest()
    {
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService
     *            the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param nodeResolver
     *            the nodeResolver to set
     */
    public void setNodeResolver(CorrespondingNodeResolver nodeResolver)
    {
        this.nodeResolver = nodeResolver;
    }

}
