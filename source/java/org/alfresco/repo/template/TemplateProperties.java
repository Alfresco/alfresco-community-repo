/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Contract for Template API objects that have properties, aspects and children.
 * 
 * @author Kevin Roast
 */
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
