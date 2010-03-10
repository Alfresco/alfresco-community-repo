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
package org.alfresco.repo.rendition.script;

import java.io.Serializable;

import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * RenditionDefinition JavaScript Object. This class is a JavaScript-friendly wrapper for
 * the {@link RenditionDefinition renditionDefinition} class.
 * 
 * @author Neil McErlean
 */
public final class ScriptRenditionDefinition implements Serializable
{
    private static final long serialVersionUID = 8132935577891455490L;
    private final RenditionDefinition renditionDefinition;
    private final NamespaceService namespaceService;

    public ScriptRenditionDefinition(NamespaceService namespaceService, RenditionDefinition renditionDefinition)
    {
        this.namespaceService = namespaceService;
        this.renditionDefinition = renditionDefinition;
    }
    
    /**
     * Returns the name of this rendition definition in prefix:localName format.
     * 
     * @return the name which uniquely identifies this rendition definition.
     */
    public String getRenditionName()
    {
        QName qname = this.renditionDefinition.getRenditionName();
        return qname.toPrefixString(namespaceService);
    }

    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
            .append("[").append(getRenditionName()).append("]");

        return msg.toString();
    }
}
