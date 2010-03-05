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

import static org.alfresco.model.ContentModel.PROP_NODE_DBID;
import static org.alfresco.model.ContentModel.PROP_NODE_REF;
import static org.alfresco.model.ContentModel.PROP_NODE_UUID;
import static org.alfresco.model.ContentModel.PROP_STORE_IDENTIFIER;
import static org.alfresco.model.ContentModel.PROP_STORE_NAME;
import static org.alfresco.model.ContentModel.PROP_STORE_PROTOCOL;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.NodeLocator;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/*
 * This class is the action executer for the perform-rendition action. All renditions are
 * executed by this class as wrapping them all in a containing action facilitates
 * asynchronous renditions.
 * <P/>
 * Some of the logic is executed directly by this class (handling of rendition-related
 * aspects and associations) and the rest is executed by subordinate actions called
 * from within this action (the actual rendering code). These subordinate actions are
 * renditionDefinitions.
 * 
 * @author Neil McErlean
 * @since 3.3
 */
public class PerformRenditionActionExecuter extends ActionExecuterAbstractBase
{
    private static final Log log = LogFactory.getLog(PerformRenditionActionExecuter.class);

    /** Action name and parameters */
    public static final String NAME = "perform-rendition";
    public static final String PARAM_RENDITION_DEFINITION = "renditionDefinition";

    private static final String DEFAULT_RUN_AS_NAME = AuthenticationUtil.getSystemUserName();

    private static final List<QName> unchangedProperties = Arrays.asList(PROP_NODE_DBID, PROP_NODE_REF, PROP_NODE_UUID,
                PROP_STORE_IDENTIFIER, PROP_STORE_NAME, PROP_STORE_PROTOCOL);
    /**
     * Default {@link NodeLocator} simply returns the source node.
     */
    private final static NodeLocator defaultNodeLocator = new NodeLocator()
    {
        public NodeRef getNode(NodeRef sourceNode, Map<String, Serializable> params)
        {
            return sourceNode;
        }
    };

    /*
     * Injected beans
     */
    private RenditionLocationResolver renditionLocationResolver;
    private ActionService actionService;
    private NodeService nodeService;
    private RenditionService renditionService;

    private final NodeLocator temporaryParentNodeLocator;
    private final QName temporaryRenditionLinkType;

    public PerformRenditionActionExecuter(NodeLocator temporaryParentNodeLocator, QName temporaryRenditionLinkType)
    {
        this.temporaryParentNodeLocator = temporaryParentNodeLocator != null ? temporaryParentNodeLocator
                    : defaultNodeLocator;
        this.temporaryRenditionLinkType = temporaryRenditionLinkType != null ? temporaryRenditionLinkType
                    : RenditionModel.ASSOC_RENDITION;
    }

    public PerformRenditionActionExecuter()
    {
        this(null, null);
    }

