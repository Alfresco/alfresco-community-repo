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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.rendition.CompositeRenditionDefinition;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * @author Nick Smith
 * @author Neil McErlean
 * @since 3.3
 */
public class RenditionServiceImpl implements RenditionService, RenditionDefinitionPersister
{
    private static final Log log = LogFactory.getLog(RenditionServiceImpl.class);

    private ActionService actionService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    
    private RenditionDefinitionPersisterImpl renditionDefinitionPersister;
    
    /**
     * Injects the RenditionDefinitionPersister bean.
     * @param renditionDefinitionPersister
     */
    public void setRenditionDefinitionPersister(RenditionDefinitionPersisterImpl renditionDefinitionPersister)
    {
        this.renditionDefinitionPersister = renditionDefinitionPersister;
    }
    
    /**
     * Injects the ServiceRegistry bean.
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.contentService = serviceRegistry.getContentService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    /**
     * Injects the ActionService bean.
     * @param actionService
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Injects the DictionaryService bean.
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
        ChildAssociationRef result = executeRenditionAction(sourceNode, definition, false);
        
        if (log.isDebugEnabled())
        {
            log.debug("Produced rendition " + result);
        }
        
        return result;
    }

    public void render(NodeRef sourceNode, RenditionDefinition definition,
            RenderCallback callback)
    {
        // The asynchronous render can't return a ChildAssociationRef as it is created
        // asynchronously after this method returns.
        definition.setCallback(callback);
        
        executeRenditionAction(sourceNode, definition, true);
        
        return;
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
        final boolean checkConditions = false;
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
        List<ChildAssociationRef> result = Collections.emptyList();

        // Check that the node has the renditioned aspect applied
        if (nodeService.hasAspect(node, RenditionModel.ASPECT_RENDITIONED) == true)
        {
            // Get all the renditions that match the given rendition name
            result = nodeService.getChildAssocs(node, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
            
            result = removeArchivedRenditionsFrom(result);
        }
        return result;
    }
    
    private List<ChildAssociationRef> removeArchivedRenditionsFrom(List<ChildAssociationRef> renditionAssocs)
    {
    	// This is a workaround for a bug in the NodeService (no JIRA number yet) whereby a call to
    	// nodeService.getChildAssocs can return all children, including children in the archive store.
    	List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
    	
        for (ChildAssociationRef chAssRef : renditionAssocs)
        {
        	// If the rendition has *not* been deleted, then it should remain in the result list.
        	if (chAssRef.getChildRef().getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) == false)
        	{
        		result.add(chAssRef);
        	}
        }
    	
    	return result;
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
        filteredResults = removeArchivedRenditionsFrom(filteredResults);

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
            renditions = this.removeArchivedRenditionsFrom(renditions);
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
            return renditions.get(0);
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
}
