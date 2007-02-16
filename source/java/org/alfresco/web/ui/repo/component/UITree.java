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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.repo.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * JSF component that renders an AJAX based tree for browsing the 
 * repository.
 * 
 * @author gavinc
 */
public class UITree extends UIComponentBase
{  
   public static final String COMPONENT_TYPE = "org.alfresco.faces.Tree";
   public static final String DEFAULT_RENDERER = "org.alfresco.faces.Yahoo";
   
   protected List<TreeNode> rootNodes = null;
   protected String retrieveChildrenUrl;
   protected String nodeCollapsedUrl;
   protected String nodeExpandedCallback;
   protected String nodeCollapsedCallback;
   protected String nodeSelectedCallback;

   // ------------------------------------------------------------------------------
   // Component Impl 

   public UITree()
   {
      setRendererType(DEFAULT_RENDERER);
   }
   
   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.rootNodes = (List<TreeNode>)values[1];
      this.retrieveChildrenUrl = (String)values[2];
      this.nodeCollapsedUrl = (String)values[3];
      this.nodeExpandedCallback = (String)values[4];
      this.nodeCollapsedCallback = (String)values[5];
      this.nodeSelectedCallback = (String)values[6];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.rootNodes;
      values[2] = this.retrieveChildrenUrl;
      values[3] = this.nodeCollapsedUrl;
      values[4] = this.nodeExpandedCallback;
      values[5] = this.nodeCollapsedCallback;
      values[6] = this.nodeSelectedCallback;
      return values;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Get the root nodes for the tree
    *
    * @return the list of nodes representing the root nodes of the tree
    */
   @SuppressWarnings("unchecked")
   public List<TreeNode> getRootNodes()
   {
      ValueBinding vb = getValueBinding("rootNodes");
      if (vb != null)
      {
         this.rootNodes = (List<TreeNode>)vb.getValue(getFacesContext());
      }
      
      return this.rootNodes;
   }

   /**
    * Set the root nodes for the tree to show
    *
    * @param rootNodes The list of node for the tree
    */
   public void setRootNodes(List<TreeNode> rootNodes)
   {
      this.rootNodes = rootNodes;
   }
   
   /**
    * Returns the Javascript function name to be used for node collapsed event
    * 
    * @return Javascript function name to be used for node collapsed event
    */
   public String getNodeCollapsedCallback()
   {
      ValueBinding vb = getValueBinding("nodeCollapsedCallback");
      if (vb != null)
      {
         this.nodeCollapsedCallback = (String)vb.getValue(getFacesContext());
      }
      
      return this.nodeCollapsedCallback;
   }

   /**
    * Sets the name of the Javascript function to use for the node collapsed event
    * 
    * @param nodeCollapsedCallback The Javascript function to use for the node collapsed event
    */
   public void setNodeCollapsedCallback(String nodeCollapsedCallback)
   {
      this.nodeCollapsedCallback = nodeCollapsedCallback;
   }

   /**
    * Returns the Javascript function name to be used for node expanded event
    * 
    * @return Javascript function name to be used for node expanded event
    */
   public String getNodeExpandedCallback()
   {
      ValueBinding vb = getValueBinding("nodeExpandedCallback");
      if (vb != null)
      {
         this.nodeExpandedCallback = (String)vb.getValue(getFacesContext());
      }
      
      return this.nodeExpandedCallback;
   }

   /**
    * Sets the name of the Javascript function to use for the expanded event
    * 
    * @param nodeCollapsedCallback The Javascript function to use for the expanded event
    */
   public void setNodeExpandedCallback(String nodeExpandedCallback)
   {
      this.nodeExpandedCallback = nodeExpandedCallback;
   }

   /**
    * Returns the Javascript function name to be used for node selected event
    * 
    * @return Javascript function name to be used for node selected event
    */
   public String getNodeSelectedCallback()
   {
      ValueBinding vb = getValueBinding("nodeSelectedCallback");
      if (vb != null)
      {
         this.nodeSelectedCallback = (String)vb.getValue(getFacesContext());
      }
      
      return this.nodeSelectedCallback;
   }

   /**
    * Sets the name of the Javascript function to use for the node selected event
    * 
    * @param nodeCollapsedCallback The Javascript function to use for the node selected event
    */
   public void setNodeSelectedCallback(String nodeSelectedCallback)
   {
      this.nodeSelectedCallback = nodeSelectedCallback;
   }

