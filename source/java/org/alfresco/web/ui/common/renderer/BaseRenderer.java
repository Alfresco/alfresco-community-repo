/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.ui.common.renderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * Base renderer class. Contains helper methods to assist most renderers. 
 * 
 * @author kevinr
 */
public abstract class BaseRenderer extends Renderer
{
   /**
    * Helper to output an attribute to the output stream
    * 
    * @param out        ResponseWriter
    * @param attr       attribute value object (cannot be null)
    * @param mapping    mapping to output as e.g. style="..."
    * 
    * @throws IOException
    */
   protected static void outputAttribute(ResponseWriter out, Object attr, String mapping)
      throws IOException
   {
      if (attr != null)
      {
         out.write(' ');
         out.write(mapping);
         out.write("=\"");
         out.write(attr.toString());
         out.write('"');
      }
   }
   
   /**
    * Ensures that the given context and component are not null. This method
    * should be called by all renderer methods that are given these parameters.
    * 
    * @param ctx Faces context
    * @param component The component
    */
   protected static void assertParmeters(FacesContext ctx, UIComponent component)
   {
      if (ctx == null)
      {
         throw new IllegalStateException("context can not be null");
      }
      
      if (component == null)
      {
         throw new IllegalStateException("component can not be null");
      }
   }
   
   /**
    * Return the map of name/value pairs for any child UIParameter components.
    * 
    * @param component to find UIParameter child values for
    * 
    * @return a Map of name/value pairs or <tt>null</tt> if none found
    */
   protected static Map<String, String> getParameterComponents(final UIComponent component)
   {
      if (component.getChildCount() == 0)
      {
         return null;
      }

      final Map<String, String> params = new HashMap<String, String>(component.getChildCount(), 1.0f);
      for (UIComponent child : (List<UIComponent>)component.getChildren())
      {
         if (child instanceof UIParameter)
         {
            final UIParameter param = (UIParameter)child;
            params.put(param.getName(), param.getValue() != null ? param.getValue().toString() : null);
         }
      }
      return params;
   }
}
