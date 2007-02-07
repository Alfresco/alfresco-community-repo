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

import javax.faces.component.UIComponent;

public class ErrorsTag extends HtmlComponentTag
{
   private String message;
   private String errorClass;
   private String infoClass;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "javax.faces.Messages";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.Errors";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "message", this.message);
      setStringProperty(component, "errorClass", this.errorClass);
      setStringProperty(component, "infoClass", this.infoClass);
   }

   /**
    * @param message Sets the message to display
    */
   public void setMessage(String message)
   {
      this.message = message;
   }
   
   /**
    * @param errorClass The CSS class to use for error messages
    */
   public void setErrorClass(String errorClass)
   {
      this.errorClass = errorClass;
   }
   
   /**
    * @param infoClass The CSS class to use for info messages
    */
   public void setInfoClass(String infoClass)
   {
      this.infoClass = infoClass;
   }
}
