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
package org.alfresco.repo.thumbnail.script;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Scriptable;

/**
 * @author Roy Wetherall
 * @author Neil McErlean
 */
public class ScriptThumbnail extends ScriptNode
{
    private static final long serialVersionUID = 7854749986083635678L;

    /** Logger */
    private static Log logger = LogFactory.getLog(ScriptThumbnail.class);

    /**
     * Constructor
     * 
     * @param nodeRef
     * @param services
     * @param scope
     */
    public ScriptThumbnail(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
    {
        super(nodeRef, services, scope);
    }

    /**
     * Updates the thumbnails content
     */
    public void update()
    {
        List<ChildAssociationRef> parentRefs = services.getNodeService().getParentAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
        // There should in fact only ever be one parent association of type rendition on any rendition node.
        if (parentRefs.size() != 1)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Node ")
                .append(nodeRef)
                .append(" has ")
                .append(parentRefs.size())
                .append(" rendition parents. Unable to update.");
            if (logger.isWarnEnabled())
            {
                logger.warn(msg.toString());
            }
            throw new AlfrescoRuntimeException(msg.toString());
        }
        
        String name = parentRefs.get(0).getQName().getLocalName();
        
        ThumbnailDefinition def = services.getThumbnailService().getThumbnailRegistry().getThumbnailDefinition(name);
        services.getThumbnailService().updateThumbnail(this.nodeRef, def.getTransformationOptions());
    }
    
}
