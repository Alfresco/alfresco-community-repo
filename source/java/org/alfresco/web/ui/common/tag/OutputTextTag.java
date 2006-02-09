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

/**
 * Tag to place the UIOutputText component on the page
 * 
 * @author gavinc
 */
public class OutputTextTag extends HtmlComponentTag
{
   private String value;
   private String encodeForJavaScript;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.OutputText";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the component is self renderering
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "encodeForJavaScript", this.encodeForJavaScript);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.encodeForJavaScript = null;
   }
   
   /**
    * Set the value
    *
    * @param value  The text
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the encodeForJavaScript flag
    * 
    * @param encodeForJavaScript true to encode the text for use in JavaScript
    */
   public void setEncodeForJavaScript(String encodeForJavaScript)
   {
      this.encodeForJavaScript = encodeForJavaScript;
   }
}
