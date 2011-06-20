/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface for public and internal version operations.
 * 
 * @author Roy Wetherall, janv
 */
public interface VersionService
{
    /**
     * The version store protocol label, used in store references
     */
    public static final String VERSION_STORE_PROTOCOL = "versionStore";
    
    /**
     * Gets the reference to the version store
     * 
     * @return  reference to the version store
     */
    @Auditable
    public StoreRef getVersionStoreReference();
    
    /**
     * Creates a new version based on the referenced node.
     * <p>
     * If the node has not previously been versioned then a version history and
     * initial version will be created.
     * <p>
     * If the node referenced does not or can not have the version aspect
     * applied to it then an exception will be raised.
     * <p>
     * The version properties are stored as version meta-data against the newly
     * created version.
     * 
     * @param  nodeRef              a node reference
     * @param  versionProperties    the version properties that are stored with the newly created
     *                              version, or <tt>null</tt> if there are no relevant properties
     * @return                      the created version object
     * @throws ReservedVersionNameException
     *                              thrown if a reserved property name is used int he version properties 
     *                              provided
     * @throws AspectMissingException
     *                              thrown if the version aspect is missing                              
     */
    @Auditable(parameters = {"nodeRef", "versionProperties"})
    public Version createVersion(
            NodeRef nodeRef, 
            Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException;

    /**
     * Creates a new version based on the referenced node.
     * <p>
     * If the node has not previously been versioned then a version history and
     * initial version will be created.
     * <p>
     * If the node referenced does not or can not have the version aspect
     * applied to it then an exception will be raised.
     * <p>
     * The version properties are stored as version meta-data against the newly
     * created version.
     * 
     * @param nodeRef               a node reference
     * @param versionProperties     the version properties that are stored with the newly created
     *                              version
     * @param versionChildren       if true then the children of the referenced node are also
     *                              versioned, false otherwise
     * @return                      the created version object(s)
     * @throws ReservedVersionNameException
     *                              thrown if a reserved property name is used int he version properties 
     *                              provided
     * @throws AspectMissingException
     *                              thrown if the version aspect is missing
     */
    @Auditable(parameters = {"nodeRef", "versionProperties", "versionChildren"})
    public Collection<Version> createVersion(
            NodeRef nodeRef, 
            Map<String, Serializable> versionProperties,
            boolean versionChildren)
            throws ReservedVersionNameException, AspectMissingException;

    /**
     * Creates new versions based on the list of node references provided.
     * 
     * @param nodeRefs              a list of node references
     * @param versionProperties     version property values
     * @return                      a collection of newly created versions
     * @throws ReservedVersionNameException
     *                              thrown if a reserved property name is used in the version properties 
     *                              provided
     * @throws AspectMissingException
     *                              thrown if the version aspect is missing
     */
    @Auditable(parameters = {"nodeRef", "versionProperties"})
    public Collection<Version> createVersion(
            Collection<NodeRef> nodeRefs, 
            Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException;

    /**
     * Gets the version history information for a node.
     * <p>
     * If the node has not been versioned then null is returned.
     * <p>
     * If the node referenced does not or can not have the version aspect
     * applied to it then an exception will be raised.
     * 
     * @param  nodeRef  a node reference
     * @return          the version history information
     * @throws AspectMissingException
     *                  thrown if the version aspect is missing
     */
    @Auditable(parameters = {"nodeRef"})
    public VersionHistory getVersionHistory(NodeRef nodeRef)
        throws AspectMissingException;     
	
	/**
	 * Gets the version object for the current version of the node reference
	 * passed.
	 * <p>
	 * Returns null if the node is not versionable or has not been versioned.
	 * @param nodeRef   the node reference
	 * @return			the version object for the current version
	 */
    @Auditable(parameters = {"nodeRef"})
	public Version getCurrentVersion(NodeRef nodeRef);
	
	/**
	 * The node reference will be reverted to the current version.
     * <p>
     * A deep revert will be performed.
	 * 
	 * @see VersionService#revert(NodeRef, Version, boolean)
	 * 
	 * @param 	nodeRef					the node reference
	 */
    @Auditable(parameters = {"nodeRef"})
	public void revert(NodeRef nodeRef);
    
    /**
     * The node reference will be reverted to the current version.
     * 
     * @see VersionService#revert(NodeRef, Version, boolean)
     * 
     * @param nodeRef                       the node reference
     * @param deep                          true if a deep revert is to be performed, flase otherwise
     */
    @Auditable(parameters = {"nodeRef", "deep"})
    public void revert(NodeRef nodeRef, boolean deep);
    
    /**
     * A deep revert will take place by default.
     * 
     * @see VersionService#revert(NodeRef, Version, boolean)
     *  
     * @param nodeRef   the node reference
     * @param version   the version to revert to
     */
    @Auditable(parameters = {"nodeRef", "version"})
    public void revert(NodeRef nodeRef, Version version);
	
	/**
	 * Revert the state of the node to the specified version.  
	 * <p>
	 * Any changes made to the node will be lost and the state of the node will reflect
	 * that of the version specified.
	 * <p>
	 * The version label property on the node reference will remain unchanged. 
	 * <p>
	 * If the node is further versioned then the new version will be created at the head of 
	 * the version history graph.  A branch will not be created.
     * <p>
     * If a deep revert is to be performed then any child nodes that are no longer present will
     * be deep restored (if appropriate) otherwise child associations to deleted, versioned nodes
     * will not be restored.
	 * 
	 * @param 	nodeRef			the node reference
	 * @param 	version			the version to revert to
     * @param   deep            true is a deep revert is to be performed, false otherwise.
	 */
    @Auditable(parameters = {"nodeRef", "version", "deep"})
	public void revert(NodeRef nodeRef, Version version, boolean deep);
    
    /**
     * By default a deep restore is performed.
     * 
     * @see org.alfresco.service.cmr.version.VersionService#restore(NodeRef, NodeRef, QName, QName, boolean)
     * 
     * @param nodeRef           the node reference to a node that no longer exists in the store
     * @param parentNodeRef     the new parent of the restored node
     * @param assocTypeQName    the assoc type qname
     * @param assocQName        the assoc qname
     * @return                  the newly restored node reference
     */
    @Auditable(parameters = {"nodeRef", "parentNodeRef", "assocTypeQName", "assocQName"})
    public NodeRef restore(
            NodeRef nodeRef,
            NodeRef parentNodeRef, 
            QName assocTypeQName,
            QName assocQName);
    
    /**
     * Restores a node not currently present in the store, but that has a version
     * history.
     * <p>
     * The restored node will be at the head (most resent version).
     * <p>
     * Restoration will fail if there is no version history for the specified node id in
     * the specified store.
     * <p>
     * If the node already exists in the store then an exception will be raised.
     * <p>
     * Once the node is restored it is reverted to the head version in the appropriate 
     * version history tree.  If deep is set to true then this will be a deep revert, false 
     * otherwise.
     * 
     * @param nodeRef           the node reference to a node that no longer exists in 
     *                          the store
     * @param parentNodeRef     the new parent of the restored node
     * @param assocTypeQName    the assoc type qname
     * @param assocQName        the assoc qname  
     * @param deep              true is a deep revert should be performed once the node has been 
     *                          restored, false otherwise
     * @return                  the newly restored node reference                            
     */
    @Auditable(parameters = {"nodeRef", "parentNodeRef", "assocTypeQName", "assocQName", "deep"})
    public NodeRef restore(
            NodeRef nodeRef,
            NodeRef parentNodeRef, 
            QName assocTypeQName,
            QName assocQName,
            boolean deep);
	
	/**
	 * Delete the version history associated with a node reference.
	 * <p>
	 * This operation is permanent, all versions in the version history are
	 * deleted and cannot be retrieved.
	 * <p>
	 * The current version label for the node reference is reset and any subsequent versions
	 * of the node will result in a new version history being created.
	 * 
	 * @param 	nodeRef					the node reference
	 * @throws	AspectMissingException	thrown if the version aspect is missing
	 */
    @Auditable(parameters = {"nodeRef"})
	public void deleteVersionHistory(NodeRef nodeRef)
		throws AspectMissingException;
    
    /**
     * Delete a specific version associated with a node reference.
     * <p>
     * This operation is permanent, the specific version in the version history is
     * deleted and cannot be retrieved.
     * <p> 
     * If this is the last version, then the current version label for the node reference is 
     * reset and any subsequent versions of the node will result in a new version history being created.
     *  
     * @param nodeRef   the node reference
     * @param version   the version to delete
     */
    @Auditable(parameters = {"nodeRef", "version"})
    public void deleteVersion(NodeRef nodeRef, Version version);
    
    /**
     * Ensures that a node has the versionable aspect applied to it, and has
     *  at least an initial entry in the version store. 
     * If any of these requirements are missing, then they will be fixed.
     *  
     * @param nodeRef             the node reference
     * @param  versionProperties  the version properties to apply if versioning
     *                            isn't currently enabled for the node
     */
    @Auditable(parameters = {"nodeRef"})
    public void ensureVersioningEnabled(
            NodeRef nodeRef, 
            Map<QName, Serializable> versionProperties);
    
    /**
     * Register a version label policy
     * 
     * @param typeQName the QName of the type to register
     * @param policy the policy to register for the specified type
     */
    public void registerVersionLabelPolicy(QName typeQName, CalculateVersionLabelPolicy policy);
}
