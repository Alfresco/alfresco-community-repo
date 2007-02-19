/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.encodeForJavaScript = (Boolean)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.encodeForJavaScript;
      return values;
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
