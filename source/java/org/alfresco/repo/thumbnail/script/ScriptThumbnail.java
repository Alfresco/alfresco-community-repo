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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mozilla.javascript.Scriptable;

/**
 * @author Roy Wetherall
 */
public class ScriptThumbnail extends ScriptNode
{
    private static final long serialVersionUID = 7854749986083635678L;

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
        String name = (String)services.getNodeService().getProperty(nodeRef, ContentModel.PROP_THUMBNAIL_NAME);
        ThumbnailDefinition def = services.getThumbnailService().getThumbnailRegistry().getThumbnailDefinition(name);
        services.getThumbnailService().updateThumbnail(this.nodeRef, def.getTransformationOptions());
    }
    
}
