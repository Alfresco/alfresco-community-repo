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

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * @author kevinr
 */
public class ActionsTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Actions";
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
      
      setBooleanProperty(component, "showLink", this.showLink);
      setStringProperty(component, "value", this.value);
      setStringBindingProperty(component, "context", this.context);
      setIntProperty(component, "verticalSpacing", this.verticalSpacing);
   }

   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.showLink = null;
      this.context = null;
      this.verticalSpacing = null;
   }
   
   /**
    * Set the value (id of the action group config to use)
    *
    * @param value     the value (id of the action group config to use)
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Set the showLink
    *
    * @param showLink     the showLink
    */
   public void setShowLink(String showLink)
   {
      this.showLink = showLink;
   }
   
   /**
    * Set the context object
    *
    * @param context     the context object
    */
   public void setContext(String context)
   {
      this.context = context;
   }

   /**
    * Set the verticalSpacing
    *
    * @param verticalSpacing     the verticalSpacing
    */
   public void setVerticalSpacing(String verticalSpacing)
   {
      this.verticalSpacing = verticalSpacing;
   }


   /** the verticalSpacing */
   private String verticalSpacing;

   /** the context object */
   private String context;
   
   /** the value (id of the action group config to use) */
   private String value;

   /** the showLink boolean */
   private String showLink;
}
