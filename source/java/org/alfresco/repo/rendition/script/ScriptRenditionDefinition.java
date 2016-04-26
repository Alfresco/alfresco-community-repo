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
package org.alfresco.repo.rendition.script;

import org.alfresco.repo.jscript.ScriptAction;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

/**
 * RenditionDefinition JavaScript Object. This class is a JavaScript-friendly wrapper for
 * the {@link RenditionDefinition renditionDefinition} class.
 * 
 * @author Neil McErlean
 * @see org.alfresco.service.cmr.rendition.RenditionDefinition
 */
public final class ScriptRenditionDefinition extends ScriptAction
{
    private static final long serialVersionUID = 8132935577891455490L;

    public ScriptRenditionDefinition(ServiceRegistry serviceRegistry, Scriptable scope,
            RenderingEngineDefinition engineDefinition, RenditionDefinition renditionDefinition)
    {
    	super(serviceRegistry, renditionDefinition, engineDefinition);
    }
    
    /**
     * Returns the name of this rendition definition in prefix:localName format.
     * 
     * @return the name which uniquely identifies this rendition definition.
     */
    public String getRenditionName()
    {
        QName qname = getRenditionDefinition().getRenditionName();
        return qname.toPrefixString(services.getNamespaceService());
    }
    
    /**
     * Returns the name of the Rendering Engine used by this definition.
     * @return String
     */
    public String getRenderingEngineName()
    {
        return getRenderingEngineDefinition().getName();
    }

    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
            .append("[").append(getRenditionName()).append("]");

        return msg.toString();
    }
    
    RenditionDefinition getRenditionDefinition()
    {
    	this.performParamConversionForRepo();
        return (RenditionDefinition)action;
    }
    
    RenderingEngineDefinition getRenderingEngineDefinition()
    {
        return (RenderingEngineDefinition)actionDef;
    }
    
    @Override
    protected void executeImpl(ScriptNode node)
    {
    	RenditionDefinition renditionDefinition = getRenditionDefinition();
    	this.services.getRenditionService().render(node.getNodeRef(), renditionDefinition);
    }
}
