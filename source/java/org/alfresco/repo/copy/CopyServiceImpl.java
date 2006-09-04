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
package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CopyServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;

/**
 * Node operations service implmentation.
 * 
 * @author Roy Wetherall
 */
public class CopyServiceImpl implements CopyService
{
    /** The node service */
    private NodeService nodeService;
	
	/** The dictionary service*/
	private DictionaryService dictionaryService; 	
	
    /** The search service */
    private SearchService searchService;
    
	/** Policy component */
	private PolicyComponent policyComponent;
    
    /** Rule service */
    private RuleService ruleService;
    
    /** Permission service */
    private PermissionService permissionService;
    
    /** Authentication service */
    private AuthenticationService authenticationService;

	/** Policy delegates */
	private ClassPolicyDelegate<CopyServicePolicies.OnCopyNodePolicy> onCopyNodeDelegate;
	private ClassPolicyDelegate<CopyServicePolicies.OnCopyCompletePolicy> onCopyCompleteDelegate;
    
    /**
     * Set the node service
     * 
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
	
	/**
	 * Sets the dictionary service
	 * 
	 * @param dictionaryService  the dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) 
	{
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Sets the policy component
	 * 
	 * @param policyComponent  the policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) 
	{
		this.policyComponent = policyComponent;
	}
    
    /**
     * Sets the search service
     * 
     * @param searchService     the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set the rule service
     * 
     * @param ruleService  the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService		the permission service
     */
    public void setPermissionService(PermissionService permissionService) 
    {
		this.permissionService = permissionService;
	}
    
