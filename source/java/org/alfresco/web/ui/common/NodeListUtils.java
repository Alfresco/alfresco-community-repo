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