    /**
     * Injects the actionService bean.
     * 
     * @param actionService
     *            the actionService.
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Injects the nodeService bean.
     * 
     * @param nodeService
     *            the nodeService.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Injects the renditionService bean.
     * 
     * @param renditionService
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    public void setRenditionLocationResolver(RenditionLocationResolver renditionLocationResolver)
    {
        this.renditionLocationResolver = renditionLocationResolver;
    }

    @Override
    protected void executeImpl(final Action containingAction, final NodeRef actionedUponNodeRef)
    {
        final RenditionDefinition renditionDefinition = getRenditionDefinition(containingAction);
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendering node ").append(actionedUponNodeRef).append(" with rendition definition ").append(
                        renditionDefinition.getRenditionName());
            log.debug(msg.toString());
        }

        Serializable runAsParam = renditionDefinition.getParameterValue(AbstractRenderingEngine.PARAM_RUN_AS);
        String runAsName = runAsParam == null ? DEFAULT_RUN_AS_NAME : (String) runAsParam;

        // Renditions should all be created by system by default.
        // When renditions are created by a user and are to be created under a
        // node
        // other than the source node, it is possible that the user will not
        // have
        // permissions to create content under that node.
        // For that reason, we execute all perform-rendition actions as system
        // by default.
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                ChildAssociationRef result = null;
                try
                {
                    setTemporaryRenditionProperties(actionedUponNodeRef, renditionDefinition);

                    // Adds the 'Renditioned' aspect to the source node if it
                    // doesn't exist.
                    if (!nodeService.hasAspect(actionedUponNodeRef, RenditionModel.ASPECT_RENDITIONED))
                    {
                        nodeService.addAspect(actionedUponNodeRef, RenditionModel.ASPECT_RENDITIONED, null);
                    }
                    ChildAssociationRef tempRendAssoc = executeRendition(actionedUponNodeRef, renditionDefinition);
                    result = createOrUpdateRendition(actionedUponNodeRef, tempRendAssoc, renditionDefinition);
                    containingAction.setParameterValue(PARAM_RESULT, result);
                } catch (Throwable t)
                {
                    notifyCallbackOfException(renditionDefinition, t);
                    throwWrappedException(t);
                }
                if (result != null)
                {
                    notifyCallbackOfResult(renditionDefinition, result);
                }
                return null;
            }
        }, runAsName);
    }

    private RenditionDefinition getRenditionDefinition(final Action containingAction)
    {
        Serializable rendDefObj = containingAction.getParameterValue(PARAM_RENDITION_DEFINITION);
        ParameterCheck.mandatory(PARAM_RENDITION_DEFINITION, rendDefObj);
        return (RenditionDefinition) rendDefObj;
    }

    // Rendition has failed. If there is a callback, it needs to be notified
    private void notifyCallbackOfException(RenditionDefinition renditionDefinition, Throwable t)
    {
        if (renditionDefinition != null)
        {
            RenderCallback callback = renditionDefinition.getCallback();
            if (callback != null)
            {
                callback.handleFailedRendition(t);
            }
        }
    }

    // and rethrow Exception
    private void throwWrappedException(Throwable t)
    {
        if (t instanceof AlfrescoRuntimeException)
        {
            throw (AlfrescoRuntimeException) t;
        } else
        {
            throw new RenditionServiceException(t.getMessage(), t);
        }
    }

    private void notifyCallbackOfResult(RenditionDefinition renditionDefinition, ChildAssociationRef result)
    {
        // Rendition was successful. Notify the callback object.
        if (renditionDefinition != null)
        {
            RenderCallback callback = renditionDefinition.getCallback();
            if (callback != null)
            {
                callback.handleSuccessfulRendition(result);
            }
        }
    }

    /**
     * This method sets the renditionParent and rendition assocType.
     * 
     * @param sourceNode
     * @param definition
     */
    private void setTemporaryRenditionProperties(NodeRef sourceNode, RenditionDefinition definition)
    {
        // Set the parent and assoc type for the temporary rendition to be
        // created.
        NodeRef parent = temporaryParentNodeLocator.getNode(sourceNode, definition.getParameterValues());
        definition.setRenditionParent(parent);
        definition.setRenditionAssociationType(temporaryRenditionLinkType);
    }

    /**
     * @param sourceNode
     * @param definition
     * @return
     */
    private ChildAssociationRef executeRendition(NodeRef sourceNode, RenditionDefinition definition)
    {
        actionService.executeAction(definition, sourceNode);
        // Extract the result from the action
        Serializable serializableResult = definition.getParameterValue(ActionExecuter.PARAM_RESULT);
        return (ChildAssociationRef) serializableResult;
    }

