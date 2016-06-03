/*
 * #%L
 * Alfresco Repository WAR Community
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
package org.alfresco.web.bean.repository;

import java.io.Serializable;

/**
 * Simple interface used to implement small classes capable of calculating dynamic property values
 * for Nodes at runtime. This allows bean responsible for building large lists of Nodes to
 * encapsulate the code needed to retrieve non-standard Node properties. The values are then
 * calculated on demand by the property resolver.
 * 
 * When a node is reset() the standard and other props are cleared. If property resolvers are used
 * then the non-standard props will be restored automatically as well. 
 * 
 * @author Kevin Roast
 */
public interface NodePropertyResolver extends Serializable
{
   /**
    * Get the property value for this resolver
    * 
    * @param node       Node this property is for
    * 
    * @return property value
    */
   public Object get(Node node);
}
