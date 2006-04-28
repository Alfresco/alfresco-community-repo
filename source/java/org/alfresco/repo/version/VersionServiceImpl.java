/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.version.common.AbstractVersionServiceImpl;
import org.alfresco.repo.version.common.VersionHistoryImpl;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.repo.version.common.counter.VersionCounterService;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.ReservedVersionNameException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;

/**
 * The version service implementation.
 * 
 * @author Roy Wetheral
 */
public class VersionServiceImpl extends AbstractVersionServiceImpl
								implements VersionService, VersionModel
{
    /**
     * Error message I18N id's
     */
    private static final String MSGID_ERR_NOT_FOUND = "version_service.err_not_found";
    private static final String MSGID_ERR_NO_BRANCHES = "version_service.err_unsupported";
    private static final String MSGID_ERR_RESTORE_EXISTS = "version_service.err_restore_exists";
    private static final String MSGID_ERR_ONE_PRECEEDING = "version_service.err_one_preceeding";
    private static final String MSGID_ERR_RESTORE_NO_VERSION = "version_service.err_restore_no_version";
    private static final String MSGID_ERR_REVERT_MISMATCH = "version_service.err_revert_mismatch";
    
    /**
     * The version counter service
     */
    private VersionCounterService versionCounterService;
    
    /**
     * The db node service, used as the version store implementation
     */
    protected NodeService dbNodeService;
    
    /**
     * Policy behaviour filter
     */
    private BehaviourFilter policyBehaviourFilter;

    /**
     * The repository searcher
     */
    @SuppressWarnings("unused")
    private SearchService searcher;       	
    
    /**
     * Sets the db node service, used as the version store implementation
     * 
     * @param nodeService  the node service
     */
    public void setDbNodeService(NodeService nodeService)
    {
        this.dbNodeService = nodeService;
    }

    /**
     * @param searcher  the searcher
     */
    public void setSearcher(SearchService searcher)
    {
        this.searcher = searcher; 
    }
	
    /**
     * @param versionCounterService  the version counter service
     */
    public void setVersionCounterService(VersionCounterService versionCounterService)
    {
        this.versionCounterService = versionCounterService;
    }    
    
    /**
     * Set the policy behaviour filter
     * 
     * @param policyBehaviourFilter     the policy behaviour filter
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
    
	/**
	 * Initialise method
	 */
	@Override
    public void initialise()
    {
		super.initialise();
		
		// Register the serial version label behaviour
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "calculateVersionLabel"),
				ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(new SerialVersionLabelPolicy(), "calculateVersionLabel"));
	}
    
	/**
     * Gets the reference to the version store
     * 
     * @return  reference to the version store
     */
    public StoreRef getVersionStoreReference()
    {
        return new StoreRef(
                StoreRef.PROTOCOL_WORKSPACE,
				VersionModel.STORE_ID);
    }
	
    /**
     * @see VersionCounterService#nextVersionNumber(StoreRef)
     */
    public Version createVersion(
            NodeRef nodeRef, 
            Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException
    {
        // Get the next version number
        int versionNumber = this.versionCounterService.nextVersionNumber(getVersionStoreReference());
        
        // Create the version
        return createVersion(nodeRef, versionProperties, versionNumber);
    }        

    /**
     * The version's are created from the children upwards with the parent being created first.  This will
     * ensure that the child version references in the version node will point to the version history nodes
     * for the (possibly) newly created version histories.
     */
    public Collection<Version> createVersion(
            NodeRef nodeRef, 
            Map<String, Serializable> versionProperties,
            boolean versionChildren)
            throws ReservedVersionNameException, AspectMissingException
    {
        // Get the next version number
        int versionNumber = this.versionCounterService.nextVersionNumber(getVersionStoreReference());
        
        // Create the versions
        return createVersion(nodeRef, versionProperties, versionChildren, versionNumber);
    }
    
    /**
     * Helper method used to create the version when the versionChildren flag is provided.  This method
     * ensures that all the children (if the falg is set to true) are created with the same version 
     * number, this ensuring that the version stripe is correct.
     * 
     * @param nodeRef                           the parent node reference
     * @param versionProperties                 the version properties
     * @param versionChildren                   indicates whether to version the children of the parent
     *                                          node
     * @param versionNumber                     the version number
     
     * @return                                  a collection of the created versions
     * @throws ReservedVersionNameException     thrown if there is a reserved version property name clash
     * @throws AspectMissingException    thrown if the version aspect is missing from a node
     */
    private Collection<Version> createVersion(
            NodeRef nodeRef, 
            Map<String, Serializable> versionProperties,
            boolean versionChildren,
            int versionNumber) 
            throws ReservedVersionNameException, AspectMissingException
    {

        Collection<Version> result = new ArrayList<Version>();
        
        if (versionChildren == true)
        {
            // Get the children of the node
            Collection<ChildAssociationRef> children = this.dbNodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssoc : children)
            {
                // Recurse into this method to version all the children with the same version number
                Collection<Version> childVersions = createVersion(
                        childAssoc.getChildRef(), 
                        versionProperties, 
                        versionChildren, 
                        versionNumber);
                result.addAll(childVersions);
            }
        }
        
        result.add(createVersion(nodeRef, versionProperties, versionNumber));
        
        return result;
    }

    /**
     * Note:  we can't control the order of the list, so if we have children and parents in the list and the
     * parents get versioned before the children and the children are not already versioned then the parents 
     * child references will be pointing to the node ref, rather than the verison history.
     */
    public Collection<Version> createVersion(
            Collection<NodeRef> nodeRefs, 
            Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException
    {
        Collection<Version> result = new ArrayList<Version>(nodeRefs.size());
        
        // Get the next version number
        int versionNumber = this.versionCounterService.nextVersionNumber(getVersionStoreReference());
        
        // Version each node in the list
        for (NodeRef nodeRef : nodeRefs)
        {
            result.add(createVersion(nodeRef, versionProperties, versionNumber));
        }
        
        return result;
    }
    
    /**
     * Creates a new version of the passed node assigning the version properties 
     * accordingly.
     * 
     * @param  nodeRef              a node reference
     * @param  versionProperties    the version properties
     * @param  versionNumber        the version number
     * @return                      the newly created version
     * @throws ReservedVersionNameException
     *                              thrown if there is a name clash in the version properties   
     */
    private Version createVersion(
            NodeRef nodeRef, 
            Map<String, Serializable> origVersionProperties, 
            int versionNumber)
            throws ReservedVersionNameException
    {

		// Copy the version properties (to prevent unexpected side effects to the caller)
		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        if (origVersionProperties != null)
        {
            versionProperties.putAll(origVersionProperties);
        }
		
        // If the version aspect is not there then add it
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        }
        
        // Call the policy behaviour
		invokeBeforeCreateVersion(nodeRef);		
        
        // Check that the supplied additional version properties do not clash with the reserved ones
        VersionUtil.checkVersionPropertyNames(versionProperties.keySet());
        
        // Check the repository for the version history for this node
        NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef); 
        NodeRef currentVersionRef = null;
        
        if (versionHistoryRef == null)
        {
            HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(PROP_QNAME_VERSIONED_NODE_ID, nodeRef.getId());
            
            // Create a new version history node
            ChildAssociationRef childAssocRef = this.dbNodeService.createNode(
                    getRootNode(), 
					ContentModel.ASSOC_CHILDREN, 
                    CHILD_QNAME_VERSION_HISTORIES,
                    TYPE_QNAME_VERSION_HISTORY,
                    props);
            versionHistoryRef = childAssocRef.getChildRef();            
        }
        else
        {
            // Since we have an exisiting version history we should be able to lookup
            // the current version
            currentVersionRef = getCurrentVersionNodeRef(versionHistoryRef, nodeRef);     
            
            if (currentVersionRef == null)
            {
                throw new VersionServiceException(MSGID_ERR_NOT_FOUND);
            }
            
            // Need to check that we are not about to create branch since this is not currently supported
            VersionHistory versionHistory = buildVersionHistory(versionHistoryRef, nodeRef);
            Version currentVersion = getVersion(currentVersionRef);
            if (versionHistory.getSuccessors(currentVersion).size() != 0)
            {
                throw new VersionServiceException(MSGID_ERR_NO_BRANCHES);
            }
        }
        
		// Create the node details
		QName classRef = this.nodeService.getType(nodeRef);
		PolicyScope nodeDetails = new PolicyScope(classRef);
		
		// Get the node details by calling the onVersionCreate policy behaviour
		invokeOnCreateVersion(nodeRef, versionProperties, nodeDetails);
		
		// Create the new version node (child of the version history)
        NodeRef newVersionRef = createNewVersion(
                nodeRef, 
                versionHistoryRef,
                getStandardVersionProperties(versionProperties, nodeRef, currentVersionRef, versionNumber),
                versionProperties, 
                nodeDetails);
        
        if (currentVersionRef == null)
        {
            // Set the new version to be the root version in the version history
            this.dbNodeService.createAssociation(
                    versionHistoryRef, 
                    newVersionRef, 
                    VersionServiceImpl.ASSOC_ROOT_VERSION);
        }
        else
        {
            // Relate the new version to the current version as its successor
            this.dbNodeService.createAssociation(
                    currentVersionRef, 
                    newVersionRef, 
                    VersionServiceImpl.ASSOC_SUCCESSOR);
        }
        
        // Create the version data object
        Version version = getVersion(newVersionRef);
        
        // Set the new version label on the versioned node
        this.nodeService.setProperty(
                nodeRef, 
                ContentModel.PROP_VERSION_LABEL, 
                version.getVersionLabel());
        
        // Return the data object representing the newly created version
        return version;
    }

    /**
     * @see org.alfresco.service.cmr.version.VersionService#getVersionHistory(NodeRef)
     */
    public VersionHistory getVersionHistory(NodeRef nodeRef)
    {
        VersionHistory versionHistory = null;
        
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
        {
            NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef);
            if (versionHistoryRef != null)
            {
                versionHistory = buildVersionHistory(versionHistoryRef, nodeRef);
            }
        }
            
        return versionHistory;
    }       
	
	/**
	 * @see VersionService#getCurrentVersion(NodeRef)
	 */
	public Version getCurrentVersion(NodeRef nodeRef)
	{
		Version version = null;
		
		if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
		{
			VersionHistory versionHistory = getVersionHistory(nodeRef);
			if (versionHistory != null)
			{
				String versionLabel = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
				version = versionHistory.getVersion(versionLabel);
			}
		}
		
		return version;
	}
	
    /**
     * Get a map containing the standard list of version properties populated.
     * 
     * @param versionProperties     the version meta data properties
     * @param nodeRef               the node reference
     * @param preceedingNodeRef     the preceeding node reference
     * @param versionNumber         the version number
     * @return                      the standard version properties
     */
	private Map<QName, Serializable> getStandardVersionProperties(Map<String, Serializable> versionProperties, NodeRef nodeRef, NodeRef preceedingNodeRef, int versionNumber)
	{
        Map<QName, Serializable> result = new HashMap<QName, Serializable>(10);
        
		// Set the version number for the new version
        result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_VERSION_NUMBER), Integer.toString(versionNumber));
		
		// Set the versionable node id
        result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_NODE_ID), nodeRef.getId());
		
		// Set the versionable node store protocol
        result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL), nodeRef.getStoreRef().getProtocol());
		
		// Set the versionable node store id
        result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_NODE_STORE_ID), nodeRef.getStoreRef().getIdentifier());
        
        // Store the current node type
        QName nodeType = this.nodeService.getType(nodeRef);
		result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_NODE_TYPE), nodeType);
        
        // Store the current aspects
        Set<QName> aspects = this.nodeService.getAspects(nodeRef);
        result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_ASPECTS), (Serializable)aspects);
        
        // Calculate the version label
		QName classRef = this.nodeService.getType(nodeRef);
		Version preceedingVersion = getVersion(preceedingNodeRef);
        String versionLabel = invokeCalculateVersionLabel(classRef, preceedingVersion, versionNumber, versionProperties);
        result.put(QName.createQName(NAMESPACE_URI, VersionModel.PROP_VERSION_LABEL), versionLabel);
        
        return result;
	}	
    
    /**
     * Creates a new version node, setting the properties both calculated and specified.
     * 
     * @param versionableNodeRef  the reference to the node being versioned
     * @param versionHistoryRef   version history node reference
     * @param preceedingNodeRef   the version node preceeding this in the version history
     * 							  , null if none
     * @param versionProperties   version properties
     * @param versionNumber		  the version number
     * @return                    the version node reference
     */
    private NodeRef createNewVersion(
			NodeRef versionableNodeRef, 
			NodeRef versionHistoryRef, 
            Map<QName, Serializable> standardVersionProperties,
			Map<String, Serializable> versionProperties,
			PolicyScope nodeDetails)
    {       
        // Create the new version
        ChildAssociationRef childAssocRef = this.dbNodeService.createNode(
                versionHistoryRef, 
				CHILD_QNAME_VERSIONS,
                CHILD_QNAME_VERSIONS,
                TYPE_QNAME_VERSION,
                standardVersionProperties);
        NodeRef versionNodeRef = childAssocRef.getChildRef();
        
        // Store the meta data
        storeVersionMetaData(versionNodeRef, versionProperties);
		
		// Freeze the various parts of the node
		freezeProperties(versionNodeRef, nodeDetails.getProperties());
		freezeChildAssociations(versionNodeRef, nodeDetails.getChildAssociations());
		freezeAssociations(versionNodeRef, nodeDetails.getAssociations());
		freezeAspects(nodeDetails, versionNodeRef, nodeDetails.getAspects());
		
		// Return the created node reference
		return versionNodeRef;
    }
    
    /**
     * Store the version meta data
     *  
     * @param versionNodeRef        the version node reference
     * @param versionProperties     the version properties
     */
	private void storeVersionMetaData(NodeRef versionNodeRef, Map<String, Serializable> versionProperties)
    {
        for (Map.Entry<String, Serializable> entry : versionProperties.entrySet())
        {
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            
            properties.put(PROP_QNAME_META_DATA_NAME, entry.getKey());
            properties.put(PROP_QNAME_META_DATA_VALUE, entry.getValue());
            
            this.dbNodeService.createNode(
                    versionNodeRef,
                    CHILD_QNAME_VERSION_META_DATA, 
                    CHILD_QNAME_VERSION_META_DATA, 
                    TYPE_QNAME_VERSION_META_DATA_VALUE,
                    properties);
        }
    }

    /**
	 * Freeze the aspects
     * 
	 * @param nodeDetails      the node details
	 * @param versionNodeRef   the version node reference
	 * @param aspects          the set of aspects
	 */
    private void freezeAspects(PolicyScope nodeDetails, NodeRef versionNodeRef, Set<QName> aspects) 
	{
		for (QName aspect : aspects) 
		{
			// Freeze the details of the aspect
			freezeProperties(versionNodeRef, nodeDetails.getProperties(aspect));
			freezeChildAssociations(versionNodeRef, nodeDetails.getChildAssociations(aspect));
			freezeAssociations(versionNodeRef, nodeDetails.getAssociations(aspect));
		}
	}

	/**
	 * Freeze associations
     * 
	 * @param versionNodeRef   the version node reference
	 * @param associations     the list of associations
	 */
	private void freezeAssociations(NodeRef versionNodeRef, List<AssociationRef> associations) 
	{
		for (AssociationRef targetAssoc : associations)
        {
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            
            // Set the qname of the association
            properties.put(PROP_QNAME_ASSOC_TYPE_QNAME, targetAssoc.getTypeQName());
            
            // Set the reference property to point to the child node
            properties.put(ContentModel.PROP_REFERENCE, targetAssoc.getTargetRef());
            
            // Create child version reference
            this.dbNodeService.createNode(
                    versionNodeRef,
					CHILD_QNAME_VERSIONED_ASSOCS, 
                    CHILD_QNAME_VERSIONED_ASSOCS, 
                    TYPE_QNAME_VERSIONED_ASSOC,
                    properties);
        }
	}

	/**
	 * Freeze child associations
     * 
	 * @param versionNodeRef       the version node reference
	 * @param childAssociations    the child associations
	 */
	private void freezeChildAssociations(NodeRef versionNodeRef, List<ChildAssociationRef> childAssociations) 
	{
		for (ChildAssociationRef childAssocRef : childAssociations) 
		{
			HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            
            // Set the qname, isPrimary and nthSibling properties
            properties.put(PROP_QNAME_ASSOC_QNAME, childAssocRef.getQName());
            properties.put(PROP_QNAME_ASSOC_TYPE_QNAME, childAssocRef.getTypeQName());
            properties.put(PROP_QNAME_IS_PRIMARY, Boolean.valueOf(childAssocRef.isPrimary()));
            properties.put(PROP_QNAME_NTH_SIBLING, Integer.valueOf(childAssocRef.getNthSibling()));
            
            // Set the reference property to point to the child node
            properties.put(ContentModel.PROP_REFERENCE, childAssocRef.getChildRef());
            
            // Create child version reference
            this.dbNodeService.createNode(
                    versionNodeRef,
					CHILD_QNAME_VERSIONED_CHILD_ASSOCS,
                    CHILD_QNAME_VERSIONED_CHILD_ASSOCS, 
                    TYPE_QNAME_VERSIONED_CHILD_ASSOC,
                    properties);
		}
	}

	/**
	 * Freeze properties
     * 
	 * @param versionNodeRef   the version node reference
	 * @param properties       the properties
	 */
	private void freezeProperties(NodeRef versionNodeRef, Map<QName, Serializable> properties) 
	{
		// Copy the property values from the node onto the version node
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {                               
            // Get the property values
            HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
			props.put(PROP_QNAME_QNAME, entry.getKey());
            
            if (entry.getValue() instanceof Collection)
            {
                props.put(PROP_QNAME_MULTI_VALUE, entry.getValue());
                props.put(PROP_QNAME_IS_MULTI_VALUE, true);
            }
            else
            {
                props.put(PROP_QNAME_VALUE, entry.getValue());
                props.put(PROP_QNAME_IS_MULTI_VALUE, false);
            }
            
            // Create the node storing the frozen attribute details
            this.dbNodeService.createNode(
                    versionNodeRef, 
					CHILD_QNAME_VERSIONED_ATTRIBUTES,
                    CHILD_QNAME_VERSIONED_ATTRIBUTES,
                    TYPE_QNAME_VERSIONED_PROPERTY,
                    props);                
        }
	}  		
	
	/**
	 * Gets the version stores root node
	 * 
	 * @return the node ref to the root node of the version store
	 */
	private NodeRef getRootNode() 
	{
		// Get the version store root node reference
        return this.dbNodeService.getRootNode(getVersionStoreReference());
	}
	
	/**
     * Builds a version history object from the version history reference.
     * <p>
     * The node ref is passed to enable the version history to be scoped to the
     * appropriate branch in the version history.
     * 
     * @param versionHistoryRef  the node ref for the version history
     * @param nodeRef            the node reference
     * @return                   a constructed version history object
     */
    private VersionHistory buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef)
    {
        VersionHistory versionHistory = null;
        
        ArrayList<NodeRef> versionHistoryNodeRefs = new ArrayList<NodeRef>();
        NodeRef currentVersion = getCurrentVersionNodeRef(versionHistoryRef, nodeRef);
        
        while (currentVersion != null)
        {
            AssociationRef preceedingVersion = null;
            
            versionHistoryNodeRefs.add(0, currentVersion);
            
            List<AssociationRef> preceedingVersions = this.dbNodeService.getSourceAssocs(
																				currentVersion, 
																				VersionModel.ASSOC_SUCCESSOR);
            if (preceedingVersions.size() == 1)
            {
                preceedingVersion = (AssociationRef)preceedingVersions.toArray()[0];
                currentVersion = preceedingVersion.getSourceRef();                
            }
            else if (preceedingVersions.size() > 1)
            {
                // Error since we only currently support one preceeding version
                throw new VersionServiceException(MSGID_ERR_ONE_PRECEEDING);
            }     
            else
            {
                currentVersion = null;
            }
        }
        
        // Build the version history object
        boolean isRoot = true;
        Version preceeding = null;
        for (NodeRef versionRef : versionHistoryNodeRefs)
        {
            Version version = getVersion(versionRef);
            
            if (isRoot == true)
            {
                versionHistory = new VersionHistoryImpl(version);
                isRoot = false;
            }
            else
            {
                ((VersionHistoryImpl)versionHistory).addVersion(version, preceeding);
            }
            preceeding = version;
        }
        
        return versionHistory;
    }       
    
    /**
     * Constructs the a version object to contain the version information from the version node ref.
     * 
     * @param versionRef  the version reference
     * @return            object containing verison data
     */
    private Version getVersion(NodeRef versionRef)
    {
        if (versionRef == null)
        {
            return null;
        }
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        
        // Get the standard node details
        Map<QName, Serializable> nodeProperties = this.dbNodeService.getProperties(versionRef);
        for (QName key : nodeProperties.keySet())
        {                 
            Serializable value = nodeProperties.get(key);
            versionProperties.put(key.getLocalName(), value);
        }
        
        // Get the meta data
        List<ChildAssociationRef> metaData = this.dbNodeService.getChildAssocs(
                versionRef,
                RegexQNamePattern.MATCH_ALL,
                CHILD_QNAME_VERSION_META_DATA);
        for (ChildAssociationRef ref : metaData)
        {
            NodeRef metaDataValue = (NodeRef)ref.getChildRef();
            String name = (String)this.dbNodeService.getProperty(metaDataValue, PROP_QNAME_META_DATA_NAME);
            Serializable value = this.dbNodeService.getProperty(metaDataValue, PROP_QNAME_META_DATA_VALUE);
            versionProperties.put(name, value);
        }
        
        // Create and return the version object
        NodeRef newNodeRef = new NodeRef(new StoreRef(STORE_PROTOCOL, STORE_ID), versionRef.getId());
        Version result = new VersionImpl(versionProperties, newNodeRef);
		// done
		return result;
    }
    
    /**
     * Gets a reference to the version history node for a given 'real' node.
     * 
     * @param nodeRef  a node reference
     * @return         a reference to the version history node, null of none
     */
    private NodeRef getVersionHistoryNodeRef(NodeRef nodeRef)
    {
        NodeRef result = null;
        
        Collection<ChildAssociationRef> versionHistories = this.dbNodeService.getChildAssocs(getRootNode());
        for (ChildAssociationRef versionHistory : versionHistories)
        {
            String nodeId = (String)this.dbNodeService.getProperty(versionHistory.getChildRef(), VersionModel.PROP_QNAME_VERSIONED_NODE_ID);
            if (nodeId != null && nodeId.equals(nodeRef.getId()) == true)
            {
                result = versionHistory.getChildRef();
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Gets a reference to the node for the current version of the passed node ref.
     * 
     * This uses the version label as a mechanism for looking up the version node in
     * the version history.
     * 
     * @param nodeRef  a node reference
     * @return         a reference to a version reference
     */
    private NodeRef getCurrentVersionNodeRef(NodeRef versionHistory, NodeRef nodeRef)
    {
        NodeRef result = null;
        String versionLabel = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
        
        Collection<ChildAssociationRef> versions = this.dbNodeService.getChildAssocs(versionHistory);
        for (ChildAssociationRef version : versions)
        {
            String tempLabel = (String)this.dbNodeService.getProperty(version.getChildRef(), VersionModel.PROP_QNAME_VERSION_LABEL);
            if (tempLabel != null && tempLabel.equals(versionLabel) == true)
            {
                result = version.getChildRef(); 
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Checks the given node for the version aspect.  Throws an exception if it is not present.
     * 
     * @param nodeRef   the node reference
     * @throws AspectMissingException
     *                  the version aspect is not present on the node
     */
    private void checkForVersionAspect(NodeRef nodeRef)
       throws AspectMissingException
    {
        QName aspectRef = ContentModel.ASPECT_VERSIONABLE;
        
        if (this.nodeService.hasAspect(nodeRef, aspectRef) == false)
        {
            // Raise exception to indicate version aspect is not present
            throw new AspectMissingException(aspectRef, nodeRef);
        }
    }	
    
    /**
     * @see org.alfresco.cms.version.VersionService#revert(NodeRef)
     */
    public void revert(NodeRef nodeRef) 
    {
		revert(nodeRef, getCurrentVersion(nodeRef), true);
	}
    
    /**
     * @see org.alfresco.service.cmr.version.VersionService#revert(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void revert(NodeRef nodeRef, boolean deep) 
    {
        revert(nodeRef, getCurrentVersion(nodeRef), deep);
    }
    
    /**
     * @see org.alfresco.service.cmr.version.VersionService#revert(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.version.Version)
     */
    public void revert(NodeRef nodeRef, Version version) 
    {
        revert(nodeRef, version, true);
    }

    /**
     * @see org.alfresco.service.cmr.version.VersionService#revert(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.version.Version, boolean)
     */
	public void revert(NodeRef nodeRef, Version version, boolean deep) 
	{
		// Check the mandatory parameters
		ParameterCheck.mandatory("nodeRef", nodeRef);
		ParameterCheck.mandatory("version", version);
		
        // Cross check that the version provided relates to the node reference provided
        if (nodeRef.getId().equals(version.getVersionProperty(VersionModel.PROP_FROZEN_NODE_ID)) == false)
        {
            // Error since the version provided does not correspond to the node reference provided
            throw new VersionServiceException(MSGID_ERR_REVERT_MISMATCH);
        }
        
        // Turn off any auto-version policy behaviours
        this.policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        try
        {
            // Store the current version label
            String currentVersionLabel = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
            
            // Get the node that represents the frozen state
    		NodeRef versionNodeRef = version.getFrozenStateNodeRef();        
            
    		// Revert the property values
    		this.nodeService.setProperties(nodeRef, this.nodeService.getProperties(versionNodeRef));
    		
    		// Apply/remove the aspects as required
    		Set<QName> aspects = new HashSet<QName>(this.nodeService.getAspects(nodeRef));
    		for (QName versionAspect : this.nodeService.getAspects(versionNodeRef)) 
    		{
    			if (aspects.contains(versionAspect) == false)
    			{
    				this.nodeService.addAspect(nodeRef, versionAspect, null);
    			}
    			else
    			{
    				aspects.remove(versionAspect);
    			}
    		}
    		for (QName aspect : aspects) 
    		{
    			this.nodeService.removeAspect(nodeRef, aspect);
    		}
            
    		// Re-add the versionable aspect to the reverted node
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
            {
                this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
            }
            
            // Re-set the version label property (since it should not be modified from the origional)
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, currentVersionLabel);
    
    		// Add/remove the child nodes
    		List<ChildAssociationRef> children = new ArrayList<ChildAssociationRef>(this.nodeService.getChildAssocs(nodeRef));
    		for (ChildAssociationRef versionedChild : this.nodeService.getChildAssocs(versionNodeRef)) 
    		{
    			if (children.contains(versionedChild) == false)
    			{			
    				if (this.nodeService.exists(versionedChild.getChildRef()) == true)
    				{
    					// The node was a primary child of the parent, but that is no longer the case.  Dispite this
    					// the node still exits so this means it has been moved.
    					// The best thing to do in this situation will be to re-add the node as a child, but it will not
    					// be a primary child.
    					this.nodeService.addChild(nodeRef, versionedChild.getChildRef(), versionedChild.getTypeQName(), versionedChild.getQName());
    				}
    				else
    				{
                        if (versionedChild.isPrimary() == true)
                        {
                            // Only try to resotre missing children if we are doing a deep revert
                            // Look and see if we have a version history for the child node
                            if (deep == true && getVersionHistoryNodeRef(versionedChild.getChildRef()) != null)
                            {
                                // We're going to try and restore the missing child node and recreate the assoc
                                restore(
                                   versionedChild.getChildRef(),
                                   nodeRef,
                                   versionedChild.getTypeQName(), 
                                   versionedChild.getQName());
                            }
                            // else the deleted child did not have a version history so we can't restore the child
                            // and so we can't revert the association
                        }
                        
                        // else
                        // Since this was never a primary assoc and the child has been deleted we won't recreate
                        // the missing node as it was never owned by the node and we wouldn't know where to put it.
    				}
    			}
    			else
    			{
    				children.remove(versionedChild);
    			}
    		}
    		for (ChildAssociationRef ref : children) 
    		{
    			this.nodeService.removeChild(nodeRef, ref.getChildRef());
    		}
    		
    		// Add/remove the target associations
    		for (AssociationRef assocRef : this.nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL)) 
    		{
    			this.nodeService.removeAssociation(assocRef.getSourceRef(), assocRef.getTargetRef(), assocRef.getTypeQName());
    		}		
    		for (AssociationRef versionedAssoc : this.nodeService.getTargetAssocs(versionNodeRef, RegexQNamePattern.MATCH_ALL)) 
    		{
    			if (this.nodeService.exists(versionedAssoc.getTargetRef()) == true)
    			{
    				this.nodeService.createAssociation(nodeRef, versionedAssoc.getTargetRef(), versionedAssoc.getTypeQName());
    			}
                
    			// else
                // Since the tareget of the assoc no longer exists we can't recreate the assoc
    		}	
        }
        finally
        {
            // Turn auto-version policies back on
            this.policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        }
	}
    
    /**
     * @see org.alfresco.service.cmr.version.VersionService#restore(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
     public NodeRef restore(
                NodeRef nodeRef,
                NodeRef parentNodeRef, 
                QName assocTypeQName,
                QName assocQName)
     {
         return restore(nodeRef, parentNodeRef, assocTypeQName, assocQName, true);
     }
    
    /**
     * @see org.alfresco.service.cmr.version.VersionService#restore(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, boolean)
     */
     public NodeRef restore(
            NodeRef nodeRef,
            NodeRef parentNodeRef, 
            QName assocTypeQName,
            QName assocQName,
            boolean deep)
    {
        NodeRef restoredNodeRef = null;
             
        // Check that the node does not exist 
        if (this.nodeService.exists(nodeRef) == true)
        {
            // Error since you can not restore a node that already exists
            throw new VersionServiceException(MSGID_ERR_RESTORE_EXISTS, new Object[]{nodeRef.toString()});
        }
        
        // Try and get the version details that we want to restore to
        Version version = getHeadVersion(nodeRef);
        if (version == null)
        {
            // Error since there is no version information available to restore the node from
            throw new VersionServiceException(MSGID_ERR_RESTORE_NO_VERSION, new Object[]{nodeRef.toString()});
        }
        
        // Set the uuid of the new node
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NODE_UUID, version.getVersionProperty(VersionModel.PROP_FROZEN_NODE_ID));
        
        // Get the type of the node node
        QName type = (QName)version.getVersionProperty(VersionModel.PROP_FROZEN_NODE_TYPE);
        
        // Disable auto-version behaviour
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        try
        {
            // Create the restored node
            restoredNodeRef = this.nodeService.createNode(
                    parentNodeRef,
                    assocTypeQName,
                    assocQName,
                    type,
                    props).getChildRef();
        }
        finally
        {
            // Enable auto-version behaviour
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }
        
        // Now we need to revert the newly restored node
        revert(restoredNodeRef, version, deep);
        
        return restoredNodeRef;
    }
    
    /**
     * Get the head version given a node reference
     * 
     * @param nodeRef   the node reference
     * @return          the 'head' version
     */
    private Version getHeadVersion(NodeRef nodeRef)
    {
        Version version = null;
        StoreRef storeRef = nodeRef.getStoreRef();
        
        NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(nodeRef);
        if (versionHistoryNodeRef != null)
        {
            List<ChildAssociationRef> versionsAssoc = this.dbNodeService.getChildAssocs(versionHistoryNodeRef, RegexQNamePattern.MATCH_ALL, VersionModel.CHILD_QNAME_VERSIONS);
            for (ChildAssociationRef versionAssoc : versionsAssoc)
            {
                NodeRef versionNodeRef = versionAssoc.getChildRef();
                List<AssociationRef> successors = this.dbNodeService.getTargetAssocs(versionNodeRef, VersionModel.ASSOC_SUCCESSOR);
                if (successors.size() == 0)
                {
                    String storeProtocol = (String)this.dbNodeService.getProperty(
                            versionNodeRef,
                            QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL));
                    String storeId = (String)this.dbNodeService.getProperty(
                            versionNodeRef,
                            QName.createQName(NAMESPACE_URI, VersionModel.PROP_FROZEN_NODE_STORE_ID));
                    StoreRef versionStoreRef = new StoreRef(storeProtocol, storeId);
                    if (storeRef.equals(versionStoreRef) == true)
                    {
                        version = getVersion(versionNodeRef);
                    }                
                }
            }
        }
        
        return version;
    }

	/**
	 * @see org.alfresco.cms.version.VersionService#deleteVersionHistory(NodeRef)
	 */
	public void deleteVersionHistory(NodeRef nodeRef) 
		throws AspectMissingException 
	{
		// First check that the versionable aspect is present
		checkForVersionAspect(nodeRef);
		
		// Get the version history node for the node is question and delete it
		NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(nodeRef);
		this.dbNodeService.deleteNode(versionHistoryNodeRef);
		
		// Reset the version label property on the versionable node
		this.nodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, null);
	}
}
