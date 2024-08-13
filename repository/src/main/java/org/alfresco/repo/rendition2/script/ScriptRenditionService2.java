/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.repo.rendition2.script;

import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.rendition2.RenditionService2;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The {@code ScriptRenditionService2} class provides a scripting interface for working with Rendition Service 2.
 * It allows rendering source nodes with specified rendition definitions and retrieving renditions associated with a node.
 */
public class ScriptRenditionService2 extends BaseScopableProcessorExtension
{
    private static final Log logger = LogFactory.getLog(ScriptRenditionService2.class);

    private ServiceRegistry serviceRegistry;
    private RenditionService2 renditionService;

    /**
     * Sets the {@link ServiceRegistry} to be used by this script object.
     *
     * @param serviceRegistry The ServiceRegistry to be set.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.renditionService = serviceRegistry.getRenditionService2();
    }

    /**
     * Renders the specified source node with the given rendition definition.
     * Since renditionService2 is designed to asynchronous, no result is returned.
     *
     * @param sourceNode The source node to be rendered.
     * @param renditionName The name of the rendition definition, like "pdf".
     */
    public void render(ScriptNode sourceNode, String renditionName)
    {
        if (logger.isDebugEnabled())
        {
            String msg = "Rendering source node '" +
                    sourceNode +
                    "' with renditionDef '" + renditionName +
                    "'";
            logger.debug(msg);
        }
        this.renditionService.render(sourceNode.getNodeRef(), renditionName);
    }

    /**
     * Retrieves an array of {@link ScriptNode} objects representing renditions associated with the specified node.
     *
     * @param node The node for which to retrieve renditions.
     * @return An array of {@code ScriptNode} objects representing renditions.
     */
    public ScriptNode[] getRenditions(ScriptNode node)
    {
        List<ChildAssociationRef> renditions = this.renditionService.getRenditions(node.getNodeRef());

        ScriptNode[] renditionObjs = new ScriptNode[renditions.size()];
        for (int i = 0; i < renditions.size(); i++)
        {
            renditionObjs[i] = new ScriptNode(renditions.get(i).getChildRef(), serviceRegistry);
        }

        return renditionObjs;
    }

    /**
     * Retrieves a {@link ScriptNode} object representing the rendition with the specified name associated with the given node.
     *
     * @param node The node for which to retrieve the rendition.
     * @param renditionName The name of the rendition.
     * @return A {@code ScriptNode} object representing the specified rendition, or {@code null} if not found.
     */
    public ScriptNode getRenditionByName(ScriptNode node, String renditionName)
    {
        ChildAssociationRef result = this.renditionService.getRenditionByName(node.getNodeRef(), renditionName);
        return result == null ? null : new ScriptNode(result.getChildRef(), serviceRegistry);
    }
}