    private ChildAssociationRef createOrUpdateRendition(NodeRef sourceNode, ChildAssociationRef tempRendition,
                RenditionDefinition renditionDefinition)
    {
        NodeRef tempRenditionNode = tempRendition.getChildRef();
        RenditionLocation location = getDestinationParentAssoc(sourceNode, renditionDefinition, tempRenditionNode);
        QName renditionQName = renditionDefinition.getRenditionName();
        if (log.isDebugEnabled())
        {
            final String lineBreak = System.getProperty("line.separator", "\n");
            StringBuilder msg = new StringBuilder();
            msg.append("Creating/updating rendition based on:").append(lineBreak).append("    sourceNode: ").append(
                        sourceNode).append(lineBreak).append("    tempRendition: ").append(tempRendition).append(
                        lineBreak).append("    parentNode: ").append(location.getParentRef()).append(lineBreak).append(
                        "    childName: ").append(location.getChildName()).append(lineBreak).append(
                        "    renditionDefinition.name: ").append(renditionQName);
            log.debug(msg.toString());
        }
        ChildAssociationRef primaryAssoc = findOrCreatePrimaryRenditionAssociation(sourceNode, renditionDefinition,
                    location);

        // Copy relevant properties from the temporary node to the new rendition
        // node.
        NodeRef renditionNode = primaryAssoc.getChildRef();
        transferNodeProperties(tempRenditionNode, renditionNode);

        // Set the name property on the rendition if it has not already been
        // set.
        String renditionName = getRenditionName(tempRenditionNode, location, renditionDefinition);
        nodeService.setProperty(renditionNode, ContentModel.PROP_NAME, renditionName);

        // Delete the temporary rendition.
        nodeService.removeChildAssociation(tempRendition);

        // Handle the rendition aspects
        manageRenditionAspects(sourceNode, primaryAssoc);
        ChildAssociationRef renditionAssoc = renditionService.getRenditionByName(sourceNode, renditionQName);
        if (renditionAssoc == null)
        {
            String msg = "A rendition of type: " + renditionQName + " should have been created for source node: "
                        + sourceNode;
            throw new RenditionServiceException(msg);
        }
        return renditionAssoc;
    }

    private void manageRenditionAspects(NodeRef sourceNode, ChildAssociationRef renditionParentAssoc)
    {
        NodeRef renditionNode = renditionParentAssoc.getChildRef();
        NodeRef primaryParent = renditionParentAssoc.getParentRef();

        // If the rendition is located directly underneath its own source node
        if (primaryParent.equals(sourceNode))
        {
            // It should be a 'hidden' rendition.
            nodeService.addAspect(renditionNode, RenditionModel.ASPECT_HIDDEN_RENDITION, null);
            nodeService.removeAspect(renditionNode, RenditionModel.ASPECT_VISIBLE_RENDITION);
            // We remove the other aspect to cover the potential case where a
            // rendition
            // has been updated in a different location.
        } else
        {
            // Renditions stored underneath any node other than their source are
            // 'visible'.
            nodeService.addAspect(renditionNode, RenditionModel.ASPECT_VISIBLE_RENDITION, null);
            nodeService.removeAspect(renditionNode, RenditionModel.ASPECT_HIDDEN_RENDITION);
        }
    }

    private String getRenditionName(NodeRef tempRenditionNode, RenditionLocation location,
                RenditionDefinition renditionDefinition)
    {
        // If a location name is set then use it.
        String locName = location.getChildName();
        if (locName != null && locName.length() > 0)
        {
            return locName;
        }
        // Else if the temporary rendition specifies a name property use that.
        Serializable tempName = nodeService.getProperty(tempRenditionNode, ContentModel.PROP_NAME);
        if (tempName != null)
        {
            return (String) tempName;
        }
        // Otherwise use the rendition definition local name.
        return renditionDefinition.getRenditionName().getLocalName();
    }

    private ChildAssociationRef findOrCreatePrimaryRenditionAssociation(NodeRef sourceNode,
                RenditionDefinition renditionDefinition, RenditionLocation location)
    {
        // Get old Rendition if exists.
        QName renditionName = renditionDefinition.getRenditionName();
        ChildAssociationRef oldRenditionAssoc = renditionService.getRenditionByName(sourceNode, renditionName);
        // If no rendition already exists create anew rendition node and
        // association.
        if (oldRenditionAssoc == null)
        {
            return getSpecifiedRenditionOrCreateNewRendition(sourceNode, location, renditionName);
        }
        // If a rendition exists and is already in the correct location then
        // return that renditions primary parent association
        NodeRef oldRendition = oldRenditionAssoc.getChildRef();
        if (renditionLocationMatches(oldRendition, location))
        {
            return nodeService.getPrimaryParent(oldRendition);
        }
        // If the old rendition is in the wrong location and the 'orphan
        // existing rendition' param is set to true or the RenditionLocation
        // specifies a destination NodeRef then ldelete the old
        // rendition association and create a new rendition node.
        if (orphanExistingRendition(renditionDefinition, location))
        {
            orphanRendition(oldRenditionAssoc);
            return getSpecifiedRenditionOrCreateNewRendition(sourceNode, location, renditionName);
        }
        // If the old rendition is in the wrong place and the 'orphan existing
        // rendition' param is not set to true then move the existing rendition
        // to the correct location.
        return moveRendition(oldRendition, location, renditionName);
    }

