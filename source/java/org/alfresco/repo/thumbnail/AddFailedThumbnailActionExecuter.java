/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluator;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This {@link ActionExecuter} implementation is used to record a failed thumbnail attempt
 * having occurred on a source node. For the specified {@link ThumbnailDefinition#getName() thumbnail definition name}
 * and the specified failure datetime, this action executer applies the {@link ContentModel#ASPECT_FAILED_THUMBNAIL_SOURCE
 * cm:failedThumbnailSource} aspect and creates a {@link ContentModel#TYPE_FAILED_THUMBNAIL cm:failedThumbnail} child to store
 * the failure data.
 * <p/>
 * Some pieces of content cannot be thumbnailed. This can happen for various reasons, e.g.
 * <ul>
 *   <li>there is something in the content itself which is challenging, complex or not compliant with the relevant spec.</li>
 *   <li>there is something missing from the relevant library/ies which are trying to produce the thumbnail.</li>
 * </ul>
 * Some content can take a not insignificant amount of time in producing the thumbnail - only to fail.
 * This cost is borne each time a create-thumbnail action is run on that content, which happens each
 * time a user looks at the doclib page for that content in Share. For problematic documents that take a long time
 * to fail, this can add up to a significant cpu cost on the repository server.
 * Therefore we limit the frequency with which the repository retries to create thumbnails.
 * <p/>
 * The details of how these thumbnail creations are limited is described in {@link NodeEligibleForRethumbnailingEvaluator}.

 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 * 
 * @see FailedThumbnailInfo
 * @see NodeEligibleForRethumbnailingEvaluator
 * @see ThumbnailServiceImpl#init()
 */
public class AddFailedThumbnailActionExecuter extends ActionExecuterAbstractBase
{
    private static Log log = LogFactory.getLog(AddFailedThumbnailActionExecuter.class);
    
    /**
     * The action bean name.
     */
    public static final String NAME = "add-failed-thumbnail";
    
    /**
     * The name of the failed thumbnail definition e.g. doclib.
     */
    public static final String PARAM_THUMBNAIL_DEFINITION_NAME = "thumbnail-definition-name";
    
    /**
     * The parameter defines the failure datetime to be recorded against the source node.
     * We explicitly require a parameterised value for this (rather than simply using 'now')
     * because this action is executed asynchronously and there is the possibility that the time
     * of action execution is later than the actual failure time.
     */
    public static final String PARAM_FAILURE_DATETIME = "failure-datetime";
    
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * Thumbnail Service
     */
    ThumbnailService thumbnailService;
    
    /**
     * The behaviour filter.
     */
    private BehaviourFilter behaviourFilter;

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
     * Set the thumbnail service
     * 
     * @param thumbnailService  the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService) 
    {
        this.thumbnailService = thumbnailService;
    }
    
    /**
     * Set the behaviour filter.
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

	/**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        final boolean nodeExists = this.nodeService.exists(actionedUponNodeRef);
        if (nodeExists)
        {
            Map<String, Serializable> paramValues = ruleAction.getParameterValues();
            final String thumbDefName = (String)paramValues.get(PARAM_THUMBNAIL_DEFINITION_NAME);
            final Date failureDateTime = (Date)paramValues.get(PARAM_FAILURE_DATETIME);
            
            final QName thumbDefQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbDefName);
            
            NodeRef existingThumbnail = thumbnailService.getThumbnailByName(actionedUponNodeRef,
                    ContentModel.PROP_CONTENT_PROPERTY_NAME, thumbDefName);
            
            if (log.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Adding ").append(ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE)
                .append(" to ").append(actionedUponNodeRef);
                log.debug(msg.toString());

                msg = new StringBuilder();
                msg.append("  failed thumbnail definition is ").append(thumbDefName);
                log.debug(msg.toString());

                msg = new StringBuilder();
                msg.append("  failed datetime is ").append(failureDateTime);
                log.debug(msg.toString());

                msg = new StringBuilder();
                msg.append("  existing thumbnail is ").append(existingThumbnail);
                log.debug(msg.toString());
            }
            
            if (nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE) == false)
            {
                behaviourFilter.disableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    this.nodeService.addAspect(actionedUponNodeRef, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE, null);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
                }
            }
            
            List<ChildAssociationRef> failedChildren = nodeService.getChildAssocs(actionedUponNodeRef, ContentModel.ASSOC_FAILED_THUMBNAIL, thumbDefQName);
            NodeRef childNode = failedChildren.isEmpty() ? null : failedChildren.get(0).getChildRef();
            
            // Does the actionedUponNodeRef already have a child for this thumbnail definition?
            if (childNode == null)
            {
                // No existing failedThumbnail child, so this is a first time failure to render this source node with the current
                // thumbnail definition.
                // We'll create a new failedThumbnail child under the source node.
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_FAILED_THUMBNAIL_TIME, failureDateTime);
                props.put(ContentModel.PROP_FAILURE_COUNT, 1);

                behaviourFilter.disableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    // The association is named after the failed thumbnail definition.
                    nodeService.createNode(actionedUponNodeRef, ContentModel.ASSOC_FAILED_THUMBNAIL,
                            thumbDefQName, ContentModel.TYPE_FAILED_THUMBNAIL, props);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
                }
            }
            else
            {
                // There is already an existing failedThumbnail child, so this is a repeat failure to perform the same
                // thumbnail definition.
                // Therefore we don't need to create a new failedThumbnail child.
                // But we do need to update the failedThumbnailTime property.
                nodeService.setProperty(childNode, ContentModel.PROP_FAILED_THUMBNAIL_TIME, failureDateTime);
                
                // and increment the failure count.
                int currentFailureCount = (Integer) nodeService.getProperty(childNode, ContentModel.PROP_FAILURE_COUNT);
                nodeService.setProperty(childNode, ContentModel.PROP_FAILURE_COUNT, currentFailureCount + 1);
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_THUMBNAIL_DEFINITION_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_THUMBNAIL_DEFINITION_NAME), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_FAILURE_DATETIME, DataTypeDefinition.DATETIME, true, getParamDisplayLabel(PARAM_FAILURE_DATETIME), false));
    }
}
