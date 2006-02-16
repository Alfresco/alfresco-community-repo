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
package org.alfresco.web.ui.common.component.debug;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.ui.common.component.SelfRenderingComponent;

/**
 * Base class for all debug components
 * 
 * @author gavinc
 */
public abstract class BaseDebugComponent extends SelfRenderingComponent
{
   private String title;

   /**
    * Retrieves the debug data to show for the component as a Map
    * 
    * @return The Map of data
    */
   public abstract Map getDebugData();
   
   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      out.write("<table cellpadding='3' cellspacing='3' border='1'>");
      
      if (this.getTitle() != null)
      {
         out.write("<tr><td colspan='2'>");
         out.write(this.getTitle());
         out.write("</td></tr>");
      }
      
      out.write("<tr><th align='left'>Property</th><th align='left'>Value</th></tr>");
      
      Map session = getDebugData();
      for (Object key : session.keySet())
      {
         out.write("<tr><td>");
         out.write(key.toString());
         out.write("</td><td>");
         Object obj = session.get(key);
         if (obj == null)
         {
            out.write("null");
         }
         else
         {
            String value = obj.toString();
            if (value.length() == 0)
            {
               out.write("&nbsp;");
            }
            else
            {
               // replace any ; characters with ;<space> as that will help break up long lines
               value = value.replaceAll(";", "; ");
               out.write(value);
            }
         }
         out.write("</td></tr>");
      }
      
      out.write("</table>");
      
      super.encodeBegin(context);
   }

   /**
    * @see javax.faces.component.UIComponent#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return false;
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.title = (String)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.title;
      return (values);
   }
   
   /**
    * Returns the title
    * 
    * @return The title
    */
   public String getTitle()
   {
      ValueBinding vb = getValueBinding("title");
      if (vb != null)
      {
         this.title = (String)vb.getValue(getFacesContext());
      }
      
      return this.title;
   }

   /**
    * Sets the title
    * 
    * @param title The title
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
}
