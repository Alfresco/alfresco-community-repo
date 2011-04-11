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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Class containing behaviour for the versionable aspect
 * 
 * @author Roy Wetherall, janv
 */
public class VersionableAspect implements ContentServicePolicies.OnContentUpdatePolicy, 
                                          NodeServicePolicies.OnAddAspectPolicy,
                                          NodeServicePolicies.OnRemoveAspectPolicy,
                                          NodeServicePolicies.OnDeleteNodePolicy,
                                          NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          VersionServicePolicies.AfterCreateVersionPolicy,
                                          CopyServicePolicies.OnCopyNodePolicy
{
    /** The i18n'ized messages */
    private static final String MSG_INITIAL_VERSION = "create_version.initial_version";
    private static final String MSG_AUTO_VERSION = "create_version.auto_version";
    private static final String MSG_AUTO_VERSION_PROPS = "create_version.auto_version_props";
    
    /** Transaction resource key */
    private static final String KEY_VERSIONED_NODEREFS = "versioned_noderefs";
    
    /** The policy component */
    private PolicyComponent policyComponent;
    
    /** The node service */
    private NodeService nodeService;
    
    /** The Version service */
    private VersionService versionService;
    
    /** Behaviours */
    JavaBehaviour onUpdatePropertiesBehaviour;
    
    /** 
     * Optional list of excluded props 
     * - only applies if cm:autoVersionOnUpdateProps=true (and cm:autoVersion=true)
     * - if any one these props changes then "auto version on prop update" does not occur (even if there are other property changes)
     */
    private List<String> excludedOnUpdateProps = Collections.emptyList();
    
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
     * @param versionService    the version service
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
     * @return              Returns the current list of properties that <b>do not</b> trigger versioning
     */
    public List<String> getExcludedOnUpdateProps()
    {
        return excludedOnUpdateProps;
    }

    /**
     * @param excludedOnUpdateProps         the list of properties that force versioning to ignore changes
     */
    public void setExcludedOnUpdateProps(List<String> excludedOnUpdateProps)
    {
        this.excludedOnUpdateProps = Collections.unmodifiableList(excludedOnUpdateProps);
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
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "afterCreateVersion"),
                ContentModel.ASPECT_VERSIONABLE,
                new JavaBehaviour(this, "afterCreateVersion", Behaviour.NotificationFrequency.EVERY_EVENT));
        
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.ASPECT_VERSIONABLE,
                new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        
        onUpdatePropertiesBehaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(
            QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
            ContentModel.ASPECT_VERSIONABLE,
            onUpdatePropertiesBehaviour);
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_VERSIONABLE,
                new JavaBehaviour(this, "getCopyCallback"));
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
     * @return          Returns the {@link VersionableAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return VersionableAspectCopyBehaviourCallback.INSTANCE;
    }

    /**
     * Copy behaviour for the <b>cm:versionable</b> aspect
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class VersionableAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new VersionableAspectCopyBehaviourCallback();

        /**
         * Copy the aspect, but only the {@link ContentModel#PROP_AUTO_VERSION} and {@link ContentModel#PROP_AUTO_VERSION_PROPS} properties
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName,
                CopyDetails copyDetails,
                Map<QName, Serializable> properties)
        {
            Serializable value1 = properties.get(ContentModel.PROP_AUTO_VERSION);
            Serializable value2 = properties.get(ContentModel.PROP_AUTO_VERSION_PROPS);
            
            if ((value1 != null) || (value2 != null))
            {
                Map<QName, Serializable> newProperties = new HashMap<QName, Serializable>(2);
                
                if (value1 != null)
                {
                    newProperties.put(ContentModel.PROP_AUTO_VERSION, value1);
                }
                
                if (value2 != null)
                {
                    newProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, value2);
                }
                
                return newProperties;
            }
            else
            {
                return Collections.emptyMap();
            }
        }
    }
    
    /**
     * On add aspect policy behaviour
     * 
     * @param nodeRef
     * @param aspectTypeQName
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (this.nodeService.exists(nodeRef) == true && aspectTypeQName.equals(ContentModel.ASPECT_VERSIONABLE) == true)
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
                @SuppressWarnings("unchecked")
                Map<NodeRef, NodeRef> versionedNodeRefs = (Map<NodeRef, NodeRef>) AlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS);
                if (versionedNodeRefs == null || versionedNodeRefs.containsKey(nodeRef) == false)
                {
                    // Create the initial-version
                    Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
                    versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_INITIAL_VERSION));
                    
                    createVersionImpl(nodeRef, versionProperties);
                }
            }
        }
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) 
    {
        // When the versionable aspect is removed from a node, then delete the associated version history
        this.versionService.deleteVersionHistory(nodeRef);
    }
    
    /**
     * On content update policy behaviour
     * 
     * If applicable and "cm:autoVersion" is TRUE then version the node on content update (even if no property updates)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (this.nodeService.exists(nodeRef) == true && 
            this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true && 
            this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY) == false)
        {
            Map<NodeRef, NodeRef> versionedNodeRefs = (Map)AlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS);
            if (versionedNodeRefs == null || versionedNodeRefs.containsKey(nodeRef) == false)
            {
                // Determine whether the node is auto versionable (for content updates) or not
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
                    
                    createVersionImpl(nodeRef, versionProperties);
                }
            }
        }
    }
    
    /**
     * On update properties policy behaviour 
     * 
     * If applicable and "cm:autoVersionOnUpdateProps" is TRUE then version the node on properties update (even if no content updates)
     * 
     * @since 3.2
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if ((this.nodeService.exists(nodeRef) == true) && 
            (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true) && 
            (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY) == false) &&
            (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false))
        {
        	onUpdatePropertiesBehaviour.disable();
        	try
        	{
	            Map<NodeRef, NodeRef> versionedNodeRefs = (Map)AlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS);
	            if (versionedNodeRefs == null || versionedNodeRefs.containsKey(nodeRef) == false)
	            {
	                // Determine whether the node is auto versionable (for property only updates) or not
	                boolean autoVersion = false;
	                Boolean value = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION);
	                if (value != null)
	                {
	                    // If the value is not null then 
	                    autoVersion = value.booleanValue();
	                }
	                
	                boolean autoVersionProps = false;
	                value = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS);
	                if (value != null)
	                {
	                    // If the value is not null then 
	                    autoVersionProps = value.booleanValue();
	                }
	                
	                if ((autoVersion == true) && (autoVersionProps == true))
	                {
	                    // Check for explicitly excluded props - if one or more excluded props changes then do not auto-version on this event (even if other props changed)
	                    if (excludedOnUpdateProps.size() > 0)
	                    {
	                        Map<String, QName> propNames = new HashMap<String, QName>(after.size());
	                        for (QName afterProp : after.keySet())
	                        {
	                            if (excludedOnUpdateProps.contains(afterProp.getPrefixString()))
	                            {
	                                propNames.put(afterProp.getPrefixString(), afterProp);
	                            }
	                        }
	                        for (QName beforeProp : before.keySet())
	                        {
	                            if (excludedOnUpdateProps.contains(beforeProp.getPrefixString()))
	                            {
	                                propNames.put(beforeProp.getPrefixString(), beforeProp);
	                            }
	                        }
	                        
	                        if (propNames.size() > 0)
	                        {
	                            for (QName prop : propNames.values())
	                            {
	                                Serializable beforeValue = before.get(prop);
	                                Serializable afterValue = after.get(prop);
	                                
	                                if (EqualsHelper.nullSafeEquals(beforeValue, afterValue) != true)
	                                {
	                                    // excluded - do not version
	                                    return;
	                                }
	                            }
	                        }
	                        
	                        // drop through and auto-version
	                    }
	                    
	                    // Create the auto-version
	                    Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(4);
	                    versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_AUTO_VERSION_PROPS));
	                    versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
	                    
	                    createVersionImpl(nodeRef, versionProperties);
	                }
	            }
	        }
            finally
            {
            	onUpdatePropertiesBehaviour.enable();
            }
        }
    }
    
    /**
     * On create version implementation method
     * 
     * @param nodeRef
     * @param versionProperties
     */
    private void createVersionImpl(NodeRef nodeRef, Map<String, Serializable> versionProperties)
    {
        recordCreateVersion(nodeRef, null);
        this.versionService.createVersion(nodeRef, versionProperties);
    }
    
    /**
     * @see org.alfresco.repo.version.VersionServicePolicies.OnCreateVersionPolicy#onCreateVersion(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, java.util.Map, org.alfresco.repo.policy.PolicyScope)
     */
    public void afterCreateVersion(NodeRef versionableNode, Version version) 
    {
        recordCreateVersion(versionableNode, version);
    }
    
    @SuppressWarnings("unchecked")
    private void recordCreateVersion(NodeRef versionableNode, Version version) 
    {
        Map<NodeRef, NodeRef> versionedNodeRefs = (Map<NodeRef, NodeRef>)AlfrescoTransactionSupport.getResource(KEY_VERSIONED_NODEREFS);
        if (versionedNodeRefs == null)
        {
            versionedNodeRefs = new HashMap<NodeRef, NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_VERSIONED_NODEREFS, versionedNodeRefs);
        }
        versionedNodeRefs.put(versionableNode, versionableNode);
    }
}
