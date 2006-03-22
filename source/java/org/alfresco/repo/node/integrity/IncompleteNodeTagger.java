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
package org.alfresco.repo.node.integrity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component that tags {@link org.alfresco.model.ContentModel#ASPECT_INCOMPLETE incomplete} nodes. 
 * 
 * @author Derek Hulley
 */
public class IncompleteNodeTagger
        extends     TransactionListenerAdapter
        implements  NodeServicePolicies.OnCreateNodePolicy,
                    NodeServicePolicies.OnUpdatePropertiesPolicy,
                    NodeServicePolicies.OnAddAspectPolicy,
                    NodeServicePolicies.OnRemoveAspectPolicy
{
    private static Log logger = LogFactory.getLog(IncompleteNodeTagger.class);
    
    /** key against which the set of nodes to check is stored in the current transaction */
    private static final String KEY_NODE_SET = "IncompleteNodeTagger.NodeSet";
    
    private PolicyComponent policyComponent;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    
    public IncompleteNodeTagger()
    {
    }

    /**
     * @param policyComponent the component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param dictionaryService the dictionary against which to confirm model details
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeService the node service to use for browsing node structures
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Registers the system-level policy behaviours
     */
    public void init()
    {
        // check that required properties have been set
        PropertyCheck.mandatory("IncompleteNodeTagger", "dictionaryService", dictionaryService);
        PropertyCheck.mandatory("IncompleteNodeTagger", "nodeService", nodeService);
        PropertyCheck.mandatory("IncompleteNodeTagger", "policyComponent", policyComponent);

        // register behaviour
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                this,
                new JavaBehaviour(this, "onCreateNode"));   
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                this,
                new JavaBehaviour(this, "onUpdateProperties"));   
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                this,
                new JavaBehaviour(this, "onAddAspect"));   
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
                this,
                new JavaBehaviour(this, "onRemoveAspect"));   
    }
    
    /**
     * @return Returns the set of nodes to check, or null if none were registered
     */
    @SuppressWarnings("unchecked")
    private Set<NodeRef> getNodeSet()
    {
        return (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_NODE_SET);
    }
    
    /**
     * Ensures that this service is registered with the transaction and saves the node
     * reference for use later.
     * 
     * @param nodeRef
     */
    private void save(NodeRef nodeRef)
    {
        // register this service
        AlfrescoTransactionSupport.bindListener(this);
        
        // get the event list
        Set<NodeRef> nodeRefs = getNodeSet();
        if (nodeRefs == null)
        {
            nodeRefs = new HashSet<NodeRef>(31, 0.75F);
            AlfrescoTransactionSupport.bindResource(KEY_NODE_SET, nodeRefs);
        }
        // add node to the set
        nodeRefs.add(nodeRef);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Added node reference to set: " + nodeRef);
        }
    }

    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        save(nodeRef);
    }

    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        save(nodeRef);
    }

    /**
     * Save the node for checking of properties.
     * The {@link org.alfresco.model.ContentModel#ASPECT_INCOMPLETE incomplete} aspect is
     * not processed.
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (aspectTypeQName.equals(ContentModel.ASPECT_INCOMPLETE))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignoring aspect addition: " + ContentModel.ASPECT_INCOMPLETE);
            }
        }
        save(nodeRef);
    }

    /**
     * Recheck the node as an aspect was removed.
     */
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (aspectTypeQName.equals(ContentModel.ASPECT_INCOMPLETE))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignoring aspect removal: " + ContentModel.ASPECT_INCOMPLETE);
            }
        }
        save(nodeRef);
    }
    
    /**
     * Process all the nodes that require checking within the transaction.
     */
    @Override
    public void beforeCommit(boolean readOnly)
    {
        Set<NodeRef> nodeRefs = getNodeSet();
        // clear the set out of the transaction
        // there may be processes that react to the addition/removal of the aspect,
        //    and these will, in turn, lead to further events
        AlfrescoTransactionSupport.unbindResource(KEY_NODE_SET);
        // process each node
        for (NodeRef nodeRef : nodeRefs)
        {
            if (nodeService.exists(nodeRef))
            {
                processNode(nodeRef);
            }
        }
    }

    private void processNode(NodeRef nodeRef)
    {
        // ignore the node if the marker aspect is already present
        boolean isTagged = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE);
        
        // get the node properties
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
        // get the node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        // get property definitions for the node type
        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
        if (typeDef == null)
        {
            throw new AlfrescoRuntimeException("Node type is not recognised: " + nodeTypeQName);
        }
        Collection<PropertyDefinition> propertyDefs = typeDef.getProperties().values();
        // check them
        boolean classPropertiesOK = checkProperties(propertyDefs, nodeProperties);
        
        // were there outstanding properties to check?
        if (!classPropertiesOK)
        {
            addOrRemoveTag(nodeRef, true, isTagged);
            // no further checking required
            return;
        }
        
        // get the node aspects
        Set<QName> aspectTypeQNames = nodeService.getAspects(nodeRef);
        for (QName aspectTypeQName : aspectTypeQNames)
        {
            // get property definitions for the aspect
            AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
            propertyDefs = aspectDef.getProperties().values();
            // check them
            boolean aspectPropertiesOK = checkProperties(propertyDefs, nodeProperties);
            // were there outstanding properties to check?
            if (!aspectPropertiesOK)
            {
                addOrRemoveTag(nodeRef, true, isTagged);
                // no further checking required
                return;
            }
        }
        // all properties passed (both class- and aspect-defined) - remove aspect
        addOrRemoveTag(nodeRef, false, isTagged);
    }
    
    /**
     * @param propertyDefs the property definitions to check
     * @param properties the properties
     * @return Returns true if the property definitions were all satisified
     */
    private boolean checkProperties(
            Collection<PropertyDefinition> propertyDefs,
            Map<QName, Serializable> properties)
    {
        for (PropertyDefinition propertyDef : propertyDefs)
        {
            if (!propertyDef.isMandatory())
            {
                // The property isn't mandatory in any way
                continue;
            }
            else if (propertyDef.isMandatoryEnforced())
            {
                // The mandatory nature of the property is fully enforced
                // Leave these for integrity
                continue;
            }
            // The mandatory nature of the property is 'soft' a.k.a. 'required'
            // Check that the property value has been supplied
            if (properties.get(propertyDef.getName()) == null)
            {
                // property NOT supplied
                return false;
            }
        }
        // all properties were present
        return true;
    }
    
    /**
     * Adds or removes the {@link ContentModel#ASPECT_INCOMPLETE incomplete} marker aspect.
     * This only performs the operation if the tag aspect is or is not present, depending
     * on the operation required.
     * 
     * @param nodeRef the node to apply the change to
     * @param addTag <tt>true</tt> to add the tag and <tt>false</tt> to remove the tag
     * @param isTagged <tt>true</tt> if the node already has the tag aspect applied,
     *      otherwise <tt>false</tt>
     */
    private void addOrRemoveTag(NodeRef nodeRef, boolean addTag, boolean isTagged)
    {
        if (addTag && !isTagged)
        {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE, null);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Tagged node as INCOMPLETE: " + nodeRef);
            }
        }
        else if (!addTag && isTagged)
        {
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Untagged node as INCOMPLETE: " + nodeRef);
            }
        }
    }
}
