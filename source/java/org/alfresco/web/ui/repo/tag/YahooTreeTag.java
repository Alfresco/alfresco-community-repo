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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Tag class for using the Yahoo tree component on a JSP page.
 * 
 * @author gavinc
 */
public class YahooTreeTag extends HtmlComponentTag
{
   private String rootNodes;
   private String retrieveChildrenUrl;
   private String nodeCollapsedUrl;
   private String nodeExpandedCallback;
   private String nodeCollapsedCallback;
   private String nodeSelectedCallback;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Tree";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.Yahoo";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringBindingProperty(component, "rootNodes", this.rootNodes);
      setStringBindingProperty(component, "retrieveChildrenUrl", this.retrieveChildrenUrl);
      setStringBindingProperty(component, "nodeCollapsedUrl", this.nodeCollapsedUrl);
      setStringBindingProperty(component, "nodeExpandedCallback", this.nodeExpandedCallback);
      setStringBindingProperty(component, "nodeCollapsedCallback", this.nodeCollapsedCallback);
      setStringBindingProperty(component, "nodeSelectedCallback", this.nodeSelectedCallback);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.rootNodes = null;
      this.retrieveChildrenUrl = null;
      this.nodeCollapsedUrl = null;
      this.nodeExpandedCallback = null;
      this.nodeCollapsedCallback = null;
      this.nodeSelectedCallback = null;
   }
   
   /**
    * Set the root nodes for the tree
    *
    * @param rootNodes
    */
   public void setRootNodes(String rootNodes)
   {
      this.rootNodes = rootNodes;
   }

   /**
    * Set the name of the Javascript function to handle the node collapsed event
    * 
    * @param nodeCollapsedCallback
    */
   public void setNodeCollapsedCallback(String nodeCollapsedCallback)
   {
      this.nodeCollapsedCallback = nodeCollapsedCallback;
   }

   /**
    * Set the name of the Javascript function to handle the node expanded event
    * 
    * @param nodeExpandedCallback
    */
   public void setNodeExpandedCallback(String nodeExpandedCallback)
   {
      this.nodeExpandedCallback = nodeExpandedCallback;
   }

   /**
    * Set the name of the Javascript function to handle the node selected event
    * 
    * @param nodeSelectedCallback
    */
   public void setNodeSelectedCallback(String nodeSelectedCallback)
   {
      this.nodeSelectedCallback = nodeSelectedCallback;
   }

   /**
    * Set the URL to use to retrieve child nodes
    * 
    * @param retrieveChildrenUrl
    */
   public void setRetrieveChildrenUrl(String retrieveChildrenUrl)
   {
      this.retrieveChildrenUrl = retrieveChildrenUrl;
   }
   
   /**
    * Set the URL to use to inform the server that a node has been collapsed
    * 
    * @param nodeCollapsedUrl
    */
   public void setNodeCollapsedUrl(String nodeCollapsedUrl)
   {
      this.nodeCollapsedUrl = nodeCollapsedUrl;
   }
}
