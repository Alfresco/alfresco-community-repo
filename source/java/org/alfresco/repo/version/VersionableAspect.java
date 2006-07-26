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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class containing behaviour for the versionable aspect
 * 
 * @author Roy Wetherall
 */
public class VersionableAspect implements ContentServicePolicies.OnContentUpdatePolicy, 
										  NodeServicePolicies.OnAddAspectPolicy,
										  NodeServicePolicies.OnRemoveAspectPolicy,
										  NodeServicePolicies.OnDeleteNodePolicy
{
	/** The i18n'ized messages */
	private static final String MSG_INITIAL_VERSION = "create_version.initial_version";
	private static final String MSG_AUTO_VERSION = "create_version.auto_version";
	
	/** Transaction resource key */
	private static final String KEY_INITIAL_VERSION = "initial_version_";
	
    /** The policy component */
	private PolicyComponent policyComponent;
    
    /** The node service */
    private NodeService nodeService;
    
    /** The Version service */
    private VersionService versionService;

    /** Auto version behaviour */
    private Behaviour autoVersionBehaviour;
	
    /**
     * Set the policy component
     * 
     * @param policyComponent   the policy component
     */
	public void setPolicyComponent(PolicyComponent policyComponent)
	{
		this.policyComponent = policyComponent;
	}
    
	/**
	 * Set the version service
	 * 
	 * @param versionService	the version service
	 */
    public void setVersionService(VersionService versionService) 
    {
		this.versionService = versionService;
	}
    
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Initialise the versionable aspect policies
     */
	public void init()
	{
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), 
				ContentModel.ASPECT_VERSIONABLE, 
                new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"), 
				ContentModel.ASPECT_VERSIONABLE, 
                new JavaBehaviour(this, "onRemoveAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), 
				ContentModel.ASPECT_VERSIONABLE, 
                new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
		
        autoVersionBehaviour = new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.ON_CONTENT_UPDATE,
                ContentModel.ASPECT_VERSIONABLE,
                autoVersionBehaviour);
        
        // Register the copy behaviour
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_VERSIONABLE,
                new JavaBehaviour(this, "onCopy"));
	}
    
	/**
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy#onDeleteNode(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
	 */
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) 
	{
    	if (isNodeArchived == false)
    	{
    		// If we are perminantly deleting the node then we need to remove the associated version history
    		this.versionService.deleteVersionHistory(childAssocRef.getChildRef());
    	}
    	// otherwise we do nothing since we need to hold onto the version history in case the node is restored later
	}
	
    /**
     * OnCopy behaviour implementation for the version aspect.
     * <p>
     * Ensures that the propety values of the version aspect are not copied onto
     * the destination node.
     * 
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#onCopyNode(QName, NodeRef, StoreRef, boolean, PolicyScope)
     */
    public void onCopy(
            QName sourceClassRef, 
            NodeRef sourceNodeRef, 
            StoreRef destinationStoreRef,
            boolean copyToNewNode,            
            PolicyScope copyDetails)
    {
        // Add the version aspect, but do not copy the version label
        copyDetails.addAspect(ContentModel.ASPECT_VERSIONABLE);
        copyDetails.addProperty(
                ContentModel.ASPECT_VERSIONABLE, 
                ContentModel.PROP_AUTO_VERSION, 
                this.nodeService.getProperty(sourceNodeRef, ContentModel.PROP_AUTO_VERSION));
    }   
 
	
	/**
	 * On add aspect policy behaviour
     * 
	 * @param nodeRef
	 * @param aspectTypeQName
	 */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
	    if (aspectTypeQName.equals(ContentModel.ASPECT_VERSIONABLE) == true)
        {
            boolean initialVersion = true;
            Boolean value = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_INITIAL_VERSION);
            if (value != null)
            {
                initialVersion = value.booleanValue();
            }
            // else this means that the default value has not been set the versionable aspect we applied pre-1.2
            
            if (initialVersion == true)
            {
                // Queue create version action
            	Map<String, Serializable> versionDetails = new HashMap<String, Serializable>(1);
            	versionDetails.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_INITIAL_VERSION));
            	this.versionService.createVersion(nodeRef, versionDetails);
            	
            	// Keep track of the fact that the initial version has been created
            	AlfrescoTransactionSupport.bindResource(KEY_INITIAL_VERSION + nodeRef.toString(), nodeRef);
            }
        }
	}
	
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) 
	{
		// When the versionable aspect is removed from a node, then delete the associatied verison history
		this.versionService.deleteVersionHistory(nodeRef);
	}
    
    /**
     * On content update policy bahaviour
     * 
     * @param nodeRef   the node reference
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
        {
        	// Determine whether we have already created an initial version during this transaction
        	if (AlfrescoTransactionSupport.getResource(KEY_INITIAL_VERSION + nodeRef.toString()) == null)
        	{
	            // Determine whether the node is auto versionable or not
	            boolean autoVersion = false;
	            Boolean value = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION);
	            if (value != null)
	            {
	                // If the value is not null then 
	                autoVersion = value.booleanValue();
	            }
	            // else this means that the default value has not been set and the versionable aspect was applied pre-1.1
	            
	            if (autoVersion == true)
	            {
	                // Create the auto-version
	            	Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
	            	versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_AUTO_VERSION));
	                this.versionService.createVersion(nodeRef, versionProperties);
	            }
        	}
        }
    }
	
    /**
     * Enable the auto version behaviour
     *
     */
    public void enableAutoVersion()
    {
        this.autoVersionBehaviour.enable();
    }
    
    /**
     * Disable the auto version behaviour
     *
     */
    public void disableAutoVersion()
    {
        this.autoVersionBehaviour.disable();
    }  
}
