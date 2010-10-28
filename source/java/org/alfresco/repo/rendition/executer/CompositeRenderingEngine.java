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

package org.alfresco.repo.rendition.executer;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.rendition.CompositeRenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This RenderingEngine is used for rendering
 * {@link CompositeRenditionDefinition}s, which specify a list of
 * {@link RenditionDefinition}s. The {@link CompositeRenderingEngine} iterates
 * over the {@link RenditionDefinition}s sequentially and feeds the output of
 * one definition in as the input of the next definition. The output of the last
 * definition executed is the output of this rendering engine.
 * 
 * @author Nick Smith
 */
public class CompositeRenderingEngine extends AbstractRenderingEngine
{
    /** Logger */
    private static Log logger = LogFactory.getLog(CompositeRenderingEngine.class);

    public static final String NAME = "compositeRenderingEngine";

    private ActionService actionService;

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#executeRenditionImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRenditionImpl(Action action, NodeRef sourceNode)
    {
        checkSourceNodeExists(sourceNode);
        if (action instanceof CompositeRenditionDefinition)
        {
            CompositeRenditionDefinition compositeDefinition = (CompositeRenditionDefinition) action;
            ChildAssociationRef renditionAssoc = executeCompositeRendition(compositeDefinition, sourceNode);

            // Setting result.
            compositeDefinition.setParameterValue(PARAM_RESULT, renditionAssoc);
        }
        else
        {
            String msg = "This method requires that the RenditionDefinition be of type CompositeRenditionDefinition";
            logger.warn(msg);
            throw new RenditionServiceException(msg);
        }
    }

    private ChildAssociationRef executeCompositeRendition(CompositeRenditionDefinition definition, NodeRef sourceNode)
    {
        NodeRef source = sourceNode;
        ChildAssociationRef result = null;
        QName assocType = definition.getRenditionAssociationType();
        NodeRef parent = definition.getRenditionParent();
        for (RenditionDefinition subDefinition : definition.getActions())
        {
            ChildAssociationRef nextResult = executeSubDefinition(source, subDefinition, parent, assocType);
            if (result != null)
            {
                // Clean up temporary renditions.
                nodeService.removeChild(parent, result.getChildRef());
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("removeChild parentRef:" + parent + ", childRef;" + result.getChildRef());
                }
            }
            result = nextResult;
            source = nextResult.getChildRef();
        }
        return result;
    }

    /**
     * Executes the specified subdefinition. Note that each of these component rendition definitions
     * will be executed with the {@link RenditionService#PARAM_IS_COMPONENT_RENDITION is-component-rendition}
     * flag set to true. This is so that the common pre- and post-rendition code in
     * {@link AbstractRenderingEngine#executeImpl(Action, NodeRef)} will only be executed at the start
     * and at the end of the chain of components.
     */
    private ChildAssociationRef executeSubDefinition(NodeRef source,//
                RenditionDefinition subDefinition,//
                NodeRef parent,//
                QName assocType)
    {
        subDefinition.setRenditionParent(parent);
        subDefinition.setRenditionAssociationType(assocType);

        subDefinition.setParameterValue(RenditionService.PARAM_IS_COMPONENT_RENDITION, true);
        actionService.executeAction(subDefinition, source);
        ChildAssociationRef newResult = (ChildAssociationRef) subDefinition.getParameterValue(PARAM_RESULT);
        return newResult;
    }

    /*
     * @see
     * org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org
     * .alfresco
     * .repo.rendition.executer.AbstractRenderingEngine.RenderingContext)
     */
    @Override
    protected void render(RenderingContext data)
    {
        throw new RenditionServiceException("This method should never be caleld!");
    }

    /**
     * @param actionService the actionService to set
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
}
