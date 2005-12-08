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
package org.alfresco.web.ui.common.tag.description;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * Tag class to allow the dynamic description component to be used on a JSP page
 * 
 * @author gavinc
 */
public class DynamicDescriptionTag extends BaseComponentTag
{
   private String selected;
   private String functionName;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.DynamicDescription";
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
      
      setStringProperty(component, "selected", this.selected);
      setStringStaticProperty(component, "functionName", this.functionName);
   }

   /**
    * @param selected Sets the selected description id
    */
   public void setSelected(String selected)
   {
      this.selected = selected;
   }
   
   /**
    * @param functionName Sets the JavaScript function name
    */
   public void setFunctionName(String functionName)
   {
      this.functionName = functionName;
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      
      this.selected = null;
      this.functionName = null;
   }
}
