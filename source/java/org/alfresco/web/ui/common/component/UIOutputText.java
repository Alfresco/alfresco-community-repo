/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.common.component;

import java.io.IOException;
import java.net.URLEncoder;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

/**
 * Component that simply renders text
 * 
 * @author gavinc
 */
public class UIOutputText extends UIOutput
{
   private Boolean encodeForJavaScript = null;
   
   /**
    * Default constructor
    */
   public UIOutputText()
   {
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.OutputText";
   }
   
   /**
    * Sets whether the text should be encoded for JavaScript consumption
    * 
    * @param encodeForJavaScript true to escape text
    */
   public void setEncodeForJavaScript(boolean encodeForJavaScript)
   {
      this.encodeForJavaScript = Boolean.valueOf(encodeForJavaScript);
   }

   /**
    * Returns whether the text is going to be encoded or not
    * 
    * @return true if the text is going to be encoded
    */
   public boolean isEncodeForJavaScript()
   {
      if (this.encodeForJavaScript == null)
      {
         ValueBinding vb = getValueBinding("encodeForJavaScript");
         if (vb != null)
         {
            this.encodeForJavaScript = (Boolean)vb.getValue(getFacesContext());
         }
         
         if (this.encodeForJavaScript == null)
         {
            this.encodeForJavaScript = Boolean.FALSE;
         }
      }
      
      return this.encodeForJavaScript.booleanValue();
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      String output = null;
      
      if (isEncodeForJavaScript())
      {
         output = URLEncoder.encode((String)getValue(), "UTF-8").replace('+', ' ');
      }
      else
      {
         output = (String)getValue();
      }

      out.write(output);
   }
}
