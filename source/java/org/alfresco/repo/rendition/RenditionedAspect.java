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
package org.alfresco.repo.rendition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.RenditionModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renditioned aspect behaviour bean.
 * When any node with the renditioned aspect has a property updated, then all
 * associated renditions are eligible for re-rendering.
 * Each rendition (as identified by the name in its rn:rendition association) will
 * be loaded and if the renditionDefinition exists, the rendition will be updated
 * asynchronously, subject to the defined update policy.
 * 
 * @author Neil McErlean
 * @author Roy Wetherall
 */
public class RenditionedAspect implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          CopyServicePolicies.OnCopyNodePolicy
{
    /** logger */
    private static final Log logger = LogFactory.getLog(RenditionedAspect.class);

    /** Services */
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private RenditionService renditionService;
    
    /**
     * Set the policy component
     * 
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the rendition service
     * 
     * @param renditionService   rendition service
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }
    
    /**
     * Set the dictionary service
     * 
     * @param dictionaryService     dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                RenditionModel.ASPECT_RENDITIONED, 
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), 
                RenditionModel.ASPECT_RENDITIONED, 
                new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if (this.nodeService.exists(nodeRef))
        {
            // Find the changed properties
            List<QName> changedProperties = getChangedProperties(before, after);
            
            // There may be a different policy for different rendition kinds.
            List<ChildAssociationRef> renditions = this.renditionService.getRenditions(nodeRef);
            for (ChildAssociationRef chAssRef : renditions)
            {
                QName renditionAssocName = chAssRef.getQName();
                RenditionDefinition rendDefn = this.renditionService.loadRenditionDefinition(renditionAssocName);
                if (rendDefn == null)
                {
                    if (logger.isDebugEnabled())
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Cannot update rendition ")
                            .append(renditionAssocName)
                            .append(" on node ").append(nodeRef)
                            .append(" as the renditionDefinition could not be loaded.");
                        logger.debug(msg.toString());
                    }
                    continue;
                }
                Serializable updateRenditionsPolicy = rendDefn.getParameterValue(AbstractRenderingEngine.PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE);
                boolean updateRenditionsAlways = updateRenditionsPolicy == null ? false : (Boolean)updateRenditionsPolicy;
            
                boolean renditionUpdateRequired = false;
                
                for (QName qname : changedProperties)
                {
                    try
                    {
                        PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
                        if (propertyDef == null)
                        {
                            // the property is not recognised
                            continue;
                        }
                        else if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                        {
                            // not a content type
                            if (updateRenditionsAlways)
                            {
                                renditionUpdateRequired = true;
                            }
                            continue;
                        }
                        else
                        {
                            // it is a content property. We always update renditions for changes to content.
                            renditionUpdateRequired = true;
                        }
                    } catch (ClassCastException ccx)
                    {
                        // the property does not confirm to the model
                        continue;
                    }
                }
                
                if (renditionUpdateRequired)
                {
                    this.queueUpdate(nodeRef, rendDefn, chAssRef);
                }
            }
        }
    }
    
    private List<QName> getChangedProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        List<QName> results = new ArrayList<QName>();
        for (QName propQName : before.keySet())
        {
            if (after.keySet().contains(propQName) == false)
            {
                // property was deleted
                results.add(propQName);
            }
            else
            {
                Serializable beforeValue = before.get(propQName);
                Serializable afterValue = after.get(propQName);
                if (EqualsHelper.nullSafeEquals(beforeValue, afterValue) == false)
                {
                    // Property was changed
                    results.add(propQName);
                }
            }
        }
        for (QName propQName : after.keySet())
        {
            if (before.containsKey(propQName) == false)
            {
                // property was added
                results.add(propQName);
            }
        }
        
        return results;
    }

    /**
     * Queue the update to happen asynchronously
     * 
     * @param sourceNodeRef           node reference
     */
    private void queueUpdate(final NodeRef sourceNodeRef, final RenditionDefinition rendDefn,
            final ChildAssociationRef renditionAssoc)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Queueing rendition update for node ").append(sourceNodeRef).append(": ").append(rendDefn.getRenditionName());
            logger.debug(msg.toString());
        }

        if (rendDefn != null)
        {
            renditionService.render(sourceNodeRef, rendDefn, new RenderCallback()
            {
                public void handleFailedRendition(Throwable t)
                {
                    // In the event of a failed re-rendition, we will delete the rendition node
                    if (logger.isDebugEnabled())
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Re-rendering of node ")
                            .append(sourceNodeRef)
                            .append(" with renditionDefinition ")
                            .append(rendDefn.getRenditionName())
                            .append(" failed. Deleting defunct rendition. ")
                            .append("The following exception is shown for informational purposes only ")
                            .append("and does not affect operation of the system.");
                        logger.debug(msg.toString(), t);
                    }

                    if (nodeService.exists(renditionAssoc.getChildRef()))
                    {
                        nodeService.deleteNode(renditionAssoc.getChildRef());
                    }
                }

                public void handleSuccessfulRendition(ChildAssociationRef primaryParentOfNewRendition)
                {
                    // Intentionally empty
                }
            });
        }
    }

    /**
     * @return              Returns {@link RenditionedAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return RenditionedAspectCopyBehaviourCallback.INSTANCE;
    }

    /**
     * Behaviour for the {@link RenditionModel#ASPECT_RENDITIONED <b>rn:renditioned</b>} aspect.
     * 
     * @author Derek Hulley
     * @author Neil McErlean
     * @since 3.2
     */
    private static class RenditionedAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new RenditionedAspectCopyBehaviourCallback();
        
        /**
         * We do not copy the {@link RenditionModel#ASPECT_RENDITIONED rn:renditioned} aspect.
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            // Prevent the copying of the renditioned aspect only.
            return (! RenditionModel.ASPECT_RENDITIONED.equals(classQName));
        }
    }
}
