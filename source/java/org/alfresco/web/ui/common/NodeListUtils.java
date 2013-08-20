/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.web.ui.common;

import java.util.List;

import org.alfresco.web.bean.repository.Node;

/**
 * Helper class to get next or previos node from the list of nodes.
* 
* @author vdanilchenko
* @since 4.1.3
*/
public class NodeListUtils 
{
   /**
     * @param nodes     the list of the nodes
     * @param currentNodeId     the current node ID
     */
   public static Node nextItem(List<? extends Node> nodes, String currentNodeId)
   {
      Node next = null;

      // perform a linear search - this is slow but stateless
      // otherwise we would have to manage state of last selected node
      // this gets very tricky as this bean is instantiated once and never
      // reset - it does not know when the document has changed etc.
      for (int i=0; i<nodes.size(); i++)
      {
         if (currentNodeId.equals(nodes.get(i).getId()) == true)
         {
            // found our item - navigate to next
            if (i != nodes.size() - 1)
            {
               next = nodes.get(i + 1);
            }
            else
            {
               // handle wrapping case
               next = nodes.get(0);
            }

            break;
         }
      }

      return next;
   }

   /**
     * @param nodes     the list of the nodes
     * @param currentNodeId     the current node ID
     */
   public static Node previousItem(List<? extends Node> nodes, String currentNodeId)
   {
      Node previous = null;

      // perform a linear search - this is slow but stateless
      // otherwise we would have to manage state of last selected node
      // this gets very tricky as this bean is instantiated once and never
      // reset - it does not know when the document has changed etc.
      for (int i=0; i<nodes.size(); i++)
      {
         if (currentNodeId.equals(nodes.get(i).getId()) == true)
         {
            // found our item - navigate to previous
            if (i != 0)
            {
               previous = nodes.get(i - 1);
            }
            else
            {
               // handle wrapping case
               previous = nodes.get(nodes.size() - 1);
            }

            break;
         }
      }

      return previous;
   }
}