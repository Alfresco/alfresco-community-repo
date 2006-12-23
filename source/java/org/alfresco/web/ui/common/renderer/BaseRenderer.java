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
            if (param.getValue() == null || param.getValue() instanceof String)
            {
               params.put(param.getName(), (String)param.getValue());
            }
            else
            {
               throw new ClassCastException("value of parameter " + param.getName() +
                                            " is a " + param.getValue().getClass().getName() +
                                            ". Expected a " + String.class.getName());
            }
         }
      }
      return params;
   }
}
