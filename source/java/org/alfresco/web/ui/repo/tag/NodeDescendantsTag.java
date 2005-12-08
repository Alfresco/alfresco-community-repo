/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
