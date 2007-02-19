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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;

/**
 * JSF component that displays information about a node.
 * <p>
 * The node to show information on 
 * 
 * @author gavinc
 */
public class UINodeInfo extends SelfRenderingComponent
{
   protected final static String NODE_INFO_SCRIPTS_WRITTEN = "_alfNodeInfoScripts";
   
   protected Object value = null;
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.NodeInfo";
   }
   
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[8];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      return values;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // if AJAX is disabled don't render anything
      if (Application.getClientConfig(context).isAjaxEnabled())
      {
         ResponseWriter out = context.getResponseWriter();
         
         // output the scripts required by the component (checks are 
         // made to make sure the scripts are only written once)
         Utils.writeDojoScripts(context, out);
         
         // write out the JavaScript specific to the NodeInfo component,
         // again, make sure it's only done once
         Object present = context.getExternalContext().getRequestMap().
            get(NODE_INFO_SCRIPTS_WRITTEN);
         if (present == null)
         {
            out.write("<script type=\"text/javascript\" src=\"");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/scripts/ajax/node-info.js\"> </script>\n");
            
            context.getExternalContext().getRequestMap().put(
                  NODE_INFO_SCRIPTS_WRITTEN, Boolean.TRUE);
         }
         
         // wrap the child components in a <span> that has the onmouseover
         // event which kicks off the request for node information
         String id = (String)this.getValue();
         out.write("<span onmouseover=\"showNodeInfo('");
         out.write(Repository.getStoreRef().toString());
         out.write("/");
         out.write(id);
         out.write("', this)\" onmouseout=\"hideNodeInfo()\">");
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // if AJAX is disabled don't render anything
      if (Application.getClientConfig(context).isAjaxEnabled())
      {
         context.getResponseWriter().write("</span>");
      }
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Get the value - the value is used in a equals() match against the current value in the
    * parent ModeList component to set the selected item.
    *
    * @return the value
    */
   public Object getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = vb.getValue(getFacesContext());
      }
      
      return this.value;
   }

   /**
    * Set the value - the value is used in a equals() match against the current value in the
    * parent ModeList component to set the selected item.
    *
    * @param value     the value
    */
   public void setValue(Object value)
   {
      this.value = value;
   }
}