    private ChildAssociationRef moveRendition(NodeRef renditionNode, RenditionLocation location, QName associationName)
    {
        ChildAssociationRef assoc = nodeService.moveNode(renditionNode, location.getParentRef(),
                    ContentModel.ASSOC_CONTAINS, associationName);
        return assoc;
    }

    private void orphanRendition(ChildAssociationRef oldRenditionAssoc)
    {
        NodeRef oldRendition = oldRenditionAssoc.getChildRef();
        nodeService.removeAspect(oldRendition, RenditionModel.ASPECT_HIDDEN_RENDITION);
        nodeService.removeAspect(oldRendition, RenditionModel.ASPECT_VISIBLE_RENDITION);
        nodeService.removeChildAssociation(oldRenditionAssoc);
    }

    private boolean orphanExistingRendition(RenditionDefinition renditionDefinition, RenditionLocation location)
    {
        if (location.getChildRef() != null)
            return true;
        else
            return AbstractRenderingEngine.getParamWithDefault(RenditionService.PARAM_ORPHAN_EXISTING_RENDITION,
                        Boolean.FALSE, renditionDefinition);
    }

    private boolean renditionLocationMatches(NodeRef oldRendition, RenditionLocation location)
    {
        NodeRef destination = location.getChildRef();
        if (destination != null)
        {
            return destination.equals(oldRendition);
        }
        ChildAssociationRef oldParentAssoc = nodeService.getPrimaryParent(oldRendition);
        NodeRef oldParent = oldParentAssoc.getParentRef();
        if (oldParent.equals(location.getParentRef()))
        {
            String childName = location.getChildName();
            if (childName == null)
                return true;
            else
            {
                Serializable oldName = nodeService.getProperty(oldRendition, ContentModel.PROP_NAME);
                return childName.equals(oldName);
            }
        }
        return false;
    }

    private ChildAssociationRef getSpecifiedRenditionOrCreateNewRendition(NodeRef sourceNode,
                RenditionLocation location, QName renditionName)
    {
        NodeRef destination = location.getChildRef();
        if (destination != null)
            return nodeService.getPrimaryParent(destination);
        else
            return createNewRendition(sourceNode, location, renditionName);
    }

    private ChildAssociationRef createNewRendition(NodeRef sourceNode, RenditionLocation location, QName renditionName)
    {
        NodeRef parentRef = location.getParentRef();
        boolean parentIsSource = parentRef.equals(sourceNode);
        QName renditionType = RenditionModel.ASSOC_RENDITION;
        QName assocTypeQName = parentIsSource ? renditionType : ContentModel.ASSOC_CONTAINS;
        QName nodeTypeQName = ContentModel.TYPE_CONTENT;
        ChildAssociationRef primaryAssoc = nodeService.createNode(parentRef, assocTypeQName, renditionName,
                    nodeTypeQName);

        // If the new rendition is not directly under the source node then add
        // the rendition association.
        if (parentIsSource == false)
        {
            NodeRef rendition = primaryAssoc.getChildRef();
            nodeService.addChild(sourceNode, rendition, renditionType, renditionName);
        }
        return primaryAssoc;
    }

    /**
     * @param sourceNode
     * @param targetNode
     */
    private void transferNodeProperties(NodeRef sourceNode, NodeRef targetNode)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Transferring some properties from ").append(sourceNode).append(" to ").append(targetNode);
            log.debug(msg.toString());
        }

        Map<QName, Serializable> newProps = nodeService.getProperties(sourceNode);
        for (QName propKey : unchangedProperties)
        {
            newProps.remove(propKey);
        }
        nodeService.setProperties(targetNode, newProps);
    }

    private RenditionLocation getDestinationParentAssoc(NodeRef sourceNode, RenditionDefinition definition,
                NodeRef tempRendition)
    {
        return renditionLocationResolver.getRenditionLocation(sourceNode, definition, tempRendition);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_RENDITION_DEFINITION, DataTypeDefinition.ANY, true,
                    getParamDisplayLabel(PARAM_RENDITION_DEFINITION)));

        paramList.add(new ParameterDefinitionImpl(PARAM_RESULT, DataTypeDefinition.CHILD_ASSOC_REF, false,
                    getParamDisplayLabel(PARAM_RESULT)));
    }
}
