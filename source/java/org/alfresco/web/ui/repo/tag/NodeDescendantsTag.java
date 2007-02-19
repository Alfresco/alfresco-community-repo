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

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * @author Kevin Roast
 */
public class NodeDescendantsTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.NodeDescendants";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.NodeDescendantsLinkRenderer";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setBooleanProperty(component, "showEllipses", this.showEllipses);
      setActionProperty((UICommand)component, this.action);
      setActionListenerProperty((UICommand)component, this.actionListener);
      setIntProperty(component, "maxChildren", this.maxChildren);
      setStringProperty(component, "separator", this.separator);
      setStringBindingProperty(component, "value", this.value);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.showEllipses = null;
      this.maxChildren = null;
      this.separator = null;
      this.action = null;
      this.actionListener = null;
      this.value = null;
   }
   
   /**
    * Set the maxChildren
    *
    * @param maxChildren     the maxChildren
    */
   public void setMaxChildren(String maxChildren)
   {
      this.maxChildren = maxChildren;
   }

   /**
    * Set the separator
    *
    * @param separator     the separator
    */
   public void setSeparator(String separator)
   {
      this.separator = separator;
   }

   /**
    * Set the action
    *
    * @param action     the action
    */
   public void setAction(String action)
   {
      this.action = action;
   }

   /**
    * Set the actionListener
    *
    * @param actionListener     the actionListener
    */
   public void setActionListener(String actionListener)
   {
      this.actionListener = actionListener;
   }

   /**
    * Set the value
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the show ellipses value
    *
    * @param showEllipses     true to show ellipses when we have > maxchildren to display
    */
   public void setShowEllipses(String showEllipses)
   {
      this.showEllipses = showEllipses;
   }


   /** the showEllipses */
   private String showEllipses;

   /** the maxChildren */
   private String maxChildren;

   /** the separator */
   private String separator;

   /** the action */
   private String action;

   /** the actionListener */
   private String actionListener;

   /** the value */
   private String value;
}
