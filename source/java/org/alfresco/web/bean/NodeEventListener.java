/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

/**
 * @author Kevin Roast
 */
public interface NodeEventListener
{
   /**
    * Callback executed when a Node wrapped object is created. This is generally used
    * to add additional property resolvers to the Node for a specific model type.
    * 
    * @param node    The Node wrapper that has been created
    * @param type    Type of the Node that has been created
    */
   public void created(Node node, QName type);
}
