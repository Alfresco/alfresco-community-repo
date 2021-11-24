/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCheckOut;
import org.alfresco.repo.lock.LockServicePolicies.BeforeLock;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2;
import org.alfresco.repo.rendition2.RenditionService2Impl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.rendition.CompositeRenditionDefinition;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionPreventedException;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/*
 * @author Nick Smith
 * @author Neil McErlean
 * @since 3.3
 *
 * @deprecated The RenditionService is being replace by the simpler async RenditionService2.
 */
@Deprecated
public class RenditionServiceImpl implements 
        RenditionService, 
        RenditionDefinitionPersister, 
        BeforeCheckOut,
        BeforeLock
{
    private static final Log log = LogFactory.getLog(RenditionServiceImpl.class);

    private ActionService actionService;
    private ActionTrackingService actionTrackingService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private RenditionService2Impl renditionService2;

    private RenditionDefinitionPersister renditionDefinitionPersister;
    
    /**
     * @since 4.0.1
     */
    private RenditionPreventionRegistry renditionPreventionRegistry;
    
    /**
     * @since 4.1.6
     */
    private List<String> knownCancellableActionTypes;
    
    /**
     * Injects the RenditionDefinitionPersister bean.
     * @param renditionDefinitionPersister RenditionDefinitionPersister
     */
    public void setRenditionDefinitionPersister(RenditionDefinitionPersister renditionDefinitionPersister)
    {
        this.renditionDefinitionPersister = renditionDefinitionPersister;
    }
    
    /**
     * @since 4.0.1
     */
    public void setRenditionPreventionRegistry(RenditionPreventionRegistry registry)
    {
        this.renditionPreventionRegistry = registry;
    }
    
    /**
     * Injects the ServiceRegistry bean.
     * @param serviceRegistry ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.contentService = serviceRegistry.getContentService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    /**
     * Injects the ActionService bean.
     * @param actionService ActionService
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Injects the ActionTrackingService bean.
     * @param actionTrackingService ActionTrackingService
     */
    public void setActionTrackingService(ActionTrackingService actionTrackingService)
    {
        this.actionTrackingService = actionTrackingService;
    }

    /**
     * Injects the DictionaryService bean.
     * @param dictionaryService DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Injects the PolicyComponent bean.
     * @param policyComponent PolicyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Sets the list of known cancellable actions used by {@link #cancelRenditions(NodeRef)}.
     * @since 4.1.6
     */
    public void setKnownCancellableActionTypes(List<String> knownCancellableActionTypes)
    {
        this.knownCancellableActionTypes = knownCancellableActionTypes;
    }

    public void setRenditionService2(RenditionService2Impl renditionService2)
    {
        this.renditionService2 = renditionService2;
    }

    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                BeforeCheckOut.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeCheckOut"));
        this.policyComponent.bindClassBehaviour(
                BeforeLock.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeLock"));
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#getRenderingEngineDefinition(java.lang.String)
     */
    public RenderingEngineDefinition getRenderingEngineDefinition(String name)
    {
        ActionDefinition actionDefinition = actionService.getActionDefinition(name);
        if (actionDefinition instanceof RenderingEngineDefinition)
        {
            return (RenderingEngineDefinition) actionDefinition;
        }
        else
            return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#getRenderingEngineDefinitions()
     */
    public List<RenderingEngineDefinition> getRenderingEngineDefinitions()
    {
        List<RenderingEngineDefinition> results = new ArrayList<RenderingEngineDefinition>();
        List<ActionDefinition> actionDefs = actionService.getActionDefinitions();
        for (ActionDefinition actionDef : actionDefs)
        {
            if (actionDef instanceof RenderingEngineDefinition)
            {
                RenderingEngineDefinition renderingDef = (RenderingEngineDefinition) actionDef;
                results.add(renderingDef);
            }
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.rendition.RenditionService#createRenditionDefinition
     * (org.alfresco.service.namespace.QName, java.lang.String)
     */
    public RenditionDefinition createRenditionDefinition(QName renditionDefinitionName, String renderingEngineName)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Creating rendition definition ")
                    .append(renditionDefinitionName)
                    .append(" ")
                    .append(renderingEngineName);
            log.debug(msg.toString());
        }
        return new RenditionDefinitionImpl(GUID.generate(), renditionDefinitionName, renderingEngineName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#createCompositeRenditionDefinition(org.alfresco.service.namespace.QName)
     */
    public CompositeRenditionDefinition createCompositeRenditionDefinition(QName renditionName)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Creating composite rendition definition ")
                    .append(renditionName);
            log.debug(msg.toString());
        }
        return new CompositeRenditionDefinitionImpl(GUID.generate(), renditionName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#render(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.rendition.RenditionDefinition)
     */
    public ChildAssociationRef render(NodeRef sourceNode, RenditionDefinition definition)
    {
        checkSourceNodeForPreventionClass(sourceNode);
        log.debug("Original RenditionService render no callback START");

        ChildAssociationRef result = executeRenditionAction(sourceNode, definition, false);
        
        if (log.isDebugEnabled())
        {
            log.debug("Produced rendition " + result);
        }

        log.debug("Original RenditionService render no callback END");
        return result;
    }

    public void render(NodeRef sourceNode, RenditionDefinition definition,
            RenderCallback callback)
    {
        checkSourceNodeForPreventionClass(sourceNode);
        log.debug("Original RenditionService render    callback START");

        // The asynchronous render can't return a ChildAssociationRef as it is created
        // asynchronously after this method returns.
        definition.setCallback(callback);
        
        executeRenditionAction(sourceNode, definition, true);
        log.debug("Original RenditionService render   callback END");
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#render(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public ChildAssociationRef render(NodeRef sourceNode, final QName renditionDefinitionQName)
    {
        checkSourceNodeForPreventionClass(sourceNode);
        
        RenditionDefinition rendDefn = AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<RenditionDefinition>()
                {
                    public RenditionDefinition doWork() throws Exception
                    {
                        return loadRenditionDefinition(renditionDefinitionQName);
                    }
                }, AuthenticationUtil.getSystemUserName());
        
        if (rendDefn == null)
        {
            throw new RenditionServiceException("Rendition Definition " + renditionDefinitionQName + " was not found.");
        }
        
        return this.render(sourceNode, rendDefn);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#render(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.rendition.RenderCallback)
     */
    public void render(NodeRef sourceNode, final QName renditionDefinitionQName, RenderCallback callback)
    {
        checkSourceNodeForPreventionClass(sourceNode);
        
        RenditionDefinition rendDefn = AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<RenditionDefinition>()
                {
                    public RenditionDefinition doWork() throws Exception
                    {
                        return loadRenditionDefinition(renditionDefinitionQName);
                    }
                }, AuthenticationUtil.getSystemUserName());
            
        if (rendDefn == null)
        {
            throw new RenditionServiceException("Rendition Definition " + renditionDefinitionQName + " was not found.");
        }
        
        this.render(sourceNode, rendDefn, callback);
    }
    
    /**
     * This method checks whether the specified source node is of a content class which has been registered for rendition prevention.
     * 
     * @param sourceNode the node to check.
     * @throws RenditionPreventedException if the source node is configured for rendition prevention.
     * @since 4.0.1
     * @see RenditionPreventionRegistry
     */
    private void checkSourceNodeForPreventionClass(NodeRef sourceNode)
    {
        // A node's content class is its type and all its aspects.
        // We'll not check the source node for null and leave that to the rendering action.
        if (sourceNode != null && nodeService.exists(sourceNode))
        {
            Set<QName> nodeContentClasses = nodeService.getAspects(sourceNode);
            nodeContentClasses.add(nodeService.getType(sourceNode));
            
            for (QName contentClass : nodeContentClasses)
            {
                if (renditionPreventionRegistry.isContentClassRegistered(contentClass))
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Node ").append(sourceNode)
                            .append(" cannot be renditioned as it is of class ").append(contentClass);
                    if (log.isDebugEnabled())
                    {
                        log.debug(msg.toString());
                    }
                    throw new RenditionPreventedException(msg.toString());
                }
            }
        }
    }
    
    /**
     * This method delegates the execution of the specified RenditionDefinition
     * to the {@link ActionService action service}.
     * 
     * @param sourceNode the source node which is to be rendered.
     * @param definition the rendition definition to be used.
     * @param asynchronous <code>true</code> for asynchronous execution,
     *                     <code>false</code> for synchronous.
     * @return the ChildAssociationRef whose child is the rendition node.
     */
    private ChildAssociationRef executeRenditionAction(NodeRef sourceNode,
            RenditionDefinition definition, boolean asynchronous)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            if (asynchronous)
            {
                msg.append("Asynchronously");
            }
            else
            {
                msg.append("Synchronously");
            }
            msg.append(" rendering node ").append(sourceNode)
                    .append(" with ").append(definition.getRenditionName());
            log.debug(msg.toString());
        }
        final boolean checkConditions = true;
        actionService.executeAction(definition, sourceNode, checkConditions, asynchronous);
        
        ChildAssociationRef result = (ChildAssociationRef)definition.getParameterValue(ActionExecuter.PARAM_RESULT);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.rendition.RenditionService#saveRenditionDefinition
     * (org.alfresco.service.cmr.rendition.RenditionDefinition)
     */
    public void saveRenditionDefinition(RenditionDefinition renderingAction)
    {
        this.renditionDefinitionPersister.saveRenditionDefinition(renderingAction);
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionService#loadRenderingAction
     * (org.alfresco.service.namespace.QName)
     */
    public RenditionDefinition loadRenditionDefinition(QName renditionDefinitionName)
    {
        return this.renditionDefinitionPersister.loadRenditionDefinition(renditionDefinitionName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#loadRenditionDefinitions()
     */
    public List<RenditionDefinition> loadRenditionDefinitions()
    {
        return this.renditionDefinitionPersister.loadRenditionDefinitions();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.rendition.RenditionService#loadRenderingActions
     * (java.lang.String)
     */
    public List<RenditionDefinition> loadRenditionDefinitions(String renditionEngineName)
    {
        return this.renditionDefinitionPersister.loadRenditionDefinitions(renditionEngineName);
    }
    

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.rendition.RenditionService#getRenditions(org
     * .alfresco.service.cmr.repository.NodeRef)
     */
    public List<ChildAssociationRef> getRenditions(NodeRef node)
    {
        return renditionService2.getRenditions(node);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.cmr.rendition.RenditionService#getRenditions(org
     * .alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public List<ChildAssociationRef> getRenditions(NodeRef node, String mimeTypePrefix)
    {
        List<ChildAssociationRef> allRenditions = this.getRenditions(node);
        List<ChildAssociationRef> filteredResults = new ArrayList<ChildAssociationRef>();

        for (ChildAssociationRef chAssRef : allRenditions)
        {
            NodeRef renditionNode = chAssRef.getChildRef();

            QName contentProperty = ContentModel.PROP_CONTENT;
            Serializable contentPropertyName = nodeService.getProperty(renditionNode,
                    ContentModel.PROP_CONTENT_PROPERTY_NAME);
            if (contentPropertyName != null)
            {
                contentProperty = (QName) contentPropertyName;
            }

            ContentReader reader = contentService.getReader(renditionNode, contentProperty);
            if (reader != null && reader.exists())
            {
                String readerMimeType = reader.getMimetype();
                if (readerMimeType.startsWith(mimeTypePrefix))
                {
                    filteredResults.add(chAssRef);
                }

            }
        }

        return filteredResults;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#getRenditionByName(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public ChildAssociationRef getRenditionByName(NodeRef node, QName renditionName)
    {
        List<ChildAssociationRef> renditions = Collections.emptyList();

        // Check that the node has the renditioned aspect applied
        if (nodeService.hasAspect(node, RenditionModel.ASPECT_RENDITIONED) == true)
        {
            // Get all the renditions that match the given rendition name -
            // there should only be 1 (or 0)
            renditions = this.nodeService.getChildAssocs(node, RenditionModel.ASSOC_RENDITION, renditionName);
        }
        if (renditions.isEmpty())
        {
            return null;
        }
        else
        {
            if (renditions.size() > 1 && log.isDebugEnabled())
            {
                log.debug("Unexpectedly found " + renditions.size() + " renditions of name " + renditionName + " on node " + node);
            }
            ChildAssociationRef childAssoc = renditions.get(0);
            NodeRef renditionNode = childAssoc.getChildRef();
            return !renditionService2.isRenditionAvailable(node, renditionNode) ? null: childAssoc;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#isRendition(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRendition(NodeRef node)
    {
        final QName aspectToCheckFor = RenditionModel.ASPECT_RENDITION;
        
        Set<QName> existingAspects = nodeService.getAspects(node);
        for (QName nextAspect : existingAspects)
        {
            if (nextAspect.equals(aspectToCheckFor) || dictionaryService.isSubClass(nextAspect, aspectToCheckFor))
            {
                return true;
            }
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rendition.RenditionService#getSourceNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public ChildAssociationRef getSourceNode(NodeRef renditionNode)
    {
        // In normal circumstances only a node which is itself a rendition can have
        // a source node - as linked by the rn:rendition association.
        //
        // However there are some circumstances where a node which is not
        // technically a rendition can still have a source. One such example is the case
        // of thumbnail nodes created in a pre-3.3 Alfresco which have not been patched
        // to have the correct rendition aspect applied.
        // This will also occur *during* execution of the webscript patch and so the
        // decision was made not to throw an exception or log a warning if such a
        // situation is encountered.
        
        // A rendition node should have 1 and only 1 source node.
        List<ChildAssociationRef> parents = nodeService.getParentAssocs(renditionNode,
                RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
        if (parents.size() > 1)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("NodeRef ")
                    .append(renditionNode)
                    .append(" unexpectedly has ")
                    .append(parents.size())
                    .append(" rendition parents.");
            if (log.isWarnEnabled())
            {
                log.warn(msg.toString());
            }
            throw new RenditionServiceException(msg.toString());
        }
        else
        {
            return parents.isEmpty() ? null : parents.get(0);
        }
    }
    
    public void cancelRenditions(NodeRef sourceNode)
    {
        if (knownCancellableActionTypes == null)
        {
            return;
        }
        for (String type : knownCancellableActionTypes)
        {
            cancelRenditions(sourceNode, type);
        }
    }
    
    public void cancelRenditions(NodeRef sourceNode, String type)
    {
        if (log.isDebugEnabled())
        {
            log.debug("cancelling renditions of type " + type + " on nodeRef: " + sourceNode.toString());
        }
        List<ExecutionSummary> executionSummaries = actionTrackingService.getExecutingActions(type, sourceNode);
        for (ExecutionSummary executionSummary : executionSummaries)
        {
            actionTrackingService.requestActionCancellation(executionSummary);
        }
    }
    
    @Override
    public void beforeCheckOut(
            NodeRef nodeRef,
            NodeRef destinationParentNodeRef,           
            QName destinationAssocTypeQName, 
            QName destinationAssocQName)
    {
        cancelRenditions(nodeRef);
    }

    @Override
    public void beforeLock(NodeRef nodeRef, LockType lockType)
    {
        cancelRenditions(nodeRef);
    }

    @Override
    public boolean usingRenditionService2(NodeRef sourceNodeRef, RenditionDefinition rendDefn)
    {
        boolean useRenditionService2 = false;

        QName renditionQName = rendDefn.getRenditionName();
        String renditionName = renditionQName.getLocalName();
        RenditionDefinition2 renditionDefinition2 = getEquivalentRenditionDefinition2(rendDefn);
        boolean createdByRenditionService2 = renditionService2.isCreatedByRenditionService2(sourceNodeRef, renditionName);

        if (renditionService2.isEnabled())
        {
            if (createdByRenditionService2)
            {
                // The rendition has been created by RenditionService2 and the older RenditionService should leave it to
                // the newer service to do the work unless there is no matching rendition in which case we remove the
                // rendition and the old service takes over again.
                if (renditionDefinition2 != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("OnContentUpdate ignored by original service as the rendition for \""+sourceNodeRef+"\", \""+renditionName+"\" new service has taken over.");
                    }
                    useRenditionService2 = true;
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("OnContentUpdate remove rendition for \""+sourceNodeRef+"\", \""+renditionName+"\" so we switch back to the original service, as the new service does not have the definition.");
                    }
                    renditionService2.deleteRendition(sourceNodeRef, renditionName);
                }
            }
            else if (renditionDefinition2 != null)
            {
                // The rendition has been created by the older RenditionService but we know that RenditionService2
                // can do the work, so we ask the newer service to do it here. This will result in the rendition2
                // aspect being added, so future renditions will also be done by the newer service.
                if (log.isDebugEnabled())
                {
                    log.debug("OnContentUpdate calling RenditionService2.render(\""+sourceNodeRef+"\", \""+renditionName+"\" so we switch to the new service.");
                }
                useRenditionService2 = true;
                renditionService2.clearRenditionContentData(sourceNodeRef, renditionName);
                renditionService2.render(sourceNodeRef, renditionName);
            }
        }
        else if (createdByRenditionService2)
        {
            // As the new service has been disabled the old service needs to take over, so the rendition is removed.
            if (log.isDebugEnabled())
            {
                log.debug("OnContentUpdate remove rendition for \""+sourceNodeRef+"\", \""+renditionName+"\" so we switch back to the original service, as the new service is disabled.");
            }
            renditionService2.deleteRendition(sourceNodeRef, renditionName);
        }

        return useRenditionService2;
    }

    // Finds a RenditionDefinition2 with the same name (local part) and target mimetype.
    private RenditionDefinition2 getEquivalentRenditionDefinition2(RenditionDefinition rendDefn)
    {
        QName renditionQName = rendDefn.getRenditionName();
        String renditionName = renditionQName.getLocalName();
        RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
        RenditionDefinition2 renditionDefinition2 = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
        RenditionDefinition2 equivalentRenditionDefinition2 = null;
        if (renditionDefinition2 != null)
        {
            String targetMimetype = (String) rendDefn.getParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE);
            String targetMimetype2 = renditionDefinition2.getTargetMimetype();
            equivalentRenditionDefinition2 = targetMimetype.equals(targetMimetype2) ? renditionDefinition2 : null;
        }
        return equivalentRenditionDefinition2;
    }
}
