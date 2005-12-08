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
 * Tag class for the UINodePath component
 * 
 * @author Kevin Roast
 */
public class NodePathTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.NodePath";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.NodePathLinkRenderer";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setActionProperty((UICommand)component, this.action);
      setActionListenerProperty((UICommand)component, this.actionListener);
      setBooleanProperty(component, "breadcrumb", this.breadcrumb);
      setBooleanProperty(component, "disabled", this.disabled);
      setBooleanProperty(component, "showLeaf", this.disabled);
      setStringBindingProperty(component, "value", this.value);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.action = null;
      this.actionListener = null;
      this.value = null;
      this.disabled = null;
      this.breadcrumb = null;
      this.showLeaf = null;
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
    * Set whether to display the path as a breadcrumb or a single long link (default)
    *
    * @param breadcrumb     breadcrumb true|false
    */
   public void setBreadcrumb(String breadcrumb)
   {
      this.breadcrumb = breadcrumb;
   }
   
   /**
    * Set whether the component is disabled
    *
    * @param disabled     whether the component is disabled
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }
   
   /**
    * Set whether the final leaf node is shown as part of the path
    *
    * @param showLeaf     whether the final leaf node is shown as part of the path
    */
   public void setShowLeaf(String showLeaf)
   {
      this.showLeaf = showLeaf;
   }


   /** the showLeaf boolean */
   private String showLeaf;

   /** the disabled boolean */
   private String disabled;

   /** the breadcrumb boolean */
   private String breadcrumb;

   /** the action */
   private String action;

   /** the actionListener */
   private String actionListener;

   /** the value */
   private String value;
}
