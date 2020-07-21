/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.rendition;

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
import org.alfresco.repo.rendition.executer.DeleteRenditionActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
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
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 *
 * @deprecated The RenditionService is being replace by the simpler async RenditionService2.
 */
@Deprecated
public class RenditionedAspect implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          CopyServicePolicies.OnCopyNodePolicy
{
    /** logger */
    private static final Log logger = LogFactory.getLog(RenditionedAspect.class);

    /** Services */
    private ActionService actionService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private PolicyComponent policyComponent;
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
     * @since 3.4.2
     */
    public void setActionService(ActionService actionService) 
    {
        this.actionService = actionService;
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
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.EVERY_EVENT));
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
            List<ChildAssociationRef> renditions = getRenditionChildAssociations(nodeRef);
            for (ChildAssociationRef chAssRef : renditions)
            {
                final QName renditionAssocName = chAssRef.getQName();
                
                // Rendition Definitions are persisted underneath the Data Dictionary for which Group ALL
                // has Consumer access by default. However, we cannot assume that that access level applies for all deployments. See ALF-7334.
                RenditionDefinition rendDefn = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<RenditionDefinition>()
                    {
                        @Override
                        public RenditionDefinition doWork() throws Exception
                        {
                            return renditionService.loadRenditionDefinition(renditionAssocName);
                        }
                    }, AuthenticationUtil.getSystemUserName());
                
                if (rendDefn == null)
                {
                    if (logger.isDebugEnabled())
                    {
                        // We will see this debug if a new RenditionService2 definition exists.
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

    // Identical to the original RenditionService.getRenditions(NodeRef) method.
    // It returns all renditions (including those that need to be replaced).
    // The code in RenditionService.getRenditions(NodeRef) no longer does this as
    // it hides any rendition service 2 renditions that are out of date or have failed.
    private List<ChildAssociationRef> getRenditionChildAssociations(NodeRef sourceNodeRef)
    {
        // Copy of code from the original RenditionService.
        List<ChildAssociationRef> result = Collections.emptyList();

        // Check that the node has the renditioned aspect applied
        if (nodeService.hasAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED))
        {
            // Get all the renditions that match the given rendition name
            result = nodeService.getChildAssocs(sourceNodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
        }
        return result;
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
        if (logger.isDebugEnabled() && rendDefn != null)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Queueing rendition update for node ").append(sourceNodeRef).append(": ").append(rendDefn.getRenditionName());
            logger.debug(msg.toString());
        }

        if (rendDefn != null && !renditionService.usingRenditionService2(sourceNodeRef, rendDefn))
        {
            Action deleteRendition = actionService.createAction(DeleteRenditionActionExecuter.NAME);
            deleteRendition.setParameterValue(DeleteRenditionActionExecuter.PARAM_RENDITION_DEFINITION_NAME, rendDefn.getRenditionName());
            rendDefn.setCompensatingAction(deleteRendition);
            
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
                }

                public void handleSuccessfulRendition(ChildAssociationRef primaryParentOfNewRendition)
                {
                    // Intentionally empty
                }
            });
        }
    }

    /**
     * @return              Returns CopyBehaviourCallback
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
            return false;
        }
        
        @Override
        public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyAssociationDetails assocCopyDetails)
        {
            return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                    AssocCopySourceAction.IGNORE,
                    AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET);
        }
        
        @Override
        public ChildAssocCopyAction getChildAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyChildAssociationDetails childAssocCopyDetails)
        {
            return ChildAssocCopyAction.IGNORE;            
        }
    }
}
