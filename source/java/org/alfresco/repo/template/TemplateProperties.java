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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.service.namespace.QName;

/**
 * Contract for Template API objects that have properties, aspects and children.
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
public interface TemplateProperties extends TemplateNodeRef
{
    /**
     * @return The properties available on this node.
     */
    public Map<String, Serializable> getProperties();
    
    /**
     * @return The list of aspects applied to this node
     */
    public Set<QName> getAspects();
    
    /**
     * @param aspect The aspect name to test for
     * 
     * @return true if the node has the aspect false otherwise
     */
    public boolean hasAspect(String aspect);
    
    /**
     * @return The children of this Node as TemplateNode wrappers
     */
    public List<TemplateProperties> getChildren();
    
    /**
     * @return the primary parent of this node 
     */
    public TemplateProperties getParent();
}
