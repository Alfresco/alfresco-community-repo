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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transfer.CorrespondingNodeResolver.ResolvedParentChildPair;
import org.alfresco.repo.transfer.manifest.ManifestAccessControl;
import org.alfresco.repo.transfer.manifest.ManifestPermission;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author brian
 * 
 * The primary manifest processor is responsible for the first parsing the snapshot 
 * file and writing nodes into the receiving repository.
 * 
 * New nodes may be written into a "temporary" space if their primary parent node 
 * has not yet been transferred. 
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
    private PermissionService permissionService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private CorrespondingNodeResolver nodeResolver;
    private AlienProcessor alienProcessor;
       
    // State within this class
    /**
     * The header of the manifest
     */
    TransferManifestHeader header;

    /**
     * The list of orphans, during processing orphans are added and removed from this list.
     * If at the end of processing there are still orphans then an exception will be thrown.
     */
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
        logComment("Primary Processing incoming deleted node: " + node.getNodeRef());
        
        ChildAssociationRef origPrimaryParent = node.getPrimaryParentAssoc();
        NodeRef origNodeRef = new NodeRef(origPrimaryParent.getParentRef().getStoreRef(), node.getNodeRef().getId());

        CorrespondingNodeResolver.ResolvedParentChildPair resolvedNodes = nodeResolver.resolveCorrespondingNode(
                origNodeRef, origPrimaryParent, node.getParentPath());

        // Does a corresponding node exist in this repo?
        if (resolvedNodes.resolvedChild != null)
        {
            NodeRef exNode = resolvedNodes.resolvedChild;
            // Yes, it does. Delete it.
            if (log.isDebugEnabled())
            {
                log.debug("Incoming deleted noderef " + node.getNodeRef()
                        + " has been resolved to existing local noderef " + exNode
                        + "  - deleting");
            }
            
            logDeleted(node.getNodeRef(), exNode, nodeService.getPath(exNode));    
            
            delete(node, exNode);
        }
        else
        {
            logComment("Unable to find corresponding node for incoming deleted node: " + node.getNodeRef());
            if (log.isDebugEnabled())
            {
                log.debug("Incoming deleted noderef has no corresponding local noderef: " + node.getNodeRef()
                            + "  - ignoring");
            }
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
        logComment("Primary Processing incoming node: " + node.getNodeRef() + " --  Source path = " + node.getParentPath() + "/" + node.getPrimaryParentAssoc().getQName());

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
            // Yes, the corresponding node does exist. Update it.
            if (log.isDebugEnabled())
            {
                log.debug("Incoming noderef " + node.getNodeRef() + " has been resolved to existing local noderef "
                        + resolvedNodes.resolvedChild);
            }
            update(node, resolvedNodes, primaryParentAssoc);
        }
        else
        {
            // No, there is no corresponding node. 
            NodeRef archiveNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, node.getNodeRef().getId());
            if (nodeService.exists(archiveNodeRef))
            {
                // We have found a node in the archive store that has the same
                // UUID as the one that we've been sent.    If it remains it may cause problems later on
                // We delete from the archive store and treat the new node as a create.
                if (log.isInfoEnabled())
                {
                    log.info("Located an archived node with UUID matching transferred node: " + archiveNodeRef);
                    log.info("Attempting to restore " + archiveNodeRef);
                }
                logComment("Delete node from archive: " + archiveNodeRef);
                nodeService.deleteNode(archiveNodeRef);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Incoming noderef has no corresponding local noderef: " + node.getNodeRef());
            }
            create(node, resolvedNodes, primaryParentAssoc);
        }
    }

    /**
     * Create new node.
     * 
     * @param node
     * @param resolvedNodes
     * @param primaryParentAssoc
     */
    private void create(TransferManifestNormalNode node, ResolvedParentChildPair resolvedNodes,
            ChildAssociationRef primaryParentAssoc)
    {
        log.info("Creating new node with noderef " + node.getNodeRef());
        

        
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
            logComment("Unable to resolve parent for new incoming node. Storing it in temp folder: " + node.getNodeRef());
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
        Map<QName, Serializable> contentProps = processProperties(null, props, null);
        
        injectTransferred(props);
            
        // Remove the invadedBy property since that is used by the transfer service 
        // and is local to this repository.
        props.remove(TransferModel.PROP_INVADED_BY);
         
        // Do we need to worry about locking this new node ?
        if(header.isReadOnly())
        {
            log.debug("new node needs to be locked");
            props.put(ContentModel.PROP_LOCK_OWNER, AuthenticationUtil.getAdminUserName());
            props.put(ContentModel.PROP_LOCK_TYPE, LockType.NODE_LOCK.toString());
            props.put(ContentModel.PROP_EXPIRY_DATE, null);
         }
 
        // Create the corresponding node...
        ChildAssociationRef newNode = nodeService.createNode(parentNodeRef, parentAssocType, parentAssocName, node
                .getType(), props);
        
        if (log.isDebugEnabled())
        {
            log.debug("Created new node (" + newNode.getChildRef() + ") parented by node " + newNode.getParentRef());
        }
        
        logCreated(node.getNodeRef(), newNode.getChildRef(), newNode.getParentRef(), nodeService.getPath(newNode.getChildRef()), false);
        
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
        
        ManifestAccessControl acl = node.getAccessControl();        
        // Apply new ACL to this node
        if(acl != null)
        {
            permissionService.setInheritParentPermissions(newNode.getChildRef(), acl.isInherited());
            
            if(acl.getPermissions() != null)
            {
                for(ManifestPermission permission : acl.getPermissions())
                {
                    log.debug("setting permission on node");
                    AccessStatus status = AccessStatus.valueOf(permission.getStatus());
                    // The node has its own access control list
                    permissionService.setPermission(newNode.getChildRef(), 
                        permission.getAuthority(), 
                        permission.getPermission(), 
                        status == AccessStatus.ALLOWED);
                }
            }
        }
        
        /**
         * are we adding an alien node here? The transfer service has policies disabled 
         * so have to call the consequence of the policy directly.
         */ 
        if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED) || nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
        {
            alienProcessor.onCreateChild(newNode, header.getRepositoryId(), true);
        }

        // Is the node that we've just added the parent of any orphans that
        // we've found earlier?
        checkOrphans(newNode.getChildRef());
    }
    
    /**
     * Delete this node
     * @param exNode
     */
    protected void delete(TransferManifestDeletedNode node, NodeRef nodeToDelete)
    {
        if(alienProcessor.isAlien(nodeToDelete))
        {
            logComment("Node contains alien content and can't be deleted: " + nodeToDelete);
            if (log.isDebugEnabled())
            {
                log.debug("Node to be deleted is alien prune rather than delete: " + nodeToDelete);
            }
            alienProcessor.pruneNode(nodeToDelete, header.getRepositoryId());
        }
        else
        {
            /**
             * Check that if the destination is "from" the transferring repo if it is "from" another repo then ignore
             */
            if(nodeService.hasAspect(nodeToDelete, TransferModel.ASPECT_TRANSFERRED))
            {
                String fromRepository = (String)nodeService.getProperty(nodeToDelete, TransferModel.PROP_FROM_REPOSITORY_ID);
                String transferringRepo = header.getRepositoryId();
                
                if(fromRepository != null && transferringRepo != null)
                {
                    if(!fromRepository.equalsIgnoreCase(transferringRepo))
                    {
                        logComment("Not deleting local node (not from the transferring repository): " + nodeToDelete);
                        return;
                    }
                }
            }
            
            // Not alien or from another repo - delete it.
            logDeleted(node.getNodeRef(), nodeToDelete, nodeService.getPath(nodeToDelete));
            
            nodeService.deleteNode(nodeToDelete);
            if (log.isDebugEnabled())
            {
                log.debug("Deleted local node: " + nodeToDelete);
            }
        }
    }

    private void checkOrphans(NodeRef parentNode)
    {
        List<ChildAssociationRef> orphansToClaim = orphans.get(parentNode);
        if (orphansToClaim != null)
        {
            // Yes, it is...
            for (ChildAssociationRef orphan : orphansToClaim)
            {
                logComment("Re-parenting previously orphaned node (" + orphan.getChildRef() + ") with found parent " + orphan.getParentRef());
                ChildAssociationRef newRef = nodeService.moveNode(orphan.getChildRef(), orphan.getParentRef(), orphan.getTypeQName(), orphan
                        .getQName());
                
                /**
                 * We may be creating an alien node here and the policies are turned off.
                 */
                if(nodeService.hasAspect(newRef.getParentRef(), TransferModel.ASPECT_TRANSFERRED))
                {
                    alienProcessor.onCreateChild(newRef, header.getRepositoryId(), true);
                }
            }
            // We can now remove the record of these orphans, as their parent
            // has been found
            orphans.remove(parentNode);
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
        

        /**
         * Check that if the destination is "from" the transferring repo if it is "from" another repo then ignore
         */
        if(nodeService.hasAspect(nodeToUpdate, TransferModel.ASPECT_TRANSFERRED))
        {
            String fromRepository = (String)nodeService.getProperty(nodeToUpdate, TransferModel.PROP_FROM_REPOSITORY_ID);
            String transferringRepo = header.getRepositoryId();
            
            if(fromRepository != null && transferringRepo != null)
            {
                if(!fromRepository.equalsIgnoreCase(transferringRepo))
                {
                    logComment("Not updating local node (not from the transferring repository): " + node.getNodeRef());
                    return;
                }
            }
        }
        
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
            
            /**
             * Yes, the parent assoc has changed so we need to move the node
             */
            if(nodeService.hasAspect(currentParent.getParentRef(), TransferModel.ASPECT_ALIEN))
            {
                // old parent node ref may be alien so treat as a delete
                alienProcessor.beforeDeleteAlien(currentParent.getChildRef(), null);
            }
            
            // Yes, we need to move the node
            ChildAssociationRef newNode = nodeService.moveNode(nodeToUpdate, parentNodeRef, parentAssocType, parentAssocName);
            logMoved(node.getNodeRef(), nodeToUpdate, node.getParentPath(), newNode.getParentRef(), nodeService.getPath(newNode.getChildRef()));
            
            /**
             * are we adding an alien node here? The transfer service has policies disabled 
             * so have to call the consequence of the policy directly.
             */ 
            if(nodeService.hasAspect(newNode.getChildRef(), TransferModel.ASPECT_ALIEN))
            {
                alienProcessor.afterMoveAlien(newNode); 
            }
            else
            {    
                /**
                 * are we adding an alien node here? The transfer service has policies disabled 
                 * so have to call the consequence of the policy directly.
                 */ 
                if(nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_TRANSFERRED) || nodeService.hasAspect(parentNodeRef, TransferModel.ASPECT_ALIEN))
                {
                    alienProcessor.onCreateChild(newNode, header.getRepositoryId(), true);
                }
            }
        }

        log.info("Resolved parent node to " + parentNodeRef);

        if (updateNeeded(node, nodeToUpdate))
        {

            logUpdated(node.getNodeRef(), nodeToUpdate, nodeService.getPath(nodeToUpdate));
            
            // We need to process content properties separately.
            // First, create a shallow copy of the supplied property map...
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(node.getProperties());
            Map<QName, Serializable> existingProps = nodeService.getProperties(nodeToUpdate);
            
            // inject transferred properties/aspect here
            injectTransferred(props);
             
            // Remove the invadedBy property since that is used by the transfer service 
            // and is local to this repository.
            props.remove(TransferModel.PROP_INVADED_BY);
            
            // Do we need to worry about locking this updated ?
            if(header.isReadOnly())
            {
                props.put(ContentModel.PROP_LOCK_OWNER, AuthenticationUtil.getAdminUserName());
                props.put(ContentModel.PROP_LOCK_TYPE, LockType.NODE_LOCK.toString());
                props.put(ContentModel.PROP_EXPIRY_DATE, null);
                log.debug("updated node needs to be locked");
            }

            // Split out the content properties and sanitise the others
            Map<QName, Serializable> contentProps = processProperties(nodeToUpdate, props, existingProps);
            
            // If there was already a value for invadedBy then leave it alone rather than replacing it.
            if(existingProps.containsKey(TransferModel.PROP_INVADED_BY))
            {
                props.put(TransferModel.PROP_INVADED_BY, existingProps.get(TransferModel.PROP_INVADED_BY));
            }

            // Update the non-content properties
            nodeService.setProperties(nodeToUpdate, props);

            // Deal with the content properties
            writeContent(nodeToUpdate, contentProps);
            
            // Blend the aspects together
            Set<QName> suppliedAspects = new HashSet<QName>(node.getAspects());
            Set<QName> existingAspects = nodeService.getAspects(nodeToUpdate);
            Set<QName> aspectsToRemove = new HashSet<QName>(existingAspects);
            
            // Add mandatory aspects to the supplied aspects (eg. should not explicitly remove auditable aspect from a folder - see also DMDeploymentTarget for similar)
            List<AspectDefinition> aspectDefs = dictionaryService.getType(nodeService.getType(nodeToUpdate)).getDefaultAspects(true);
            for (AspectDefinition aspectDef : aspectDefs)
            {
                suppliedAspects.add(aspectDef.getName());
            }
            
            if(header.isReadOnly())
            {
                suppliedAspects.add(ContentModel.ASPECT_LOCKABLE);
            }
            
            aspectsToRemove.removeAll(suppliedAspects);
            
            /**
             * Don't remove the aspects that the transfer service uses itself.
             */
            aspectsToRemove.remove(TransferModel.ASPECT_TRANSFERRED);
            aspectsToRemove.remove(TransferModel.ASPECT_ALIEN);
            
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
            
            // Check the ACL of this updated node
            
            ManifestAccessControl acl = node.getAccessControl();
            if(acl != null)
            {
                boolean existInherit = permissionService.getInheritParentPermissions(nodeToUpdate);
                if(existInherit != acl.isInherited())
                {
                    log.debug("changed inherit permissions flag");
                    permissionService.setInheritParentPermissions(nodeToUpdate, acl.isInherited());
                }
                
                Set<AccessPermission> existingPermissions = permissionService.getAllSetPermissions(nodeToUpdate);
                List<ManifestPermission> newPermissions = acl.getPermissions();
                
                if(existingPermissions.size() > 0 || newPermissions != null)
                {
                    // Yes we have explicit permissions on this node.
                    log.debug("have to check permissions");

                    Set<ManifestPermission>work = new HashSet<ManifestPermission>();
                    for(AccessPermission permission : existingPermissions)
                    {
                        if(permission.isSetDirectly())
                        {
                            ManifestPermission p = new ManifestPermission();
                            p.setAuthority(permission.getAuthority());
                            p.setPermission(permission.getPermission());
                            p.setStatus(permission.getAccessStatus().toString());
                            work.add(p);
                        }
                    }                    
                
                    // Do we need to check whether to add any permissions ?
                    if(newPermissions != null)
                    {
                        // Do we need to add any permissions ?
                        for(ManifestPermission permission : acl.getPermissions())
                        {
                            if(!work.contains(permission))
                            {
                                log.debug("setting permission on node:" + permission);
                                AccessStatus status = AccessStatus.valueOf(permission.getStatus());
                                permissionService.setPermission(nodeToUpdate, 
                                        permission.getAuthority(), 
                                        permission.getPermission(), 
                                        status == AccessStatus.ALLOWED);
                            }
                        }
                    
                        // Remove permissions from "work" that should be there
                        work.removeAll(newPermissions);
                    
                    }
                    
                    // Do we need to remove any permissions
                    for(ManifestPermission permission : work)
                    {
                        log.debug("removing permission on node:" + permission);
                        permissionService.deletePermission(nodeToUpdate, permission.getAuthority(), permission.getPermission());
                    }
                }
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
     * @param props the new properties
     * @param the existing properties, null if this is a create
     * @return A map containing the content properties which are going to be replaced from the supplied "props" map
     */
    private Map<QName, Serializable> processProperties(NodeRef nodeToUpdate, Map<QName, Serializable> props,
            Map<QName, Serializable> existingProps)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        // ...and copy any supplied content properties into this new map...
        for (Map.Entry<QName, Serializable> propEntry : props.entrySet())
        {
            Serializable value = propEntry.getValue();
            QName key = propEntry.getKey();
            if (log.isDebugEnabled())
            {
                if (value == null)
                {
                    log.debug("Received a null value for property " + propEntry.getKey());
                }
            }
            if ((value != null) && ContentData.class.isAssignableFrom(value.getClass()))
            {
                if(existingProps != null)
                {
                    // This is an update and we have content data 
                    File stagingDir = getStagingFolder();
                    ContentData contentData = (ContentData) propEntry.getValue();
                    String contentUrl = contentData.getContentUrl();
                    String fileName = TransferCommons.URLToPartName(contentUrl);
                    File stagedFile = new File(stagingDir, fileName);
                    if (stagedFile.exists())
                    {
                        if(log.isDebugEnabled())
                        {
                            log.debug("replace content for node:" + nodeToUpdate + ", " + key);
                        }
                        // Yes we are going to replace the content item
                        contentProps.put(propEntry.getKey(), propEntry.getValue());
                    }
                    else
                    {    
                        // Staging file does not exist
                        if(props.containsKey(key))
                        {
                            if(log.isDebugEnabled())
                            {
                                log.debug("keep existing content for node:" + nodeToUpdate + ", " + key);
                            }
                            // keep the existing content value
                            props.put(propEntry.getKey(), existingProps.get(key));
                        } 
                    }
                }
                else
                {
                    // This is a create so all content items are new
                    contentProps.put(propEntry.getKey(), propEntry.getValue());
                }                  
            }
        }

        // Now we can remove the content properties from amongst the other kinds
        // of properties
        // (no removeAll on a Map...)
        for (QName contentPropertyName : contentProps.keySet())
        {
            props.remove(contentPropertyName);
        }

        if (existingProps != null)
        {
            // Finally, overlay the repo-specific properties from the existing
            // node (if there is one)
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
            if(contentUrl == null || contentUrl.isEmpty())
            {
                log.debug("content data is null or empty:" + nodeToUpdate);
                ContentData cd = new ContentData(null, null, 0, null);
                nodeService.setProperty(nodeToUpdate, contentEntry.getKey(), cd);
            }
            else
            {
                String fileName = TransferCommons.URLToPartName(contentUrl);
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
        // squirrel away the header for later use
        this.header = header;
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
     * @param dictionaryService
     *            the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeResolver
     *            the nodeResolver to set
     */
    public void setNodeResolver(CorrespondingNodeResolver nodeResolver)
    {
        this.nodeResolver = nodeResolver;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    /**
     * inject transferred
     */
    private void injectTransferred(Map<QName, Serializable> props)
    {       
        if(!props.containsKey(TransferModel.PROP_REPOSITORY_ID))
        {
            log.debug("injecting repositoryId property");
            props.put(TransferModel.PROP_REPOSITORY_ID, header.getRepositoryId());
        }
        props.put(TransferModel.PROP_FROM_REPOSITORY_ID, header.getRepositoryId());
        
        /**
         * For each property
         */
        List<String> contentProps = new ArrayList();
        for (Serializable value : props.values())
        {
            if ((value != null) && ContentData.class.isAssignableFrom(value.getClass()))
            {
                ContentData srcContent = (ContentData)value;

                if(srcContent.getContentUrl() != null && !srcContent.getContentUrl().isEmpty())
                {
                    log.debug("adding part name to from content field");
                    contentProps.add(TransferCommons.URLToPartName(srcContent.getContentUrl()));
                }  
            }
        }
        
        props.put(TransferModel.PROP_FROM_CONTENT, (Serializable)contentProps);
    }

    public void setAlienProcessor(AlienProcessor alienProcessor)
    {
        this.alienProcessor = alienProcessor;
    }

    public AlienProcessor getAlienProcessor()
    {
        return alienProcessor;
    }

}
