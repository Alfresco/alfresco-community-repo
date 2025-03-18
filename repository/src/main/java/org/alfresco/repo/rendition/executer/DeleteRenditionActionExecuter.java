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
package org.alfresco.repo.rendition.executer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.RenditionedAspect;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.thumbnail.AddFailedThumbnailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * This {@link ActionExecuter} implementation is used internally to delete rendition nodes when a rendition update has failed. The scenario is as follows: a content node exists in the repository and has a number of rendition nodes associated with it. When the content node is given new content, each of the rendition nodes must be updated to reflect the new source content. But if one or more of those re-renditions fail, then the old rendition nodes now refer to out of date content and should be deleted.
 * <p/>
 * This class executes the deletion of the specified rendition node.
 * 
 * @author Neil Mc Erlean
 * @since 3.4.2
 * 
 * @see RenditionedAspect
 * @see AddFailedThumbnailActionExecuter
 *
 * @deprecated The RenditionService is being replace by the simpler async RenditionService2.
 */
@Deprecated
public class DeleteRenditionActionExecuter extends ActionExecuterAbstractBase
{
    private static Log log = LogFactory.getLog(DeleteRenditionActionExecuter.class);

    /**
     * The action bean name.
     */
    public static final String NAME = "delete-rendition";

    /**
     * The name of the rendition definition to delete e.g. cm:doclib.
     */
    public static final String PARAM_RENDITION_DEFINITION_NAME = "rendition-definition-name";

    private NodeService nodeService;
    private RenditionService renditionService;
    private BehaviourFilter behaviourFilter;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, NodeRef)
     */
    public void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        // It is possible that the user who triggered the thumbnail update does not have delete permissions on the node
        // but does have write permissions. e.g. if a SiteCollaborator updates a node which they do not own, any failure
        // to update renditions would trigger this DeleteRendition action and they would not have permissions to delete.
        //
        // For that reason this action is run as the system user.
        final NodeService finalNodeService = nodeService;

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                final boolean nodeExists = finalNodeService.exists(actionedUponNodeRef);
                if (nodeExists)
                {
                    Map<String, Serializable> paramValues = action.getParameterValues();
                    final QName renditionDefName = (QName) paramValues.get(PARAM_RENDITION_DEFINITION_NAME);

                    ChildAssociationRef existingRendition = renditionService.getRenditionByName(actionedUponNodeRef, renditionDefName);

                    if (existingRendition != null)
                    {
                        if (log.isDebugEnabled())
                        {
                            StringBuilder msg = new StringBuilder();
                            msg.append("Deleting rendition node: ").append(existingRendition);
                            log.debug(msg.toString());
                        }

                        behaviourFilter.disableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
                        try
                        {
                            nodeService.deleteNode(existingRendition.getChildRef());
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
                        }
                    }
                }
                return null;
            }
        });
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_RENDITION_DEFINITION_NAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_RENDITION_DEFINITION_NAME), false));
    }
}
