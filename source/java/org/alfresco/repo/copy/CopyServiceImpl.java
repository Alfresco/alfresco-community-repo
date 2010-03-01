/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionServiceImpl;
import org.alfresco.repo.copy.CopyBehaviourCallback.ChildAssocCopyAction;
import org.alfresco.repo.copy.CopyBehaviourCallback.ChildAssocRecurseAction;
import org.alfresco.repo.copy.CopyBehaviourCallback.CopyChildAssociationDetails;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CopyServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Node operations service implmentation.
 * 
 * @author Roy Wetherall
 */
public class CopyServiceImpl implements CopyService
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(ActionServiceImpl.class);
    
    /** I18N labels */
    private String COPY_OF_LABEL = "copy_service.copy_of_label";
    
    /** The node service */
    private NodeService nodeService;
    private NodeService internalNodeService;
    
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
     * Sets the internal node service
     * 
     * @param internalNodeService    the internal node service
     */
    public void setInternalNodeService(NodeService internalNodeService) 
    {
        this.internalNodeService = internalNodeService;
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
     * @param permissionService        the permission service
     */
    public void setPermissionService(PermissionService permissionService) 
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Sets the authentication service
     * 
     * @param authenticationService        the authentication service
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
        onCopyNodeDelegate = policyComponent.registerClassPolicy(CopyServicePolicies.OnCopyNodePolicy.class);
        onCopyCompleteDelegate = policyComponent.registerClassPolicy(CopyServicePolicies.OnCopyCompletePolicy.class);
        
        // Register policy behaviours
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_COPIEDFROM,
                new JavaBehaviour(this, "getCallbackForCopiedFromAspect"));    
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.TYPE_FOLDER,
                new JavaBehaviour(this, "getCallbackForFolderType"));    
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_OWNABLE,
                new JavaBehaviour(this, "getCallbackForOwnableAspect"));
    }
    
    public NodeRef copy(
            NodeRef sourceNodeRef,
            NodeRef targetParentRef, 
            QName assocTypeQName,
            QName assocQName, 
            boolean copyChildren)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("sourceNodeRef", sourceNodeRef);
        ParameterCheck.mandatory("targetParentRef", targetParentRef);
        ParameterCheck.mandatory("assocTypeQName", assocTypeQName);
        ParameterCheck.mandatory("assocQName", assocQName);

        if (sourceNodeRef.getStoreRef().equals(targetParentRef.getStoreRef()) == false)
        {
            // TODO We need to create a new node in the other store with the same id as the source

            // Error - since at the moment we do not support cross store copying
            throw new UnsupportedOperationException("Copying nodes across stores is not currently supported.");
        }
        
        // Keep track of copied children
        Map<NodeRef, NodeRef> copiesByOriginals = new HashMap<NodeRef, NodeRef>(17);
        Set<NodeRef> copies = new HashSet<NodeRef>(17);

        NodeRef copiedNodeRef = copyImpl(
                sourceNodeRef, targetParentRef,
                assocTypeQName, assocQName,
                copyChildren, true,                     // Drop cm:name for top-level node
                copiesByOriginals, copies);
        // Check if the node was copied
        if (copiedNodeRef == null)
        {
            CopyDetails copyDetails = getCopyDetails(sourceNodeRef, targetParentRef, null, assocTypeQName, assocQName);
            // Denied!
            throw new CopyServiceException(
                    "A bound policy denied copy: \n" +
                    "   " + copyDetails);
        }
        
        // Foreach of the newly created copies call the copy complete policy
        for (Map.Entry<NodeRef, NodeRef> entry : copiesByOriginals.entrySet())
        {
            NodeRef mappedSourceNodeRef = entry.getKey();
            NodeRef mappedTargetNodeRef = entry.getValue();
            invokeCopyComplete(mappedSourceNodeRef, mappedTargetNodeRef, true, copiesByOriginals);
        }
        
        // Done
        return copiedNodeRef;
    }
    
    public NodeRef copyAndRename(
            NodeRef sourceNodeRef,
            NodeRef destinationParent,
            QName assocTypeQName,
            QName assocQName, 
            boolean copyChildren) 
    {
        // To fix ETWOONE-224 issue it is necessary to change a QName of the new node accordingly to its name.
        NodeRef result = null;
        String sourceName = (String)this.internalNodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME);
                
        // Find a non-duplicate name
        String newName = sourceName;
        while (this.internalNodeService.getChildByName(destinationParent, assocTypeQName, newName) != null)
        {
            newName = I18NUtil.getMessage(COPY_OF_LABEL, newName);                        
        }
                
        if (assocQName == null)
        {
            // Change a QName of the new node accordingly to its name
            assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(newName));
        }
        
        // Make a copy
        result = copy(sourceNodeRef, destinationParent, assocTypeQName, assocQName, copyChildren);
        
        // Set name property
        this.internalNodeService.setProperty(result, ContentModel.PROP_NAME, newName);
        
        // Return new NodeRef
        return result;    
    }
    
    /**
     * {@inheritDoc}
     * 
     * Defer to the standard implementation with copyChildren set to false
     */
    public NodeRef copy(
            NodeRef sourceNodeRef,
            NodeRef targetParentNodeRef, 
            QName assocTypeQName,
            QName assocQName)
    {
        return copy(
                sourceNodeRef, 
                targetParentNodeRef, 
                assocTypeQName, 
                assocQName, 
                false);
    }

    public void copy(NodeRef sourceNodeRef, NodeRef targetNodeRef)
    {
        QName sourceNodeTypeQName = nodeService.getType(sourceNodeRef);
        QName targetNodeTypeQName = nodeService.getType(targetNodeRef);
        // Check that the source and destination node are the same type
        if (!sourceNodeTypeQName.equals(targetNodeTypeQName))
        {
            // Error - can not copy objects that are of different types
            throw new CopyServiceException("The source and destination node must be the same type.");
        }
        
        Map<QName, Serializable> sourceNodeProperties = nodeService.getProperties(sourceNodeRef);
        
        // Get the destinations node's details
        ChildAssociationRef destinationPrimaryAssocRef = nodeService.getPrimaryParent(targetNodeRef);
        NodeRef destinationParentNodeRef = destinationPrimaryAssocRef.getParentRef();
        QName assocTypeQName = destinationPrimaryAssocRef.getTypeQName();
        QName assocQName = destinationPrimaryAssocRef.getQName();
        
        // Get the copy details
        CopyDetails copyDetails = getCopyDetails(
                sourceNodeRef, destinationParentNodeRef, targetNodeRef,
                assocTypeQName, assocQName);
        
        // Get callbacks
        Map<QName, CopyBehaviourCallback> callbacks = getCallbacks(copyDetails);
        
        // Remove the name property from the source properties to avoid having it copied
        sourceNodeProperties.remove(ContentModel.PROP_NAME);
        
        // Copy 
        copyProperties(copyDetails, targetNodeRef, sourceNodeTypeQName, callbacks);
        copyAspects(copyDetails, targetNodeRef, Collections.<QName>emptySet(), callbacks);
        copyResidualProperties(copyDetails, targetNodeRef);
        
        // invoke the copy complete policy
        Map<NodeRef, NodeRef> copiedNodeRefs = new HashMap<NodeRef, NodeRef>(1);
        copiedNodeRefs.put(sourceNodeRef, targetNodeRef);
        invokeCopyComplete(sourceNodeRef, targetNodeRef, false, copiedNodeRefs);         
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

    /**
     * @return                  Returns <tt>null</tt> if the node was denied a copy
     */
    private NodeRef copyImpl(
            NodeRef sourceNodeRef,
            NodeRef targetParentRef,
            QName assocTypeQName,
            QName assocQName, 
            boolean copyChildren,
            boolean dropName,
            Map<NodeRef, NodeRef> copiesByOriginals,
            Set<NodeRef> copies)
    {
        // Build the top-level node's copy details
        CopyDetails copyDetails = getCopyDetails(sourceNodeRef, targetParentRef, null, assocTypeQName, assocQName);
        
        // Get the callbacks that will determine the copy behaviour
        Map<QName, CopyBehaviourCallback> callbacks = getCallbacks(copyDetails);
        
        // Check that the primary (type) callback allows copy
        QName sourceNodeTypeQName = copyDetails.getSourceNodeTypeQName();
        CopyBehaviourCallback callback = callbacks.get(sourceNodeTypeQName);
        if (callback == null)
        {
            throw new IllegalStateException("Source node type has no callback: " + sourceNodeTypeQName);
        }
        if (!callback.getMustCopy(sourceNodeTypeQName, copyDetails))
        {
            // Denied!
            return null;
        }
        
        // Recursive copies cannot have name conflicts, therefore copy names verbatim.
        NodeRef copiedNodeRef = recursiveCopy(
                copyDetails, copyChildren, dropName, copiesByOriginals, copies, callbacks);
        
        return copiedNodeRef;
    }
    
    /**
     * Recursive copy algorithm
     * 
     * @param dropName      drop the name property when associations don't allow duplicately named children
     */
    private NodeRef recursiveCopy(
            CopyDetails copyDetails,
            boolean copyChildren,
            boolean dropName,
            Map<NodeRef, NodeRef> copiesByOriginal,
            Set<NodeRef> copies,
            Map<QName, CopyBehaviourCallback> callbacks)
    {
        NodeRef sourceNodeRef = copyDetails.getSourceNodeRef();
        Set<QName> sourceNodeAspectQNames = copyDetails.getSourceNodeAspectQNames();
        NodeRef targetParentNodeRef = copyDetails.getTargetParentNodeRef();
        QName assocTypeQName = copyDetails.getAssocTypeQName();
        QName assocQName = copyDetails.getAssocQName();

        // Avoid duplicate and cyclic copies
        if (copies.contains(sourceNodeRef))
        {
            throw new IllegalStateException(
                    "Nested copy prevention has failed: \n" +
                    "   " + copyDetails + "\n" +
                    "   Copies by original: " + copiesByOriginal);
        }
        else if (copiesByOriginal.containsKey(sourceNodeRef))
        {
            throw new IllegalStateException(
                    "Multiple child assocs between same two nodes detected: \n" +
                    "   " + copyDetails + "\n" +
                    "   Copies by original: " + copiesByOriginal);
        }
        
        // Extract Type Definition
        QName sourceNodeTypeQName = copyDetails.getSourceNodeTypeQName();
        
        // Does this node get copied at all?
        // The source node's type-bound behaviour has an effective veto.
        CopyBehaviourCallback sourceTypeBehaviour = callbacks.get(sourceNodeTypeQName);
        if (sourceTypeBehaviour == null)
        {
            throw new IllegalStateException("Source node type has no callback: " + sourceNodeTypeQName);
        }
        if (!sourceTypeBehaviour.getMustCopy(sourceNodeTypeQName, copyDetails))
        {
            // Nothing to do
            return null;
        }
        
        // Get the type properties to copy
        Map<QName, Serializable> targetNodeProperties = buildCopyProperties(
                copyDetails,
                Collections.singleton(sourceNodeTypeQName),
                callbacks);

        // Some aspects are going to be applied automatically.  For efficiency, the initial node properties
        // for these aspects should be provided.
        Set<QName> defaultAspectQNames = getDefaultAspects(sourceNodeTypeQName);
        Map<QName, Serializable> defaultAspectsProperties = buildCopyProperties(
                copyDetails,
                defaultAspectQNames,
                callbacks);
        targetNodeProperties.putAll(defaultAspectsProperties);

        // Drop the name property, if required.  This prevents duplicate names and leaves it up to the client
        // to assign a new name.
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
        if (!assocDef.isChild())
        {
            throw new AlfrescoRuntimeException("Association is not a child association: " + assocTypeQName);
        }
        else
        {
            ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
            if (dropName && !childAssocDef.getDuplicateChildNamesAllowed())
            {
                // duplicate children are not allowed.
                targetNodeProperties.remove(ContentModel.PROP_NAME);
            }
        }
        
        // Lastly, make sure the the Node UUID is set correctly; after all, the contract
        // of the CopyDetails says that the targetNodeRef was already determined.
        String targetNodeUuid = copyDetails.getTargetNodeRef().getId();
        targetNodeProperties.put(ContentModel.PROP_NODE_UUID, targetNodeUuid);
        
        // The initial node copy is good to go
        ChildAssociationRef targetChildAssocRef = this.nodeService.createNode(
                targetParentNodeRef, 
                assocTypeQName,
                assocQName,
                sourceNodeTypeQName,
                targetNodeProperties);
        NodeRef targetNodeRef = targetChildAssocRef.getChildRef();
        // Save the mapping for later
        copiesByOriginal.put(sourceNodeRef, targetNodeRef);
        copies.add(targetNodeRef);

        // Work out which aspects still need copying.  The source aspects less the default aspects
        // will give this set.
        Set<QName> remainingAspectQNames = new HashSet<QName>(sourceNodeAspectQNames);
        remainingAspectQNames.removeAll(defaultAspectQNames);
        
        // Prevent any rules being fired on the new destination node
        this.ruleService.disableRules(targetNodeRef);
        try
        {
            // Apply the remaining aspects and properties
            for (QName remainingAspectQName : remainingAspectQNames)
            {
                copyProperties(copyDetails, targetNodeRef, remainingAspectQName, callbacks);
            }
            
            // Copy residual properties
            copyResidualProperties(copyDetails, targetNodeRef);
            
            //  Apply the copy aspect to the new node   
            Map<QName, Serializable> copyProperties = new HashMap<QName, Serializable>();
            copyProperties.put(ContentModel.PROP_COPY_REFERENCE, sourceNodeRef);
            internalNodeService.addAspect(targetNodeRef, ContentModel.ASPECT_COPIEDFROM, copyProperties);

            // Do not copy permissions
            
            // We present the recursion option regardless of what the client chooses
            copyChildren(
                    copyDetails,
                    targetNodeRef,
                    true,                               // We know that the node has been created
                    copyChildren,
                    copiesByOriginal,
                    copies,
                    callbacks);
        }
        finally
        {
            this.ruleService.enableRules(targetNodeRef);
        }
        
        return targetNodeRef;
    }
    
    private Set<QName> getDefaultAspects(QName sourceNodeTypeQName)
    {
        TypeDefinition sourceNodeTypeDef = dictionaryService.getType(sourceNodeTypeQName);
        if (sourceNodeTypeDef == null)
        {
            return Collections.emptySet();
        }
        Set<QName> defaultAspectQNames = new HashSet<QName>(7);
        for (AspectDefinition aspectDef : sourceNodeTypeDef.getDefaultAspects())
        {
            defaultAspectQNames.add(aspectDef.getName());
        }
        // Done
        return defaultAspectQNames;
    }
    
    /**
     * Constructs the properties to copy that apply to the type and default aspects 
     */
    private Map<QName, Serializable> buildCopyProperties(
            CopyDetails copyDetails,
            Set<QName> classQNames,
            Map<QName, CopyBehaviourCallback> callbacks)
    {
        Map<QName, Serializable> sourceNodeProperties = copyDetails.getSourceNodeProperties();
        Map<QName, Serializable> copyProperties = new HashMap<QName, Serializable>(sourceNodeProperties.size(), 1.0F);
        Map<QName, Serializable> scratchProperties = new HashMap<QName, Serializable>(11);
        // Each defined callback gets a chance to say which properties get copied
        // Only model-defined properties are considered
        for (QName classQName : classQNames)
        {
            CopyBehaviourCallback callback = callbacks.get(classQName);
            if (callback == null)
            {
                throw new IllegalStateException("Source node class has no callback: " + classQName);
            }
            // Ignore if not present or if not scheduled for a copy
            if (!callback.getMustCopy(classQName, copyDetails))
            {
                continue;
            }
            // Get the dictionary definition
            ClassDefinition classDef = dictionaryService.getClass(classQName);
            if (classDef == null)
            {
                continue;
            }
            // Get the defined properties
            Map<QName, PropertyDefinition> propertyDefs = classDef.getProperties();
            // Extract these from the source nodes properties and store in a safe (modifiable) map
            scratchProperties.clear();
            for (QName propertyQName : propertyDefs.keySet())
            {
                Serializable value = sourceNodeProperties.get(propertyQName);
                if (value == null)
                {
                    continue;
                }
                scratchProperties.put(propertyQName, value);
            }
            // What does the behaviour do with properties?
            scratchProperties = callback.getCopyProperties(classQName, copyDetails, scratchProperties);
            // Add to the final properties
            copyProperties.putAll(scratchProperties);
        }
        // Done
        return copyProperties;
    }
    
    /**
     * Invokes the copy complete policy for the node reference provided
     * 
     * @param sourceNodeRef         the source node reference
     * @param targetNodeRef         the destination node reference
     * @param copiedNodeRefs        the map of copied node references
     */
    private void invokeCopyComplete(
            NodeRef sourceNodeRef, 
            NodeRef targetNodeRef, 
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copiedNodeRefs)
    {
        QName sourceClassRef = internalNodeService.getType(sourceNodeRef);     
        invokeCopyComplete(sourceClassRef, sourceNodeRef, targetNodeRef, copyToNewNode, copiedNodeRefs);
        
        // Get the source aspects
        Set<QName> sourceAspects = this.nodeService.getAspects(sourceNodeRef);
        for (QName sourceAspect : sourceAspects) 
        {
            invokeCopyComplete(sourceAspect, sourceNodeRef, targetNodeRef, copyToNewNode, copiedNodeRefs);
        }
    }

    private void invokeCopyComplete(
            QName typeQName, 
            NodeRef sourceNodeRef, 
            NodeRef targetNodeRef, 
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copiedNodeRefs)
    {
        Collection<CopyServicePolicies.OnCopyCompletePolicy> policies = onCopyCompleteDelegate.getList(typeQName);
        for (CopyServicePolicies.OnCopyCompletePolicy policy : policies) 
        {
            policy.onCopyComplete(typeQName, sourceNodeRef, targetNodeRef, copyToNewNode, copiedNodeRefs);
        }
    }


    /**
     * Gets the copy details.  This calls the appropriate policies that have been registered
     * against the node and aspect types in order to pick-up any type specific copy behaviour.
     * <p>
     * The full {@link NodeService} is used for property retrieval.  After this, read permission
     * can be assumed to have passed on the source node - at least w.r.t. properties and aspects.
     * <p>
     * <b>NOTE:</b> If a target node is not supplied, then one is created in the same store as the
     *              target parent node.  This allows behavioural code always know which node will
     *              be copied to, even if the node does not exist.
     */
    private CopyDetails getCopyDetails(
            NodeRef sourceNodeRef,
            NodeRef targetParentNodeRef,
            NodeRef targetNodeRef,
            QName assocTypeQName,
            QName assocQName)
    {
        // The first call will fail permissions, so there is no point doing permission checks with
        // the other calls
        Map<QName, Serializable> sourceNodeProperties = nodeService.getProperties(sourceNodeRef);
        QName sourceNodeTypeQName = internalNodeService.getType(sourceNodeRef);
        Set<QName> sourceNodeAspectQNames = internalNodeService.getAspects(sourceNodeRef);
        
        // Create a target node, if necessary
        boolean targetNodeIsNew = false;
        if (targetNodeRef == null)
        {
            targetNodeRef = new NodeRef(targetParentNodeRef.getStoreRef(), GUID.generate());
            targetNodeIsNew = true;
        }
        
        CopyDetails copyDetails = new CopyDetails(
                sourceNodeRef,
                sourceNodeTypeQName,
                sourceNodeAspectQNames,
                sourceNodeProperties,
                targetParentNodeRef,
                targetNodeRef,
                targetNodeIsNew,
                assocTypeQName,
                assocQName);
        
        // Done
        return copyDetails;
    }
    
    /**
     * @return         Returns a map of all the copy behaviours keyed by type and aspect qualified names
     */
    private Map<QName, CopyBehaviourCallback> getCallbacks(CopyDetails copyDetails)
    {
        QName sourceNodeTypeQName = copyDetails.getSourceNodeTypeQName();
        
        Map<QName, CopyBehaviourCallback> callbacks = new HashMap<QName, CopyBehaviourCallback>(11);
        // Get the type-specific behaviour
        CopyBehaviourCallback callback = getCallback(sourceNodeTypeQName, copyDetails);
        callbacks.put(sourceNodeTypeQName, callback);
        
        // Get the source aspects
        for (QName sourceNodeAspectQName : copyDetails.getSourceNodeAspectQNames()) 
        {
            callback = getCallback(sourceNodeAspectQName, copyDetails);
            callbacks.put(sourceNodeAspectQName, callback);
        }
        
        return callbacks;
    }
    
    /**
     * @return             Returns the copy callback for the given criteria
     */
    private CopyBehaviourCallback getCallback(QName sourceClassQName, CopyDetails copyDetails)
    {
        Collection<CopyServicePolicies.OnCopyNodePolicy> policies = this.onCopyNodeDelegate.getList(sourceClassQName);
        ClassDefinition sourceClassDef = dictionaryService.getClass(sourceClassQName);
        CopyBehaviourCallback callback = null;
        if (sourceClassDef == null)
        {
            // Do nothing as the type is not in the dictionary
            callback = DoNothingCopyBehaviourCallback.getInstance();
        }
        if (policies.isEmpty())
        {
            // Default behaviour
            callback = DefaultCopyBehaviourCallback.getInstance();
        }
        else if (policies.size() == 1)
        {
            callback = policies.iterator().next().getCopyCallback(sourceClassQName, copyDetails);
        }
        else
        {
            // There are multiple
            CompoundCopyBehaviourCallback compoundCallback = new CompoundCopyBehaviourCallback(sourceClassQName);
            for (CopyServicePolicies.OnCopyNodePolicy policy : policies)
            {
                CopyBehaviourCallback nestedCallback = policy.getCopyCallback(sourceClassQName, copyDetails);
                compoundCallback.addBehaviour(nestedCallback);
            }
            callback = compoundCallback;
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Fetched copy callback: \n" +
                    "   Class:                      " + sourceClassQName + "\n" +
                    "   Details:                    " + copyDetails + "\n" +
                    "   Callback: " + callback);
        }
        return callback;
    }
    
    /**
     * Copies the properties for the node type or aspect onto the destination node.
     */
    private void copyProperties(
            CopyDetails copyDetails, 
            NodeRef targetNodeRef,
            QName classQName,
            Map<QName, CopyBehaviourCallback> callbacks)
    {
        ClassDefinition targetClassDef = dictionaryService.getClass(classQName);
        if (targetClassDef == null)
        {
            return;                        // Ignore unknown types
        }
        // First check if the aspect must be copied at all
        CopyBehaviourCallback callback = callbacks.get(classQName);
        if (callback == null)
        {
            throw new IllegalStateException("Source node class has no callback: " + classQName);
        }
        // Ignore if not present or if not scheduled for a copy
        if (!callback.getMustCopy(classQName, copyDetails))
        {
            // Do nothing with this
            return;
        }
        // Compile the properties to copy, even if they are empty
        Map<QName, Serializable> classProperties = buildCopyProperties(
                copyDetails,
                Collections.singleton(classQName),
                callbacks);
        // We don't need permissions as we've just created the node
        if (targetClassDef.isAspect())
        {
            internalNodeService.addAspect(targetNodeRef, classQName, classProperties);
        }
        else
        {
            internalNodeService.addProperties(targetNodeRef, classProperties);
        }
    }
    
    /**
     * Copy properties that do not belong to the source node's type or any of the aspects.
     */
    private void copyResidualProperties(
            CopyDetails copyDetails,
            NodeRef targetNodeRef)
    {
        Map<QName, Serializable> residualProperties = new HashMap<QName, Serializable>();
        // Start with the full set
        residualProperties.putAll(copyDetails.getSourceNodeProperties());
        
        QName sourceNodeTypeQName = copyDetails.getSourceNodeTypeQName();
        Set<QName> knownClassQNames = new HashSet<QName>(13);
        // We add the default aspects, source-applied aspects and the source node type
        knownClassQNames.addAll(getDefaultAspects(sourceNodeTypeQName));
        knownClassQNames.addAll(copyDetails.getSourceNodeAspectQNames());
        knownClassQNames.add(sourceNodeTypeQName);
        
        for (QName knownClassQName : knownClassQNames)
        {
            ClassDefinition classDef = dictionaryService.getClass(knownClassQName);
            if (classDef == null)
            {
                continue;
            }
            // Remove defined properties form the residual list
            for (QName definedPropQName : classDef.getProperties().keySet())
            {
                residualProperties.remove(definedPropQName);
                // We've removed them all, so shortcut out
                if (residualProperties.size() == 0)
                {
                    break;
                }
            }
        }
        // Add the residual properties to the node
        if (residualProperties.size() > 0)
        {
            internalNodeService.addProperties(targetNodeRef, residualProperties);
        }
    }
    
    /**
     * Copies aspects from the source to the target node.
     */
    private void copyAspects(
            CopyDetails copyDetails, 
            NodeRef targetNodeRef,
            Set<QName> aspectsToIgnore,
            Map<QName, CopyBehaviourCallback> callbacks)
    {
        Set<QName> sourceAspectQNames = copyDetails.getSourceNodeAspectQNames();
        for (QName aspectQName : sourceAspectQNames)
        {
            if (aspectsToIgnore.contains(aspectQName))
            {
                continue;
            }
            
            // Double check that the aspect must be copied at all
            CopyBehaviourCallback callback = callbacks.get(aspectQName);
            if (callback == null)
            {
                throw new IllegalStateException("Source aspect class has no callback: " + aspectQName);
            }
            if (!callback.getMustCopy(aspectQName, copyDetails))
            {
                continue;
            }
            copyProperties(copyDetails, targetNodeRef, aspectQName, callbacks);
        }
    }
    
    /**
     * @param copyChildren              <tt>false</tt> if the client selected not to recurse
     */
    private void copyChildren(
            CopyDetails copyDetails,
            NodeRef targetNodeRef,
            boolean targetNodeIsNew,
            boolean copyChildren,
            Map<NodeRef, NodeRef> copiesByOriginals,
            Set<NodeRef> copies,
            Map<QName, CopyBehaviourCallback> callbacks)
    {
        QName sourceNodeTypeQName = copyDetails.getSourceNodeTypeQName();
        Set<QName> sourceNodeAspectQNames = copyDetails.getSourceNodeAspectQNames();
        // First check associations on the type
        copyChildren(
                copyDetails,
                sourceNodeTypeQName,
                targetNodeRef,
                targetNodeIsNew,
                copyChildren,
                copiesByOriginals,
                copies,
                callbacks);
        // Check associations for the aspects
        for (QName aspectQName : sourceNodeAspectQNames)
        {
            AspectDefinition aspectDef = dictionaryService.getAspect(aspectQName);
            if (aspectDef == null)
            {
                continue;
            }
            copyChildren(
                    copyDetails,
                    aspectQName,
                    targetNodeRef,
                    targetNodeIsNew,
                    copyChildren,
                    copiesByOriginals,
                    copies,
                    callbacks);
        }
    }

    /**
     * @param copyChildren              <tt>false</tt> if the client selected not to recurse
     */
    private void copyChildren(
            CopyDetails copyDetails,
            QName classQName,
            NodeRef targetNodeRef,
            boolean targetNodeIsNew,
            boolean copyChildren,
            Map<NodeRef, NodeRef> copiesByOriginals,
            Set<NodeRef> copies,
            Map<QName, CopyBehaviourCallback> callbacks)
    {
        NodeRef sourceNodeRef = copyDetails.getSourceNodeRef();
        
        ClassDefinition classDef = dictionaryService.getClass(classQName);
        if (classDef == null)
        {
            // Ignore missing types
            return;
        }
        // Check the behaviour
        CopyBehaviourCallback callback = callbacks.get(classQName);
        if (callback == null)
        {
            throw new IllegalStateException("Source node class has no callback: " + classQName);
        }
        for (Map.Entry<QName, ChildAssociationDefinition> entry : classDef.getChildAssociations().entrySet())
        {
            QName assocTypeQName = entry.getKey();
            ChildAssociationDefinition assocDef = entry.getValue();
            if (!assocDef.isChild())
            {
                continue;                   // Ignore non-child assocs
            }
            // Get the child associations
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
                    sourceNodeRef, assocTypeQName, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef childAssocRef : childAssocRefs)
            {
                NodeRef childNodeRef = childAssocRef.getChildRef();
                QName assocQName = childAssocRef.getQName();
                
                CopyChildAssociationDetails childAssocCopyDetails = new CopyChildAssociationDetails(
                        childAssocRef,
                        targetNodeRef,
                        targetNodeIsNew,
                        copyChildren);
                
                // Handle nested copies
                if (copies.contains(childNodeRef))
                {
                    // The node was already copied i.e. we are seeing a copy produced by some earlier
                    // copy process.
                    // The first way this can occur is if a hierarchy is copied into some lower part
                    // of the hierarchy.  We avoid the copied part.
                    // The other way this could occur is if there are multiple assocs between a
                    // parent and child.  Calls to this method are scoped by class, so the newly-created
                    // node will not be found because it will have been created using a different assoc
                    // type.
                    // A final edge case is where there are multiple assocs between parent and child
                    // of the same type.  This is ignorable.
                    continue;
                }
                // Check the callbacks
                ChildAssocCopyAction childAssocCopyAction = callback.getChildAssociationCopyAction(
                        classQName,
                        copyDetails,
                        childAssocCopyDetails);
                switch (childAssocCopyAction)
                {
                case IGNORE:
                    break;
                case COPY_ASSOC:
                    nodeService.addChild(targetNodeRef, childNodeRef, assocTypeQName, assocQName);
                    break;
                case COPY_CHILD:
                    // Handle potentially cyclic relationships
                    if (copiesByOriginals.containsKey(childNodeRef))
                    {
                        // This is either a cyclic relationship or there are multiple different
                        // types of associations between the same parent and child.
                        // Just hook the child up with the association.
                        nodeService.addChild(targetNodeRef, childNodeRef, assocTypeQName, assocQName);
                    }
                    else
                    {
                        // Find out whether to force a recursion
                        ChildAssocRecurseAction childAssocRecurseAction = callback.getChildAssociationRecurseAction(
                                classQName,
                                copyDetails,
                                childAssocCopyDetails);
                        switch (childAssocRecurseAction)
                        {
                        case RESPECT_RECURSE_FLAG:
                            // Keep child copy flag the same
                            break;
                        case FORCE_RECURSE:
                            // Force recurse
                            copyChildren = true;
                            break;
                        default:
                            throw new IllegalStateException("Unrecognized enum");
                        }
                        // This copy may fail silently
                        copyImpl(
                                childNodeRef, targetNodeRef,
                                assocTypeQName, assocQName,
                                copyChildren, false,                // Keep child names for deep copies
                                copiesByOriginals, copies);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unrecognized enum");
                }
            }
        }
    }

    /**
     * Callback behaviour retrieval for the 'copiedFrom' aspect.
     * 
     * @return              Returns {@link DoNothingCopyBehaviourCallback} always
     */
    public CopyBehaviourCallback getCallbackForCopiedFromAspect(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }
    
    /**
     * Callback behaviour retrieval for {@link ContentModel#TYPE_FOLDER} aspect.
     * 
     * @return              Returns {@link FolderTypeCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCallbackForFolderType(QName classRef, CopyDetails copyDetails)
    {
        return FolderTypeCopyBehaviourCallback.INSTANCE;
    }

    /**
     * <b>cm:folder</b> behaviour
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class FolderTypeCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new FolderTypeCopyBehaviourCallback();

        /**
         * Respects the <code>copyChildren</code> flag.  Child nodes are copied fully if the association
         * is primary otherwise secondary associations are duplicated.
         */
        @Override
        public ChildAssocCopyAction getChildAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyChildAssociationDetails childAssocCopyDetails)
        {
            ChildAssociationRef childAssocRef = childAssocCopyDetails.getChildAssocRef();
            boolean copyChildren = childAssocCopyDetails.isCopyChildren();
            if (childAssocRef.getTypeQName().equals(ContentModel.ASSOC_CONTAINS))
            {
                if (!copyChildren)
                {
                    return ChildAssocCopyAction.IGNORE;
                }
                if (childAssocRef.isPrimary())
                {
                    return ChildAssocCopyAction.COPY_CHILD;
                }
                else
                {
                    return ChildAssocCopyAction.COPY_ASSOC;
                }
            }
            else
            {
                throw new IllegalStateException(
                        "Behaviour should have been invoked: \n" +
                        "   Aspect: " + this.getClass().getName() + "\n" +
                        "   Assoc:  " + childAssocRef + "\n" +
                        "   " + copyDetails);
            }
        }
        
    }
    
    /**
     * Callback behaviour retrieval for the 'ownable' aspect.
     * 
     * @return              Returns {@link DoNothingCopyBehaviourCallback} always
     */
    public CopyBehaviourCallback getCallbackForOwnableAspect(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }    
}
