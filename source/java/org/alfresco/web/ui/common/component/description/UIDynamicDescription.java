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
package org.alfresco.web.ui.common.component.description;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dynamic description component that switches text based on the events
 * of another input control
 * 
 * @author gavinc
 */
public class UIDynamicDescription extends SelfRenderingComponent
{
   private static Log logger = LogFactory.getLog(UIDynamicDescription.class);
   private String selected;
   private String functionName;
   
   /**
    * @return The id of the selected description
    */
   public String getSelected()
   {
      if (this.selected == null)
      {
         ValueBinding vb = getValueBinding("selected");
         if (vb != null)
         {
            this.selected = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.selected;
   }

   /**
    * @param selected The id of the selected
    */
   public void setSelected(String selected)
   {
      this.selected = selected;
   }
   
   /**
    * @return Returns the JavaScript function name to use
    */
   public String getFunctionName()
   {
      if (this.functionName == null)
      {
         this.functionName = "itemSelected";
      }
      
      return this.functionName;
   }

   /**
    * @param functionName Sets the name of the JavaScript function to use 
    */
   public void setFunctionName(String functionName)
   {
      this.functionName = functionName;
   }

   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.DynamicDescription";
   }

   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (this.isRendered() == false)
      {
         return;
      }
      
      // output the required JavaScript
      ResponseWriter out = context.getResponseWriter();
      
      out.write("<script language='JavaScript'>\n");
      out.write("var m_");
      out.write(getFunctionName());
      out.write(" = '");
      if (getSelected() != null)
      {  
         out.write("desc-");
         out.write(getSelected());
      }
      out.write("';\n");
      out.write("function ");
      out.write(getFunctionName());
      out.write("(inputControl) {\n");
      out.write("if (m_");
      out.write(getFunctionName());
      out.write(" != '') {\n");
      out.write("   document.getElementById(m_");
      out.write(getFunctionName());
      out.write(").style.display = 'none';\n");
      out.write("}\nm_");
      out.write(getFunctionName());
      out.write(" = 'desc-' + inputControl.value;\n");
      out.write("document.getElementById(m_");
      out.write(getFunctionName());
      out.write(").style.display = 'inline';\n");
      out.write("} </script>\n");
   }

   /**
    * @see javax.faces.component.UIComponent#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      if (this.isRendered() == false)
      {
         return;
      }
      
      List<UIComponent> kids = getChildren();
      for (UIComponent child : kids)
      {
         if (child instanceof UIDescription)
         {
            // render the single description
            renderDescription(context, ((UIDescription)child).getControlValue(),
                  ((UIDescription)child).getText());
         }
         else if (child instanceof UIDescriptions)
         {
            // retrieve the object being pointed to and get
            // the descriptions from that
            renderDescriptions(context, (UIDescriptions)child);
         }
      }
   }

   /**
    * @see javax.faces.component.UIComponent#encodeEnd(javax.faces.context.FacesContext)
    */
   public void encodeEnd(FacesContext context) throws IOException
   {
      // don't need to do anything
   }

   /**
    * @see javax.faces.component.UIComponent#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.selected = (String)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.selected;
      return (values);
   }
   
   /**
    * Renders a description item
    * 
    * @param context The faces context
    * @param controlId The id of the control the description is for
    * @param test The description text 
    */
   private void renderDescription(FacesContext context, String controlId, String text)
      throws IOException
   {
      ResponseWriter out = context.getResponseWriter();
      out.write("<span");
      String spanId = "desc-" + controlId;
      outputAttribute(out, spanId, "id");
      
      if (controlId.equals(this.selected))
      {
         outputAttribute(out, "display: inline", "style");
      }
      else
      {
         outputAttribute(out, "display: none", "style");
      }
      
      out.write(">");
      out.write(Utils.encode(text));
      out.write("</span>\n");
   }
   
   /**
    * Renders the given descriptions component
    * 
    * @param context The faces context
    * @param descriptions The descriptions to render
    */
   private void renderDescriptions(FacesContext context, UIDescriptions descriptions)
      throws IOException
   {
      // get hold of the object holding the descriptions and make sure
      // it is of the correct type
      Object obj = descriptions.getValue();
      
      if (obj instanceof Map)
      {
         Map<String, String> items = (Map)obj;
         for (String id : items.keySet())
         {
            renderDescription(context, id, items.get(id));
         }
      }
      else if (obj instanceof List)
      {
         Iterator iter = ((List)obj).iterator();
         while (iter.hasNext())
         {
            UIDescription desc = (UIDescription)iter.next();
            renderDescription(context, desc.getControlValue(), desc.getText());
         }
      }
   }
}