    /**
     * Sets the authentication service
     * 
     * @param authenticationService		the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService) 
    {
		this.authenticationService = authenticationService;
	}
    
	/**
	 * Initialise method
	 */
	public void init()
	{
		// Register the policies
		this.onCopyNodeDelegate = this.policyComponent.registerClassPolicy(CopyServicePolicies.OnCopyNodePolicy.class);
		this.onCopyCompleteDelegate = this.policyComponent.registerClassPolicy(CopyServicePolicies.OnCopyCompletePolicy.class);
		
		// Register policy behaviours
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
				ContentModel.ASPECT_COPIEDFROM,
				new JavaBehaviour(this, "copyAspectOnCopy"));	
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_OWNABLE,
                new JavaBehaviour(this, "onCopyOwnable"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_AUTHOR,
                new JavaBehaviour(this, "onCopyAuthor"));	
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
				ContentModel.ASPECT_COPIEDFROM,
				new JavaBehaviour(this, "onCopyComplete"));	
	}
	
    /**
     * @see com.activiti.repo.node.copy.NodeCopyService#copy(com.activiti.repo.ref.NodeRef, com.activiti.repo.ref.NodeRef, com.activiti.repo.ref.QName, QName, boolean)
     */
    public NodeRef copy(
            NodeRef sourceNodeRef,
            NodeRef destinationParentRef, 
            QName destinationAssocTypeQName,
            QName destinationQName, 
            boolean copyChildren)
    {
		// Check that all the passed values are not null
        ParameterCheck.mandatory("Source Node", sourceNodeRef);
        ParameterCheck.mandatory("Destination Parent", destinationParentRef);
        ParameterCheck.mandatory("Destination Association Name", destinationQName);

        if (sourceNodeRef.getStoreRef().equals(destinationParentRef.getStoreRef()) == false)
        {
            // TODO We need to create a new node in the other store with the same id as the source

            // Error - since at the moment we do not support cross store copying
            throw new UnsupportedOperationException("Copying nodes across stores is not currently supported.");
        }

        // Get the original parent reference
        NodeRef sourceParentRef = nodeService.getPrimaryParent(sourceNodeRef).getParentRef();
        // Recursively copy node
        Map<NodeRef, NodeRef> copiedChildren = new HashMap<NodeRef, NodeRef>();
        NodeRef copy = recursiveCopy(
                sourceNodeRef,
                sourceParentRef,
                destinationParentRef,
                destinationAssocTypeQName,
                destinationQName,
                copyChildren,
                true,                                   // top-level copy drops the name, if the parent is different
                copiedChildren);
        
        // Foreach of the newly created copies call the copy complete policy
        for (Map.Entry<NodeRef, NodeRef> entry : copiedChildren.entrySet())
		{
			invokeCopyComplete(entry.getKey(), entry.getValue(), true, copiedChildren);
		}
        
        return copy;
    }
    
    /**
     * Invokes the copy complete policy for the node reference provided
     * 
     * @param sourceNodeRef			the source node reference
     * @param destinationNodeRef	the destination node reference
     * @param copiedNodeRefs		the map of copied node references
     */
    private void invokeCopyComplete(
    		NodeRef sourceNodeRef, 
    		NodeRef destinationNodeRef, 
            boolean copyToNewNode,
    		Map<NodeRef, NodeRef> copiedNodeRefs)
	{
    	QName sourceClassRef = this.nodeService.getType(sourceNodeRef);		
    	invokeCopyComplete(sourceClassRef, sourceNodeRef, destinationNodeRef, copyToNewNode, copiedNodeRefs);
		
		// Get the source aspects
		Set<QName> sourceAspects = this.nodeService.getAspects(sourceNodeRef);
		for (QName sourceAspect : sourceAspects) 
		{
			invokeCopyComplete(sourceAspect, sourceNodeRef, destinationNodeRef, copyToNewNode, copiedNodeRefs);
		}
	}

    /**
     * 
     * @param typeQName
     * @param sourceNodeRef
     * @param destinationNodeRef
     * @param copiedNodeRefs
     */
	private void invokeCopyComplete(
			QName typeQName, 
			NodeRef sourceNodeRef, 
			NodeRef destinationNodeRef, 
            boolean copyToNewNode,
			Map<NodeRef, NodeRef> copiedNodeRefs)
	{
		Collection<CopyServicePolicies.OnCopyCompletePolicy> policies = this.onCopyCompleteDelegate.getList(typeQName);
		if (policies.isEmpty() == true)
		{
			defaultOnCopyComplete(typeQName, sourceNodeRef, destinationNodeRef, copiedNodeRefs);
		}
		else
		{
			for (CopyServicePolicies.OnCopyCompletePolicy policy : policies) 
			{
				policy.onCopyComplete(typeQName, sourceNodeRef, destinationNodeRef, copyToNewNode, copiedNodeRefs);
			}
		}		
	}

	/**
	 * 
	 * @param typeQName
	 * @param sourceNodeRef
	 * @param destinationNodeRef
	 * @param copiedNodeRefs
	 */
	private void defaultOnCopyComplete(
			QName typeQName, 
			NodeRef sourceNodeRef, 
			NodeRef destinationNodeRef, 
			Map<NodeRef, NodeRef> copiedNodeRefs)
	{
		ClassDefinition classDefinition = this.dictionaryService.getClass(typeQName);	
		if (classDefinition != null)
		{
             // Check the properties
            Map<QName,PropertyDefinition> propertyDefinitions = classDefinition.getProperties();
            for (Map.Entry<QName,PropertyDefinition> entry : propertyDefinitions.entrySet()) 
            {
            	QName propertyTypeDefinition = entry.getValue().getDataType().getName();
            	if (DataTypeDefinition.NODE_REF.equals(propertyTypeDefinition) == true ||
            		DataTypeDefinition.ANY.equals(propertyTypeDefinition) == true)
            	{
            		// Re-set the node ref so that it is still relative (if appropriate)
            		Serializable value = this.nodeService.getProperty(destinationNodeRef, entry.getKey());
            		if (value != null && value instanceof NodeRef)
            		{
            			NodeRef nodeRef = (NodeRef)value;
            			if (copiedNodeRefs.containsKey(nodeRef) == true)
            			{
            				NodeRef copiedNodeRef = copiedNodeRefs.get(nodeRef);
            				this.nodeService.setProperty(destinationNodeRef, entry.getKey(), copiedNodeRef);
            			}
            		}            		
            	}
            }           

            // Copy the associations (child and target)
            Map<QName, AssociationDefinition> assocDefs = classDefinition.getAssociations();

            // TODO: Need way of getting child assocs of a given type
            List<ChildAssociationRef> childAssocRefs = this.nodeService.getChildAssocs(destinationNodeRef);
            for (ChildAssociationRef childAssocRef : childAssocRefs) 
            {
                if (assocDefs.containsKey(childAssocRef.getTypeQName()) &&
                	childAssocRef.isPrimary() == false &&
                	copiedNodeRefs.containsKey(childAssocRef.getChildRef()) == true)
                {                    	
                	// Remove the assoc and re-point to the new node
                	this.nodeService.removeChild(destinationNodeRef, childAssocRef.getChildRef());
                	this.nodeService.addChild(
                			destinationNodeRef, 
                			copiedNodeRefs.get(childAssocRef.getChildRef()) , 
                			childAssocRef.getTypeQName(),
                			childAssocRef.getQName());
                }
            }
            
            // TODO: Need way of getting assocs of a given type
            List<AssociationRef> nodeAssocRefs = this.nodeService.getTargetAssocs(destinationNodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef nodeAssocRef : nodeAssocRefs) 
            {
                if (assocDefs.containsKey(nodeAssocRef.getTypeQName()) &&
                	copiedNodeRefs.containsKey(nodeAssocRef.getTargetRef()) == true)
                {
                    // Remove the assoc and re-point to the new node
                	this.nodeService.removeAssociation(
                			destinationNodeRef, 
                			nodeAssocRef.getTargetRef(), 
                			nodeAssocRef.getTypeQName());
                	this.nodeService.createAssociation(
                			destinationNodeRef, 
                			copiedNodeRefs.get(nodeAssocRef.getTargetRef()), 
                			nodeAssocRef.getTypeQName());
                }
            }
		}
		
	}

	/**
     * Recursive copy algorithm
     * 
     * @param dropName      drop the name property when associations don't allow duplicately named children
     */
    private NodeRef recursiveCopy(
              NodeRef sourceNodeRef,
              NodeRef sourceParentRef,
              NodeRef destinationParentRef, 
              QName destinationAssocTypeQName,
              QName destinationQName, 
              boolean copyChildren,
              boolean dropName,
              Map<NodeRef, NodeRef> copiedChildren)
    {
        // Extract Type Definition
		QName sourceTypeRef = this.nodeService.getType(sourceNodeRef);
        TypeDefinition typeDef = dictionaryService.getType(sourceTypeRef);
        if (typeDef == null)
        {
            throw new InvalidTypeException(sourceTypeRef);
        }
        
        // Establish the scope of the copy
		PolicyScope copyDetails = getCopyDetails(sourceNodeRef, destinationParentRef.getStoreRef(), true);
		
        // Create collection of properties for type and mandatory aspects
        Map<QName, Serializable> typeProps = copyDetails.getProperties(); 
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        if (typeProps != null)
        {
            properties.putAll(typeProps);
        }
        for (AspectDefinition aspectDef : typeDef.getDefaultAspects())
        {
            Map<QName, Serializable> aspectProps = copyDetails.getProperties(aspectDef.getName());
            if (aspectProps != null)
            {
                properties.putAll(aspectProps);
            }
        }
        
        // Drop the name property, if required.  This prevents duplicate names and leaves it up to the client
        // to assign a new name.
        AssociationDefinition assocDef = dictionaryService.getAssociation(destinationAssocTypeQName);
        if (!assocDef.isChild())
        {
            throw new AlfrescoRuntimeException("Association is not a child association: " + destinationAssocTypeQName);
        }
        else
        {
            ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
            if (dropName && !childAssocDef.getDuplicateChildNamesAllowed())
            {
                // duplicate children are not allowed.
                properties.remove(ContentModel.PROP_NAME);
            }
        }
        
		// Create the new node
        ChildAssociationRef destinationChildAssocRef = this.nodeService.createNode(
                destinationParentRef, 
                destinationAssocTypeQName,
                destinationQName,
                sourceTypeRef,
                properties);
        NodeRef destinationNodeRef = destinationChildAssocRef.getChildRef();
        copiedChildren.put(sourceNodeRef, destinationNodeRef);
        
        // Prevent any rules being fired on the new destination node
        this.ruleService.disableRules(destinationNodeRef);
        try
        {
            //	Apply the copy aspect to the new node	
    		Map<QName, Serializable> copyProperties = new HashMap<QName, Serializable>();
    		copyProperties.put(ContentModel.PROP_COPY_REFERENCE, sourceNodeRef);
    		this.nodeService.addAspect(destinationNodeRef, ContentModel.ASPECT_COPIEDFROM, copyProperties);
    		
    		// Copy the aspects 
    		copyAspects(destinationNodeRef, copyDetails);
    		
    		// Copy the associations
    		copyAssociations(destinationNodeRef, copyDetails, copyChildren, copiedChildren);
    		
    		// Copy permissions
    		copyPermissions(sourceNodeRef, destinationNodeRef);
        }
        finally
        {
            this.ruleService.enableRules(destinationNodeRef);
        }
        
        return destinationNodeRef;
    }
	
	/**
	 * Copies the permissions of the source node reference onto the destination node reference
	 * 
	 * @param sourceNodeRef			the source node reference
	 * @param destinationNodeRef	the destination node reference
	 */
    private void copyPermissions(NodeRef sourceNodeRef, NodeRef destinationNodeRef) 
    {
        if(this.permissionService.hasPermission(sourceNodeRef, PermissionService.READ_PERMISSIONS) == AccessStatus.ALLOWED)
        {
            // Get the permission details of the source node reference
            Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(sourceNodeRef);
            boolean includeInherited = this.permissionService.getInheritParentPermissions(sourceNodeRef);
            
            AccessStatus writePermission = permissionService.hasPermission(destinationNodeRef, PermissionService.CHANGE_PERMISSIONS);
            if (writePermission.equals(AccessStatus.ALLOWED) || this.authenticationService.isCurrentUserTheSystemUser() )
            {
                // Set the permission values on the destination node    	
                for (AccessPermission permission : permissions) 
                {
                    this.permissionService.setPermission(
                            destinationNodeRef, 
                            permission.getAuthority(), 
                            permission.getPermission(), 
                            permission.getAccessStatus().equals(AccessStatus.ALLOWED));
                }
                this.permissionService.setInheritParentPermissions(destinationNodeRef, includeInherited);
            }
        }
    }

	/**
	 * Gets the copy details.  This calls the appropriate policies that have been registered
	 * against the node and aspect types in order to pick-up any type specific copy behaviour.
	 * <p>
	 * If no policies for a type are registered then the default copy takes place which will 
	 * copy all properties and associations in the ususal manner.
	 * 
	 * @param sourceNodeRef		the source node reference
	 * @return					the copy details
	 */
	private PolicyScope getCopyDetails(NodeRef sourceNodeRef, StoreRef destinationStoreRef, boolean copyToNewNode)
	{
		QName sourceClassRef = this.nodeService.getType(sourceNodeRef);		
		PolicyScope copyDetails = new PolicyScope(sourceClassRef);
		
		// Invoke the onCopy behaviour
		invokeOnCopy(sourceClassRef, sourceNodeRef, destinationStoreRef, copyToNewNode, copyDetails);
		
		// TODO What do we do aboout props and assocs that are on the node node but not part of the type definition?
		
		// Get the source aspects
		Set<QName> sourceAspects = this.nodeService.getAspects(sourceNodeRef);
		for (QName sourceAspect : sourceAspects) 
		{
			// Invoke the onCopy behaviour
			invokeOnCopy(sourceAspect, sourceNodeRef, destinationStoreRef, copyToNewNode, copyDetails);
		}
		
		return copyDetails;
	}
	
	/**
	 * Invoke the correct onCopy behaviour
	 * 
	 * @param sourceClassRef	source class reference
	 * @param sourceNodeRef		source node reference
	 * @param copyDetails		the copy details
	 */
	private void invokeOnCopy(
            QName sourceClassRef, 
            NodeRef sourceNodeRef, 
            StoreRef destinationStoreRef, 
            boolean copyToNewNode, 
            PolicyScope copyDetails)
	{
		Collection<CopyServicePolicies.OnCopyNodePolicy> policies = this.onCopyNodeDelegate.getList(sourceClassRef);
		if (policies.isEmpty() == true)
		{
			defaultOnCopy(sourceClassRef, sourceNodeRef, copyDetails);
		}
		else
		{
			for (CopyServicePolicies.OnCopyNodePolicy policy : policies) 
			{
				policy.onCopyNode(sourceClassRef, sourceNodeRef, destinationStoreRef, copyToNewNode, copyDetails);
			}
		}
	}
	
	/**
	 * Default implementation of on copy, used when there is no policy specified for a class.
	 * 
	 * @param classRef			the class reference of the node being copied
	 * @param sourceNodeRef		the source node reference
	 * @param copyDetails		details of the state being copied
	 */
    private void defaultOnCopy(QName classRef, NodeRef sourceNodeRef, PolicyScope copyDetails) 
	{
		ClassDefinition classDefinition = this.dictionaryService.getClass(classRef);	
		if (classDefinition != null)
		{
            if (classDefinition.isAspect() == true)
            {
                // make sure any aspects without any properties or associations are copied
                copyDetails.addAspect(classRef);
            }

            // Copy the properties
            Map<QName,PropertyDefinition> propertyDefinitions = classDefinition.getProperties();
            for (QName propertyName : propertyDefinitions.keySet()) 
            {
                Serializable propValue = this.nodeService.getProperty(sourceNodeRef, propertyName);
                copyDetails.addProperty(classDefinition.getName(), propertyName, propValue);
            }           

            // Copy the associations (child and target)
            Map<QName, AssociationDefinition> assocDefs = classDefinition.getAssociations();

            // TODO: Need way of getting child assocs of a given type
            if (classDefinition.isContainer())
            {
                List<ChildAssociationRef> childAssocRefs = this.nodeService.getChildAssocs(sourceNodeRef);
                for (ChildAssociationRef childAssocRef : childAssocRefs) 
                {
                    if (assocDefs.containsKey(childAssocRef.getTypeQName()))
                    {
                        copyDetails.addChildAssociation(classDefinition.getName(), childAssocRef);
                    }
                }
            }
            
            // TODO: Need way of getting assocs of a given type
            List<AssociationRef> nodeAssocRefs = this.nodeService.getTargetAssocs(sourceNodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef nodeAssocRef : nodeAssocRefs) 
            {
                if (assocDefs.containsKey(nodeAssocRef.getTypeQName()))
                {
                    copyDetails.addAssociation(classDefinition.getName(), nodeAssocRef);
                }
            }
		}
	}
    
	/**
	 * Copies the properties for the node type onto the destination node.
	 * 	
	 * @param destinationNodeRef	the destintaion node reference
	 * @param copyDetails			the copy details
	 */
	private void copyProperties(NodeRef destinationNodeRef, PolicyScope copyDetails)
	{
		Map<QName, Serializable> props = copyDetails.getProperties();
		if (props != null)
		{
			for (QName propName : props.keySet()) 
			{
				this.nodeService.setProperty(destinationNodeRef, propName, props.get(propName));
			}
		}
	}
	
	/**
	 * Applies the aspects (thus copying the associated properties) onto the destination node
	 * 
	 * @param destinationNodeRef	the destination node reference
	 * @param copyDetails			the copy details
	 */
	private void copyAspects(NodeRef destinationNodeRef, PolicyScope copyDetails)
	{
		Set<QName> apects = copyDetails.getAspects();
		for (QName aspect : apects) 
		{
			if (this.nodeService.hasAspect(destinationNodeRef, aspect) == false)
			{
				// Add the aspect to the node
				this.nodeService.addAspect(
						destinationNodeRef, 
						aspect, 
						copyDetails.getProperties(aspect));
			}
			else
			{
				// Set each property on the destination node since the aspect has already been applied
				Map<QName, Serializable> aspectProps = copyDetails.getProperties(aspect);
				if (aspectProps != null)
				{
					for (Map.Entry<QName, Serializable> entry : aspectProps.entrySet()) 
					{
						this.nodeService.setProperty(destinationNodeRef, entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}	
	
	/**
	 * Copies the associations (child and target) for the node type and aspects onto the 
	 * destination node.
	 * <p>
	 * If copyChildren is true then all child nodes of primary child associations are copied
	 * before they are associatied with the destination node.
	 * 
	 * @param destinationNodeRef	the destination node reference
	 * @param copyDetails			the copy details
	 * @param copyChildren			indicates whether the primary children are copied or not
     * @param copiedChildren        set of children already copied
	 */
	private void copyAssociations(
			NodeRef destinationNodeRef, 
			PolicyScope copyDetails, 
			boolean copyChildren, 
			Map<NodeRef, NodeRef> copiedChildren)
	{
		QName classRef = this.nodeService.getType(destinationNodeRef);
		copyChildAssociations(classRef, destinationNodeRef, copyDetails, copyChildren, copiedChildren);
		copyTargetAssociations(classRef, destinationNodeRef, copyDetails);
		
		Set<QName> apects = copyDetails.getAspects();
		for (QName aspect : apects) 
		{
			if (this.nodeService.hasAspect(destinationNodeRef, aspect) == false)
			{
				// Error since the aspect has not been added to the destination node (should never happen)
				throw new CopyServiceException("The aspect has not been added to the destination node.");
			}
			
			copyChildAssociations(aspect, destinationNodeRef, copyDetails, copyChildren, copiedChildren);
			copyTargetAssociations(aspect, destinationNodeRef, copyDetails);
		}
	}
	
	/**
	 * Copies the target associations onto the destination node reference.
	 * 
	 * @param classRef				the class reference
	 * @param destinationNodeRef	the destination node reference
	 * @param copyDetails			the copy details 
	 */
	private void copyTargetAssociations(QName classRef, NodeRef destinationNodeRef, PolicyScope copyDetails) 
	{
		List<AssociationRef> nodeAssocRefs = copyDetails.getAssociations(classRef);
		if (nodeAssocRefs != null)
		{
			for (AssociationRef assocRef : nodeAssocRefs) 
			{
				NodeRef targetRef = assocRef.getTargetRef();
				
				boolean exists = false;
				for (AssociationRef assocRef2 : this.nodeService.getTargetAssocs(destinationNodeRef, assocRef.getTypeQName())) 
				{
					if (targetRef.equals(assocRef2.getTargetRef()) == true)
					{
						exists = true;
						break;
					}
				}
				
				if (exists == false)
				{
					// Add the association				
					this.nodeService.createAssociation(destinationNodeRef, targetRef, assocRef.getTypeQName());
				}
			}
		}
	}

	/**
	 * Copies the child associations onto the destiantion node reference.
	 * <p>
	 * If copyChildren is true then the nodes at the end of a primary assoc will be copied before they
	 * are associated.
	 * 
	 * @param classRef				the class reference
	 * @param destinationNodeRef	the destination node reference
	 * @param copyDetails			the copy details
	 * @param copyChildren			indicates whether to copy the primary children
	 */
	private void copyChildAssociations(
			QName classRef, 
			NodeRef destinationNodeRef,
			PolicyScope copyDetails, 
			boolean copyChildren,
			Map<NodeRef, NodeRef> copiedChildren)
	{
		List<ChildAssociationRef> childAssocs = copyDetails.getChildAssociations(classRef);
		if (childAssocs != null)
		{
			for (ChildAssociationRef childAssoc : childAssocs) 
			{
				if (copyChildren == true)
				{
					if (childAssoc.isPrimary() == true)
					{
                        // Do not recurse further, if we've already copied this node
                        if (copiedChildren.containsKey(childAssoc.getChildRef()) == false &&
                        	copiedChildren.containsValue(childAssoc.getChildRef()) == false)
                        {
    						// Copy the child
    						recursiveCopy(
                                    childAssoc.getChildRef(),
                                    childAssoc.getParentRef(),
    								destinationNodeRef, 
                                    childAssoc.getTypeQName(), 
                                    childAssoc.getQName(),
    								copyChildren,
                                    false,                      // the target and source parents can't be the same
                                    copiedChildren);
                        }
					}
					else
					{
						// Add the child 
						NodeRef childRef = childAssoc.getChildRef();
						this.nodeService.addChild(destinationNodeRef, childRef, childAssoc.getTypeQName(), childAssoc.getQName());
					}
				}
				else
				{
					NodeRef childRef = childAssoc.getChildRef();
					QName childType = this.nodeService.getType(childRef);
					
					// TODO will need to remove this reference to the configurations association
					if (this.dictionaryService.isSubClass(childType, ContentModel.TYPE_CONFIGURATIONS) == true ||
						copyDetails.isChildAssociationRefAlwaysTraversed(classRef, childAssoc) == true)
					{
						if (copiedChildren.containsKey(childRef) == false)
                        {
							// Always recursivly copy configuration folders
							recursiveCopy(
	                                childRef,
                                    childAssoc.getParentRef(),
									destinationNodeRef, 
	                                childAssoc.getTypeQName(), 
	                                childAssoc.getQName(),
									true,
                                    false,                      // the target and source parents can't be the same
									copiedChildren);
                        }
					}
					else
					{
						// Add the child (will not be primary reguardless of its origional state)
						this.nodeService.addChild(destinationNodeRef, childRef, childAssoc.getTypeQName(), childAssoc.getQName());
					}
				}							
			}
		}
	}

	/**
	 * Defer to the standard implementation with copyChildren set to false
	 * 
     * @see com.activiti.repo.node.copy.NodeCopyService#copy(com.activiti.repo.ref.NodeRef, com.activiti.repo.ref.NodeRef, com.activiti.repo.ref.QName)
     */
    public NodeRef copy(
            NodeRef sourceNodeRef,
            NodeRef destinationParent, 
            QName destinationAssocTypeQName,
            QName destinationQName)
    {
        return copy(
				sourceNodeRef, 
				destinationParent, 
				destinationAssocTypeQName, 
				destinationQName, 
				false);
    }

    /**
     * @see com.activiti.repo.node.copy.NodeCopyService#copy(com.activiti.repo.ref.NodeRef, com.activiti.repo.ref.NodeRef)
     */
    public void copy(
            NodeRef sourceNodeRef, 
            NodeRef destinationNodeRef)
    {
		// Check that the source and destination node are the same type
		if (this.nodeService.getType(sourceNodeRef).equals(this.nodeService.getType(destinationNodeRef)) == false)
		{
			// Error - can not copy objects that are of different types
			throw new CopyServiceException("The source and destination node must be the same type.");
		}
		
		// Get the copy details
		PolicyScope copyDetails = getCopyDetails(sourceNodeRef, destinationNodeRef.getStoreRef(), false);
		
		// Copy over the top of the destination node
		copyProperties(destinationNodeRef, copyDetails);
		copyAspects(destinationNodeRef, copyDetails);
		copyAssociations(destinationNodeRef, copyDetails, false, new HashMap<NodeRef, NodeRef>());
        
        // invoke the copy complete policy
        Map<NodeRef, NodeRef> copiedNodes = new HashMap<NodeRef, NodeRef>(1);
        copiedNodes.put(sourceNodeRef, destinationNodeRef);
        invokeCopyComplete(sourceNodeRef, destinationNodeRef, false, copiedNodes);         
    }
	
	/**
	 * OnCopy behaviour registered for the copy aspect.  
	 * <p>
	 * Doing nothing in this behaviour ensures that the copy aspect found on the source node does not get 
	 * copied onto the destination node.
	 * 
	 * @param sourceClassRef	the source class reference
	 * @param sourceNodeRef		the source node reference
	 * @param copyDetails	    the copy details
	 */
	public void copyAspectOnCopy(
            QName classRef,
            NodeRef sourceNodeRef,
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
            PolicyScope copyDetails)
	{
		// Do nothing.  This will ensure that copy aspect on the source node does not get copied onto
		// the destination node.
	}	
    
    public void onCopyOwnable(
            QName classRef,
            NodeRef sourceNodeRef,
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
            PolicyScope copyDetails)
    {
        // Do nothing since the ownable aspect should not be copied
    }   
    
    public void onCopyAuthor(
            QName classRef,
            NodeRef sourceNodeRef,
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
            PolicyScope copyDetails)
    {
        // Do nothing since the author aspect should not be copied
    }
	
	public void onCopyComplete(
			QName classRef,
			NodeRef sourceNodeRef,
			NodeRef destinationRef,
            boolean copyToNew,
			Map<NodeRef, NodeRef> copyMap)
	{
		// Do nothing since we do not want the copy from aspect to be relative to the copied nodes
	}
    
    public List<NodeRef> getCopies(NodeRef nodeRef)
    {
        List<NodeRef> copies = new ArrayList<NodeRef>();
        
        // Do a search to find the origional document
        ResultSet resultSet = null;
        try
        {
            resultSet = this.searchService.query(
                    nodeRef.getStoreRef(), 
                    SearchService.LANGUAGE_LUCENE, 
                    "+@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_COPY_REFERENCE.getLocalName() + ":\"" + nodeRef.toString() + "\"");
            
            for (NodeRef copy : resultSet.getNodeRefs())
            {
                copies.add(copy);
            }
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        
        return copies;
    }
}
