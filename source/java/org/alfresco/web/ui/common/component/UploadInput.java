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

import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

public class UploadInput extends UIInput implements NamingContainer
{
   private static final long serialVersionUID = 4064734856565167835L;

   private String framework;

   public void encodeBegin(FacesContext context) throws IOException
   {
      ResponseWriter writer = context.getResponseWriter();
      String path = context.getExternalContext().getRequestContextPath();
      
      writer.write("<script type='text/javascript' src='/alfresco/scripts/upload_helper.js'></script>\n");
      
      writer.write("<script type='text/javascript'>");
      writer.write("function handle_upload(target)\n");
      writer.write("{\n");
      writer.write("handle_upload_helper(target, '', upload_complete, '"+path+"')\n");
      writer.write("}\n");
   
      writer.write("function upload_complete(id, path, filename)\n");
      writer.write("{\n");
      writer.write("var schema_file_input = document.getElementById('"+framework+":"+framework+"-body:"+getId()+"');\n");
      writer.write("schema_file_input.value = filename;\n");
      writer.write("schema_file_input.form.submit();\n");
      writer.write("}\n");
      writer.write("</script>\n");
      
      super.encodeBegin(context);
      
      writer.write("\n<input id='" + framework + ":" + framework + "-body:file-input' type='file' size='35' name='alfFileInput' onchange='javascript:handle_upload(this)'/>");
   }

   public Object saveState(FacesContext context)
   {
      Object[] values = new Object[2];
      values[0] = super.saveState(context);
      values[1] = framework;
      return values;
   }

   public void restoreState(FacesContext context, Object state)
   {
      Object[] values = (Object[]) state;
      super.restoreState(context, values[0]);
      framework = (String) values[1];
   }
   
   public String getFramework()
   {
      return framework;
   }

   public void setFramework(String framework)
   {
      this.framework = framework;
   }
}
