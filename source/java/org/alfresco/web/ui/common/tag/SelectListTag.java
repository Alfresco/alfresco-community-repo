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
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

/**
 * @author Kevin Roast
 */
public class SelectListTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.SelectList";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setActionProperty((UICommand)component, this.action);
      setActionListenerProperty((UICommand)component, this.actionListener);
      setBooleanProperty(component, "multiSelect", this.multiSelect);
      setStringProperty(component, "buttonLabel", this.buttonLabel);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.action = null;
      this.actionListener = null;
      this.multiSelect = null;
      this.buttonLabel = null;
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
    * Set the multiSelect
    *
    * @param multiSelect     the multiSelect
    */
   public void setMultiSelect(String multiSelect)
   {
      this.multiSelect = multiSelect;
   }

   /**
    * Set the buttonLabel
    *
    * @param buttonLabel     the buttonLabel
    */
   public void setButtonLabel(String buttonLabel)
   {
      this.buttonLabel = buttonLabel;
   }


   /** the multiSelect */
   private String multiSelect;

   /** the buttonLabel */
   private String buttonLabel;

   /** the action */
   private String action;

   /** the actionListener */
   private String actionListener;
}