   /**
    * Returns the URL to use for the AJAX call to retrieve the child nodea
    * 
    * @return AJAX URL to get children
    */
   public String getRetrieveChildrenUrl()
   {
      ValueBinding vb = getValueBinding("retrieveChildrenUrl");
      if (vb != null)
      {
         this.retrieveChildrenUrl = (String)vb.getValue(getFacesContext());
      }
      
      return this.retrieveChildrenUrl;
   }

   /**
    * Sets the AJAX URL to use to retrive child nodes
    * 
    * @param retrieveChildrenUrl The AJAX URL to use
    */
   public void setRetrieveChildrenUrl(String retrieveChildrenUrl)
   {
      this.retrieveChildrenUrl = retrieveChildrenUrl;
   }
   
   /**
    * Returns the URL to use for the AJAX call to inform the server 
    * that a node has been collapsed
    * 
    * @return AJAX URL to inform of node collapse
    */
   public String getNodeCollapsedUrl()
   {
      ValueBinding vb = getValueBinding("nodeCollapsedUrl");
      if (vb != null)
      {
         this.nodeCollapsedUrl = (String)vb.getValue(getFacesContext());
      }
      
      return this.nodeCollapsedUrl;
   }

   /**
    * Sets the AJAX URL to use to inform the server that a node
    * has been collapsed
    * 
    * @param nodeCollapsedUrl The AJAX URL to use
    */
   public void setNodeCollapsedUrl(String nodeCollapsedUrl)
   {
      this.nodeCollapsedUrl = nodeCollapsedUrl;
   }
   
   /**
    * Inner class representing a node in the tree
    * 
    * @author gavinc
    */
   public static class TreeNode
   {
      private String nodeRef;
      private String name;
      private String icon;
      private boolean leafNode = false;
      private boolean expanded = false;
      private boolean selected = false;
      private TreeNode parent;
      private List<TreeNode> children = new ArrayList<TreeNode>();
      
      /**
       * Default constructor
       * 
       * @param nodeRef The NodeRef of the item the node is representing
       * @param name The name for the tree label
       * @param icon The icon for the node
       */
      public TreeNode(String nodeRef, String name, String icon)
      {
         this.nodeRef = nodeRef;
         this.name = name;
         this.icon = icon;
         
         if (this.icon == null || this.icon.length() == 0)
         {
            this.icon = "space-icon-default";
         }
      }
      
      public String getIcon()
      {
         return this.icon;
      }
      
      public void setIcon(String icon)
      {
         this.icon = icon;
      }
      
      public boolean isLeafNode()
      {
         return this.leafNode;
      }
      
      public void setLeafNode(boolean leafNode)
      {
         this.leafNode = leafNode;
      }
      
      public boolean isExpanded()
      {
         return this.expanded;
      }

      public void setExpanded(boolean expanded)
      {
         this.expanded = expanded;
      }
      
      public boolean isSelected()
      {
         return this.selected;
      }

      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }

      public String getName()
      {
         return this.name;
      }
      
      public void setName(String name)
      {
         this.name = name;
      }
      
      public String getNodeRef()
      {
         return this.nodeRef;
      }
      
      public void setNodeRef(String nodeRef)
      {
         this.nodeRef = nodeRef;
      }

      public TreeNode getParent()
      {
         return this.parent;
      }

      public void setParent(TreeNode parent)
      {
         this.parent = parent;
      }

      public List<TreeNode> getChildren()
      {
         return this.children;
      }
      
      public void addChild(TreeNode child)
      {
         child.setParent(this);
         this.children.add(child);
      }
      
      public void removeChildren()
      {
         this.children = new ArrayList<TreeNode>();
      }
      
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (nodeRef=").append(this.nodeRef);
         buffer.append(", name=").append(this.name);
         buffer.append(", icon=").append(this.icon);
         buffer.append(", expanded=").append(this.expanded);
         buffer.append(", selected=").append(this.selected);
         if (this.parent != null)
         {
            buffer.append(", parent=").append(this.parent.getNodeRef());
         }
         else
         {
            buffer.append(", parent=null");
         }
         buffer.append(", leafNode=").append(this.leafNode).append(")");
         return buffer.toString();
      }
      
      public String toXML()
      {
         StringBuilder xml = new StringBuilder();
         xml.append("<node ref=\"");
         xml.append(this.nodeRef);
         xml.append("\" name=\"");
         xml.append(this.name);
         xml.append("\" icon=\"");
         xml.append(this.icon);
         xml.append("\"/>");
         return xml.toString();
      }
   }
}
